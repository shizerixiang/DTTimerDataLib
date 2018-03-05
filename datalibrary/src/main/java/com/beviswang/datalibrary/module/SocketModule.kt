package com.beviswang.datalibrary.module

import com.beviswang.datalibrary.logE
import com.beviswang.datalibrary.logI
import com.beviswang.datalibrary.message.PackageMsg
import com.beviswang.datalibrary.util.ConvertHelper
import com.beviswang.datalibrary.util.MessageHelper
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.nio.charset.Charset

/**
 * Socket 通信模块
 * Created by shize on 2018/1/10.
 */
open class SocketModule : Closeable {
    private var mSocket: Socket = Socket() // Socket 对象
    private var mOutputStream: OutputStream? = null // 输出流
    private var mInputStream: InputStream? = null // 输入流
    private var mBufferInputStream: BufferedInputStream? = null // 输入流缓冲

    /**
     * 是否连接
     */
    var mIsConnect: Boolean = false
        get() = mSocket.isConnected

    /**
     * 连接服务器
     *
     * @return 是否成功连接
     */
    open fun connect(): Boolean {
        // 检测是否已经连接
        if (mSocket.isConnected) return true
        try {
            val socketAddress: SocketAddress = InetSocketAddress(SERVER_IP, SERVER_PORT)
            mSocket = Socket()
            // 设置超时时间为 5s
            mSocket.connect(socketAddress, SOCKET_TIME_OUT)
            // 保持连接
            mSocket.keepAlive = true
            // 设置一个读取的超时时间
            mSocket.soTimeout = SOCKET_INPUT_TIME_OUT
            logI("连接成功！")
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return mSocket.isConnected
    }

    /**
     * 断开连接
     *
     * @return 是否成功断开连接
     */
    open fun disConnect(): Boolean {
        try {
            mSocket.shutdownInput()
            mSocket.shutdownOutput()
            // 断开 客户端发送到服务器 的连接，即关闭输出流对象 OutputStream
            mOutputStream?.close()
            // 关闭输入流对象 InputStream
            mBufferInputStream?.close()
            mInputStream?.close()
            // 最终关闭整个 Socket 连接
            mSocket.close()
            logI("断开成功！")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return !mSocket.isConnected
    }

    override fun close() {
        disConnect()
    }

    /**
     * 发送消息到服务器
     *
     * @param msg 消息内容
     */
    fun sendMessage(msg: String, charset: Charset) = send(msg.toByteArray(charset))

    /**
     * 发送消息到服务器
     *
     * @param msgBytes 消息内容
     */
    fun sendMessage(msgBytes: ByteArray) = send(msgBytes)

    /**
     * 从服务器接收数据
     * 注意：需要异步获取
     *
     * @return 接收的消息内容
     */
    fun receiverMessage(): PackageMsg? {
        val bytesList = ArrayList<ByteArray>()
        val indexOfFlag = ArrayList<Int>()
        val readMsg = receiver() // 接收消息
        // 读取标识位个数
        readMsg.mapIndexed { index, byte ->
            if (byte == (0x7e).toByte()) indexOfFlag.add(index)
        }
        val count = indexOfFlag.size / 2
        logI("消息包数量为：$count")
        // 读取的数据为单个包
        if (count == 1) return PackageMsg().setReadHexMsg(readMsg)
        // 读取的数据为多个分包
        (0 until indexOfFlag.size / 2).forEach {
            // 循环添加分包
            bytesList.add(readMsg.asList().subList(indexOfFlag[it * 2], indexOfFlag[it * 2 + 1] + 1).toByteArray())
        }
        return PackageMsg().setReadHexMsgList(bytesList)
    }

    /**
     * 发送消息到服务器
     *
     * @param msgBytes 消息内容
     */
    open protected fun send(msgBytes: ByteArray) {
        if (!mSocket.isConnected) {
            connect()
        }
        try {
            // 步骤1：从Socket 获得输出流对象OutputStream
            // 该对象作用：发送数据
            mOutputStream = mSocket.getOutputStream()
            val dos = DataOutputStream(mOutputStream)
            /** // 步骤2：写入需要发送的数据到输出流对象中
            mOutputStream!!.write(msgBytes)
            // 特别注意：数据的结尾加上换行符才可让服务器端的 readline() 停止阻塞
            // 步骤3：发送数据到服务端
            mOutputStream!!.flush() */
            dos.write(msgBytes)
            dos.flush()
//            logI("发送消息成功！内容：${ConvertHelper.byteArray2HexString(msgBytes)}")
            logI("发送消息成功！")
        } catch (e: IOException) {
            logE("发送消息失败！内容：${ConvertHelper.byteArray2HexString(msgBytes)}")
        }
    }

    /**
     * 从服务器接收数据
     * 注意：需要异步获取
     *
     * @return 接收的消息内容集合
     */
    open protected fun receiver(): ByteArray {
        val arrayList = ArrayList<Byte>()
        try {
            mBufferInputStream = BufferedInputStream(mSocket.getInputStream())
            readInputStream(arrayList)
            logI("接收消息成功！服务器应答消息长度：${arrayList.size}")
        } catch (e: IOException) {
            logE("接收消息失败！")
            e.printStackTrace()
        }
        return arrayList.toByteArray()
    }

    /**
     * 读取输入流的内容
     *
     * @param arrayList 存储内容的集合
     */
    private fun readInputStream(arrayList: ArrayList<Byte>) {
        val dis = DataInputStream(mBufferInputStream)
        var len = 0
        //            loop@ while (len != -1) {
        //                val bytes = ByteArray(1024)
        //                len = dis.read(bytes)
        //                if (len == 1024) {
        //                    arrayList.addAll(bytes.asList())
        //                } else {
        //                    if (len == -1) break@loop
        //                    // 检测到没有数据后跳出循环
        //                    arrayList.addAll(bytes.asList().subList(0, len))
        //                    break@loop
        //                }
        //            }
        val currByteArray = ArrayList<Byte>() // 当前解析消息包
        var bytes = ByteArray(1)
        var first = true // 第一个标识位
        loop@ while (len != -1) {
            len = dis.read(bytes)
            currByteArray.add(bytes[0])
            if (bytes[0] == (0x7e).toByte()) {
                first = if (!first) {
                    arrayList.addAll(currByteArray)
                    val count = MessageHelper.getSubPackagesCount(currByteArray.toByteArray())
                    if (count == 1) break@loop
                    val currIndex = MessageHelper.getSubPackageIndex(currByteArray.toByteArray())
                    if (currIndex == count) break@loop
                    currByteArray.clear()
                    true
                } else false
            }
        }
    }

    companion object {
        // Socket 配置信息
        val SOCKET_TIME_OUT = 5000 // Socket 连接超时时间
        val SOCKET_INPUT_TIME_OUT = 10000 // Socket 读取超时时间
        // 服务器信息
        var SERVER_IP = "114.215.173.239" // IP 地址
        var SERVER_PORT = 9001 // 服务器端口号
    }
}
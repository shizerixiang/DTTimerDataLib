package com.beviswang.datalibrary.module

import java.io.File
import java.io.IOException
import android.util.Log
import com.beviswang.datalibrary.serialport.SerialPort
import com.beviswang.datalibrary.util.ConvertHelper
import org.jetbrains.anko.doAsync

/**
 * 串口模块
 * Created by shize on 2018/1/8.
 */
class SerialPortModule(file: File, baudrate: Int, flags: Int) {
    // 串口
    private var mSerialPort = SerialPort(file, baudrate, flags)

    /**
     * 写入串口数据
     */
    fun writePort(data: ByteArray) {
        doAsync {
            val mOutputStream = mSerialPort.outputStream
            try {
                mOutputStream.write(data)
                mOutputStream.flush()
                Log.i(javaClass.simpleName, "写入成功！")
            } catch (e: IOException) {
                Log.i(javaClass.simpleName, "写入失败！")
                e.printStackTrace()
            }
        }
    }

    /**
     * 读取串口数据
     *
     * @param callback 回调接口
     */
    fun readPort(callback: OnResultListener?) {
        doAsync {
            val size: Int
            val mInputStream = mSerialPort.inputStream
            try {
                val buffer = ByteArray(256)
                size = mInputStream.read(buffer)
                if (size > 0)
                    Log.i(javaClass.simpleName, "接收到的数据：" + ConvertHelper.byteArray2HexString(buffer))
                callback?.onReceived(buffer)
                Log.i(javaClass.simpleName, "接收成功！")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.i(javaClass.simpleName, "接收失败！")
            }
        }
    }

    /**
     * 关闭串口
     */
    fun close() {
        mSerialPort.close()
    }

    /**
     * 结果监听器
     */
    interface OnResultListener {
        /**
         * 当接收到数据时
         *
         * @param data 接收的数据
         */
        fun onReceived(data: ByteArray)
    }

    companion object {
        private var INSTANCE: SerialPortModule? = null
        /**
         * 获取串口
         * @param path 串口设备路径
         * @param baudrate 串口通讯比特率
         * @param flags 标志位
         */
        fun getSerialPort(path: String, baudrate: Int, flags: Int): SerialPortModule {
            if (INSTANCE == null) INSTANCE = SerialPortModule(File(path), baudrate, flags)
            return INSTANCE!!
        }

        /**
         * 关闭串口
         */
        fun closeSerialPort() {
            if (INSTANCE == null) {
                return
            }
            INSTANCE!!.close()
            INSTANCE = null
        }
    }
}
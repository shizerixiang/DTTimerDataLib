package com.beviswang.datalibrary.message

import com.beviswang.datalibrary.logD
import com.beviswang.datalibrary.util.ConvertHelper

/**
 * 终端控制消息体实体类
 * Created by shize on 2018/1/25.
 */
class ControlMsg : PackageMsg.MsgBody {
    /**
     * 命令字 1 byte
     * 1、无线升级
     * 2、控制终端连接指定服务器
     * 3、终端关机
     * 4、终端复位
     * 5、终端恢复出厂设置
     * 6、关闭数据通信
     * 7、关闭所有无限通信
     */
    var commendChar: Int = 0
    /** 命令参数，每个字段之间采用“;”分隔，每个 String 字段先按 GBK 编码处理后再组成消息 不限制 byte */
    var commendParam: PackageMsg.MsgBody? = null

    override fun setBodyBytes(byteArray: ByteArray) {
        commendChar = byteArray[0].toInt()
        commendParam = getCommendParamMsg(commendChar)
        commendParam!!.setBodyBytes(byteArray.asList().subList(1, byteArray.size).toByteArray())
    }

    override fun getBodyArray(): ByteArray {
        return byteArrayOf()
    }

    /**
     * 通过命令字获取命令参数
     *
     * @param commendChar 命令字
     * @return 命令参数
     */
    private fun getCommendParamMsg(commendChar: Int): PackageMsg.MsgBody {
        return when (commendChar) {
            1 -> ControlMsg.UpgradeParam()
            2 -> ControlMsg.AppointServerParam()
            else -> EmptyMsg()
        }
    }

    /**
     * 升级指令消息
     * 参数之间采用英文分号分隔。指令如下：
     * URL 地址;拨号点名称;拨号用户名;拨号密码;IP 地址;TCP 端口;UDP 端口;制造商 ID;硬件版本;固件版本;连接到指定服务器时限
     * 若某个参数无值则放空
     */
    class UpgradeParam : PackageMsg.MsgBody {
        var url: String = "" // 安装包下载地址
        var dialName: String = "" // 拨号点名称
        var dialUserName: String = "" // 拨号点用户名
        var dialPassword: String = "" // 拨号密码
        var urlIP: String = "" // IP 地址
        var tcpPort: Int = 0 // TCP 端口号
        var udpPort: Int = 0 // UDP 端口号
        var manufacturerId: String = "" // 制造商 ID
        var hardwareVersion: String = "" // 硬件版本
        var softwareVersion: String = "" // 软件版本
        var linkTimeout: Long = 0L // 连接到指定服务器时限

        override fun setBodyBytes(byteArray: ByteArray) {
            val se = ';'.toByte()
            var bytes = byteArray.asList()
            var index = byteArray.indexOf(se)
            if (index != 0) url = ConvertHelper.byteArray2GBKString(bytes.subList(0, index).toByteArray())
            bytes = bytes.subList(index + 1, bytes.size)
            index = bytes.indexOf(se)
            if (index != 0) dialName = ConvertHelper.byteArray2GBKString(bytes.subList(0, index).toByteArray())
            bytes = bytes.subList(index + 1, bytes.size)
            index = bytes.indexOf(se)
            if (index != 0) dialUserName = ConvertHelper.byteArray2GBKString(bytes.subList(0, index).toByteArray())
            bytes = bytes.subList(index + 1, bytes.size)
            index = bytes.indexOf(se)
            if (index != 0) dialPassword = ConvertHelper.byteArray2GBKString(bytes.subList(0, index).toByteArray())
            bytes = bytes.subList(index + 1, bytes.size)
            index = bytes.indexOf(se)
            if (index != 0) urlIP = ConvertHelper.byteArray2GBKString(bytes.subList(0, index).toByteArray())
            bytes = bytes.subList(index + 1, bytes.size)
            index = bytes.indexOf(se)
            if (index != 0) tcpPort = Integer.parseInt(ConvertHelper.byteArray2HexString(bytes.subList(0, 3).toByteArray()), 16)
            bytes = bytes.subList(index + 1, bytes.size)
            index = bytes.indexOf(se)
            if (index != 0) udpPort = Integer.parseInt(ConvertHelper.byteArray2HexString(bytes.subList(0, 3).toByteArray()), 16)
            bytes = bytes.subList(index + 1, bytes.size)
            index = bytes.indexOf(se)
            if (index != 0) manufacturerId = ConvertHelper.byteArray2GBKString(bytes.subList(0, index).toByteArray())
            bytes = bytes.subList(index + 1, bytes.size)
            index = bytes.indexOf(se)
            if (index != 0) hardwareVersion = ConvertHelper.byteArray2GBKString(bytes.subList(0, index).toByteArray())
            bytes = bytes.subList(index + 1, bytes.size)
            index = bytes.indexOf(se)
            if (index != 0) softwareVersion = ConvertHelper.byteArray2GBKString(bytes.subList(0, index).toByteArray())
            bytes = bytes.subList(index + 1, bytes.size)
            index = bytes.size - 1
            linkTimeout = Integer.parseInt(ConvertHelper.byteArray2HexString(bytes.subList(0, index).toByteArray()), 16).toLong()
            logD(toString())
        }

        override fun getBodyArray(): ByteArray {
            return byteArrayOf()
        }

        override fun toString(): String {
            return "下载地址：$url \n拨号点名称：$dialName \n拨号点用户名：$dialUserName \n" +
                    "拨号点密码：$dialPassword \nIP 地址：$urlIP \nTCP 端口号：$udpPort \n" +
                    "制造商 ID：$manufacturerId \n硬件版本：$hardwareVersion \n固件版本：$softwareVersion \n" +
                    "连接到指定服务器时限：$linkTimeout"
        }
    }

    /**
     * 控制终端连接到指定服务器
     * 参数之间采用英文分号分隔。控制指令如下：
     * 连接控制;监管平台鉴权码;拨号点名称;拨号用户名;拨号密码;IP 地址;TCP 端口;UDP 端口;连接到指定服务器时限
     * 若某个参数无指则放空，若连接控制值为 1 ，则无后继参数
     */
    class AppointServerParam : PackageMsg.MsgBody {
        override fun setBodyBytes(byteArray: ByteArray) {

        }

        override fun getBodyArray(): ByteArray {
            return byteArrayOf()
        }
    }
}
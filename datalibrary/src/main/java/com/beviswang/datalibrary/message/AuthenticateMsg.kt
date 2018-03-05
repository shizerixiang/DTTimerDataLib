package com.beviswang.datalibrary.message

import com.beviswang.datalibrary.logI
import com.beviswang.datalibrary.util.*

/**
 * 终端鉴权消息体实体类
 * 注意：每次使用该实体类发送鉴权消息时，应该重新创建实例，否则会造成重复发送同一个时间戳的错误
 * 只需要设置终端编号即可发送
 * Created by shize on 2018/1/19.
 */
class AuthenticateMsg : PackageMsg.MsgBody {
    private var timeStamp: Long = SystemHelper.systemTimeStamp // 当前时间戳 4 byte
    private var authenticateContent: String = "" // 鉴权密文 256 byte ，使用终端证书通过加密算法对终端编号、时间戳进行加密

    /**
     * @param terminalNum 设置终端编号
     */
    fun setTerminalNum(terminalNum: String) {
        val pKey = SignVerifyHelper.getDevCAInfo() ?: throw Exception("没有获取到正确的私钥！")
        authenticateContent = SignVerifyHelper.sign(terminalNum, timeStamp, pKey) ?: throw Exception("加密失败！")
        logI("加密消息长度：${authenticateContent.length}")
    }

    override fun setBodyBytes(byteArray: ByteArray) { }

    override fun getBodyArray(): ByteArray {
        return ConvertHelper.string2ByteArray(toString())
    }

    override fun toString(): String {
        return MessageHelper.supplementStr(8, timeStamp.toString(16),
                0) + authenticateContent
    }
}
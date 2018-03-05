package com.beviswang.datalibrary.message

/**
 * 分包消息体
 * Created by shize on 2018/2/5.
 */
class SubPackageMsg : PackageMsg.MsgBody {
    private var sendBytes: ByteArray = ByteArray(1)

    override fun setBodyBytes(byteArray: ByteArray) {
        sendBytes = byteArray
    }

    override fun getBodyArray(): ByteArray {
        return sendBytes
    }
}
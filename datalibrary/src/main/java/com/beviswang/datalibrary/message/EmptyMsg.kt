package com.beviswang.datalibrary.message

/**
 * 空消息体实体类
 * Created by shize on 2018/1/22.
 */
class EmptyMsg :PackageMsg.MsgBody {

    override fun setBodyBytes(byteArray: ByteArray) { }

    override fun getBodyArray(): ByteArray {
        return byteArrayOf()
    }
}
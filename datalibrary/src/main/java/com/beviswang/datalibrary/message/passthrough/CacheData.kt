package com.beviswang.datalibrary.message.passthrough

import com.beviswang.datalibrary.message.PasBodyMsg
import com.beviswang.datalibrary.util.ConvertHelper

/**
 * 缓存用消息数据模型
 * Created by shize on 2018/3/7.
 */
class CacheData():PasBodyMsg.DataContent {
    var cacheByte:ByteArray = byteArrayOf()

    constructor(hexString:String):this(){
        cacheByte = ConvertHelper.string2ByteArray(hexString)
    }

    override fun getDataByteArray(): ByteArray {
        return cacheByte
    }

    override fun toString(): String {
        return ConvertHelper.byteArray2HexString(cacheByte)
    }

    override fun setDataByteArray(byteArray: ByteArray) {
        cacheByte = byteArray
    }
}
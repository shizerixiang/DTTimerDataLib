package com.beviswang.datalibrary.message.passthrough

import com.beviswang.datalibrary.message.PasBodyMsg

/**
 * 空数据内容
 * Created by shize on 2018/1/30.
 */
class EmptyData: PasBodyMsg.DataContent {
    override fun getDataByteArray(): ByteArray {
        return byteArrayOf()
    }

    override fun setDataByteArray(byteArray: ByteArray) { }
}
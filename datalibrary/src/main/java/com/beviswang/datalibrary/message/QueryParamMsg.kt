package com.beviswang.datalibrary.message

import com.beviswang.datalibrary.util.ConvertHelper
import com.beviswang.datalibrary.util.MessageHelper

/**
 * 查询指定终端参数消息体实体类
 * Created by shize on 2018/1/24.
 */
class QueryParamMsg : PackageMsg.MsgBody {
    var paramTotal: Int = 0             // 参数总数 1 byte
    var paramIDList = ArrayList<Int>()  // 参数 ID 列表 无限制，每个 ID 4 byte

    override fun setBodyBytes(byteArray: ByteArray) {
        paramTotal = byteArray[0].toInt()
        paramIDList = MessageHelper.parseParamID(byteArray.asList().subList(1, byteArray.size).toByteArray())
    }

    override fun getBodyArray(): ByteArray {
        return ConvertHelper.string2ByteArray(toString())
    }

    override fun toString(): String {
        val pTotal = MessageHelper.supplementStr(2, ConvertHelper.integer2hexString(paramTotal), 0)
        var pIDList = ""
        paramIDList.forEach {
            pIDList += MessageHelper.supplementStr(8, ConvertHelper.integer2hexString(it), 0)
        }
        return pTotal + pIDList
    }
}
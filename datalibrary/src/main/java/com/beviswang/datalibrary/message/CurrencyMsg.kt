package com.beviswang.datalibrary.message

import com.beviswang.datalibrary.util.ConvertHelper
import com.beviswang.datalibrary.util.MessageHelper

/**
 * 通用应答消息体实体类
 * Created by shize on 2018/1/17.
 */
class CurrencyMsg : PackageMsg.MsgBody {
    var flowNum: Int = 0 // 应答流水号 2 byte，对应服务器端消息的流水号
    var responseId: String = "" // 应答 ID 2 byte，对应服务器端消息 ID
    var result: Int = 0 // 结果 1 byte，0 成功；1 失败；2 消息有误；3 不支持

    override fun getBodyArray(): ByteArray {
        return ConvertHelper.string2ByteArray(toString())
    }

    override fun setBodyBytes(byteArray: ByteArray) {
        val byteString = ConvertHelper.byteArray2HexString(byteArray)
        flowNum = Integer.parseInt(byteString.substring(0, 4), 16)
        responseId = byteString.substring(4, 8)
        result = Integer.parseInt(byteString.substring(8, 10), 16)
    }

    override fun toString(): String {
        return MessageHelper.supplementStr(4, Integer.toHexString(flowNum),
                0) + responseId + MessageHelper.supplementStr(2,
                Integer.toHexString(result), 0)
    }

    companion object {
        val RESULT_CODE_SUCCEED = 0 // 成功
        val RESULT_CODE_FAILED = 1 // 失败
        val RESULT_CODE_WRONG = 2 // 消息有误
        val RESULT_CODE_NONSUPPORT = 3 // 不支持
    }
}
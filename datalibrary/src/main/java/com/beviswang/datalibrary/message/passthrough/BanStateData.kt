package com.beviswang.datalibrary.message.passthrough

import com.beviswang.datalibrary.message.PasBodyMsg
import com.beviswang.datalibrary.util.ConvertHelper
import com.beviswang.datalibrary.util.MessageHelper

/**
 * 设置禁训状态透传消息数据
 * Created by shize on 2018/2/27.
 */
class BanStateData : PasBodyMsg.DataContent {
    /**
     * 禁训状态 1 byte
     * 1：可用，默认值
     * 2：不可用
     */
    var state: Int = 1
    /**
     * 提示消息长度 1 byte
     * 长度为 n，无提示消息则为 0
     */
    var hintMsgLen: Int = 0
    /**
     * 提示消息内容 无限制 byte
     * “可用” 状态
     */
    var hintMsgContent: String = ""

    override fun getDataByteArray(): ByteArray {
        return byteArrayOf()
    }

    override fun setDataByteArray(byteArray: ByteArray) {
        state = byteArray[0].toInt()
        hintMsgLen = byteArray[1].toInt()
        hintMsgContent = ConvertHelper.byteArray2GBKString(byteArray.asList().subList(2,byteArray.size).toByteArray())
    }

    /** 设置禁训状态应答透传消息数据 */
    class BanStateDataResponse() : PasBodyMsg.DataContent {
        /**
         * 执行结果 1 byte
         * 0：默认应答
         * 1：设置成功
         * 2：设置失败
         * 9：其他错误
         */
        var resultCode: Int = 0
        /**
         * 禁训状态 1 byte
         * 1：可用，默认值
         * 2：禁用
         */
        var banState:Int = 1
        /**
         * 提示消息长度 1 byte
         */
        var hintMsgLen:Int = 0
        /**
         * 提示消息内容 无限制 byte
         * “可用”状态
         */
        var hintMsgContent:String = ""

        constructor(result:Int):this(){
            resultCode = result
        }

        override fun getDataByteArray(): ByteArray {
            return ConvertHelper.string2ByteArray(toString())
        }

        override fun toString(): String {
            return MessageHelper.supplementStr(2,
                    ConvertHelper.integer2hexString(resultCode),0)+
                    MessageHelper.supplementStr(2,
                    ConvertHelper.integer2hexString(banState),0)+
                    MessageHelper.supplementStr(2,
                            ConvertHelper.integer2hexString(hintMsgLen),0)+
                    ConvertHelper.stringGBK2HexString(hintMsgContent)
        }

        override fun setDataByteArray(byteArray: ByteArray) { }
    }
}
package com.beviswang.datalibrary.message.passthrough

import com.beviswang.datalibrary.message.GNSSDataMsg
import com.beviswang.datalibrary.message.PasBodyMsg
import com.beviswang.datalibrary.util.ConvertHelper
import com.beviswang.datalibrary.util.MessageHelper

/**
 * 上报教练员登录消息数据格式实体类
 * Created by shize on 2018/2/1.
 */
class CoachLoginData : PasBodyMsg.DataContent {
    /** 教练员编号 16 byte */
    var coachNum: String = "8041831254662415"
    /**
     * 教练员身份证号 18 byte
     * ASCII 码，不足 18 位前补 0x00
     */
    var coachID: String = "130682198909202430"
    /**
     * 准教车型 2 byte
     * A1\A2\A3\B1\B2\C1\C2\C3\C4\D\E\F
     */
    var teachCarType: String = "C1"
    /** 基本 GNSS 数据包 28 byte */
    var mGNSSDataMsg: GNSSDataMsg = GNSSDataMsg()

    override fun getDataByteArray(): ByteArray {
        return ConvertHelper.string2ByteArray(toString())
    }

    override fun setDataByteArray(byteArray: ByteArray) {}

    override fun toString(): String {
        return ConvertHelper.stringGBK2HexString(coachNum) + ConvertHelper.byteArray2HexString(coachID.toByteArray()) +
                MessageHelper.supplementStr(4, ConvertHelper.byteArray2HexString(teachCarType.toByteArray()), 0) + mGNSSDataMsg.toString()
    }

    /** 教练员登录应答 */
    class LoginAnswerData : PasBodyMsg.DataContent {
        /**
         * 登录结果 1 byte
         * 1：登录成功
         * 2：无效的教练员编号
         * 3：准教车型不符
         * 9：其他错误
         */
        var result: Int = 1
        /** 教练编号 16 byte */
        private var coachNum: String = "8041831254662415"
        /**
         * 是否报读附加消息 1 byte
         * 0：根据全局设置决定是否报读
         * 1：需要报读
         * 2：不必报读
         */
        var isNeedSpeak: Int = 0
        /**
         * 附加消息长度 1 byte
         * 长度为 n，无附加数据则为 0
         */
        private var additionalMsgLen: Int = 0
        /** 附加消息 无限制 byte */
        private var additionalMsg: String = ""

        override fun setDataByteArray(byteArray: ByteArray) {
            val byteList = byteArray.asList()
            result = byteArray[0].toInt()
            coachNum = ConvertHelper.byteArray2GBKString(byteList.subList(1, 17).toByteArray())
            isNeedSpeak = byteArray[17].toInt()
            additionalMsgLen = byteArray[18].toInt()
            if (additionalMsgLen == 0) return
            additionalMsg = ConvertHelper.byteArray2GBKString(byteList.subList(19, byteArray.size).toByteArray())
        }

        override fun getDataByteArray(): ByteArray = byteArrayOf()
    }
}
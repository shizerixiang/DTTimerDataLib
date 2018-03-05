package com.beviswang.datalibrary.message.passthrough

import com.beviswang.datalibrary.Publish
import com.beviswang.datalibrary.message.GNSSDataMsg
import com.beviswang.datalibrary.message.PasBodyMsg
import com.beviswang.datalibrary.util.ConvertHelper

/**
 * 上报教练员登出消息数据实体类
 * Created by shize on 2018/2/1.
 */
class CoachLogoutData : PasBodyMsg.DataContent {
    /** 教练编号 16 byte */
    private var coachNum: String = Publish.getInstance().mCoachInfo?.mNumber ?:
            throw Exception("教练未登录，无法登出！")
    /** 基本 GNSS 数据包 28 byte */
    private var mGNSSDataMsg: GNSSDataMsg = GNSSDataMsg()

    override fun getDataByteArray(): ByteArray {
        return ConvertHelper.string2ByteArray(toString())
    }

    override fun toString(): String {
        return ConvertHelper.byteArray2HexString(coachNum.toByteArray()) + mGNSSDataMsg.toString()
    }

    override fun setDataByteArray(byteArray: ByteArray) {}

    /**
     * 教练员登出应答消息数据实体类
     * 注意：登出成功清理内存中的教练数据
     */
    class LogoutAnswerData : PasBodyMsg.DataContent {
        /**
         *  登出结果 1 byte
         * 1：登出成功
         * 2：登出失败
         * 9：其他错误
         */
        var result: Int = 0
        /** 教练编号 16 byte */
        private var coachNum: String = ""

        override fun getDataByteArray(): ByteArray = byteArrayOf()

        override fun setDataByteArray(byteArray: ByteArray) {
            result = byteArray[0].toInt()
            coachNum = ConvertHelper.byteArray2GBKString(byteArray.asList().subList(
                    1, byteArray.size).toByteArray())
        }
    }
}
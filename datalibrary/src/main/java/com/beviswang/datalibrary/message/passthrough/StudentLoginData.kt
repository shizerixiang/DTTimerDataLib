package com.beviswang.datalibrary.message.passthrough

import com.beviswang.datalibrary.Publish
import com.beviswang.datalibrary.message.GNSSDataMsg
import com.beviswang.datalibrary.message.PasBodyMsg
import com.beviswang.datalibrary.util.ConvertHelper
import com.beviswang.datalibrary.util.MessageHelper

/**
 * 学员登录消息数据格式实体类
 * Created by shize on 2018/2/5.
 */
class StudentLoginData : PasBodyMsg.DataContent {
    /** 学员编号 16 byte */
    var stuNum: String = "9134677538435566"
    /** 当前教练编号 16 byte */
    private var curCoachNum: String = Publish.getInstance().mCoachInfo?.mNumber ?:
            throw Exception("教练未登录！学员无法登录！")
    /** 当前培训课程 BCD 码 5 byte */
    var curClass: String = Publish.getInstance().mCurCourse
    /** 课堂 ID 4 byte */
    private var curClassId: Int = Publish.getInstance().mClassId
    /** 基本 GNSS 数据包 */
    private var mGNSSDataMsg: GNSSDataMsg = GNSSDataMsg()

    override fun getDataByteArray(): ByteArray {
        return ConvertHelper.string2ByteArray(toString())
    }

    override fun toString(): String {
        return ConvertHelper.byteArray2HexString(stuNum.toByteArray()) +
                ConvertHelper.byteArray2HexString(curCoachNum.toByteArray()) + curClass +
                MessageHelper.supplementStr(8, Integer.toHexString(curClassId), 0) +
                mGNSSDataMsg.toString()
    }

    override fun setDataByteArray(byteArray: ByteArray) {}

    /** 学员登录应答消息数据格式实体类 */
    class LoginAnswerData : PasBodyMsg.DataContent {
        /**
         * 登录结果代码 1 byte
         * 1：登录成功
         * 2：无效的学员编号
         * 3：禁止登录的学员
         * 4：区域外教学提醒
         * 5：准教车型与培训车型不符
         * 9：其他错误
         */
        var resultCode: Int = 0
        /** 学员编号 16 byte */
        private var stuNum: String = ""
        /** 总培训学时，单位 min 2 byte */
        var trainHours: Int = 0
        /** 当前培训部分已完成学时，单位 min 2 byte */
        var curHours: Int = 0
        /** 总培训里程，单位 1/10km 2 byte */
        var trainMileage: Int = 0
        /** 当前培训部分已完成里程，单位 1/10km 2 byte */
        var curMileage: Int = 0
        /** 是否报读附加消息，0：不必报读；1：需要报读 1 byte */
        var isSpeakAddMsg: Int = 0
        /**
         * 附加消息长度 1 byte
         * 长度为 n，无附加数据则为 0
         */
        private var additionalMsgLen: Int = 0
        /** 附加消息 最大 254 byte */
        var additionalMsg: String = ""

        override fun getDataByteArray(): ByteArray = byteArrayOf()

        override fun setDataByteArray(byteArray: ByteArray) {
            val byteList = byteArray.asList()
            resultCode = byteArray[0].toInt()
            stuNum = ConvertHelper.byteArray2GBKString(byteList.subList(1, 17).toByteArray())
            trainHours = Integer.parseInt(ConvertHelper.byteArray2HexString(
                    byteList.subList(17, 19).toByteArray()), 16)
            curHours = Integer.parseInt(ConvertHelper.byteArray2HexString(
                    byteList.subList(19, 21).toByteArray()), 16)
            trainMileage = Integer.parseInt(ConvertHelper.byteArray2HexString(
                    byteList.subList(21, 23).toByteArray()), 16)
            curMileage = Integer.parseInt(ConvertHelper.byteArray2HexString(
                    byteList.subList(23, 25).toByteArray()), 16)
            isSpeakAddMsg = byteArray[25].toInt()
            additionalMsgLen = byteArray[26].toInt()
            if (additionalMsgLen != 0) additionalMsg = ConvertHelper.byteArray2GBKString(
                    byteList.subList(27, byteArray.size).toByteArray())
        }
    }
}
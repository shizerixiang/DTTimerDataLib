package com.beviswang.datalibrary.message.passthrough

import com.beviswang.datalibrary.Publish
import com.beviswang.datalibrary.message.GNSSDataMsg
import com.beviswang.datalibrary.message.PasBodyMsg
import com.beviswang.datalibrary.util.ConvertHelper
import com.beviswang.datalibrary.util.MessageHelper
import com.beviswang.datalibrary.util.SystemHelper

/**
 * 学员登出消息数据格式实体类
 * Created by shize on 2018/2/5.
 */
class StudentLogoutData : PasBodyMsg.DataContent {
    /** 学员编号 16 byte */
    private var stuNum: String = Publish.getInstance().mStudentInfo?.mNum ?:
            throw Exception("学员未登录，登出失败！")
    /** 登出时间 BCD 码的 YYMMDDhhmmss 6 byte */
    private var logoutTime: String = SystemHelper.yyMMddHHmmssData
    /** 学员该次登录总时间，单位 min 2 byte */
    private var loginHours: Int = Publish.getInstance().mStudentInfo?.mCurCourseTotalDuration ?:
            throw  Exception("学员未登录，无法登出！")
    /** 学员该次登录总里程，单位 1/10 km 2 byte */
    private var loginMileage: Int
    /** 课堂 ID 4 byte */
    private var classId: Int = Publish.getInstance().mClassId
    /** 基本 GNSS 数据包 28 byte */
    private var mGNSSDataMsg: GNSSDataMsg = GNSSDataMsg()

    init {
        val distance = Publish.getInstance().mStudentInfo?.mCurCourseTotalDistance ?:
                throw  Exception("学员未登录，无法登出！")
        loginMileage = (distance * 10).toInt()
    }

    override fun getDataByteArray(): ByteArray {
        return ConvertHelper.string2ByteArray(toString())
    }

    override fun toString(): String {
        return ConvertHelper.byteArray2HexString(stuNum.toByteArray()) + logoutTime +
                MessageHelper.supplementStr(4, Integer.toHexString(loginHours), 0) +
                MessageHelper.supplementStr(4, Integer.toHexString(loginMileage), 0) +
                MessageHelper.supplementStr(8, Integer.toHexString(classId), 0) +
                mGNSSDataMsg.toString()
    }

    override fun setDataByteArray(byteArray: ByteArray) {}

    /** 学员登出应答消息数据格式实体类 */
    class LogoutAnswerData : PasBodyMsg.DataContent {
        /**
         * 登出结果 1 byte
         * 1：登出成功
         * 2：登出失败
         * 9：其他错误
         */
        var result: Int = 0
        /** 学员编号 16 byte */
        private var stuNum: String = ""

        override fun getDataByteArray(): ByteArray = byteArrayOf()

        override fun setDataByteArray(byteArray: ByteArray) {
            result = byteArray[0].toInt()
            stuNum = ConvertHelper.byteArray2GBKString(byteArray.asList().subList(
                    1, byteArray.size).toByteArray())
        }
    }
}
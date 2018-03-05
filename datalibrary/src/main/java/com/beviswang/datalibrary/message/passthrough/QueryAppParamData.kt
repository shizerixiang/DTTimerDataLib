package com.beviswang.datalibrary.message.passthrough

import com.beviswang.datalibrary.message.PasBodyMsg
import com.beviswang.datalibrary.util.ConvertHelper
import com.beviswang.datalibrary.util.MessageHelper

/**
 * 查询计时终端应用参数透传消息数据
 * Created by shize on 2018/3/5.
 */
class QueryAppParamData : PasBodyMsg.DataContent {

    override fun getDataByteArray(): ByteArray {
        return byteArrayOf()
    }

    override fun setDataByteArray(byteArray: ByteArray) {

    }

    /** 查询计时终端应用参数应答透传消息数据 */
    class QueryAnswerData : PasBodyMsg.DataContent {
        /**
         * 查询结果 1 byte
         * 0：成功
         * 1：失败
         * 9：其他错误
         */
        var result: Int = 0
        /**
         * 定时拍照时间间隔 1 byte
         * 单位：min，默认值：15
         * 在学员登录后间隔固定时间拍摄照片
         */
        var timingInterval: Int = 15
        /**
         * 照片上传设置 1 byte
         * 0：不自动请求上传
         * 1：自动请求上传
         */
        var uploadSetting: Int = 0
        /**
         * 是否报读附加消息 1 byte
         * 1：自动报读
         * 2：不报读
         */
        var isSpeakAddMsg: Int = 1
        /**
         * 熄火后停止学时计时的延时时间 1 byte
         * 单位：min
         */
        var flameoutDelayed: Int = 0
        /**
         * 熄火后 GNSS 数据包上传间隔 2 byte
         * 单位：s，默认值 3600，0 表示不上传
         */
        var upGNSSDataDuration: Long = 3600
        /**
         * 熄火后教练自动登出的延时时间 2 byte
         * 单位：min，默认值 150
         */
        var flameoutCoachAutoOut: Int = 150
        /**
         * 重新验证身份时间 2 byte
         * 单位：min，默认值 30
         */
        var reVerificationTime: Int = 30
        /**
         * 教练跨校教学 1 byte
         * 1：允许
         * 2：禁止
         */
        var canCoachCrossTeach: Int = 2
        /**
         * 学员跨校学习 1 byte
         * 1：允许
         * 2：禁止
         */
        var canStuCrossStudy: Int = 2
        /**
         * 响应平台同类消息时间间隔 2 byte
         * 单位：s，在该时间间隔内对平台发送的多次相同 ID 消息可拒绝执行回复失败
         */
        var responsePlatformDuration: Long = 0

        override fun getDataByteArray(): ByteArray {
            return ConvertHelper.string2ByteArray(toString())
        }

        override fun toString(): String {
            return MessageHelper.supplementStr(2, result.toString(16), 0) +
                    MessageHelper.supplementStr(2, timingInterval.toString(16), 0) +
                    MessageHelper.supplementStr(2, uploadSetting.toString(16), 0) +
                    MessageHelper.supplementStr(2, isSpeakAddMsg.toString(16), 0) +
                    MessageHelper.supplementStr(2, flameoutDelayed.toString(16), 0) +
                    MessageHelper.supplementStr(4, upGNSSDataDuration.toString(16), 0) +
                    MessageHelper.supplementStr(4, flameoutCoachAutoOut.toString(16), 0) +
                    MessageHelper.supplementStr(4, reVerificationTime.toString(16), 0) +
                    MessageHelper.supplementStr(2, canCoachCrossTeach.toString(16), 0) +
                    MessageHelper.supplementStr(2, canStuCrossStudy.toString(16), 0) +
                    MessageHelper.supplementStr(4, responsePlatformDuration.toString(16), 0)

        }

        override fun setDataByteArray(byteArray: ByteArray) {}
    }
}
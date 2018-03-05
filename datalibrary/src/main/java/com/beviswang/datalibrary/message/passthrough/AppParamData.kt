package com.beviswang.datalibrary.message.passthrough

import com.beviswang.datalibrary.message.PasBodyMsg
import com.beviswang.datalibrary.util.ConvertHelper
import com.beviswang.datalibrary.util.MessageHelper
import com.beviswang.datalibrary.util.ParameterHelper

/**
 * 设置计时终端应用参数消息数据实体类 16 byte
 * Created by shize on 2018/1/31.
 */
class AppParamData : PasBodyMsg.DataContent {
    /**
     * 参数编号 1 byte
     * 参数编号与后续的字段编号一致，定义如下：
     * 0：设置所有已定义的参数
     * 1：定时拍照时间间隔
     * 2：照片上传设置
     * 3：是否报读附加消息
     * 4：熄火后停止学时计时的延时时间
     * 5：熄火后 GNSS 数据包上报间隔
     * 6：熄火后教练自动登出的延时时间
     * 7：重新验证身份时间间隔
     * 220-255：自定义
     */
    private var paramNumber: Int = 0
    /** 参数集合 */
    var params: ArrayList<Pair<Int, String>> = ArrayList()

    override fun getDataByteArray(): ByteArray {
        return byteArrayOf()
    }

    override fun setDataByteArray(byteArray: ByteArray) {
        val byteList = byteArray.asList()
        paramNumber = byteArray[0].toInt()
        when (paramNumber) {
            0 -> setAllAppParams(byteList)
            1 -> params.add(Pair(ParameterHelper.APP_TAKE_PHOTO_DURATION,
                    ConvertHelper.byteArray2HexString(byteList.subList(1, 2).toByteArray())))
            2 -> params.add(Pair(ParameterHelper.APP_IS_AUTO_REQUEST_UP,
                    ConvertHelper.byteArray2HexString(byteList.subList(2, 3).toByteArray())))
            3 -> params.add(Pair(ParameterHelper.APP_IS_READ_MSG,
                    ConvertHelper.byteArray2HexString(byteList.subList(3, 4).toByteArray())))
            4 -> params.add(Pair(ParameterHelper.APP_FLAMEOUT_DELAYED_TIME,
                    ConvertHelper.byteArray2HexString(byteList.subList(4, 5).toByteArray())))
            5 -> params.add(Pair(ParameterHelper.APP_UP_GNSS_DATA_DURATION,
                    ConvertHelper.byteArray2HexString(byteList.subList(5, 7).toByteArray())))
            6 -> params.add(Pair(ParameterHelper.APP_FLAMEOUT_COACH_AUTO_OUT,
                    ConvertHelper.byteArray2HexString(byteList.subList(7, 9).toByteArray())))
            7 -> params.add(Pair(ParameterHelper.APP_RE_VERIFICATION_TIME,
                    ConvertHelper.byteArray2HexString(byteList.subList(9, 11).toByteArray())))
            8 -> params.add(Pair(ParameterHelper.APP_CAN_COACH_CROSS_TEACH,
                    ConvertHelper.byteArray2HexString(byteList.subList(11, 12).toByteArray())))
            9 -> params.add(Pair(ParameterHelper.APP_CAN_STU_CROSS_STUDY,
                    ConvertHelper.byteArray2HexString(byteList.subList(12, 13).toByteArray())))
            10 -> params.add(Pair(ParameterHelper.APP_RESPONSE_DURATION,
                    ConvertHelper.byteArray2HexString(byteList.subList(13, 15).toByteArray())))
        }
    }

    /**
     * 设置所有应用参数
     *
     * @param byteList 消息 byte 集合
     */
    private fun setAllAppParams(byteList: List<Byte>) {
        params.add(Pair(ParameterHelper.APP_TAKE_PHOTO_DURATION,
                ConvertHelper.byteArray2HexString(byteList.subList(1, 2).toByteArray())))
        params.add(Pair(ParameterHelper.APP_IS_AUTO_REQUEST_UP,
                ConvertHelper.byteArray2HexString(byteList.subList(2, 3).toByteArray())))
        params.add(Pair(ParameterHelper.APP_IS_READ_MSG,
                ConvertHelper.byteArray2HexString(byteList.subList(3, 4).toByteArray())))
        params.add(Pair(ParameterHelper.APP_FLAMEOUT_DELAYED_TIME,
                ConvertHelper.byteArray2HexString(byteList.subList(4, 5).toByteArray())))
        params.add(Pair(ParameterHelper.APP_UP_GNSS_DATA_DURATION,
                ConvertHelper.byteArray2HexString(byteList.subList(5, 7).toByteArray())))
        params.add(Pair(ParameterHelper.APP_FLAMEOUT_COACH_AUTO_OUT,
                ConvertHelper.byteArray2HexString(byteList.subList(7, 9).toByteArray())))
        params.add(Pair(ParameterHelper.APP_RE_VERIFICATION_TIME,
                ConvertHelper.byteArray2HexString(byteList.subList(9, 11).toByteArray())))
        params.add(Pair(ParameterHelper.APP_CAN_COACH_CROSS_TEACH,
                ConvertHelper.byteArray2HexString(byteList.subList(11, 12).toByteArray())))
        params.add(Pair(ParameterHelper.APP_CAN_STU_CROSS_STUDY,
                ConvertHelper.byteArray2HexString(byteList.subList(12, 13).toByteArray())))
        params.add(Pair(ParameterHelper.APP_RESPONSE_DURATION,
                ConvertHelper.byteArray2HexString(byteList.subList(13, 15).toByteArray())))
    }

    override fun getDataLength(): Int = 16

    /** 设置应用参数应答消息数据实体类 */
    class AppParamAnswerData(code:Int = 1) : PasBodyMsg.DataContent {
        /**
         * 应答代码 1 byte
         * 1：设置成功
         * 2：设置失败
         * 9：其他错误
         */
        var responseCode: Int = code

        override fun getDataByteArray(): ByteArray {
            return ConvertHelper.string2ByteArray(toString())
        }

        override fun setDataByteArray(byteArray: ByteArray) {}

        override fun toString(): String {
            return MessageHelper.supplementStr(2, Integer.toHexString(responseCode), 0)
        }
    }
}
package com.beviswang.datalibrary.message.passthrough

import com.beviswang.datalibrary.message.PasBodyMsg
import com.beviswang.datalibrary.util.ConvertHelper
import com.beviswang.datalibrary.util.MessageHelper

/**
 * 查询照片消息数据实体类
 * Created by shize on 2018/2/7.
 */
class QueryPhotoData : PasBodyMsg.DataContent {
    /**
     * 查询方式 1 byte
     * 1：按时间查询
     */
    var queryType: Int = 1
    /**
     * 查询开始时间 BCD 6 byte
     * 格式为：YYMMDDhhmmss
     */
    var queryStartTime: String = ""
    /**
     * 查询结束时间 BCD 6 byte
     * 格式为：YYMMDDhhmmss
     */
    var queryEndTime: String = ""

    override fun getDataByteArray(): ByteArray = byteArrayOf()

    override fun setDataByteArray(byteArray: ByteArray) {
        val byteList = byteArray.asList()
        queryType = byteArray[0].toInt()
        queryStartTime = ConvertHelper.byteArray2HexString(byteList.subList(1, 7).toByteArray())
        queryEndTime = ConvertHelper.byteArray2HexString(byteList.subList(7, 13).toByteArray())
    }

    /** 查询照片应答消息数据实体类 */
    class QueryAnswerData : PasBodyMsg.DataContent {
        /**
         * 执行结果 1 byte
         * 1：开始查询
         * 2：执行失败
         * 9：其他错误
         */
        var resultCode: Int = 1

        override fun getDataByteArray(): ByteArray {
            return ConvertHelper.string2ByteArray(toString())
        }

        override fun toString(): String {
            return Integer.toHexString(resultCode)
        }

        override fun setDataByteArray(byteArray: ByteArray) {}
    }

    /** 上报照片查询结果消息数据实体类 */
    class ReportQueryResultData : PasBodyMsg.DataContent {
        /**
         * 是否上报结束 1 byte
         * 0：否；1：是
         */
        var isReportOver: Int = 1
        /**
         * 符合条件的照片总数 1 byte
         * 总数 n，为 0 则无后续字段
         */
        private var pictureCount: Int = 0
        /**
         * 此次发送的照片数目 1 byte
         * 数目 m，暂不使用
         */
        private var sendCount: Int = 0
        /**
         * 照片编号集合，每个编号 10 byte n*10 byte
         */
        var picNumList: ArrayList<String> = ArrayList()

        override fun getDataByteArray(): ByteArray {
            return byteArrayOf()
        }

        override fun toString(): String {
            pictureCount = picNumList.size
            var strPicList = ""
            picNumList.forEach {
                strPicList += ConvertHelper.byteArray2HexString(it.toByteArray())
            }
            return MessageHelper.supplementStr(2, Integer.toHexString(isReportOver), 0) +
                    MessageHelper.supplementStr(2, Integer.toHexString(pictureCount), 0) +
                    MessageHelper.supplementStr(2, Integer.toHexString(sendCount), 0) +
                    strPicList
        }

        override fun setDataByteArray(byteArray: ByteArray) {}
    }

    /** 上报照片查询结果应答消息数据实体类 */
    class ReportQueryAnswerData : PasBodyMsg.DataContent {
        /**
         * 应答代码 1 byte
         * 0：默认应答
         * 1：停止上报，终端收到“停止上报”应答后则停止查询结果的上报
         * 9：其他错误
         */
        var resultCode: Int = 0

        override fun getDataByteArray(): ByteArray = byteArrayOf()

        override fun setDataByteArray(byteArray: ByteArray) {
            resultCode = byteArray[0].toInt()
        }
    }

    /** 上传指定照片消息数据实体类 */
    class UploadAppointPictureData : PasBodyMsg.DataContent {
        /**
         * 上传指定照片 10 byte
         */
        var picNum: String = ""

        override fun getDataByteArray(): ByteArray = byteArrayOf()

        override fun setDataByteArray(byteArray: ByteArray) {
            picNum = ConvertHelper.byteArray2GBKString(byteArray)
        }
    }

    /** 上传指定照片应答消息数据实体类 */
    class UploadAppointAnswerData() : PasBodyMsg.DataContent {
        /**
         * 应答代码 1 byte
         * 0：找到照片，稍后上传
         * 1：没有该照片
         * 9：其他错误
         */
        var resultCode:Int = 0

        constructor(result:Int):this(){
            resultCode = result
        }

        override fun getDataByteArray(): ByteArray {
            return ConvertHelper.string2ByteArray(toString())
        }

        override fun toString(): String {
            return MessageHelper.supplementStr(2, Integer.toHexString(resultCode), 0)
        }

        override fun setDataByteArray(byteArray: ByteArray) {}
    }
}
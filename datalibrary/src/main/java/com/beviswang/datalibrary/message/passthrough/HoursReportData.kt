package com.beviswang.datalibrary.message.passthrough

import com.beviswang.datalibrary.Publish
import com.beviswang.datalibrary.message.GNSSDataMsg
import com.beviswang.datalibrary.message.PasBodyMsg
import com.beviswang.datalibrary.util.ConvertHelper
import com.beviswang.datalibrary.util.MessageHelper
import com.beviswang.datalibrary.util.SystemHelper

/**
 * 上报学时记录
 * 开始培训课程后，1 min 一次上传学时记录
 * 应答为通用应答
 * Created by shize on 2018/2/7.
 */
class HoursReportData : PasBodyMsg.DataContent {
    /**
     * 学时记录编号 26 byte
     * 学时记录编码应采用 26 位数字编码
     * 由“16 位计时设备编码 + 6 位日期码 + 4 位序列号”组成，并符合以下规则：
     * a> 计时设备编码采用全国驾培平台生成的统一编号，未申请时暂用 16 个“0”
     * b> 日期码定义：6 位数字，格式为 YYMMDD
     * c> 序列号定义：4 位数字，每日 0 时从“0001”开始，按顺序递增
     */
    var hoursNum: String = ""
    /**
     * 上报类型 1 byte
     * 0x01：自动上报
     * 0x02：应中心要求上报，如果是应中心要求上传
     * 则本次上传作业的驾培包序号保持与请求上传消息的驾培包序号一致
     * 后续分段上传的驾培包序号也保持一致
     */
    var reportType: Int = 0x01
    /**
     * 学员编号 16 byte
     */
    var stuNum: String = Publish.getInstance().mStudentInfo?.mNum ?:
            throw Exception("学员未登录")
    /**
     * 教练编号 16 byte
     */
    var coachNum: String = Publish.getInstance().mCoachInfo?.mNumber ?:
            throw Exception("教练未登录")
    /**
     * 课堂 ID 4 byte
     */
    var classId: Int = Publish.getInstance().mClassId
    /**
     * 记录产生时间 BCD 3 byte
     * 格式：HHmmss，1 min 最后 1s 的时间
     */
    var reportTime: String = SystemHelper.HHmmssData
    /**
     * 培训课程 BCD 5 byte
     * 课程编码
     */
    var curCourse: String = Publish.getInstance().mCurCourse
    /**
     * 记录状态 1 byte
     * 0：正常记录
     * 1：一次记录
     */
    var recodeState: Int = 0
    /**
     * 最大速度 2 byte
     * 1 min 内车辆到达的最大卫星定位速度
     * 1/10km/h
     */
    var maxSpeed: Int = 0
    /**
     * 里程 2 byte
     * 车辆 1 min 内行驶的总里程，1/10km
     */
    var mileage: Int = 0
    /**
     * 附加 GNSS 数据包 38 byte
     * 1 min 内第 30 s 的卫星定位数据
     * 由位置基本信息 + 位置附加信息项中的里程和发动机转速组成
     */
    var mGNSSData: GNSSDataMsg = GNSSDataMsg()

    init {
        hoursNum = Publish.getInstance().mTerminalNo + SystemHelper.YYMMDD +
                MessageHelper.supplementStr(4,
                        Publish.getInstance().mHoursSerialNum.toString(), 0)
        Publish.getInstance().mHoursSerialNum++
        val additionalInfo = ArrayList<GNSSDataMsg.AdditionalInfo>()
        additionalInfo.add(GNSSDataMsg.AdditionalInfo(0x01))
        additionalInfo.add(GNSSDataMsg.AdditionalInfo(0x05))
        mGNSSData.additionalInfo = additionalInfo
    }

    override fun getDataByteArray(): ByteArray {
        return ConvertHelper.string2ByteArray(toString())
    }

    override fun toString(): String {
        return ConvertHelper.byteArray2HexString(hoursNum.toByteArray()) +
                MessageHelper.supplementStr(2, Integer.toHexString(reportType), 0) +
                ConvertHelper.byteArray2HexString(stuNum.toByteArray()) +
                ConvertHelper.byteArray2HexString(coachNum.toByteArray()) +
                MessageHelper.supplementStr(8, Integer.toHexString(classId), 0) + reportTime + curCourse +
                MessageHelper.supplementStr(2, Integer.toHexString(recodeState), 0) +
                MessageHelper.supplementStr(4, Integer.toHexString(maxSpeed), 0) +
                MessageHelper.supplementStr(4, Integer.toHexString(mileage), 0) +
                mGNSSData.toString()
    }

    override fun setDataByteArray(byteArray: ByteArray) {}

    /** 命令上报学时记录 */
    class CommandReportHoursData : PasBodyMsg.DataContent {
        /**
         * 查询方式 1 byte
         * 1：按时间上传
         * 2：按条数上传
         */
        var queryType: Int = 0
        /**
         * 查询起始时间 BCD 6 byte
         * YYMMDDhhmmss 格式
         */
        var queryStartTime: String = ""
        /**
         * 查询终止时间 BCD 6 byte
         * YYMMDDhhmmss 格式
         */
        var queryEndTime: String = ""
        /**
         * 查询条数 2 byte
         */
        var queryCount: Int = 0

        override fun getDataByteArray(): ByteArray = byteArrayOf()

        override fun setDataByteArray(byteArray: ByteArray) {
            val byteList = byteArray.asList()
            queryType = byteArray[0].toInt()
            queryStartTime = ConvertHelper.byteArray2HexString(byteList.subList(1, 7).toByteArray())
            queryEndTime = ConvertHelper.byteArray2HexString(byteList.subList(7, 13).toByteArray())
            queryCount = Integer.parseInt(ConvertHelper.byteArray2HexString(
                    byteList.subList(13, 15).toByteArray()), 16)
        }
    }

    /** 命令上报学时记录应答 */
    class CommandReportAnswerData : PasBodyMsg.DataContent {
        /**
         * 执行结果 1 byte
         * 1：查询的记录正在上传
         * 2：SD 卡没有找到
         * 3：执行成功，但无指定记录
         * 4：执行成功，稍后上报查询结果
         * 9：其他错误
         */
        private var resultCode: Int = 0

        override fun getDataByteArray(): ByteArray = byteArrayOf()

        override fun setDataByteArray(byteArray: ByteArray) {
            resultCode = byteArray[0].toInt()
        }
    }
}
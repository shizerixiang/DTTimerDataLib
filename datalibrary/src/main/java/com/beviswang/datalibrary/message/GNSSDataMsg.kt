package com.beviswang.datalibrary.message

import com.beviswang.datalibrary.logE
import com.beviswang.datalibrary.module.GPSModule
import com.beviswang.datalibrary.util.ConvertHelper
import com.beviswang.datalibrary.util.MessageHelper

/**
 * 基本 GNSS 数据包 28 byte
 * Created by shize on 2018/1/30.
 */
class GNSSDataMsg() : PackageMsg.MsgBody {
    /** 本地数据库编号 */
    var mId:Int = 0

    /** 报警标识 4 byte */
    var alarmSign: AlarmSignMsg = AlarmSignMsg()
    /** 状态位 4 byte */
    var stateBit: StateBitMsg = StateBitMsg()
    /** 纬度 4 byte */
    var latitude: String
    /** 经度 4 byte */
    var longitude: String
    /** 行驶记录速度 1/10 km/h 2 byte */
    var driveSpeed: Int
    /** 卫星定位速度 1/10 km/h 2 byte */
    var satelliteSpeed: Int
    /** 方向 2 byte */
    var direction: Int = 0
    /** 时间 6 byte */
    var time: String
    /** 位置附加信息项列表 */
    var additionalInfo: ArrayList<AdditionalInfo>? = null
    // 缓存 byte 字符串
    var msgByteString: String? = null

    constructor(hexString: String) : this() {
        setBodyBytes(ConvertHelper.string2ByteArray(hexString))
    }

    init {
        // 先定位
        GPSModule.getInstance().getLocationInfo(null)
        val lat = GPSModule.GPS_LATITUDE
        val long = GPSModule.GPS_LONGITUDE
        if (lat < 0) stateBit.mLatitudeDir = "1"
        if (long < 0) stateBit.mLongitudeDir = "1"
        latitude = MessageHelper.supplementStr(8, Math.abs(lat).toString(16), 0)
        longitude = MessageHelper.supplementStr(8, Math.abs(long).toString(16), 0)
        satelliteSpeed = GPSModule.GPS_SPEED
        driveSpeed = satelliteSpeed
        direction = GPSModule.GPS_DIR
        time = GPSModule.GPS_TIME
    }

    override fun setBodyBytes(byteArray: ByteArray) {
        val byteList = byteArray.asList()
        alarmSign.setHexString(ConvertHelper.byteArray2HexString(byteList.subList(0, 4).toByteArray()))
        stateBit.setHexString(ConvertHelper.byteArray2HexString(byteList.subList(4, 8).toByteArray()))
        latitude = ConvertHelper.byteArray2HexString(byteList.subList(8, 12).toByteArray())
        longitude = ConvertHelper.byteArray2HexString(byteList.subList(12, 16).toByteArray())
        driveSpeed = Integer.parseInt(ConvertHelper.byteArray2HexString(byteList.subList(16, 18).toByteArray()), 16)
        satelliteSpeed = Integer.parseInt(ConvertHelper.byteArray2HexString(byteList.subList(18, 20).toByteArray()), 16)
        direction = Integer.parseInt(ConvertHelper.byteArray2HexString(byteList.subList(20, 22).toByteArray()), 16)
        time = ConvertHelper.byteArray2HexString(byteList.subList(22, 28).toByteArray())
        if (byteArray.size == 28) return
        // 添加附加消息
        additionalInfo = ArrayList()
        var lastIndex = 28
        while (lastIndex < byteArray.size) {
            val len = byteArray[lastIndex + 1].toInt()
            val toIndex = lastIndex + len + 2
            additionalInfo!!.add(AdditionalInfo(byteList.subList(lastIndex, toIndex).toByteArray()))
            lastIndex = toIndex
        }
    }

    override fun getBodyArray(): ByteArray {
        return ConvertHelper.string2ByteArray(toString())
    }

    override fun toString(): String {
        if (msgByteString != null) return msgByteString!!
        val strDriveSpeed = MessageHelper.supplementStr(4, Integer.toHexString(driveSpeed), 0)
        val strSatelliteSpeed = MessageHelper.supplementStr(4, Integer.toHexString(satelliteSpeed), 0)
        var strAdditional = "" // 位置附加信息项
        if (additionalInfo != null) additionalInfo!!.forEach { strAdditional += it.toString() }
        msgByteString = alarmSign.getHexString() + stateBit.getHexString() + latitude + longitude +
                strDriveSpeed + strSatelliteSpeed + MessageHelper.supplementStr(
                4, Integer.toHexString(direction), 0) + time + strAdditional
        return msgByteString!!
    }

    /**
     * 设置为补发位置消息
     *
     * @param isReissueMsg 是否为补发位置消息
     */
    fun setIsReissueMsg(isReissueMsg: Boolean) {
        stateBit.mLocationType = if (isReissueMsg) "1" else "0"
    }

    /**
     * 报警标识位定义
     * 0 位：1 紧急报警触动报警开关后触发 -> 收到应答后清零
     * 1 位：1 超速报警 -> 标识维持至报警条件解除
     * 2 位：1 疲劳驾驶 -> 标识维持至报警条件解除
     * 3 位：1 预警 -> 收到应答后清零
     * 4 位：1 GNSS 模块发生故障 -> 标识维持至报警条件解除
     * 5 位：1 GNSS 天线未接或被剪断 -> 标识维持至报警条件解除
     * 6 位：1 GNSS 天线短路 -> 标识维持至报警条件解除
     * 7 位：1 终端主电源欠压 -> 标识维持至报警条件解除
     * 8 位：1 终端主电源掉电 -> 标识维持至报警条件解除
     * 9 位：1 终端 LCD 或显示器故障 -> 标识维持至报警条件解除
     * 10 位：1 TTS 模块故障 -> 标识维持至报警条件解除
     * 11 位：1 摄像头故障 -> 标识维持至报警条件解除
     * 12-17 位：保留
     * 18 位：1 当天累计驾驶超时 -> 标识维持至报警条件解除
     * 19 位：1 超时停车 -> 标识维持至报警条件解除
     * 20 位：1 进出区域 -> 收到应答后清零
     * 21 位：1 进出路线 -> 收到应答后清零
     * 22 位：1 路段行驶时间不足/过长 -> 收到应答后清零
     * 23 位：1 路线偏离报警 -> 标识维持至报警条件解除
     * 24 位：1 车辆 VSS 故障 -> 标识维持至报警条件解除
     * 25 位：1 车辆油量异常 -> 标识维持至报警条件解除
     * 26 位：1 车辆被盗（通过车辆防盗器） -> 标识维持至报警条件解除
     * 27 位：1 车辆非法点火 -> 收到应答后清零
     * 28 位：1 车辆非法位移 -> 收到应答后清零
     * 29-31 位：保留
     */
    class AlarmSignMsg {
        // 警报标识位数组
        var alarmArray: Array<Int> = Array(32, init = { 0 })

        /** @param hexString 16 进制数据 */
        fun setHexString(hexString: String) {
            val str = ConvertHelper.hexString2binaryString(hexString)!!
            alarmArray.mapIndexed { index, _ ->
                if (str[index] != '0') alarmArray[index] = 1
            }
        }

        /** @return 获取十六进制 */
        fun getHexString(): String {
            return ConvertHelper.binaryString2hexString(toString()) ?: throw  Exception("警报标识位转换出错！")
        }

        override fun toString(): String {
            var strAlarm = ""
            alarmArray.forEach {
                strAlarm += it.toString()
            }
            return strAlarm
        }

        /**
         * 设置报警
         *
         * @param bitIndex bit 位位置
         * @param state 报警开关
         */
        fun setAlarm(bitIndex: Int, state: Int) {
            alarmArray[31 - bitIndex] = state
        }
    }

    /**
     * 状态位定义
     * 0 位：0：ACC 关；1：ACC 开
     * 1 位：0：未定位；1：定位
     * 2 位：0：北纬；1：南纬
     * 3 位：0：东经；1：西经
     * 4 位：0：运营状态；1：停运状态
     * 5 位：0：经纬度未经保密插件加密；1：经纬度已经保密插件加密
     * 6 位：0：正常位置汇报；1：补传位置汇报
     * 7-9 位：保留
     * 10 位：0：车辆油路正常；1：车辆油路断开
     * 11 位：0：车辆电路正常；1：车辆电路断开
     * 12 位：0：车门解锁；1：车门加锁
     * 13-31 位：保留
     */
    class StateBitMsg {
        // 0：ACC 关；1：ACC 开
        var mACCSwitch: String = "0"
        // 0：未定位；1：定位
        var mLocationSwitch: String = "1"
        // 0：北纬；1：南纬    北纬为正数，南纬为负数
        var mLatitudeDir: String = "0"
        // 0：东经；1：西经    东经正数，西经为负数
        var mLongitudeDir: String = "0"
        // 0：运营状态；1：停运状态
        var mOperateState: String = "0"
        // 0：经纬度未经保密插件加密；1：经纬度已经保密插件加密
        var mIsEncryption: String = "0"
        // 0：正常位置汇报；1：补传位置汇报
        var mLocationType: String = "0"
        // 保留
        private var mRetain1: String = "000"
        // 0：车辆油路正常；1：车辆油路断开
        var mOilSwitch: String = "0"
        // 0：车辆电路正常；1：车辆电路断开
        var mCircuitSwitch: String = "0"
        // 0：车门解锁；1：车门加锁
        var mDoorSwitch: String = "0"
        // 保留
        private var mRetain2: String = "0000000000000000000"

        /** @param hexString 16 进制数据 */
        fun setHexString(hexString: String) {
            val binaryString = ConvertHelper.hexString2binaryString(hexString) ?: throw Exception("状态位错误！")
            logE("排错：$binaryString")
            val lastIndex = binaryString.lastIndex
            mACCSwitch = binaryString[lastIndex].toString()
            mLocationSwitch = binaryString[lastIndex - 1].toString()
            mLatitudeDir = binaryString[lastIndex - 2].toString()
            mLongitudeDir = binaryString[lastIndex - 3].toString()
            mOperateState = binaryString[lastIndex - 4].toString()
            mIsEncryption = binaryString[lastIndex - 5].toString()
            mLocationType = binaryString[lastIndex - 6].toString()
            mOilSwitch = binaryString[lastIndex - 10].toString()
            mCircuitSwitch = binaryString[lastIndex - 11].toString()
            mDoorSwitch = binaryString[lastIndex - 12].toString()
        }

        /** @return 将二进制状态位转换为十六进制 */
        fun getHexString(): String {
            return ConvertHelper.binaryString2hexString(toString()) ?: throw Exception("状态位转换失败！")
        }

        override fun toString(): String {
            return mRetain2 + mDoorSwitch + mCircuitSwitch + mOilSwitch + mRetain1 + mLocationType +
                    mIsEncryption + mOperateState + mLongitudeDir + mLatitudeDir + mLocationSwitch +
                    mACCSwitch
        }
    }

    /** 位置附加信息项 */
    class AdditionalInfo() : PasBodyMsg.DataContent {
        /**
         * 附加信息 ID 1 byte
         * 1-255
         */
        var additionalId: Int = 1
        /** 附加信息长度 1 byte */
        var additionalLen: Int = 0
        /**
         * 附加信息 无限制 byte
         * 定义如下：
         * | 附加消息ID | 附加消息长度 |                      描述及要求                     |
         * | 0x01       | 4            | 里程，4 byte，1/10km，对应车上里程表读数            |
         * | 0x02       | 2            | 油量，2 byte，1/10L，对应车上油量表读数             |
         * | 0x03       | 2            | 海拔高度，2 byte，单位为 m                          |
         * | 0x05       | 2            | 发动机转速，2 byte                                  |
         */
        var additionalContent: String = ""

        constructor(id: Int) : this() {
            additionalId = id
        }

        constructor(byteArray: ByteArray) : this() {
            setDataByteArray(byteArray)
        }

        constructor(hexString: String) : this() {
            setDataByteArray(ConvertHelper.string2ByteArray(hexString))
        }

        init {
            createAdditionalInfo()
        }

        override fun toString(): String {
            return MessageHelper.supplementStr(2, Integer.toHexString(additionalId), 0) +
                    MessageHelper.supplementStr(2, Integer.toHexString(additionalLen), 0) +
                    additionalContent
        }

        override fun getDataByteArray(): ByteArray {
            return ConvertHelper.string2ByteArray(toString())
        }

        override fun setDataByteArray(byteArray: ByteArray) {
            additionalId = byteArray[0].toInt()
            additionalLen = byteArray[1].toInt()
            additionalContent = ConvertHelper.byteArray2HexString(byteArray.asList().
                    subList(2, byteArray.size).toByteArray())
        }

        private fun createAdditionalInfo() {
            // TODO 给位置附加信息赋值
            when (additionalId) {
                0x01 -> {
                    // 里程
                    additionalLen = 4
                    additionalContent = "00000000"
                }
                0x02 -> {
                    // 油量
                    additionalLen = 2
                    additionalContent = "0000"
                }
                0x03 -> {
                    // 海拔高度
                    additionalLen = 2
                    additionalContent = "0000"
                }
                0x05 -> {
                    // 发动机转速
                    additionalLen = 2
                    additionalContent = "0000"
                }
            }
        }
    }
}
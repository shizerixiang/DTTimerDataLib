package com.beviswang.datalibrary

import android.app.Activity
import android.content.Context
import android.os.Environment
import com.beviswang.datalibrary.message.RegisterResponseMsg
import com.beviswang.datalibrary.model.CoachModel
import com.beviswang.datalibrary.model.StudentModel
import com.beviswang.datalibrary.util.MessageHelper
import com.beviswang.datalibrary.util.TerminalHelper
import java.io.File
import java.util.*
import java.util.concurrent.Executors

/**
 * 软件系统通知者
 * Created by shize on 2017/12/20.
 */
class Publish : Observable() {
    /** 线程池 */
    val exServer = Executors.newSingleThreadExecutor()
    // ************************************** 内存中的参数 **************************************
    /** 是否启动完毕 */
    var mIsStartUp: Boolean = false

    /** 是否正在拍照 */
    var mIsTakePhoto: Boolean = false

    /** 上报学时用序列号，从 0001 开始 4 位数字 */
    var mHoursSerialNum: Int = 1

    /** 终端是否通过鉴权 */
    var mIsCarOnLine: Boolean = false

    /** 是否有信号 */
    var mIsSignalOn: Boolean = false

    /** 终端是否打开 GPS 定位 */
    var mIsOpenGPS: Boolean = false

    /**  终端是否连接上计时平台 */
    var mIsConnection: Boolean = false

    /** 终端是否通电 */
    var mIsBatteryCharging: Boolean = false

    /** 时间信息 */
    var mTimeText: String = "00:00"

    /** 日期信息 */
    var mDataText: String = "1970-1-1"

    /** 教练信息 */
    var mCoachInfo: CoachModel? = null

    /**
     * 当前培训课程，具体见课程编码 BCD 码 5 byte
     * 系统中的课程编码应采用10位数字码，由“1位课程方式码+2位培训车型码+1位培训部分码+2位培训项目码+4位数字码”组成，并符合以下规则：
     * a) 课程方式码定义：1-实操，2-课堂教学，3-模拟器教学，4-远程教学；
     * b) 培训车型码定义：00-无，01- A1，02-A2，03-A3，11-B1，12-B2，21-C1，22-C2，23-C3，24-C4，
     *    25-C5，31-D，32-E，33-F，41-M，42-N，43-P；
     * c) 培训部分码定义：1-第一部分，2-第二部分，3-第三部分，4-第四部分；
     * d) 培训项目码定义：01-法律、法规及道路交通信号，02-机动车基本知识，03-第一部分综合复习及考核，
     *    11-基础驾驶，12-场地驾驶，13-第二部分综合驾驶及考核，21-跟车行驶，22-变更车道，23-靠边停车，
     *    24-掉头，25-通过路口，26-通过人行横道，27-通过学校区域，28-通过公共汽车站，29-会车，30-超车，
     *    31-夜间驾驶，32-恶劣条件下的驾驶，33-山区道路驾驶，34-高速公路驾驶，35-行驶路线选择，
     *    36-第三部分综合驾驶及考核，41-安全、文明驾驶知识，42-危险源辨识知识，
     *    43-夜间和高速公路安全驾驶知识，44-恶劣气象和复杂道路条件下的安全驾驶知识，
     *    45-紧急情况应急处置知识，46-危险化学品知识，47-典型事故案例分析，48-第四部分综合复习及考核；
     * e) 4位数字码预留，不使用时置“0”。
     */
    var mCurCourse: String = "1212120000"

    /** 学生信息 */
    var mStudentInfo: StudentModel? = null

    /** 证书路径 */
    var mCertificatePath: String = ""
        get() {
            return RegisterResponseMsg.filePath + mTerminalNo + ".pfx"
        }

    // ************************************** 观察者的参数 **************************************

    private val obs: Vector<Observer> = Vector() // observers

    /** 通知数据更新 */
    fun notifyDataChanged() {
        setChanged()
        notifyObservers()
        clearChanged()
    }

    override fun addObserver(o: Observer?) {
        super.addObserver(o)
        obs.addElement(o)
    }

    override fun deleteObserver(o: Observer?) {
        super.deleteObserver(o)
        obs.removeElement(o)
    }

    override fun deleteObservers() {
        super.deleteObservers()
        obs.forEach {
            (it as Activity).finish()
        }
        obs.removeAllElements()
        System.exit(0)
    }

    // ************************************* 需要保存的参数 *************************************
    /** 终端手机号 */
    var mPhoneNo: String = "13515666505"
        get() {
            return TerminalHelper.readTerminalInfo(obs[0] as Context, TerminalHelper.TERMINAL_PHONE_NUMBER, field)
        }
        set(value) {
            TerminalHelper.saveTerminalInfo(obs[0] as Context, mapOf(Pair(TerminalHelper.TERMINAL_PHONE_NUMBER, value)))
        }

    /**  终端车号 */
    var mCarNo: String = "XXXXXXX"
        get() {
            return TerminalHelper.readTerminalInfo(obs[0] as Context, TerminalHelper.TERMINAL_CAR_NUMBER, field)
        }
        set(value) {
            TerminalHelper.saveTerminalInfo(obs[0] as Context, mapOf(Pair(TerminalHelper.TERMINAL_CAR_NUMBER, value)))
        }

    /**  终端出厂序列号 */
    var mSerialNo: String = "0000000"
        get() {
            return TerminalHelper.readTerminalInfo(obs[0] as Context, TerminalHelper.TERMINAL_SERIAL_NUMBER, field)
        }
        set(value) {
            TerminalHelper.saveTerminalInfo(obs[0] as Context, mapOf(Pair(TerminalHelper.TERMINAL_SERIAL_NUMBER, value)))
        }

    /** 终端编号 */
    var mTerminalNo: String = "5243451245520154"
        get() {
            return TerminalHelper.readTerminalInfo(obs[0] as Context, TerminalHelper.TERMINAL_NUMBER, field)
        }
        set(value) {
            TerminalHelper.saveTerminalInfo(obs[0] as Context, mapOf(Pair(TerminalHelper.TERMINAL_NUMBER, value)))
        }

    /** 平台编号 */
    var mPlatformNo: String = "44010"
        get() {
            return TerminalHelper.readTerminalInfo(obs[0] as Context, TerminalHelper.PLATFORM_NUMBER, field)
        }
        set(value) {
            TerminalHelper.saveTerminalInfo(obs[0] as Context, mapOf(Pair(TerminalHelper.PLATFORM_NUMBER, value)))
        }

    /** 计时平台 IP */
    var mPlatformIP: String = "114.215.173.239"
        get() {
            return TerminalHelper.readTerminalInfo(obs[0] as Context, TerminalHelper.PLATFORM_IP, field)
        }
        set(value) {
            TerminalHelper.saveTerminalInfo(obs[0] as Context, mapOf(Pair(TerminalHelper.PLATFORM_IP, value)))
        }

    /** 计时平台端口号 */
    var mPlatformPort: Int = 9001
        get() {
            return TerminalHelper.readTerminalInfo(obs[0] as Context, TerminalHelper.PLATFORM_PORT, field.toString()).toInt()
        }
        set(value) {
            TerminalHelper.saveTerminalInfo(obs[0] as Context, mapOf(Pair(TerminalHelper.PLATFORM_PORT, value.toString())))
        }

    /** 培训机构名称 */
    var mMechanismName: String = "天盛驾校"
        get() {
            return TerminalHelper.readTerminalInfo(obs[0] as Context, TerminalHelper.MECHANISM_NAME, field)
        }
        set(value) {
            TerminalHelper.saveTerminalInfo(obs[0] as Context, mapOf(Pair(TerminalHelper.MECHANISM_NAME, value)))
        }

    /** 培训机构编号 */
    var mMechanismNo: String = "6218629213252614"
        get() {
            return TerminalHelper.readTerminalInfo(obs[0] as Context, TerminalHelper.MECHANISM_NUMBER, field)
        }
        set(value) {
            TerminalHelper.saveTerminalInfo(obs[0] as Context, mapOf(Pair(TerminalHelper.MECHANISM_NUMBER, value)))
        }

    /** 证书口令 */
    var mCertificateKey: String = "GgSiLEtTNDS2"
        get() {
            return TerminalHelper.readTerminalInfo(obs[0] as Context, TerminalHelper.CERTIFICATE_KEY, field)
        }
        set(value) {
            TerminalHelper.saveTerminalInfo(obs[0] as Context, mapOf(Pair(TerminalHelper.CERTIFICATE_KEY, value)))
        }

    /** 软件系统版本 */
    var mSoftVersion: String = "v1.0.0.0"
        get() {
            return TerminalHelper.readTerminalInfo(obs[0] as Context, TerminalHelper.SOFTWARE_VERSION, field)
        }
        set(value) {
            TerminalHelper.saveTerminalInfo(obs[0] as Context, mapOf(Pair(TerminalHelper.SOFTWARE_VERSION, value)))
        }

    /** 消息流水号 */
    var mFlowNum: Int = 0
        get() {
            return MessageHelper.readFlowNum(obs[0] as Activity)
        }
        set(value) {
            MessageHelper.saveFlowNum(obs[0] as Activity, value)
            field = value
        }

    /** 驾培包序号 */
    var mProtocolSerial: Int = 1
        get() {
            return MessageHelper.readProtocolSerial(obs[0] as Activity)
        }
        set(value) {
            MessageHelper.saveProtocolSerial(obs[0] as Activity, value)
            field = value
        }

    /** 照片编号 */
    var mPictureNumber: Long = 1
        get() {
            return MessageHelper.readPicNumber(obs[0] as Activity)
        }
        set(value) {
            MessageHelper.savePicNumber(obs[0] as Activity, value)
            field = value
        }

    /** 当前课程 ID */
    var mClassId: Int = 1
        get() {
            return MessageHelper.readClassId(obs[0] as Activity)
        }
        set(value) {
            MessageHelper.saveClassId(obs[0] as Activity, value)
            field = value
        }

    /**
     * 禁训状态
     * 1：可用，默认值
     * 2：禁用
     */
    var mBanState: Int = 1
        get() {
            return MessageHelper.readBanState(obs[0] as Activity)
        }
        set(value) {
            MessageHelper.saveBanState(obs[0] as Activity, value)
            field = value
        }

    companion object {
        val DIR_FILE_PATH = Environment.getExternalStorageDirectory().absolutePath + File.separator +
                "DTTimer" + File.separator

        private var INSTANCE: Publish? = null

        /** 获取实例 */
        fun getInstance(): Publish {
            if (INSTANCE == null) INSTANCE = Publish()
            return INSTANCE!!
        }

        /** 销毁实例 */
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
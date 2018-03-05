package com.beviswang.datalibrary.operator

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Message
import com.beviswang.datalibrary.Publish
import com.beviswang.datalibrary.doSend
import com.beviswang.datalibrary.message.GNSSDataMsg
import com.beviswang.datalibrary.message.PackageMsg
import com.beviswang.datalibrary.message.PasBodyMsg
import com.beviswang.datalibrary.message.passthrough.HoursReportData
import com.beviswang.datalibrary.service.DTTimerSystemService
import com.beviswang.datalibrary.source.db.DBOperator
import com.beviswang.datalibrary.source.db.IMsgCacheOperator
import com.beviswang.datalibrary.util.*
import java.lang.ref.WeakReference

/**
 * 封装了心跳包的操作类
 * Created by shize on 2018/1/23.
 */
class HeartbeatOperator(context: Context) : IMsgOperator {
    val mWeakContext = WeakReference<Context?>(context.applicationContext)  // 上下文弱引用
    // 标志是否有业务，只有在没有业务的情况下才开始发送定时心跳包
    private var hasBusiness: Boolean = false
    // 是否开启心跳
    private var isStartHeartbeat: Boolean = false
    var mOperator: IMsgOperator = MsgOperator.newInstance(context) // 连接操作类
    private val mDurationHandler = DurationHandler(this) // 心跳定时器
    val mDBOperator: IMsgCacheOperator = DBOperator(context, "localData.db", 1) // 本地数据库
    /**
     *  临时位置跟踪
     *  单位为 s,0 则停止跟踪.停止跟踪无需带后继字段
     */
    var mTrackLocationDuration: Long = 0
    /**
     * 临时位置跟踪有效期
     * 单位为 s,终端在接收到位置跟踪控制消息后,在有效期截止时间之前,依据消息中的时间间隔发送位置汇报
     */
    var mTrackSurvivalTime: Long = 0

    override fun connect(): Boolean {
        hasBusiness = true
        startHeartbeat()
        startPositionReport()
        startTakePhoto()
        startReportHours()
        return mOperator.connect()
    }

    override fun sendMsg(msgPackage: PackageMsg): Boolean {
        if (!isStartHeartbeat) connect()
        hasBusiness = true
        return mOperator.sendMsg(msgPackage)
    }

    override fun receiveMsg(): PackageMsg? {
        hasBusiness = true
        return mOperator.receiveMsg()
    }

    override fun sendOver() {
        hasBusiness = false
        mOperator.sendOver()
    }

    override fun close() {
        mOperator.close()
        sendOver()
    }

    override fun sendResponseMsg(rePMsg: PackageMsg, result: Int) {
        mOperator.sendResponseMsg(rePMsg, 0)
    }

    override fun sendUpLinkResponseMsg(rePMsg: PasBodyMsg) {
        mOperator.sendUpLinkResponseMsg(rePMsg)
    }

    /**
     * 开始发送心跳包
     */
    private fun startHeartbeat() {
        isStartHeartbeat = true
        val context = mWeakContext.get() ?:
                throw Exception("心跳操作上下文被销毁，无法继续发送心跳包！")
        mDurationHandler.sendEmptyMessageDelayed(MSG_WHAT_HEARTBEAT,
                ParameterHelper.getHeartbeatDuration(context))
    }

    /**
     * 开始定时位置信息汇报
     */
    private fun startPositionReport() {
        val context = mWeakContext.get() ?:
                throw Exception("心跳操作上下文被销毁，无法继续汇报位置信息！")
        mDurationHandler.sendEmptyMessageDelayed(MSG_WHAT_POSITION_REPORT,
                ParameterHelper.getLocationDuration(context))
    }

    /**
     * 开始定时拍照
     */
    private fun startTakePhoto() {
        val context = mWeakContext.get() ?:
                throw Exception("心跳操作上下文被销毁，无法继续定时拍照！")
        mDurationHandler.sendEmptyMessageDelayed(MSG_WHAT_TAKE_PHOTO,
                ParameterHelper.getTakePhotoDuration(context) * 60L * 1000L)
    }

    /**
     * 开始定时汇报学时记录
     */
    private fun startReportHours() {
        mDurationHandler.sendEmptyMessageDelayed(MSG_WHAT_REPORT_HOURS, 60 * 1000L)
    }

    /**
     * 开始临时位置跟踪控制
     */
    fun startTrackPositionReport() {
        if (mTrackLocationDuration < 1) return
        mDurationHandler.sendEmptyMessageDelayed(MSG_WHAT_POSITION_REPORT, mTrackLocationDuration)
    }

    /** 定时消息发送器 */
    class DurationHandler(heartbeatOperator: HeartbeatOperator) : Handler() {
        private val mWeak = WeakReference<HeartbeatOperator>(heartbeatOperator)

        override fun handleMessage(msg: Message?) {
            val heart = mWeak.get() ?: return
            when (msg!!.what) {
                MSG_WHAT_HEARTBEAT -> {
                    if (!heart.hasBusiness) doHeartbeat(heart)
                    heart.startHeartbeat()
                }
                MSG_WHAT_POSITION_REPORT -> {
                    doPositionReport(heart)
                    heart.startPositionReport()
                }
                MSG_WHAT_TAKE_PHOTO -> {
                    // 只有在学员登录后才开始定时拍照
                    if (Publish.getInstance().mStudentInfo != null) doTakePhoto(heart)
                    heart.startTakePhoto()
                }
                MSG_WHAT_REPORT_HOURS -> {
                    // 只有在学员登录后才开始定时汇报学时记录
                    if (Publish.getInstance().mStudentInfo != null) doReportHours(heart)
                    heart.startReportHours()
                }
                MSG_WHAT_TRACK_LOCATION -> {
                    // 临时位置跟踪控制
                    heart.mTrackSurvivalTime -= heart.mTrackLocationDuration
                    // 保证间隔为大于 0,同时位置跟踪有效期大于 0
                    if (heart.mTrackLocationDuration > 0 && heart.mTrackSurvivalTime > 0) doPositionReport(heart)
                    heart.startTrackPositionReport()
                }
            }
        }

        /**
         * 做定时汇报学时记录
         *
         * @param heart 心跳操作
         */
        private fun doReportHours(heart: HeartbeatOperator) {
            doSend {
                val hoursData = HoursReportData()
                // 保存学时数据到本地数据库
                val dbOperator = heart.mDBOperator
                dbOperator.cacheHoursMsg(hoursData)
                // 检测是否有网络
                if (!Publish.getInstance().mIsCarOnLine) return@doSend
                // 发送学时数据
                heart.sendMsg(MessageHelper.getUpstreamMsg(MessageID.ClientHoursRecodeID,
                        hoursData, true, true))
                OperationHelper.handleReceive(heart)
                heart.sendOver()
            }
        }

        /**
         * 做定时拍照
         *
         * @param heart 心跳操作
         */
        private fun doTakePhoto(heart: HeartbeatOperator) {
            val context = heart.mWeakContext.get() ?:
                    throw  Exception("心跳操作上下文被销毁，无法继续定时拍照！")
            context.sendBroadcast(Intent(DTTimerSystemService.ACTION_TAKE_PIC).
                    putExtra(DTTimerSystemService.EXTRA_CAMERA_CONTROL, DTTimerSystemService.CAMERA_TAKE).
                    putExtra(DTTimerSystemService.EXTRA_CAMERA_EVENT_TYPE, 5))
        }

        /**
         * 做定时位置信息汇报
         *
         * @param heart 心跳操作类
         */
        private fun doPositionReport(heart: HeartbeatOperator) {
            doSend {
                val mGNSSData = MessageHelper.getGNSSMsg(MessageID.ClientLocationID)
                // 保存位置信息到数据库
                val dbOperator = heart.mDBOperator
                dbOperator.cacheGNSSMsg(mGNSSData.getMsgBody()!! as GNSSDataMsg)
                // 检测是否有网络
                if (!Publish.getInstance().mIsCarOnLine) return@doSend
                // 汇报位置信息
                heart.sendMsg(mGNSSData)
                OperationHelper.handleReceive(heart)
                heart.sendOver()
            }
        }

        /**
         * 做心跳消息
         */
        private fun doHeartbeat(heartbeatOperator: HeartbeatOperator) {
            doSend {
                // 检测是否有网络
                if (!Publish.getInstance().mIsCarOnLine) return@doSend
                val operator = heartbeatOperator.mOperator
                operator.sendMsg(getHeartbeatMsg())
                OperationHelper.handleReceive(heartbeatOperator)
            }
        }

        /**
         * @return 创建并返回心跳消息包
         */
        private fun getHeartbeatMsg(): PackageMsg {
            val pMsg = PackageMsg()
            val msgHeader = PackageMsg.MsgHeader()
            msgHeader.msgId = MessageID.ClientHeartbeatID
            pMsg.setMessage(msgHeader, null)
            return pMsg
        }
    }

    companion object {
        /** Handler 消息种类 */
        val MSG_WHAT_HEARTBEAT = 0x01   // 心跳包
        val MSG_WHAT_POSITION_REPORT = 0x02 // 定时位置汇报
        val MSG_WHAT_TAKE_PHOTO = 0x03 // 定时拍照
        val MSG_WHAT_REPORT_HOURS = 0x04 // 定时汇报学时记录
        val MSG_WHAT_TRACK_LOCATION = 0x05 // 临时位置跟踪控制
        private var INSTANCE: HeartbeatOperator? = null

        fun getInstance(context: Context): HeartbeatOperator {
            if (INSTANCE == null) INSTANCE = HeartbeatOperator(context)
            return INSTANCE!!
        }

        fun getInstance(): HeartbeatOperator {
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
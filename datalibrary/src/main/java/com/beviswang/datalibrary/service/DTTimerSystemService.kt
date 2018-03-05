package com.beviswang.datalibrary.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.*
import android.view.SurfaceHolder
import android.widget.Toast
import com.beviswang.datalibrary.*
import com.beviswang.datalibrary.model.SplashModel
import com.beviswang.datalibrary.operator.MsgOperator.Companion.ACTION_PLATFORM_STATE
import com.beviswang.datalibrary.operator.MsgOperator.Companion.EXTRA_CONNECTION
import com.beviswang.datalibrary.module.CameraModule
import com.beviswang.datalibrary.operator.PictureUploadOperator
import com.beviswang.datalibrary.module.UpgradeModule
import com.beviswang.datalibrary.operator.OffLineOperator
import com.beviswang.datalibrary.source.repository.SplashRepository
import com.beviswang.datalibrary.util.SystemHelper
import com.beviswang.datalibrary.window.CameraWindow
import org.jetbrains.anko.doAsync
import java.lang.ref.WeakReference

/**
 * 驾培宝系统后台服务
 * Created by shize on 2017/12/20.
 */
class DTTimerSystemService : Service() {
    private var cameraModule: CameraModule? = null
    // 计时器
    private var timerHandler: TimerHandler = TimerHandler(this)
    // 计时器开启状态
    private var isCoachSign = false
    private var isCoachTrain = false
    // 是否与平台连接
    private var isConnect = false

    override fun onCreate() {
        super.onCreate()
        registerStatusReceiver()
        initSoftStatus()
    }

    /**
     * 注册状态广播接收器
     */
    private fun registerStatusReceiver() {
        val intentFilter = IntentFilter()
        // 日期时间
        intentFilter.addAction(Intent.ACTION_TIME_TICK)
        // GPS 位置信息
        intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        // 网络状态
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        // 电源拔插状态
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED)
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        // 平台连接状态
        intentFilter.addAction(ACTION_PLATFORM_STATE)
        // 应用无线升级
        intentFilter.addAction(ACTION_DOWNLOAD_APK)
        // 拍照
        intentFilter.addAction(ACTION_TAKE_PIC)
        // 计时器
        intentFilter.addAction(ACTION_TRAIN_TIMER)
        registerReceiver(mStatusReceiver, intentFilter)
    }

    override fun onBind(intent: Intent?): IBinder {
        return Binder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 启动平台连接服务
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * 初始化软件状态
     */
    private fun initSoftStatus() {
        doAsync {
            updateTime()
            updateGPS()
            networkStateChanged()
            initPowerInfo()
        }
        openCamera()
        logI("成功启动驾培宝系统服务！")
    }

    /**
     * 打开相机
     */
    private fun openCamera() {
        closeCamera()
        CameraWindow.getInstance().show(this)
        logD("打开相机")
        cameraModule = CameraModule.getInstance(this@DTTimerSystemService)
        val holder = CameraWindow.getInstance().getDummyCameraView()!!.holder
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                doAsync {
                    cameraModule?.startPreview(holder!!)
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {

            }

            override fun surfaceCreated(holder: SurfaceHolder?) {

            }
        })
    }

    /**
     * 更新 GPS 信息
     */
    private fun updateGPS() {
        Publish.getInstance().mIsOpenGPS = SystemHelper.getGPSState(this@DTTimerSystemService)
    }

    /**
     * 更新时间日期
     */
    private fun updateTime() {
        Publish.getInstance().mTimeText = SystemHelper.systemTime
        Publish.getInstance().mDataText = SystemHelper.systemDate
    }

    /**
     * 初始化电池充电信息
     */
    private fun initPowerInfo() {
        val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = registerReceiver(null, iFilter)
        //你可以读到充电状态,如果在充电，可以读到是usb还是交流电

        // 是否在充电
        val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
        Publish.getInstance().mIsBatteryCharging = isCharging
    }

    /**
     * 网络状态发生变化
     */
    @SuppressLint("MissingPermission")
    private fun networkStateChanged() {
        // 获得 ConnectivityManager 对象
        val connMgr = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // 获取所有网络连接的信息
        val networks = connMgr.allNetworks
        Publish.getInstance().mIsSignalOn = false
        // 用于存放网络连接信息
        var sb = "网络已断开！"
        // 通过循环将网络信息逐个取出来
        networks.forEach {
            Publish.getInstance().mIsSignalOn = true
            sb = "网络已连接！"
        }
        if (!Publish.getInstance().mIsSignalOn) updateConnectState(false)
        else if (!isConnect && Publish.getInstance().mIsStartUp) connect2Platform()
        logI(sb)
    }

    /**
     * 重新连接到计时平台
     */
    private fun connect2Platform() {
        doSend {
            val splash = SplashRepository()
            splash.refreshSocket()
            splash.requestAuthenticate(object : BaseDataSource.LoadSourceCallback<SplashModel> {
                override fun onDataLoaded(dataModel: SplashModel) {
                    uiThread {
                        Toast.makeText(this@DTTimerSystemService, "鉴权成功",
                                Toast.LENGTH_SHORT).show()
                        updateConnectState(true)
                        OffLineOperator(this@DTTimerSystemService).reissueMsg()
                    }
                }

                override fun onDataLoadFailed(msg: String) {
                    uiThread {
                        Toast.makeText(this@DTTimerSystemService, "鉴权失败",
                                Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mStatusReceiver)
        CameraModule.destroyInstance()
        CameraWindow.destroyInstance()
    }

    /** 关闭相机 */
    private fun closeCamera() {
        logD("关闭相机")
        cameraModule?.stopPreview()
        CameraWindow.getInstance().dismiss()
    }

    /**
     * 是否连接电源
     *
     * @param isCharging 是否充电
     */
    private fun updatePowerInfo(isCharging: Boolean) {
        Publish.getInstance().mIsBatteryCharging = isCharging
        Publish.getInstance().notifyDataChanged()
    }

    /**
     * 更新连接状态
     *
     * @param isConnect 是否连接平台
     */
    private fun updateConnectState(isConnect: Boolean) {
        this.isConnect = isConnect
        Publish.getInstance().mIsCarOnLine = isConnect
        Publish.getInstance().mIsConnection = isConnect
        Publish.getInstance().notifyDataChanged()
    }

    /**
     * 负责接收状态变化的广播
     */
    private val mStatusReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_TIME_TICK -> {
                    // 时间变化
                    updateTime()
                    Publish.getInstance().notifyDataChanged()
                    logI("时间发生变化，当前时间：${Publish.getInstance().mTimeText}\n" +
                            "当前日期：${Publish.getInstance().mDataText}")
                }
                LocationManager.PROVIDERS_CHANGED_ACTION -> {
                    // GPS 状态变化
                    updateGPS()
                    Publish.getInstance().notifyDataChanged()
                    logI("GPS 开关状态发生改变")
                }
                ConnectivityManager.CONNECTIVITY_ACTION -> {
                    // 网络状态变化
                    networkStateChanged()
                    Publish.getInstance().notifyDataChanged()
                    logI( "网络状态发生改变")
                }
                Intent.ACTION_POWER_CONNECTED -> {
                    // 连接电源
                    updatePowerInfo(true)
                    logI( "连接电源，开始充电")
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    // 未连接电源
                    updatePowerInfo(false)
                    logI( "断开电源，停止充电")
                }
                ACTION_PLATFORM_STATE -> {
                    // 计时平台状态发生改变
                    updateConnectState(intent.getBooleanExtra(EXTRA_CONNECTION, false))
                    logI( "平台连接状态发生改变")
                }
                ACTION_DOWNLOAD_APK -> {
                    // 下载升级安装包
                    logD("接收到升级消息，开始升级软件！")
                    val path = intent.getStringExtra(EXTRA_DOWNLOAD_PATH)
                    UpgradeModule(this@DTTimerSystemService).downloadFile(path)
                }
                ACTION_TAKE_PIC -> {
                    // 拍照
                    when (intent.getStringExtra(EXTRA_CAMERA_CONTROL)) {
                        CAMERA_OPEN -> openCamera()
                        CAMERA_TAKE -> takePhoto(intent.getIntExtra(EXTRA_CAMERA_EVENT_TYPE, 5),
                                intent.getBooleanExtra(EXTRA_CAMERA_IS_UPLOAD, true),
                                intent.getIntExtra(EXTRA_CAMERA_PIC_SIZE,0x06))
                        CAMERA_CLOSE -> closeCamera()
                    }
                }
                ACTION_TRAIN_TIMER -> {
                    // 计时器
                    when (intent.getStringExtra(EXTRA_TIMER_CONTROL)) {
                        TIMER_SIGN_START -> {
                            isCoachSign = true
                            timerHandler.sendEmptyMessageDelayed(MSG_WHAT_COACH_SIGN_TIMER, 60000)
                        }
                        TIMER_SIGN_STOP -> {
                            isCoachSign = false
                        }
                        TIMER_TRAIN_START -> {
                            isCoachTrain = true
                            timerHandler.sendEmptyMessageDelayed(MSG_WHAT_COACH_TRAIN_TIMER, 60000)
                        }
                        TIMER_TRAIN_STOP -> {
                            isCoachTrain = false
                        }
                    }
                }
            }
        }
    }

    /**
     * 拍照
     *
     * @param eventType 拍照事件类型
     * @param isUpload 是否上传照片
     * @param picSize 图片尺寸
     */
    private fun takePhoto(eventType: Int,isUpload:Boolean,picSize:Int) {
        logD("接收到拍照消息，开始拍摄照片！事件类型：$eventType")
        try {
            CameraModule.getInstance(this@DTTimerSystemService).takePhoto(object : CameraModule.OnTakeOver {
                override fun onSucceed(path: String) {
                    logD("拍照成功！路径：$path")
//                    sendBroadcast(Intent(ACTION_TAKEN_PIC).putExtra(EXTRA_TAKE_PATH, path))
                    sendBroadcast(Intent(DTTimerSystemService.ACTION_TAKE_PIC_STATE).
                            putExtra(DTTimerSystemService.EXTRA_TAKE_STATE, 1))
                    doSend {
                        val uploadModule = PictureUploadOperator(this@DTTimerSystemService)
                        // 是否需要上传图片，不上传则保存在本地
                        if (!isUpload) return@doSend
                        uploadModule.eventType = eventType
                        uploadModule.pictureSize = picSize
                        uploadModule.initUpload(path)
                        uploadModule.upload()
                    }
                }

                override fun onFailed() {
                    logE("拍照失败！")
                    sendBroadcast(Intent(DTTimerSystemService.ACTION_TAKE_PIC_STATE).
                            putExtra(DTTimerSystemService.EXTRA_TAKE_STATE, 2))
                }
            },picSize)
        } catch (e: Exception) {
            logE("拍照异常！")
            sendBroadcast(Intent(DTTimerSystemService.ACTION_TAKE_PIC_STATE).
                    putExtra(DTTimerSystemService.EXTRA_TAKE_STATE, 2))
            e.printStackTrace()
        }
    }

    /** 计时器 */
    class TimerHandler(service: DTTimerSystemService) : Handler() {
        private val mWeakService = WeakReference<DTTimerSystemService>(service)

        override fun handleMessage(msg: Message?) {
            val service = mWeakService.get() ?: return
            when (msg?.what) {
                MSG_WHAT_COACH_SIGN_TIMER -> {
                    val coachInfo = Publish.getInstance().mCoachInfo ?: return
                    coachInfo.mSignDuration += 1
                    if (service.isCoachSign)
                        sendEmptyMessageDelayed(MSG_WHAT_COACH_SIGN_TIMER, 60000)
                }
                MSG_WHAT_COACH_TRAIN_TIMER -> {
                    val coachInfo = Publish.getInstance().mCoachInfo ?: return
                    coachInfo.mTotalTrainTime += 1
                    if (service.isCoachTrain)
                        sendEmptyMessageDelayed(MSG_WHAT_COACH_TRAIN_TIMER, 60000)
                }
            }
        }
    }

    companion object {
        // 拍照状态 ACTION
        val ACTION_TAKE_PIC_STATE = "com.beviswang.datalibrary.service.DTTimerSystemService.take.pic.state"
        // 拍照状态
        val EXTRA_TAKE_STATE = "com.beviswang.datalibrary.service.DTTimerSystemService.take.state"

        // 下载安装包 ACTION
        val ACTION_DOWNLOAD_APK = "com.beviswang.datalibrary.service.DTTimerSystemService.download.apk"
        val EXTRA_DOWNLOAD_PATH = "com.beviswang.datalibrary.service.DTTimerSystemService.download.path."
        // 拍照 ACTION
        val ACTION_TAKE_PIC = "com.beviswang.datalibrary.service.DTTimerSystemService.camera.take"
        // 拍照完成发送 ACTION
        val ACTION_TAKEN_PIC = "com.beviswang.datalibrary.service.DTTimerSystemService.camera.taken"
        val EXTRA_TAKE_PATH = "com.beviswang.datalibrary.service.DTTimerSystemService.camera.path"
        // 相机控制
        val EXTRA_CAMERA_CONTROL = "com.beviswang.datalibrary.service.DTTimerSystemService.camera.extra.control"
        val CAMERA_TAKE = "take"
        val CAMERA_CLOSE = "close"
        val CAMERA_OPEN = "open"
        // 相机拍照类型
        val EXTRA_CAMERA_EVENT_TYPE = "com.beviswang.datalibrary.service.DTTimerSystemService.camera.extra.event.type"
        // 是否上传照片
        val EXTRA_CAMERA_IS_UPLOAD = "com.beviswang.datalibrary.service.DTTimerSystemService.camera.extra.isUpload"
        // 图片尺寸
        val EXTRA_CAMERA_PIC_SIZE = "com.beviswang.datalibrary.service.DTTimerSystemService.camera.extra.picSize"

        // 计时器控制
        val ACTION_TRAIN_TIMER = "com.beviswang.datalibrary.service.DTTimerSystemService.train.timer"
        val EXTRA_TIMER_CONTROL = "com.beviswang.datalibrary.service.DTTimerSystemService.timer.control"
        val TIMER_SIGN_START = "sign.start"
        val TIMER_SIGN_STOP = "sign.stop"
        val TIMER_TRAIN_START = "train.start"
        val TIMER_TRAIN_STOP = "train.stop"

        // 计时器
        val MSG_WHAT_COACH_SIGN_TIMER = 0x01                        // 教练签到时长
        val MSG_WHAT_COACH_TRAIN_TIMER = 0x02                       // 教练培训时长
    }
}
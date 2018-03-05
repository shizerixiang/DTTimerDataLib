package com.beviswang.datalibrary.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.kpocom.Gpioctljni
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.provider.Settings
import com.beviswang.datalibrary.module.GPSModule
import android.app.Activity
import android.hardware.Camera
import android.support.annotation.RequiresApi
import android.telephony.TelephonyManager
import com.beviswang.datalibrary.logI
import java.text.SimpleDateFormat
import java.util.*

/**
 * 系统数据处理工具类
 * Created by shize on 2017/12/27.
 */
object SystemHelper {
    private val ACTION_KPO_APK_INSTALL = "com.kpo.SILENCE_INSTALL" // 安装指令操作
    private val EXTRA_KPO_PATH_INSTALL = "appPath" // 安装路径 key
    private val EXTRA_KPO_AUTO_START_INSTALL = "autostart" // 静默安装 key

    private val ACTION_KPO_DEVACT = "com.kpo.devact" // 系统指令操作
    private val EXTRA_KPO_DEVACT_CMD = "devactcmd"
    private val EXTRA_KPO_REBOOT = "kporeboot" // 重启
    private val EXTRA_KPO_SHUTDOWN = "kposhutdown" // 关机
    private val EXTRA_KPO_SLEEP = "kposleep" // 休眠

    private val ACTION_KPO_WAKE_UP = "com.kpo.todowakeup" // 系统唤醒指令
    private val EXTRA_KPO_WAKE_UP_TYPE = "wakeUpType" // 唤醒模式 key
    private val EXTRA_KPO_WAKE_UP_TIME = "alarmintervalMillis" // 唤醒时间 key
    private val WAKE_UP_TYPE_ONCE = "oncewakeup" // 只唤醒一次

    private val SERVICE_NAME_ACC_IN = "kpocomapiservice" // KPOAPI 服务名，用于获取定制系统的值

    /**
     * 获取系统时间
     */
    val systemTime: String
        @RequiresApi(Build.VERSION_CODES.N)
        get() {
            val strTime: String
            strTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val c1 = Calendar.getInstance()
                getTimeString(c1.get(Calendar.HOUR_OF_DAY)) + ":" +
                        getTimeString(c1.get(Calendar.MINUTE)) + ":" +
                        getTimeString(c1.get(Calendar.SECOND))
            } else {
                val c2 = java.util.Calendar.getInstance()
                getTimeString(c2.get(java.util.Calendar.HOUR_OF_DAY)) + ":" +
                        getTimeString(c2.get(java.util.Calendar.MINUTE)) + ":" +
                        getTimeString(c2.get(java.util.Calendar.SECOND))
            }
            return strTime
        }

    /**
     * 获取系统时间戳
     * 单位为秒 s
     */
    val systemTimeStamp: Long
        get() {
            return System.currentTimeMillis() / 1000
        }

    /**
     * 获取系统日期
     */
    val systemDate: String
        @RequiresApi(Build.VERSION_CODES.N)
        get() {
            val strDate: String
            strDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val c1 = Calendar.getInstance()
                (c1.get(Calendar.YEAR)).toString() + "-" + (c1.get(Calendar.MONTH) + 1).toString() +
                        "-" + c1.get(Calendar.DAY_OF_MONTH)
            } else {
                val c2 = java.util.Calendar.getInstance()
                (c2.get(java.util.Calendar.YEAR)).toString() + "-" +
                        (c2.get(java.util.Calendar.MONTH) + 1).toString() + "-" +
                        c2.get(java.util.Calendar.DAY_OF_MONTH)
            }
            return strDate
        }

    /** @return 以 YYMMDDhhmmss 的格式取时间 */
    val yyMMddHHmmssData: String
        get() {
            return SimpleDateFormat("yyMMddHHmmss", Locale.CHINA).format(Date())
        }

    /**
     * 日期格式字符串转换成时间戳
     * @param date_str 字符串日期
     * @return s
     */
    fun yyMMddHHmmss2TimeStamp(date_str: String): Long {
        try {
            val sdf = SimpleDateFormat("yyMMddHHmmss", Locale.CHINA)
            return sdf.parse(date_str).time / 1000
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    val YYMMDD: String
        get() {
            return SimpleDateFormat("yyMMdd", Locale.CHINA).format(Date())
        }

    /** @return 以 YYMMDDhhmmss 的格式取时间 */
    val HHmmssData: String
        get() {
            return SimpleDateFormat("HHmmss", Locale.CHINA).format(Date())
        }

    /**
     * 将毫秒转换为显示时间字符串，从秒显示到小时
     *
     * @param timeValue   总时间 ms
     * @return 显示时间字符串
     */
    fun getConvertedTime(timeValue: Long): String {
        // 将毫秒转化为秒
        val durationS = (timeValue / 1000).toInt()
        return getTimeString(durationS / 3600) + ":" + getTimeString(durationS / 60 % 60) +
                ":" + getTimeString(durationS % 60)
    }

    /**
     * 将毫秒转换为显示时间字符串，从分钟显示到小时
     *
     * @param timeValue   总时间 min
     * @return 显示时间字符串
     */
    fun getConvertedTimeFromM(timeValue: Int): String {
        // 将毫秒转化为秒
        return getTimeString(timeValue / 60) + ":" + getTimeString(timeValue % 60)
    }

    /**
     * 将时间戳转换为时间 YYMMDDhhmmss 格式
     */
    fun stampToDate(s: Long): String {
        val res: String
        val simpleDateFormat = SimpleDateFormat("yyMMddHHmmss", Locale.CHINA)
        val date = Date(s)
        res = simpleDateFormat.format(date)
        return res
    }


    /**
     * 将时间转化为字符串

     * @param time 时间
     * *
     * @return String
     */
    private fun getTimeString(time: Int): String {
        return if (time < 10) "0" + time else time.toString()
    }

    /**
     * 获取 GPS 状态信息
     *
     * @param context 上下文
     * @return 是否开启
     */
    fun getGPSState(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /**
     * 检测是否有自带的 GPS 模块设备
     *
     * @param context 上下文
     * @return 是否含有 GPS　模块设备
     */
    fun isGPSCanUse(context: Context): Boolean {
        val mgr = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = mgr.allProviders ?: return false
        return providers.contains(LocationManager.GPS_PROVIDER) &&
                GPSModule.getInstance(context).getLocationInfo(null) != null
    }

    /**
     * @return 测试当前摄像头能否被使用
     */
    fun isCameraCanUse(): Boolean {
        var canUse = true
        var mCamera: Camera? = null
        try {
            mCamera = Camera.open()
        } catch (e: Exception) {
            canUse = false
        }
        if (canUse) {
            mCamera!!.release()
            mCamera = null
        }
        return canUse
    }

    /**
     * 改变 GPS 状态
     *
     * @param context 上下文
     */
    fun changeGPSState(context: Context) {
        val before = getGPSState(context)
        val resolver = context.contentResolver
        if (before) {
            Settings.Secure.putInt(resolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF)
        } else {
            Settings.Secure.putInt(resolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_HIGH_ACCURACY)
        }
    }

    /**
     * 检测是否有可用网络活动
     * @param context 上下文
     * @return 网络是否可用
     */
    @SuppressLint("MissingPermission")
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networks = connectivityManager.allNetworks
        networks.filter { connectivityManager.getNetworkInfo(it).state == NetworkInfo.State.CONNECTED }.forEach { return true }
        return false
    }

    /**
     * 关机指令，只适用于修改过的机器，即 M2 终端
     *
     * @param context 上下文
     */
    fun shutDownSystem(context: Context) {
        systemAction(context, EXTRA_KPO_SHUTDOWN)
    }

    /**
     * 重启指令
     * M2 不可用
     *
     * @param context 上下文
     */
    fun rebootSystem(context: Context) {
        systemAction(context, EXTRA_KPO_REBOOT)
    }

    /**
     * 休眠指令，只适用于 M2 终端
     *
     * @param context 上下文
     */
    fun sleepSystem(context: Context) {
        systemAction(context, EXTRA_KPO_SLEEP)
    }

    /**
     * 唤醒系统
     * M2 不可用
     *
     * @param context 上下文
     */
    fun wakeUpSystem(context: Context) {
        val intent = Intent(ACTION_KPO_WAKE_UP)
        intent.putExtra(EXTRA_KPO_WAKE_UP_TYPE, WAKE_UP_TYPE_ONCE)
        intent.putExtra(EXTRA_KPO_WAKE_UP_TIME, 1000)
        context.sendBroadcast(intent)
    }

    /**
     * 系统操作指令
     *
     * @param context 上下文
     * @param devact 具体指令 [EXTRA_KPO_REBOOT]、[EXTRA_KPO_SHUTDOWN]、[EXTRA_KPO_SLEEP]
     */
    private fun systemAction(context: Context, devact: String) {
        val intent = Intent(ACTION_KPO_DEVACT)
        intent.putExtra(EXTRA_KPO_DEVACT_CMD, devact)
        context.sendBroadcast(intent)
    }

    /**
     * 安装 APK ，可用于更新软件系统
     *
     * @param apkPath 安装包路径
     * @param autoInstall 是否静默安装，即：不提示用户，直接进行安装
     * @param context 上下文
     */
    fun installAPK(apkPath: String, autoInstall: Boolean, context: Context) {
        logI("开始静默安装，嘘~~~")
        val intent = Intent(ACTION_KPO_APK_INSTALL)
        intent.putExtra(EXTRA_KPO_PATH_INSTALL, apkPath)
        intent.putExtra(EXTRA_KPO_AUTO_START_INSTALL, autoInstall)
        context.sendBroadcast(intent)
    }

    /**
     * ACC 断电检测输入接口
     *
     * @return ACC 是否接电
     */
    fun isACCLink(): Boolean {
        return Gpioctljni.getAccInValue() == 0
    }

    /**
     * 获取手机型号
     *
     * @return  手机型号
     */
    fun getSystemModel(): String {
        return android.os.Build.MODEL
    }

    /**
     * 获取手机厂商
     *
     * @return  手机厂商
     */
    fun getDeviceBrand(): String {
        return android.os.Build.BRAND
    }

    /**
     * 获取手机IMEI(需要“android.permission.READ_PHONE_STATE”权限)
     *
     * @return  手机IMEI
     */
    @SuppressLint("MissingPermission")
    fun getIMEI(ctx: Context): String {
        val tm = ctx.getSystemService(Activity.TELEPHONY_SERVICE) as TelephonyManager
        return tm.deviceId
    }

}
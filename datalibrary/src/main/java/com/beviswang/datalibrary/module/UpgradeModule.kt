package com.beviswang.datalibrary.module

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.content.Intent
import android.content.IntentFilter
import com.beviswang.datalibrary.logD
import com.beviswang.datalibrary.logE
import com.beviswang.datalibrary.logI
import com.beviswang.datalibrary.logW
import com.beviswang.datalibrary.util.SystemHelper

/**
 * 安装包下载类
 * 需要使用广播接收 ACTION [ACTION_DOWNLOAD_FILE]
 * Created by shize on 2018/1/25.
 */
class UpgradeModule(context: Context) {
    // 上下文
    private val context = context.applicationContext
    /** 获取下载管理器 */
    private val mDownloadManager: DownloadManager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private var mTaskId: Long = 0

    /** 下载状态广播接收器 */
    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            checkDownloadStatus()
        }
    }

    /**
     * 检测下载状态
     */
    private fun checkDownloadStatus() {
        val query = DownloadManager.Query()
        // 筛选下载任务，传入任务ID，可变参数
        query.setFilterById(mTaskId)
        val c = mDownloadManager.query(query)
        if (c.moveToFirst()) {
            val status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))
            when (status) {
                DownloadManager.STATUS_PAUSED ->
                    logI(">>>下载暂停")
                DownloadManager.STATUS_PENDING ->
                    logW(">>>下载延迟")
                DownloadManager.STATUS_RUNNING ->
                    logI(">>>正在下载")
                DownloadManager.STATUS_SUCCESSFUL -> {
                    logI(">>>下载完成")
                    // TODO 适配警告：静默安装一般设备没有
                    //下载完成安装 APK，设备支持静默安装
                    SystemHelper.installAPK(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS).toString() + "/app-release.apk", true, context)
                    // 普通提示用户安装 APK
//                    installAPK(Environment.getExternalStoragePublicDirectory(
//                            Environment.DIRECTORY_DOWNLOADS).toString())
                }
                DownloadManager.STATUS_FAILED ->
                    logE(">>>下载失败")
            }
        }
    }

    /**
     * 下载文件
     *
     * @param url 下载地址
     */
    fun downloadFile(url: String) {
        // 创建下载目录
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdir()
        // 创建请求对象
        val request = DownloadManager.Request(Uri.parse(url))
        request.setTitle("终端自动升级程序")
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
        request.setMimeType("application/vnd.android.package-archive")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        // 设置文件存放路径
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "app-release.apk")
        // 将下载任务加入下载队列，否则不会进行下载
        mTaskId = mDownloadManager.enqueue(request)
        // 注册下载状态监听广播
        context.registerReceiver(mReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

//        val query = DownloadManager.Query()
//        val timer = Timer()
//        val task = object : TimerTask() {
//            override fun run() {
//                LogHelper.LOGD("开始下载安装包")
//                val cursor = mDownloadManager.query(query.setFilterById(downLoadId))
//                if (cursor != null && cursor.moveToFirst()) {
//                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
//                        // 下载完成，安装程序
//                        LogHelper.LOGD("下载完成")
//                        installAPK(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/app-release.apk")
//                        cancel()
//                    }
//                    val title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))
//                    val address = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
//                    val downloadedBytes = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
//                    val totalBytes = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
//                    val pro = downloadedBytes * 100 / totalBytes
//                    LogHelper.LOGD("下载进度：$pro")
//                    // 发送当前下载进度 400 ms
//                    context.sendBroadcast(Intent(ACTION_DOWNLOAD_FILE)
//                            .putExtra(DOWNLOAD_PROGRESS_KEY, pro)
//                            .putExtra(DOWNLOAD_TITLE_KEY, title)
//                            .putExtra(DOWNLOAD_ADDRESS_KEY, address))
//                }
//                cursor!!.close()
//            }
//        }
//        timer.schedule(task, 0, 400)
//        task.run()
    }

    /**
     * 提示安装程序
     * 路径：Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/app-release.apk"
     *
     * @param path 安装包文件路径
     */
    private fun installAPK(path: String) {
        logD("开始安装程序")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // 4.0 以上系统弹出安装成功打开界面
        context.startActivity(intent)
    }

    companion object {
        val ACTION_DOWNLOAD_FILE = "com.beviswang.datalibrary.download"             // 下载文件广播 ACTION
    }
}
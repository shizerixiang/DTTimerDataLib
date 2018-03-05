package com.beviswang.datalibrary.util

import android.content.Context
import android.content.pm.PackageManager
import com.beviswang.datalibrary.logD


/**
 * 版本工具类
 * Created by shize on 2018/1/29.
 */
object VersionHelper {
    /**
     * 获取本地软件版本号
     */
    fun getLocalVersion(ctx: Context): Int {
        var localVersion = 0
        try {
            val packageInfo = ctx.applicationContext
                    .packageManager
                    .getPackageInfo(ctx.packageName, 0)
            localVersion = packageInfo.versionCode
            logD("本软件的版本号。。" + localVersion)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return localVersion
    }

    /**
     * 获取本地软件版本号名称
     */
    fun getLocalVersionName(ctx: Context): String {
        var localVersion = ""
        try {
            val packageInfo = ctx.applicationContext
                    .packageManager
                    .getPackageInfo(ctx.packageName, 0)
            localVersion = packageInfo.versionName
            logD("本软件的版本号。。" + localVersion)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return localVersion
    }
}
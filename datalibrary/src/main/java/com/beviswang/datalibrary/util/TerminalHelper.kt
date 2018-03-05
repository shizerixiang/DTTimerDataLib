package com.beviswang.datalibrary.util

import android.annotation.SuppressLint
import android.content.Context
import com.beviswang.datalibrary.R

/**
 * 终端参数及信息工具类
 * Created by shize on 2018/1/25.
 */
object TerminalHelper {
    /** 终端手机号 */
    val TERMINAL_PHONE_NUMBER = "终端手机号"
    /** 终端车牌号 */
    val TERMINAL_CAR_NUMBER = "终端车牌号"
    /** 终端编号 */
    val TERMINAL_NUMBER = "终端编号"
    /** 终端序列号 */
    val TERMINAL_SERIAL_NUMBER = "终端序列号"
    /** 平台编号 */
    val PLATFORM_NUMBER = "平台编号"
    /** 平台 IP */
    val PLATFORM_IP = "平台IP"
    /** 平台端口号 */
    val PLATFORM_PORT = "平台端口号"
    /** 培训机构编号 */
    val MECHANISM_NUMBER = "培训机构编号"
    /** 培训机构名称 */
    val MECHANISM_NAME = "培训机构名称"
    /** 证书口令 */
    val CERTIFICATE_KEY = "证书口令"
    /** 证书路径 */
    val CERTIFICATE_PATH = "证书路径"
    /** 软件版本号 */
    val SOFTWARE_VERSION = "软件版本号"

    /**
     * 保存终端参数信息
     *
     * @param context 上下文
     * @param map 参数键值对
     */
    @SuppressLint("ApplySharedPref")
    fun saveTerminalInfo(context: Context, map: Map<String, String>) {
        val sp = context.getSharedPreferences(context.getString(R.string.str_terminal_info), Context.MODE_PRIVATE)
        val editor = sp.edit()
        map.forEach {
            editor.putString(it.key, it.value)
        }
        editor.commit()
    }

    /**
     * 读取字符串参数
     *
     * @param context 上下文
     * @param key 参数 key
     * @param defValue 默认值，在没有返回值的情况下的默认返回值
     * @return 返回读取到的字符串
     */
    fun readTerminalInfo(context: Context, key: String, defValue: String): String {
        val sp = context.getSharedPreferences(context.getString(R.string.str_terminal_info), Context.MODE_PRIVATE)
        return sp.getString(key, defValue)
    }
}
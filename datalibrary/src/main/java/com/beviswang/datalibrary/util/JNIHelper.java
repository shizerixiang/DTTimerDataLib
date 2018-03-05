package com.beviswang.datalibrary.util;

import android.content.Context;

/**
 * JNI 方法集合类
 * Created by shize on 2018/1/11.
 */

public class JNIHelper {
    static {
        System.loadLibrary("native_lib");
    }

    /**
     * 弹出 Toast
     *
     * @param context 上下文
     * @param msg     消息体
     * @return 消息体
     */
    public native String showToast(Context context, String msg);


    /**
     * 读取手机的型号
     *
     * @return 手机的型号
     */
    public native String getPhoneModel();

    /**
     * 获取手机的 IMEI
     *
     * @return 手机的 IMEI
     */
    public native String getIMEI(Context context);
}
package com.beviswang.datalibrary.window

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.WindowManager.LayoutParams
import android.view.WindowManager
import com.beviswang.datalibrary.R
import com.beviswang.datalibrary.logI

/**
 * 相机窗口
 * Created by shize on 2018/1/29.
 */
class CameraWindow {
    private var windowManager: WindowManager? = null
    private var applicationContext: Context? = null
    private var dummyCameraView: SurfaceView? = null

    /**
     * 显示全局窗口
     *
     * @param context
     */
    fun show(context: Context) {
        if (applicationContext == null) {
            applicationContext = context.applicationContext
            windowManager = applicationContext!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
            val winView = LayoutInflater.from(context).inflate(R.layout.float_camera_window,null)
            dummyCameraView = winView.findViewById(R.id.mWinSurface) as SurfaceView
            val params = LayoutParams()
            params.width = 1
            params.height = 1
            params.type = LayoutParams.TYPE_SYSTEM_ALERT
            params.gravity = Gravity.END or Gravity.BOTTOM
            // 屏蔽点击事件
            params.flags = (LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or LayoutParams.FLAG_NOT_FOCUSABLE
                    or LayoutParams.FLAG_NOT_TOUCHABLE)
            windowManager!!.addView(winView, params)
            logI("显示全局窗口")
        }
    }

    /**
     * @return 获取窗口视图
     */
    fun getDummyCameraView(): SurfaceView? {
        return dummyCameraView
    }

    /**
     * 隐藏窗口
     */
    fun dismiss() {
        try {
            if (windowManager != null && dummyCameraView != null) {
                windowManager!!.removeView(dummyCameraView)
                logI("隐藏全局窗口")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        applicationContext = null
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: CameraWindow? = null

        fun getInstance(): CameraWindow {
            if (INSTANCE == null) INSTANCE = CameraWindow()
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE?.dismiss()
            INSTANCE = null
        }
    }
}
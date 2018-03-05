package com.beviswang.datalibrary.module

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.util.Log
import com.beviswang.datalibrary.logI
import java.lang.ref.WeakReference
import java.util.*

/**
 * TTS 语音模块
 * Created by shize on 2017/12/22.
 */
class TTSModule(context: Context) : TextToSpeech.OnInitListener {
    /**
     * 朗读者对象
     */
    private val mSpeaker = TextToSpeech(context.applicationContext, this)

    /**
     * 上下文的弱引用
     */
    private val mWeakContext = WeakReference<Context>(context)

    /**
     * 用来初始化 TextToSpeech 引擎
     * status:SUCCESS 或 ERROR 这 2 个值
     * setLanguage 设置语言，帮助文档里面写了有 22 种
     * TextToSpeech.LANG_MISSING_DATA : 表示语言的数据丢失
     * TextToSpeech.LANG_NOT_SUPPORTED : 不支持
     */
    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            Log.e(javaClass.simpleName, "初始化 TextToSpeech 对象失败！")
            return
        }
        val result2 = mSpeaker.setLanguage(Locale.US) // 英文
        val result1 = mSpeaker.setLanguage(Locale.CHINA) // 中文
        if (result1 == TextToSpeech.LANG_MISSING_DATA || result2 == TextToSpeech.LANG_MISSING_DATA) {
            Log.e(javaClass.simpleName, "语音数据丢失")
            return
        }
        if (result1 == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w(javaClass.simpleName, "语音不支持中文，即将打开设置页面")
//            if (openTTSSettings()) {
//                Log.i(javaClass.simpleName, "打开成功！")
//                return
//            }
//            Log.e(javaClass.simpleName, "打开失败！")
            return
        }
        // 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
        mSpeaker.setPitch(1.0f)
    }

    /**
     * 开始朗读
     * @param msg 朗读内容
     */
    fun speak(msg: String) {
        if (mSpeaker.isSpeaking) {
            mSpeaker.stop()
        }
        val result = mSpeaker.speak(msg, TextToSpeech.QUEUE_FLUSH, null, SPEAKER_ID)
        logI("朗读内容：$msg 结果：$result")
        // 检测朗读结果
//        if (result == TextToSpeech.ERROR && mWeakContext.get() != null && msg.isNotEmpty()) {
//            Toast.makeText(mWeakContext.get(), "请安装中文语音引擎，推荐安装科大讯飞语音",
//                    Toast.LENGTH_SHORT).show()
//        }
    }

    /**
     * 关闭朗读者
     */
    fun shutDown() {
        if (mSpeaker.isSpeaking) mSpeaker.stop()
        mSpeaker.shutdown()
    }

    /**
     * 跳转到“语音输入与输出”设置界面
     * @return 是否跳转成功
     */
    private fun openTTSSettings(): Boolean {
        if (mWeakContext.get() == null) {
            return false
        }
        return try {
            mWeakContext.get()!!.startActivity(Intent("com.android.settings.TTS_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }

    companion object {
        private val SPEAKER_ID: String = "com.beviswang.dttimer.speakerId" // 朗读者唯一 ID
        private var INSTANCE: TTSModule? = null // 朗读者实例

        /**
         * 获取朗读者
         * @param context 上下文
         * @return 朗读者
         */
        fun getInstance(context: Context): TTSModule {
            if (INSTANCE == null) INSTANCE = TTSModule(context)
            return INSTANCE!!
        }

        /**
         * 销毁朗读者
         */
        fun destroyInstance() {
            if (INSTANCE == null) return
            INSTANCE!!.shutDown()
            INSTANCE = null
        }
    }
}
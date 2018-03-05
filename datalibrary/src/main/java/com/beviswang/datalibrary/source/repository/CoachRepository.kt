package com.beviswang.datalibrary.source.repository

import android.content.Context
import android.content.Intent
import com.beviswang.datalibrary.BaseDataSource
import com.beviswang.datalibrary.contract.CoachContract
import com.beviswang.datalibrary.operator.IMsgOperator
import com.beviswang.datalibrary.operator.HeartbeatOperator
import com.beviswang.datalibrary.message.passthrough.CoachLogoutData
import com.beviswang.datalibrary.message.PasBodyMsg
import com.beviswang.datalibrary.model.CoachModel
import com.beviswang.datalibrary.service.DTTimerSystemService
import com.beviswang.datalibrary.util.MessageHelper
import com.beviswang.datalibrary.util.MessageID
import java.lang.ref.WeakReference

/**
 * 教练数据源
 * Created by shize on 2018/1/16.
 */
class CoachRepository(context: Context) : CoachContract.ICoachDataSource {
    private val mWeakContext = WeakReference<Context>(context) // 上下文弱引用

    override fun getData(callback: BaseDataSource.LoadSourceCallback<CoachModel>) {
        takePhoto(20)
    }

    override fun requestLogout(callback: BaseDataSource.LoadSourceCallback<CoachModel>) {
        val mOperator: IMsgOperator = HeartbeatOperator.getInstance()
        val dataContent = CoachLogoutData()
        // 获取透传消息
        val pMsg = MessageHelper.getUpstreamMsg(MessageID.ClientCoachLogoutID, dataContent,
                true, true)
        // 发送消息
        if (!mOperator.sendMsg(pMsg)) {
            callback.onDataLoadFailed("服务器连接失败")
            return
        }
        // 接收消息
        try {
            val rePMsg = mOperator.receiveMsg()
            if (rePMsg == null) {
                callback.onDataLoadFailed("服务器数据获取失败")
                return
            }
            val reMsgBody = rePMsg.getMsgBody() as PasBodyMsg
            val answerData = reMsgBody.getDataContent() as CoachLogoutData.LogoutAnswerData
            checkLogoutResult(answerData, callback)
        } catch (e: Exception) {
            callback.onDataLoadFailed("服务器数据获取失败")
            e.printStackTrace()
        } finally {
            // 结束业务消息
            mOperator.sendOver()
        }
    }

    /**
     * 检测登出结果
     *
     * @param answerData 登出结果数据
     * @param callback 回调
     */
    private fun checkLogoutResult(answerData: CoachLogoutData.LogoutAnswerData, callback: BaseDataSource.LoadSourceCallback<CoachModel>) {
        when (answerData.result) {
            1 -> {
//                Publish.getInstance().mCoachInfo = null
                callback.onDataLoaded(CoachModel())
                takePhoto(21)
            }
            2 -> callback.onDataLoadFailed("登出失败")
            else -> callback.onDataLoadFailed("其他错误")
        }
    }

    /**
     * 拍照
     *
     * @param eventType 拍照事件类型
     */
    private fun takePhoto(eventType:Int) {
        val context = mWeakContext.get() ?: throw Exception("上下文失效！")
        context.sendBroadcast(Intent(DTTimerSystemService.ACTION_TAKE_PIC).
                putExtra(DTTimerSystemService.EXTRA_CAMERA_CONTROL, DTTimerSystemService.CAMERA_TAKE).
                putExtra(DTTimerSystemService.EXTRA_CAMERA_EVENT_TYPE, eventType))
    }

    override fun refreshData() {}

    override fun saveData(data: CoachModel) {}

    companion object {
        fun newInstance(context: Context): CoachRepository {
            return CoachRepository(context)
        }
    }
}
package com.beviswang.datalibrary.operator

import android.content.Context
import com.beviswang.datalibrary.doSend
import com.beviswang.datalibrary.message.CurrencyMsg
import com.beviswang.datalibrary.message.GNSSDataMsg
import com.beviswang.datalibrary.message.PackageMsg
import com.beviswang.datalibrary.message.passthrough.HoursReportData
import com.beviswang.datalibrary.source.db.DBOperator
import com.beviswang.datalibrary.source.db.IMsgCacheOperator
import com.beviswang.datalibrary.util.MessageHelper
import com.beviswang.datalibrary.util.MessageID
import com.beviswang.datalibrary.util.OperationHelper
import java.lang.ref.WeakReference

/**
 * 补发离线消息操作类
 * Created by shize on 2018/3/1.
 */
class OffLineOperator(context: Context) {
    private val weakContext = WeakReference<Context>(context)
    private val dbOperator: IMsgCacheOperator = DBOperator(context, "localData.db", 1)

    /**
     * 补发离线消息
     */
    fun reissueMsg() {
        reissueHours()
        reissueLocation()
        reissuePicture()
        reissueCacheMsg()
    }

    /**
     * 补发学时
     */
    private fun reissueHours() {
        val hourArray = dbOperator.getOffLineHoursMsg()
        hourArray?.forEach {
            sendReissueHoursMsg(it)
        }
    }

    /**
     * 补发学时消息
     */
    private fun sendReissueHoursMsg(dataContent: HoursReportData) {
        doSend {
            val pMsg = MessageHelper.getUpstreamMsg(MessageID.ClientHoursRecodeID, dataContent,
                    true, false)
            val heart = HeartbeatOperator.getInstance()
            // 发送学时数据
            heart.sendMsg(pMsg)
            val curMsg = OperationHelper.handleReceive(heart)
            heart.sendOver()
            if (curMsg != null && (curMsg.getMsgBody() as CurrencyMsg).result == 0)
                dbOperator.updateReissueHoursMsg(dataContent.hoursNum)
        }
    }

    /**
     * 补发位置
     */
    private fun reissueLocation() {
        val locationArray = dbOperator.getOffLineGNSSMsg()
        locationArray?.forEach {
            sendReissueLocation(it)
        }
    }

    /**
     * 补发位置消息
     */
    private fun sendReissueLocation(GNSSData: GNSSDataMsg) {
        doSend {
            val pMsg = PackageMsg()
            val msgHeader = PackageMsg.MsgHeader()
            msgHeader.msgId = MessageID.ClientLocationID
            pMsg.setMessage(msgHeader, GNSSData)
            val heart = HeartbeatOperator.getInstance()
            heart.sendMsg(pMsg)
            val curMsg = OperationHelper.handleReceive(heart)
            heart.sendOver()
            if (curMsg != null && (curMsg.getMsgBody() as CurrencyMsg).result == 0)
                dbOperator.updateReissueGNSSMsg(GNSSData)
        }
    }

    /**
     * 补发图片
     */
    private fun reissuePicture() {
        val picArray = dbOperator.getOffLinePictureInfo()
        val context = weakContext.get() ?: throw Exception("无效上下文！")
        picArray?.forEach {
            doSend {
                val result = MessageHelper.uploadPicture(context, it, false)
                if (result == 0) dbOperator.updateReissuePictureInfo(it.mId)
            }
        }
    }

    /**
     * 补发缓存消息
     */
    private fun reissueCacheMsg(){
        val msgArray = dbOperator.getOffLineMsg()
        msgArray?.forEach {
            doSend {
                val heart = HeartbeatOperator.getInstance()
                heart.sendMsg(it)
                val rePMsg = heart.receiveMsg()
                if (rePMsg != null) dbOperator.deleteMsg(it.mId)
            }
        }
    }
}
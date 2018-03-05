package com.beviswang.datalibrary.operator

import android.content.Context
import android.content.Intent
import com.beviswang.datalibrary.Publish
import com.beviswang.datalibrary.message.PackageMsg
import com.beviswang.datalibrary.message.PasBodyMsg
import com.beviswang.datalibrary.module.SocketModule
import com.beviswang.datalibrary.util.MessageHelper
import com.beviswang.datalibrary.util.MessageID
import java.lang.ref.WeakReference

/**
 * 消息操作类
 * Created by shize on 2018/1/19.
 */
class MsgOperator(context: Context) : IMsgOperator {
    // 对上下文进行弱引用
    private val mWeakContext = WeakReference<Context?>(context.applicationContext)
    private val mSocket = SocketModule()
    // 服务器信息
    var serverIP: String = Publish.getInstance().mPlatformIP    // IP 地址
    var serverPort: Int = Publish.getInstance().mPlatformPort   // 服务器端口号

    override fun connect(): Boolean {
        val context = mWeakContext.get() ?: return false
        val isConnect = try {
            SocketModule.SERVER_IP = serverIP
            SocketModule.SERVER_PORT = serverPort
            mSocket.connect()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
        context.sendBroadcast(Intent(ACTION_PLATFORM_STATE).putExtra(EXTRA_CONNECTION, isConnect))
        return isConnect
    }

    override fun sendMsg(msgPackage: PackageMsg): Boolean {
        try {
            mSocket.sendMessage(msgPackage.getSendMsg())
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    override fun receiveMsg(): PackageMsg? {
        val packageMsg: PackageMsg?
        try {
            packageMsg = mSocket.receiverMessage()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return packageMsg
    }

    override fun sendOver() {}

    override fun close() {
        mSocket.close()
        val context = mWeakContext.get() ?: return
        context.sendBroadcast(Intent(ACTION_PLATFORM_STATE).putExtra(EXTRA_CONNECTION, false))
    }

    override fun sendResponseMsg(rePMsg: PackageMsg, result: Int) {
        val reHeader = rePMsg.getMsgHeader()
        val flowNum = reHeader!!.flowNum
        val msgId = reHeader.msgId
        val rreMsg = MessageHelper.getAnswerMsg(flowNum, msgId, result)
        sendMsg(rreMsg)
    }

    override fun sendUpLinkResponseMsg(rePMsg: PasBodyMsg) {
        val packageMsg = PackageMsg()
        val headerMsg = PackageMsg.MsgHeader()
        headerMsg.msgId = MessageID.ClientUpstreamID
        packageMsg.setMessage(headerMsg, rePMsg)
        sendMsg(packageMsg)
    }

    companion object {
        // 发送连接状态 Action
        val ACTION_PLATFORM_STATE = "com.beviswang.dttimer.action.platform.state"
        val EXTRA_CONNECTION = "com.beviswang.dttimer.extra.connection"

        fun newInstance(context: Context): IMsgOperator {
            return MsgOperator(context)
        }
    }
}
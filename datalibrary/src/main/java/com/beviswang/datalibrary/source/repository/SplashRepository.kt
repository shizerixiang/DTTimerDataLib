package com.beviswang.datalibrary.source.repository

import com.beviswang.datalibrary.BaseDataSource
import com.beviswang.datalibrary.Publish
import com.beviswang.datalibrary.operator.IMsgOperator
import com.beviswang.datalibrary.contract.SplashContract
import com.beviswang.datalibrary.message.*
import com.beviswang.datalibrary.model.SplashModel
import com.beviswang.datalibrary.operator.HeartbeatOperator
import com.beviswang.datalibrary.util.FileHelper
import com.beviswang.datalibrary.util.MessageID

/**
 * 教练数据源
 * Created by shize on 2018/1/16.
 */
class SplashRepository : SplashContract.ISplashDataSource {
    override fun requestAuthenticate(callback: BaseDataSource.LoadSourceCallback<SplashModel>) {
        val mOperator: IMsgOperator =  HeartbeatOperator.getInstance() // 消息通信操作类
        // 检测是否注册：检测编号及证书是否存在
        if (Publish.getInstance().mTerminalNo.isNotEmpty() && !FileHelper.hasFile(
                RegisterResponseMsg.filePath + Publish.getInstance().mTerminalNo + ".pfx")) {
            callback.onDataLoadFailed("终端未注册")
            return
        }
        // 创建消息包
        val pMsg = createMsg()
        // 发送消息
        if (!mOperator.sendMsg(pMsg)) {
            callback.onDataLoadFailed("连接服务器失败")
            return
        }
        // 接收服务器发送的数据
        val rePMsg = mOperator.receiveMsg()
        // 结束业务，如果缺少这步则一直停止心跳包
        mOperator.sendOver()
        if (rePMsg == null) {
            callback.onDataLoadFailed("服务器数据获取失败")
            return
        }
        val response = rePMsg.getMsgBody() as CurrencyMsg
        // 检查服务器返回结果是否为成功 0
        if (!checkResult(response.result, callback)) return
        callback.onDataLoaded(SplashModel())
    }

    /**
     * 刷新 Socket 实例
     * 即：关闭现有 Socket 连接，重新开启 Socket 连接
     */
    fun refreshSocket(){
        HeartbeatOperator.getInstance().close()
    }

    /**
     * 检测结果
     *
     * @param result 结果代码
     * @param callback 回调
     * @return 是否成功
     */
    private fun checkResult(result: Int, callback: BaseDataSource.LoadSourceCallback<SplashModel>): Boolean {
        when (result) {
            CurrencyMsg.RESULT_CODE_SUCCEED -> return true
            CurrencyMsg.RESULT_CODE_FAILED -> callback.onDataLoadFailed("鉴权失败")
            CurrencyMsg.RESULT_CODE_WRONG -> callback.onDataLoadFailed("鉴权消息有误")
            CurrencyMsg.RESULT_CODE_NONSUPPORT -> callback.onDataLoadFailed("鉴权不支持")
        }
        return false
    }

    /**
     * @return 创建消息包
     */
    private fun createMsg(): PackageMsg {
        val pMsg = PackageMsg()
        val msgHeader = PackageMsg.MsgHeader()
        msgHeader.msgId = MessageID.ClientLoginID
        val msgBody = AuthenticateMsg()
        msgBody.setTerminalNum(Publish.getInstance().mTerminalNo)
        pMsg.setMessage(msgHeader, msgBody)
        return pMsg
    }

    override fun getData(callback: BaseDataSource.LoadSourceCallback<SplashModel>) {}

    override fun refreshData() {}

    override fun saveData(data: SplashModel) {}

    companion object {
        fun newInstance(): SplashRepository {
            return SplashRepository()
        }
    }
}
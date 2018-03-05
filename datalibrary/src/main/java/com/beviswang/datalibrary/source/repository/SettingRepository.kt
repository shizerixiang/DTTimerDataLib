package com.beviswang.datalibrary.source.repository

import com.beviswang.datalibrary.BaseDataSource
import com.beviswang.datalibrary.Publish
import com.beviswang.datalibrary.operator.IMsgOperator
import com.beviswang.datalibrary.contract.SettingContract
import com.beviswang.datalibrary.message.*
import com.beviswang.datalibrary.model.SettingModel
import com.beviswang.datalibrary.operator.HeartbeatOperator
import com.beviswang.datalibrary.util.FileHelper
import com.beviswang.datalibrary.util.MessageID

/**
 * 设置数据源
 * Created by shize on 2018/1/16.
 */
class SettingRepository : SettingContract.ISettingDataSource {

    override fun requestLogOffTerminal(callback: BaseDataSource.LoadSourceCallback<SettingModel>) {
        val mOperator: IMsgOperator = HeartbeatOperator.getInstance() // 消息通信操作类
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
        if (!FileHelper.deleteFile(RegisterResponseMsg.filePath + Publish.getInstance().mTerminalNo +
                ".pfx")) callback.onDataLoadFailed("删除注册文件失败！")
        callback.onDataLoaded(SettingModel())
    }

    /**
     * 检测结果
     *
     * @param result 结果代码
     * @param callback 回调
     * @return 是否成功
     */
    private fun checkResult(result: Int, callback: BaseDataSource.LoadSourceCallback<SettingModel>): Boolean {
        when (result) {
            CurrencyMsg.RESULT_CODE_SUCCEED -> return true
            CurrencyMsg.RESULT_CODE_FAILED -> callback.onDataLoadFailed("注销失败")
            CurrencyMsg.RESULT_CODE_WRONG -> callback.onDataLoadFailed("注销消息有误")
            CurrencyMsg.RESULT_CODE_NONSUPPORT -> callback.onDataLoadFailed("注销不支持")
        }
        return false
    }

    /**
     * 创建消息
     */
    private fun createMsg(): PackageMsg {
        val header = PackageMsg.MsgHeader()
        header.msgId = MessageID.ClientLogOffID
        val pMsg = PackageMsg()
        pMsg.setMessage(header, null)
        return pMsg
    }

    override fun getData(callback: BaseDataSource.LoadSourceCallback<SettingModel>) {}

    override fun refreshData() {}

    override fun saveData(data: SettingModel) {}

    companion object {
        fun newInstance(): SettingRepository {
            return SettingRepository()
        }
    }
}
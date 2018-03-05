package com.beviswang.datalibrary.source.repository

import com.beviswang.datalibrary.contract.RegisterContract
import com.beviswang.datalibrary.BaseDataSource
import com.beviswang.datalibrary.Publish
import com.beviswang.datalibrary.operator.IMsgOperator
import com.beviswang.datalibrary.message.*
import com.beviswang.datalibrary.util.MessageID
import com.beviswang.datalibrary.model.RegisterModel
import com.beviswang.datalibrary.operator.HeartbeatOperator

/**
 * 注册数据源
 * Created by shize on 2018/1/15.
 */
class RegisterRepository : RegisterContract.IRegisterDataSource {
    private var mRegisterModel: RegisterModel? = null

    override fun setRegisterModel(registerModel: RegisterModel) {
        mRegisterModel = registerModel
        Publish.getInstance().mPlatformIP = mRegisterModel!!.mPlatFormIP
        Publish.getInstance().mPlatformPort = mRegisterModel!!.mPlatFormPort
        Publish.getInstance().mPhoneNo = mRegisterModel!!.mPhoneNum
    }

    override fun getData(callback: BaseDataSource.LoadSourceCallback<RegisterModel>) {
        val mOperator: IMsgOperator = HeartbeatOperator.getInstance() // 消息通信操作类
        if (mRegisterModel == null) callback.onDataLoadFailed("没有获取到注册信息")
        // 创建消息包
        val pMsg = createMsg()
        // 发送消息
        if (!mOperator.sendMsg(pMsg)) {
            callback.onDataLoadFailed("连接服务器失败")
            return
        }
        // 接收服务器发送的数据
        try {
            val rePMsg = mOperator.receiveMsg()
            if (rePMsg == null) {
                callback.onDataLoadFailed("服务器数据获取失败")
                return
            }
            val response = rePMsg.getMsgBody() as RegisterResponseMsg
            // 检查服务器返回结果是否为成功 0
            if (!checkResult(response.result, callback)) return
            mRegisterModel!!.mMessage = rePMsg
            callback.onDataLoaded(mRegisterModel!!)
        } catch (e: Exception) {
            callback.onDataLoadFailed("服务器数据获取失败")
            e.printStackTrace()
        } finally {
            // 结束业务，如果缺少这步则一直停止心跳包
            mOperator.sendOver()
        }
    }

    /**
     * 检查服务器返回结果
     *
     * @param result 服务器返回结果
     * @param callback 回调
     * @return 是否成功
     */
    private fun checkResult(result: Int, callback: BaseDataSource.LoadSourceCallback<RegisterModel>): Boolean {
        when (result) {
            RESULT_CODE_REGISTER_SUCCEED -> return true
            RESULT_CODE_REGISTER_CAR_REPEAT -> callback.onDataLoadFailed("车辆已被注册")
            RESULT_CODE_REGISTER_NO_CAR -> callback.onDataLoadFailed("数据库中无该车辆")
            RESULT_CODE_REGISTER_TERMINAL_REPEAT -> callback.onDataLoadFailed("终端已被注册")
            RESULT_CODE_REGISTER_NO_TERMINAL -> callback.onDataLoadFailed("数据库中无该终端")
            else -> callback.onDataLoadFailed("未知错误")
        }
        return false
    }

    /**
     * @return 创建消息包
     */
    private fun createMsg(): PackageMsg {
        val pMsg = PackageMsg()
        val msgHeader = PackageMsg.MsgHeader()
        msgHeader.msgId = MessageID.ClientRegisterID
        val msgBody = RegisterRequestMsg()
        msgBody.carIdentification = mRegisterModel!!.mCarNum
        pMsg.setMessage(msgHeader, msgBody)
        return pMsg
    }

    override fun refreshData() {}

    override fun saveData(data: RegisterModel) {
        // TODO 保存注册数据到本地
        Publish.getInstance().mPlatformIP = mRegisterModel!!.mPlatFormIP
        Publish.getInstance().mPlatformPort = mRegisterModel!!.mPlatFormPort
        val body = (data.mMessage.getMsgBody()!! as RegisterResponseMsg)
        Publish.getInstance().mCarNo = data.mCarNum
        Publish.getInstance().mTerminalNo = body.terminalNum
        Publish.getInstance().mPlatformNo = body.platformNum
        Publish.getInstance().mMechanismNo = body.mechanismNum
        Publish.getInstance().mCertificateKey = body.certificateKey
    }

    companion object {
        private val RESULT_CODE_REGISTER_SUCCEED = 0 // 注册成功
        private val RESULT_CODE_REGISTER_CAR_REPEAT = 1 // 车辆已被注册
        private val RESULT_CODE_REGISTER_NO_CAR = 2 // 数据库中无该车辆
        private val RESULT_CODE_REGISTER_TERMINAL_REPEAT = 3 // 终端已被注册
        private val RESULT_CODE_REGISTER_NO_TERMINAL = 4 // 数据库中无该终端

        fun newInstance(): RegisterRepository {
            return RegisterRepository()
        }
    }
}
package com.beviswang.datalibrary.source.repository

import com.beviswang.datalibrary.BaseDataSource
import com.beviswang.datalibrary.Publish
import com.beviswang.datalibrary.contract.LoginContract
import com.beviswang.datalibrary.operator.HeartbeatOperator
import com.beviswang.datalibrary.operator.IMsgOperator
import com.beviswang.datalibrary.message.passthrough.CoachLoginData
import com.beviswang.datalibrary.message.PasBodyMsg
import com.beviswang.datalibrary.message.passthrough.StudentLoginData
import com.beviswang.datalibrary.model.CoachModel
import com.beviswang.datalibrary.model.LoginModel
import com.beviswang.datalibrary.model.StudentModel
import com.beviswang.datalibrary.util.MessageHelper
import com.beviswang.datalibrary.util.MessageID

/**
 * 登录数据源
 * Created by shize on 2018/1/15.
 */
class LoginRepository : LoginContract.ILoginDataSource {
    private var mLoginType = LOGIN_TYPE_COACH

    override fun requestLoginCoach(loginModel: LoginModel, callback: BaseDataSource.LoadSourceCallback<LoginModel>) {
        mLoginType = LOGIN_TYPE_COACH
        val mOperator: IMsgOperator = HeartbeatOperator.getInstance()
        val dataContent = CoachLoginData()
        dataContent.coachNum = loginModel.coachNum
        dataContent.coachID = loginModel.coachID
        dataContent.teachCarType = loginModel.teachCarType
        // 获取透传消息
        val pMsg = MessageHelper.getUpstreamMsg(MessageID.ClientCoachLoginID, dataContent,
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
            val answerData = reMsgBody.getDataContent() as CoachLoginData.LoginAnswerData
            checkCoachLoginResult(answerData.result, callback, loginModel)
        } catch (e: Exception) {
            callback.onDataLoadFailed("服务器数据获取失败")
            e.printStackTrace()
        } finally {
            // 结束业务消息
            mOperator.sendOver()
        }
    }

    /**
     * 教练员登录结果
     *
     * @param result 登录结果值
     * @param callback 回调
     * @param loginModel 登录数据
     */
    private fun checkCoachLoginResult(result: Int, callback: BaseDataSource.LoadSourceCallback<LoginModel>, loginModel: LoginModel) {
        when (result) {
            1 -> callback.onDataLoaded(loginModel)
            2 -> callback.onDataLoadFailed("无效的教练员编号")
            3 -> callback.onDataLoadFailed("准教车型不符")
            else -> callback.onDataLoadFailed("其他错误")
        }
    }

    override fun requestLoginStudent(loginModel: LoginModel, callback: BaseDataSource.LoadSourceCallback<LoginModel>) {
        mLoginType = LOGIN_TYPE_STUDENT
        val mOperator: IMsgOperator = HeartbeatOperator.getInstance()
        val dataContent = StudentLoginData()
        dataContent.curClass = loginModel.curClass
        dataContent.stuNum = loginModel.stuNum
        // 获取透传消息
        val pMsg = MessageHelper.getUpstreamMsg(MessageID.ClientStudentLoginID, dataContent,
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
            val answerData = reMsgBody.getDataContent() as StudentLoginData.LoginAnswerData
            checkStudentLoginResult(answerData, callback, loginModel)
        } catch (e: Exception) {
            callback.onDataLoadFailed("服务器数据获取失败")
            e.printStackTrace()
        } finally {
            // 结束业务消息
            mOperator.sendOver()
        }
    }

    /**
     * 教练员登录结果
     *
     * @param answerData 登录结果
     * @param callback 回调
     * @param loginModel 登录数据
     */
    private fun checkStudentLoginResult(answerData: StudentLoginData.LoginAnswerData,
                                        callback: BaseDataSource.LoadSourceCallback<LoginModel>,
                                        loginModel: LoginModel) {
        when (answerData.resultCode) {
            1 -> {
                loginModel.curHours = answerData.curHours
                loginModel.curMileage = answerData.curMileage
                loginModel.trainHours = answerData.trainHours
                loginModel.trainMileage = answerData.trainMileage
                loginModel.isSpeakAddMsg = answerData.isSpeakAddMsg
                loginModel.additionalMsg = answerData.additionalMsg
                callback.onDataLoaded(loginModel)
            }
            2 -> callback.onDataLoadFailed("无效的学员编号")
            3 -> callback.onDataLoadFailed("禁止登录的学员")
            4 -> callback.onDataLoadFailed("区域外教学提醒")
            5 -> callback.onDataLoadFailed("准教车型与培训车型不符")
            else -> callback.onDataLoadFailed("其他错误")
        }
    }

    override fun requestLoginAdmin(loginModel: LoginModel, callback: BaseDataSource.LoadSourceCallback<LoginModel>) {
        mLoginType = LOGIN_TYPE_ADMIN
        // TODO 模拟管理员登录
        callback.onDataLoaded(loginModel)
    }

    override fun getData(callback: BaseDataSource.LoadSourceCallback<LoginModel>) {}

    override fun refreshData() {}

    override fun saveData(data: LoginModel) {
        when (mLoginType) {
            LOGIN_TYPE_COACH -> {
                val coachInfo = CoachModel()
                coachInfo.mId = data.coachID
                coachInfo.mName = data.coachName
                coachInfo.mNumber = data.coachNum
                coachInfo.mCarType = data.teachCarType
                Publish.getInstance().mCoachInfo = coachInfo
            }
            LOGIN_TYPE_STUDENT -> {
                val studentInfo = StudentModel()
                studentInfo.mNum = data.stuNum
                studentInfo.mName = data.stuName
                studentInfo.mCarType = Publish.getInstance().mCoachInfo?.mCarType ?: throw Exception("教练员未登录！")
                studentInfo.mCurCourse = data.curClass
                studentInfo.mTotalDistance = data.trainMileage / 10f
                studentInfo.mTotalDuration = data.trainHours
                studentInfo.mCurDistance = data.curMileage / 10f
                studentInfo.mCurDuration = data.curHours
                Publish.getInstance().mStudentInfo = studentInfo
                // 课程 ID 在每个学员登录时叠加
                Publish.getInstance().mClassId++
            }
            LOGIN_TYPE_ADMIN -> {
                // TODO 保存管理员数据
            }
        }
    }

    companion object {
        private var LOGIN_TYPE_COACH = "coach"
        private var LOGIN_TYPE_STUDENT = "student"
        private var LOGIN_TYPE_ADMIN = "admin"

        fun newInstance(): LoginRepository {
            return LoginRepository()
        }
    }
}
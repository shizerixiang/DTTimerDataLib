package com.beviswang.datalibrary.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.beviswang.datalibrary.Publish
import com.beviswang.datalibrary.logD
import com.beviswang.datalibrary.logE
import com.beviswang.datalibrary.logI
import com.beviswang.datalibrary.message.*
import com.beviswang.datalibrary.message.passthrough.*
import com.beviswang.datalibrary.module.TTSModule
import com.beviswang.datalibrary.operator.HeartbeatOperator
import com.beviswang.datalibrary.service.DTTimerSystemService
import com.beviswang.datalibrary.source.db.DBOperator
import com.beviswang.datalibrary.source.db.IMsgCacheOperator

/**
 * 消息操作工具类
 * Created by shize on 2018/1/26.
 */
object OperationHelper {
    // 透传消息
    private var pasDataMsg: PasBodyMsg? = null

    /**
     * 通过消息 ID 获取消息体实例
     *
     * @param msgId 消息 ID
     * @return 消息体实例
     */
    fun getMsgBodyByMsgId(msgId: String): PackageMsg.MsgBody {
        return when (msgId) {
            MessageID.ClientAnswerID -> CurrencyMsg()   // 客户端通用应答
            MessageID.ClientRegisterID -> RegisterRequestMsg() // 客户端注册消息
            MessageID.ClientLocationID -> GNSSDataMsg() // 位置汇报消息
            MessageID.ClientAnswerLocationID -> GNSSDataMsg() // 位置信息查询应答
            MessageID.ClientUpstreamID -> PasBodyMsg() // 上行透传
            MessageID.ServerAnswerID -> CurrencyMsg()  // 服务器端通用应答
            MessageID.ServerRegisterAnswerID -> RegisterResponseMsg() // 服务器端注册应答
            MessageID.ServerSetParamID -> ParamMsg()    // 设置终端参数
            MessageID.ServerQueryAllParamID -> EmptyMsg() // 查询终端所有参数，为空消息体
            MessageID.ServerQueryAppointParamID -> QueryParamMsg() // 查询指定终端参数
            MessageID.ServerClientControlID -> ControlMsg() // 终端控制
            MessageID.ServerQueryLocationID -> EmptyMsg() // 位置信息查询
            MessageID.ServerTrackLocationID -> TrackLocationMsg() // 临时位置跟踪控制
            MessageID.ServerDownLinkID -> PasBodyMsg() // 下行透传
            else -> CurrencyMsg()   // 未知消息
        }
    }

    /**
     * 检测通用应答结果
     *
     * @param result 结果代码
     * @return 结果提示字符串
     */
    fun checkResult(result: Int) {
        when (result) {
            CurrencyMsg.RESULT_CODE_SUCCEED -> {
                logI("接收消息成功")
                return
            }
            CurrencyMsg.RESULT_CODE_FAILED -> {
                logE("接收消息失败")
                return
            }
            CurrencyMsg.RESULT_CODE_WRONG -> {
                logE("接收消息错误")
                return
            }
            CurrencyMsg.RESULT_CODE_NONSUPPORT -> {
                logE("接收消息不支持")
                return
            }
        }
        logE("消息解析错误")
    }

    /**
     * 处理接收的消息包
     * 注意：最终的消息包应是通用应答的消息
     *
     * @param hBOperator 心跳操作
     * @return 通用应答消息
     */
    fun handleReceive(hBOperator: HeartbeatOperator): PackageMsg? {
        val operator = hBOperator.mOperator
        val msg = operator.receiveMsg() ?: throw Exception("没有获取到消息数据！")
        val msgHeader = msg.getMsgHeader()!!
        when (msgHeader.msgId) {
            MessageID.ServerAnswerID -> {
                // 服务器通用应答
                logD("计时平台通用应答！")
                val currencyMsg = msg.getMsgBody()!! as CurrencyMsg
                checkResult(currencyMsg.result)
                return msg
            }
            MessageID.ServerSetParamID -> {
                // 不是心跳消息时，应该先接收心跳消息后，再开始发送应答消息
                val reMsg = handleReceive(hBOperator)
                logD("计时平台操作：设置参数！")
                // 设置终端参数
                val context = hBOperator.mWeakContext.get()
                if (context == null) {
                    operator.sendResponseMsg(msg, 1)
                    logE("接收消息失败，原因：无效的上下文！！！")
                    return null
                }
                // 将设置参数存储到本地
                val paramMsg = msg.getMsgBody()!! as ParamMsg
                ParameterHelper.saveParams(context, paramMsg.paramList)
                operator.sendResponseMsg(msg) // 应答服务器
                return reMsg
            }
            MessageID.ServerQueryAllParamID -> {
                val reMsg = handleReceive(hBOperator)
                logD("计时平台操作：查询所有参数！")
                // 查询所有参数
                val context = hBOperator.mWeakContext.get()
                if (context == null) {
                    operator.sendResponseMsg(msg, 1)
                    logE("接收消息失败，原因：无效的上下文！！！")
                    return null
                }
                val pMsg = PackageMsg()
                val pHeader = PackageMsg.MsgHeader()
                val paramMsg = ParamMsg()
                pHeader.msgId = MessageID.ClientParamAnswerID
                paramMsg.flowNum = msgHeader.flowNum
                paramMsg.paramList = ParameterHelper.readParams(context, null)
                pMsg.setMessage(pHeader, paramMsg)
                operator.sendMsg(pMsg)
                return reMsg
            }
            MessageID.ServerQueryAppointParamID -> {
                val reMsg = handleReceive(hBOperator)
                logD("计时平台操作：查询指定终端参数！")
                // 查询指定终端参数
                val context = hBOperator.mWeakContext.get()
                if (context == null) {
                    operator.sendResponseMsg(msg, 1)
                    logE("接收消息失败，原因：无效的上下文！！！")
                    return null
                }
                val pMsg = PackageMsg()
                val pHeader = PackageMsg.MsgHeader()
                val pBody = msg.getMsgBody() as QueryParamMsg
                val paramMsg = ParamMsg()
                pHeader.msgId = MessageID.ClientParamAnswerID
                paramMsg.flowNum = msgHeader.flowNum
                paramMsg.paramList = ParameterHelper.readParams(context, pBody.paramIDList)
                pMsg.setMessage(pHeader, paramMsg)
                operator.sendMsg(pMsg)
                return reMsg
            }
            MessageID.ServerClientControlID -> {
                val reMsg = handleReceive(hBOperator)
                logD("计时平台操作：终端操作消息！")
                // 终端操作消息
                val context = hBOperator.mWeakContext.get()
                if (context == null) {
                    operator.sendResponseMsg(msg, 1)
                    logE("接收消息失败，原因：无效的上下文！！！")
                    return null
                }
                handleCommendMsg(msg.getMsgBody()!! as ControlMsg, context)
                operator.sendResponseMsg(msg)
                return reMsg
            }
            MessageID.ServerQueryLocationID -> {
                val reMsg = handleReceive(hBOperator)
                logD("计时平台操作：位置信息查询消息！")
                // 位置信息查询消息
                operator.sendMsg(MessageHelper.getGNSSMsg(MessageID.ClientAnswerLocationID))
                return reMsg
            }
            MessageID.ServerTrackLocationID -> {
                val trackMsg = msg.getMsgBody() as TrackLocationMsg
                val reMsg = handleReceive(hBOperator)
                logD("计时平台操作：临时位置跟踪控制消息！")
                // TODO 临时位置跟踪控制消息
                hBOperator.mTrackLocationDuration = trackMsg.duration.toLong()
                hBOperator.startTrackPositionReport()
                return reMsg
            }
            MessageID.ServerDownLinkID -> {
                val pasMsg = msg.getMsgBody() as PasBodyMsg
                // 当消息为图片上传初始化应答时，直接返回消息
                if (pasMsg.getMsgId() == MessageID.ServerUpPicInitAnswerID) {
                    return msg
                }
                val reMsg = handleReceive(hBOperator)
                logD("计时平台操作：数据透传！")
                // 数据下行透传
                val context = hBOperator.mWeakContext.get()
                if (context == null) {
                    operator.sendResponseMsg(msg, 1)
                    logE("接收消息失败，原因：无效的上下文！！！")
                    return null
                }
                handleDownLinkMsg(pasMsg, context)
                return reMsg
            }
            else -> {
                return msg
            }
        }
    }

    /**
     * 处理命令消息
     *
     * @param controlMsg 命令指令
     */
    private fun handleCommendMsg(controlMsg: ControlMsg, context: Context) {
        when (controlMsg.commendChar) {
        // 无线升级
            1 -> {
                val url = (controlMsg.commendParam as ControlMsg.UpgradeParam).url
                if (url.isNotEmpty()) context.sendBroadcast(Intent(DTTimerSystemService.ACTION_DOWNLOAD_APK)
                        .putExtra(DTTimerSystemService.EXTRA_DOWNLOAD_PATH, url))
                else logE("下载地址为空！")
            }
        }
    }

    /**
     * 通过透传消息 ID 获取数据内容
     *
     * @param msgId 透传消息 ID
     * @return 数据内容
     */
    fun getDataContentByMsgId(msgId: String): PasBodyMsg.DataContent {
        return when (msgId) {
            MessageID.ServerSetAppParamID -> AppParamData() // 设置计时终端应用参数
            MessageID.ClientUpPicID -> UploadPictureData() // 照片数据包上传
            MessageID.ClientParamAnswerID -> AppParamData.AppParamAnswerData() // 设置应用参数应答
            MessageID.ServerCoachLoginAnswerID -> CoachLoginData.LoginAnswerData() // 教练员登录应答
            MessageID.ServerCoachLogoutAnswerID -> CoachLogoutData.LogoutAnswerData() // 教练员登出应答
            MessageID.ServerUpPicInitAnswerID -> UploadPictureData.UpInitAnswerData() // 照片上传初始化应答
            MessageID.ServerStudentLoginID -> StudentLoginData.LoginAnswerData() // 学员登录应答
            MessageID.ServerStudentLogoutID -> StudentLogoutData.LogoutAnswerData() // 学员登出应答
            MessageID.ServerReportHoursID -> HoursReportData.CommandReportHoursData() // 命令上报学时记录
            MessageID.ClientReportAnswerID -> HoursReportData.CommandReportAnswerData() // 命令上报学时记录应答
            MessageID.ClientHoursRecodeID -> HoursReportData() // 上报学时记录
            MessageID.ServerTakePhotoID -> TakePhotoData() // 立即拍照
            MessageID.ServerQueryPicID -> QueryPhotoData() // 查询照片
            MessageID.ServerReportPicAnswerID -> QueryPhotoData.ReportQueryAnswerData() // 上报照片查询结果应答
            MessageID.ServerUpAppointPicID -> QueryPhotoData.UploadAppointPictureData() // 上传指定照片
            MessageID.ServerSetBanStateID -> BanStateData() // 设置禁训状态
            MessageID.ServerQueryAppParamID -> QueryAppParamData() // 查询计时终端应用参数
            else -> EmptyData()
        }
    }

    /**
     * 处理下行透传消息
     *
     * @param pasBodyMsg 下行透传消息体
     * @param context 上下文
     */
    private fun handleDownLinkMsg(pasBodyMsg: PasBodyMsg, context: Context) {
        // 本地数据库操作实例
        val mDBOperator: IMsgCacheOperator = DBOperator(context, "localData.db", 1)
        when (pasBodyMsg.getMsgId()) {
            MessageID.ServerSetAppParamID -> {
                logD("平台透传指令：收到设置参数透传指令！")
                // 设置计时终端应用参数
                ParameterHelper.saveAppParams(context, (pasBodyMsg.getDataContent() as AppParamData).params)
                MessageHelper.responseCommend(MessageID.ClientSetAppParamAnswerID,
                        AppParamData.AppParamAnswerData(), pasBodyMsg.getIsRealTimeMsg())
            }
            MessageID.ServerTakePhotoID -> {
                logD("平台透传指令：收到立即拍照透传指令！")
                // 立即拍照指令
                val takePhoto = pasBodyMsg.getDataContent() as TakePhotoData
                var isUpload = true
                if (takePhoto.uploadModel == 2) isUpload = false
                pasDataMsg = pasBodyMsg
                context.sendBroadcast(Intent(DTTimerSystemService.ACTION_TAKE_PIC).
                        putExtra(DTTimerSystemService.EXTRA_CAMERA_CONTROL, DTTimerSystemService.CAMERA_TAKE).
                        putExtra(DTTimerSystemService.EXTRA_CAMERA_EVENT_TYPE, 19).
                        putExtra(DTTimerSystemService.EXTRA_CAMERA_PIC_SIZE, takePhoto.picSize).
                        putExtra(DTTimerSystemService.EXTRA_CAMERA_IS_UPLOAD, isUpload))
                // 用于接收拍照状态，通过状态进行立即拍照指令的应答
                val filter = IntentFilter()
                filter.addAction(DTTimerSystemService.ACTION_TAKE_PIC_STATE)
                context.registerReceiver(TakePicReceiver(), filter)
            }
            MessageID.ServerQueryPicID -> {
                logD("平台透传指令：收到查询图片透传指令！")
                // 查询照片指令
                // 先应答计时平台，可以查询照片
                MessageHelper.responseCommend(MessageID.ClientQueryPicAnswerID,
                        QueryPhotoData.QueryAnswerData(), pasBodyMsg.getIsRealTimeMsg())
                // 在数据库获取本地图片信息，并上传查询到的所有图片信息
                val queryPhoto = pasBodyMsg.getDataContent() as QueryPhotoData
                val picArray = mDBOperator.getPictureInfoByDate(
                        SystemHelper.yyMMddHHmmss2TimeStamp(queryPhoto.queryStartTime),
                        SystemHelper.yyMMddHHmmss2TimeStamp(queryPhoto.queryEndTime))
                val queryResult = QueryPhotoData.ReportQueryResultData()
                picArray?.forEach { queryResult.picNumList.add(it.mId) }
                MessageHelper.responseCommend(MessageID.ClientReportPicID, queryResult, pasBodyMsg.getIsRealTimeMsg())
            }
            MessageID.ServerUpAppointPicID -> {
                logD("平台透传指令：收到上传指定照片透传指令！")
                // 上传指定照片指令
                val upAppointPic = pasBodyMsg.getDataContent() as QueryPhotoData.UploadAppointPictureData
                val picModel = mDBOperator.getPictureInfoByNumber(upAppointPic.picNum)
                // 在没有找到图片的情况下
                if (picModel == null) {
                    MessageHelper.responseCommend(MessageID.ClientUpAppointPicAnswerID,
                            QueryPhotoData.UploadAppointAnswerData(1),
                            pasBodyMsg.getIsRealTimeMsg())
                    return
                }
                MessageHelper.responseCommend(MessageID.ClientUpAppointPicAnswerID,
                        QueryPhotoData.UploadAppointAnswerData(0),
                        pasBodyMsg.getIsRealTimeMsg())
                // 上传图片数据
                picModel.eventType = 0
                picModel.uploadModel = 2
                MessageHelper.uploadPicture(context, picModel, true)
            }
            MessageID.ServerSetBanStateID -> {
                logD("平台透传指令：收到设置禁训状态透传指令！")
                // 设置禁训状态指令
                val banTrain = pasBodyMsg.getDataContent() as BanStateData
                Publish.getInstance().mBanState = banTrain.state
                // 报读提示信息
                if (banTrain.hintMsgLen > 0) TTSModule.getInstance(context).speak(banTrain.hintMsgContent)
                // 设置禁训消息应答
                MessageHelper.responseCommend(MessageID.ClientSetBanStateAnswerID,
                        BanStateData.BanStateDataResponse(0),
                        pasBodyMsg.getIsRealTimeMsg())
            }
            MessageID.ServerQueryAppParamID -> {
                logD("平台透传指令：收到查询计时终端应用参数透传指令！")
                // 查询计时终端应用参数
                MessageHelper.responseCommend(MessageID.ClientQueryAppParamAnswerID,
                        ParameterHelper.getQueryAppParamAnswerData(context),
                        pasBodyMsg.getIsRealTimeMsg())
            }
        }
    }

    /**
     * 拍照状态广播接收器
     */
    class TakePicReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            logD("开始应答立即拍照指令")
            if (pasDataMsg == null) return
            val rePasBodyMsg = PasBodyMsg() // 数据上行透传消息体
            rePasBodyMsg.setMsgId(MessageID.ClientTakePhotoAnswerID)
            rePasBodyMsg.setIsRealTimeMsg(pasDataMsg!!.getIsRealTimeMsg())
            rePasBodyMsg.setIsResponse(false)
            val takePicData = pasDataMsg!!.getDataContent() as TakePhotoData
            val takePicResponse = TakePhotoData.TakePhotoAnswerData()
            takePicResponse.picSize = takePicData.picSize
            takePicResponse.uploadModel = takePicData.uploadModel
            takePicResponse.cameraChannel = takePicData.cameraChannel
            // 作立即拍照应答
            takePicResponse.resultCode = intent?.getIntExtra(
                    DTTimerSystemService.EXTRA_TAKE_STATE, 1) ?: 9
            rePasBodyMsg.setDataContent(takePicResponse)
            HeartbeatOperator.getInstance().sendUpLinkResponseMsg(rePasBodyMsg)
            context?.unregisterReceiver(this)
        }
    }
}
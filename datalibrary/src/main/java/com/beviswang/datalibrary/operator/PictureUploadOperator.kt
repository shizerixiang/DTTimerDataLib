package com.beviswang.datalibrary.operator

import android.content.Context
import com.beviswang.datalibrary.Publish
import com.beviswang.datalibrary.logE
import com.beviswang.datalibrary.message.GNSSDataMsg
import com.beviswang.datalibrary.message.PackageMsg
import com.beviswang.datalibrary.message.PasBodyMsg
import com.beviswang.datalibrary.message.RegisterResponseMsg
import com.beviswang.datalibrary.message.passthrough.UploadPictureData
import com.beviswang.datalibrary.model.PhotoModel
import com.beviswang.datalibrary.source.db.DBOperator
import com.beviswang.datalibrary.source.db.IMsgCacheOperator
import com.beviswang.datalibrary.util.*

/**
 * 图片上传模块
 * Created by shize on 2018/2/2.
 */
class PictureUploadOperator(context: Context) : IUploadOperator {
    // 调用数据库用的上下文
    private var mDBOperator: IMsgCacheOperator = DBOperator(context, "localData.db", 1)
    private val mOperator = HeartbeatOperator.getInstance()
    // 照片上传消息包
    private var pMsgList: List<PackageMsg> = ArrayList()
    // 照片信息模型
    var mPhotoModel = PhotoModel()
    /**
     * 发起图片的事件类型 1 byte
     * 定义如下：
     * 0：中心查询的图片
     * 1：紧急报警主动上传的图片
     * 2：关车门后达到指定车速主动上传的图片
     * 3：侧翻报警主动上传的图片
     * 4：上客
     * 5：定时拍照
     * 6：进区域
     * 7：出区域
     * 8：事故疑点（紧急刹车）
     * 9：开车门
     * 17：学员登录拍照
     * 18：学员登出拍照
     * 19：学员培训过程中拍照
     * 20：教练员登录拍照
     * 21：教练员登出拍照
     */
    var eventType = 20
    /**
     * 上传模式 1 byte
     * 1：平台拍照指令后上传
     * 2：平台查询后要求上传
     * 129：终端主动拍照上传
     * 255：停止拍摄并上传图片
     */
    var uploadModel = 129
    /**
     * 图片尺寸 1 byte
     * 0x01：320 X 240
     * 0x02：640 X 480
     * 0x03：800 X 600
     * 0x04：1024 X 768
     * 0x05：176 X 144 Qcif
     * 0x06：352 X 288 Cif
     * 0x07：704 X 288 HALF D1
     * 0x08：704 X 576 D1
     * 注：终端若不支持系统要求的分辨率，则取最接近的分辨率拍摄并上传
     */
    var pictureSize: Int = 0x06
    /**
     * 是否为实时消息
     * 补发消息应设置为 false
     */
    var isRealTime = true

    init {
        Publish.getInstance().mPictureNumber++
        mPhotoModel.mId = MessageHelper.supplementStr(
                10, Publish.getInstance().mPictureNumber.toString(16), 0)
    }

    override fun initUpload(path: String): Boolean {
        mPhotoModel.mImagePath = path
        mPhotoModel.uploadModel = uploadModel
        mPhotoModel.eventType = eventType
        mPhotoModel.pictureSize = pictureSize
        mPhotoModel.mGNSSDataMsg = GNSSDataMsg()
        val additionalInfo = ArrayList<GNSSDataMsg.AdditionalInfo>()
        additionalInfo.add(GNSSDataMsg.AdditionalInfo(0x01))
        additionalInfo.add(GNSSDataMsg.AdditionalInfo(0x05))
        mPhotoModel.mGNSSDataMsg!!.additionalInfo = additionalInfo
        // 根据事件类型做初始化操作
        when (eventType) {
            20 -> {
                mPhotoModel.personnelNum = Publish.getInstance().mCoachInfo?.mNumber ?: throw Exception("教练未成功登录")
            }
            21 -> {
                mPhotoModel.personnelNum = Publish.getInstance().mCoachInfo?.mNumber ?: throw Exception("教练未成功登录")
                Publish.getInstance().mCoachInfo = null
            }
            else -> {
                mPhotoModel.personnelNum = Publish.getInstance().mStudentInfo?.mNum ?: throw Exception("学员未成功登录")
                if (eventType == 18) Publish.getInstance().mStudentInfo = null
            }
        }
        mDBOperator.savePictureInfo(mPhotoModel)
        return true
    }

    override fun upload(): Int {
        // 检测是否有网络
        if (!Publish.getInstance().mIsCarOnLine) return -1
        var result = 0
        val dataContent = getUploadInitData()
        // 获取透传消息
        val pMsg = MessageHelper.getUpstreamMsg(MessageID.ClientUpPicInitID, dataContent,
                true, isRealTime)
        // 发送消息
        if (!mOperator.sendMsg(pMsg)) {
            mOperator.sendOver()
            return -1
        }
        // 接收消息
        try {
            val rePMsg = OperationHelper.handleReceive(mOperator) ?: return -1
            val reMsgBody = rePMsg.getMsgBody() as PasBodyMsg
            val answerData = reMsgBody.getDataContent() as UploadPictureData.UpInitAnswerData
            if (answerData.resultCode != 0) return answerData.resultCode
            if (!sendPicture()) {
                mDBOperator.uploadFailedPictureInfo(mPhotoModel.mId)
                return -1
            }
        } catch (e: Exception) {
            result = -1
            e.printStackTrace()
        } finally {
            // 结束业务消息
            mOperator.sendOver()
        }
        return result
    }

    /** @return 获取上传照片初始化消息数据 */
    private fun getUploadInitData(): UploadPictureData.UpInitData {
        val mPicBytes = FileHelper.file2Byte(mPhotoModel.mImagePath) ?: throw Exception("读取文件失败！")
//        val mPicBytes = BitmapHelper.bitmap2Bytes(BitmapFactory.decodeFile(picPath))
        // 测试用代码
//        debugCode(mPicBytes)
        logE("当前照片 byte 大小：${mPicBytes.size}")
        // 照片上传消息包
        val upDataContent = UploadPictureData()
        upDataContent.pictureNumber = mPhotoModel.mId
        upDataContent.pictureData = mPicBytes
        val pMsg = MessageHelper.getUpstreamMsg(MessageID.ClientUpPicID, upDataContent,
                true, true)
        pMsgList = pMsg.getSubPackages()
        // 上传图片，属性为临时属性
        val dataContent = UploadPictureData.UpInitData()
        dataContent.dataSize = mPicBytes.size
        dataContent.uploadModel = mPhotoModel.uploadModel
        dataContent.eventType = mPhotoModel.eventType
        dataContent.personnelNum = mPhotoModel.personnelNum
        dataContent.pictureNumber = mPhotoModel.mId
        dataContent.mGNSSDataMsg = mPhotoModel.mGNSSDataMsg!!
        dataContent.faceReliability = mPhotoModel.faceReliability
        dataContent.classId = mPhotoModel.classId
        dataContent.packageTotal = pMsgList.size
        return dataContent
    }

    /**
     * 上传图片到服务器
     *
     * @return 上传是否成功
     */
    private fun sendPicture(): Boolean {
        pMsgList.forEach {
            if (!mOperator.sendMsg(it)) return false
//            val rePMsg = mOperator.receiveMsg() ?: throw Exception("接收消息失败！")
//            val cMsg = rePMsg.getMsgBody() as CurrencyMsg
//            if (cMsg.result != 0) return false
            try {
                OperationHelper.handleReceive(mOperator)
            } catch (e: Exception) {
                return false
            }
            Thread.sleep(100)
        }
        // 测试用代码
//        checkSubPackages()
        return true
    }

    private fun debugCode(mPicBytes: ByteArray) {
        logE("*************************************** 图片 byte *****************************************")
        (0 until mPicBytes.size / 1024).forEach {
            logE("图片 byte ：${ConvertHelper.byteArray2HexString(mPicBytes.asList().subList(it * 1024, it * 1024 + 1024).toByteArray())}")
        }
        if (mPicBytes.size % 1024 != 0)
            logE("图片 byte ：${ConvertHelper.byteArray2HexString(mPicBytes.asList().subList(mPicBytes.size / 1024 * 1024, mPicBytes.size).toByteArray())}")
    }

    private fun checkSubPackages() {
        val sendArrayList = ArrayList<ByteArray>()
        pMsgList.forEach {
            sendArrayList.add(it.getSendMsg())
        }
        val byteArray = MessageHelper.composeMsgPackage(sendArrayList)
        val picBytes = byteArray.asList().subList(37, byteArray.size - 256).toByteArray()
        logE("*************************************** 需要存储的图片 byte *****************************************")
        debugCode(picBytes)
        FileHelper.byte2File(picBytes, RegisterResponseMsg.filePath, "takePhoto.jpg")

        logE("*************************************** 分包之后的消息体 *****************************************")
        (0 until byteArray.size / 1024).forEach {
            logE("图片 byte ：${ConvertHelper.byteArray2HexString(byteArray.asList().subList(it * 1024, it * 1024 + 1024).toByteArray())}")
        }
        if (byteArray.size % 1024 != 0)
            logE("图片 byte ：${ConvertHelper.byteArray2HexString(byteArray.asList().subList(byteArray.size / 1024 * 1024, byteArray.size).toByteArray())}")
    }
}
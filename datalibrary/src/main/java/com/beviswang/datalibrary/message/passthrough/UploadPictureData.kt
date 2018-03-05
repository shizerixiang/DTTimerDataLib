package com.beviswang.datalibrary.message.passthrough

import com.beviswang.datalibrary.Publish
import com.beviswang.datalibrary.message.GNSSDataMsg
import com.beviswang.datalibrary.message.PasBodyMsg
import com.beviswang.datalibrary.util.ConvertHelper
import com.beviswang.datalibrary.util.MessageHelper

/**
 * 照片上传透传消息数据实体类
 * Created by shize on 2018/2/2.
 */
class UploadPictureData : PasBodyMsg.DataContent {
    /**
     * 图片编号 10 byte
     * 终端自行编号，仅使用 0-9
     */
    var pictureNumber: String = ""
    /**
     * 照片数据 n byte
     * n = 照片大小
     */
    var pictureData: ByteArray? = null

    override fun getDataByteArray(): ByteArray {
        if (pictureData == null) throw Exception("照片数据不能为空！")
        val byteList = ArrayList<Byte>()
        byteList.addAll(pictureNumber.toByteArray().asList())
        byteList.addAll(pictureData!!.asList())
        return byteList.toByteArray()
    }

    override fun toString(): String {
        return ConvertHelper.byteArray2HexString(getDataByteArray())
    }

    override fun setDataByteArray(byteArray: ByteArray) {}

    /** 照片上传初始化透传消息数据实体类 79 byte */
    class UpInitData : PasBodyMsg.DataContent {
        /**
         * 照片编号 10 byte
         * 终端自行编号，仅使用 0-9
         */
        var pictureNumber: String = ""
        /** 学员或教练员编号 16 byte */
        var personnelNum: String = ""
        /**
         * 上传模式 1 byte
         * 1：平台拍照指令后上传
         * 2：平台查询后要求上传
         * 129：终端主动拍照上传
         * 255：停止拍摄并上传图片
         */
        var uploadModel: Int = 1
        /**
         * 摄像头通道号 1 byte
         * 0：自动
         * 1-255 表示通道号
         */
        var cameraChannelNum: Int = 0
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
        var eventType: Int = 0
        /** 总包数 2 byte */
        var packageTotal: Int = 0
        /** 照片数据大小 4 byte */
        var dataSize: Int = 0
        /** 标识学员的一次培训过程，计时终端自行使用 4 byte */
        var classId: Int = Publish.getInstance().mClassId
        /**
         * 附加 GNSS 数据包 38 byte
         * 照片拍摄时的卫星定位数据，见表 B.21、表 B.24
         * 由位置基本信息 + 位置附加信息项中的里程和发动机转速组成
         */
        var mGNSSDataMsg: GNSSDataMsg = GNSSDataMsg()
        /**
         * 人脸识别置信度 1 byte
         * 0-100，数值越大置信度越高
         */
        var faceReliability: Int = 100

//        init {
//            val additionalInfo = ArrayList<GNSSDataMsg.AdditionalInfo>()
//            additionalInfo.add(GNSSDataMsg.AdditionalInfo(0x01))
//            additionalInfo.add(GNSSDataMsg.AdditionalInfo(0x05))
//            mGNSSDataMsg.additionalInfo = additionalInfo
//        }

        override fun getDataByteArray(): ByteArray {
            return ConvertHelper.string2ByteArray(toString())
        }

        override fun toString(): String {
            return ConvertHelper.byteArray2HexString(pictureNumber.toByteArray()) +
                    ConvertHelper.byteArray2HexString(personnelNum.toByteArray()) +
                    MessageHelper.supplementStr(2, Integer.toHexString(uploadModel), 0) +
                    MessageHelper.supplementStr(2, Integer.toHexString(cameraChannelNum), 0) +
                    MessageHelper.supplementStr(2, Integer.toHexString(pictureSize), 0) +
                    MessageHelper.supplementStr(2, Integer.toHexString(eventType), 0) +
                    MessageHelper.supplementStr(4, Integer.toHexString(packageTotal), 0) +
                    MessageHelper.supplementStr(8, Integer.toHexString(dataSize), 0) +
                    MessageHelper.supplementStr(8, Integer.toHexString(classId), 0) + mGNSSDataMsg.toString() +
                    MessageHelper.supplementStr(2, Integer.toHexString(faceReliability), 0)
        }

        override fun setDataByteArray(byteArray: ByteArray) {}
    }

    /** 照片上传初始化应答透传消息数据实体类 */
    class UpInitAnswerData : PasBodyMsg.DataContent {
        /**
         * 应答代码 1 byte
         * 0：接受上传
         * 1：照片编号重复或错误
         * 9：其他错误
         * 255：拒绝上传，终端收到“拒绝上传”应答后则停止此照片的上传
         */
        var resultCode: Int = 0

        override fun getDataByteArray(): ByteArray {
            return byteArrayOf()
        }

        override fun setDataByteArray(byteArray: ByteArray) {
            resultCode = byteArray[0].toInt()
        }
    }
}
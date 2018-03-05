package com.beviswang.datalibrary.message.passthrough

import com.beviswang.datalibrary.message.PasBodyMsg
import com.beviswang.datalibrary.util.ConvertHelper
import com.beviswang.datalibrary.util.MessageHelper

/**
 * 立即拍照消息数据实体类
 * Created by shize on 2018/2/7.
 */
class TakePhotoData : PasBodyMsg.DataContent {
    /**
     * 上传模式 1 byte
     * 1：拍摄完成后自动请求上传
     * 2：拍摄完成后存储在本地
     * 255：停止拍摄并上传图片
     */
    var uploadModel: Int = 1
    /**
     * 摄像头通道号 1 byte
     * 0：自动
     * 1-255：表示通道号
     */
    var cameraChannel: Int = 0
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
    var picSize: Int = 0x06

    override fun getDataByteArray(): ByteArray = byteArrayOf()

    override fun setDataByteArray(byteArray: ByteArray) {
        uploadModel = byteArray[0].toInt()
        cameraChannel = byteArray[1].toInt()
        picSize = byteArray[2].toInt()
    }

    /** 立即拍照应答消息数据实体类 */
    class TakePhotoAnswerData : PasBodyMsg.DataContent {
        /**
         * 执行结果 1 byte
         * 1：可以拍摄
         * 2：拍照失败
         * 3：SD 卡故障
         * 4：正在拍照，不能执行
         * 5：重新连接摄像头，不能保证拍照
         * 6：正在上传查询照片，不能执行
         * 9：其他错误
         */
        var resultCode: Int = 1
        /**
         * 上传模式，与下行命令保持一致 1 byte
         * 1：拍摄完成后自动请求上传
         * 2：拍摄完成后存储在本地
         * 255：停止拍摄并上传图片
         */
        var uploadModel: Int = 1
        /**
         * 实际拍摄的通道号 1 byte
         */
        var cameraChannel: Int = 0
        /**
         * 图片实际尺寸 1 byte
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
        var picSize: Int = 0x06

        override fun getDataByteArray(): ByteArray {
            return ConvertHelper.string2ByteArray(toString())
        }

        override fun toString(): String {
            return MessageHelper.supplementStr(2, Integer.toHexString(resultCode), 0) +
                    MessageHelper.supplementStr(2, Integer.toHexString(uploadModel), 0) +
                    MessageHelper.supplementStr(2, Integer.toHexString(cameraChannel), 0) +
                    MessageHelper.supplementStr(2, Integer.toHexString(picSize), 0)
        }

        override fun setDataByteArray(byteArray: ByteArray) {}
    }
}
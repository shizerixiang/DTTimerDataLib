package com.beviswang.datalibrary.model

import com.beviswang.datalibrary.Publish
import com.beviswang.datalibrary.message.GNSSDataMsg

/**
 * 图片上传消息本地缓存模型
 * Created by shize on 2018/2/8.
 */
class PhotoModel {
    var mId: String = "0000000001"                   // 照片编号
    var mImagePath: String = ""                      // 照片路径
    var mTime: Long = 0L                             // 拍摄的时间，时间戳，单位 s

    var personnelNum: String = ""                    // 教练或学员编号
    var pictureSize: Int = 0x06                      // 照片尺寸
    var cameraChannelNum: Int = 0                    // 摄像头通道号
    var uploadModel: Int = 0                         // 上传模式
    var eventType: Int = 0                           // 事件类型
    var classId: Int = Publish.getInstance().mClassId// 课堂 ID
    var mGNSSDataMsg: GNSSDataMsg? = null            // 位置数据
    var faceReliability: Int = 100                   // 人脸识别置信度
}
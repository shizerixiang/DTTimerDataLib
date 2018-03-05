package com.beviswang.datalibrary.model

/**
 * 培训信息模型
 * Created by shize on 2017/12/26.
 */
class TrainInfoModel {
    var id:Int = 0              // 编号
    var type:String = ""        // 类型
    var vehType:String = ""     // 驾照类型
    var recordTime:String = ""  // 记录时间
    var hoursId:String = ""     //
    var picId:String = ""       //
    var coachNum:String = ""    // 教练编号
    var cardNum:String = ""     // IC 卡编号
    var stuNum:String = ""      // 学员编号
    var courseId:String = ""    // 课程编号
    var classId:String = ""     //
    var recordStatus:String = ""// 记录状态
    var maxSpeed:Double = 0.0   // 最大速度
    var subMile:Double = 0.0    // 科目里程
    var allTime:Int = 0         // 总时间
    var allMile:Double = 0.0    // 总里程
    var picEvent:String = ""    //
    var photo:String = ""       // 保存的图像路径
    var GPSData:String = ""     // GPS 数据
    var status:String = ""      // 状态
    var msgId:String = ""       // 消息编号

    companion object {
        val ID = "id"
        val TYPE = "type"
        val VEH_TYPE = "vehType"
        val RECORD_TIME = "recordTime"
        val HOURS_ID = "hoursId"
        val PIC_ID = "picId"
        val COACH_NUM = "coachNum"
        val CARD_NUM = "cardNum"
        val STU_NUM = "stuNum"
        val COURSE_ID = "courseId"
        val CLASS_ID = "classId"
        val RECORD_STATUS = "recordStatus"
        val MAX_SPEED = "maxSpeed"
        val SUB_MILE = "subMile"
        val ALL_TIME = "allTime"
        val ALL_MILE = "allMile"
        val PIC_EVENT = "picEvent"
        val PHOTO = "photo"
        val GPS_DATA = "GPSData"
        val STATUS = "status"
        val MSG_ID = "msgId"
    }
}
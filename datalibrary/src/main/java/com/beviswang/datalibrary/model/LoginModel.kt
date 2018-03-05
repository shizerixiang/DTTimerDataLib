package com.beviswang.datalibrary.model

/**
 * 登录信息模型
 * Created by shize on 2018/1/15.
 */
class LoginModel {
    // ************************************ 教练登录数据 ************************************
    var coachNum:String = "8041831254662415"
    var coachName:String = "教练A"
    var coachID:String = "130682198909202430"
    var teachCarType:String = "C1"

    // ************************************ 请求数据 ************************************
    var stuNum:String = "9134677538435566"
    var stuName:String = "学员A"
    var curClass:String = "1513217339"
    /** 总培训学时，单位 min 2 byte */
    var trainHours: Int = 0
    /** 当前培训部分已完成学时，单位 min 2 byte */
    var curHours: Int = 0
    /** 总培训里程，单位 1/10km 2 byte */
    var trainMileage: Int = 0
    /** 当前培训部分已完成里程，单位 1/10km 2 byte */
    var curMileage: Int = 0
    /** 是否报读附加消息，0：不必报读；1：需要报读 1 byte */
    var isSpeakAddMsg: Int = 0
    /** 附加消息 最大 254 byte */
    var additionalMsg: String = ""
}
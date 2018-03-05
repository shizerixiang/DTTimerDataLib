package com.beviswang.datalibrary.model

/**
 * 教练信息模型
 * Created by shize on 2017/12/20.
 */
class CoachModel {
    // 教练身份证号
    var mId:String = ""

    // 教练编号
    var mNumber:String = ""

    // 教练姓名
    var mName:String = ""

    // 头像地址
    var mImg:String = ""

    // 教练星级
    var mStars:Float = 0f

    // 准教车型
    var mCarType:String = ""

    // 签到时长 min
    var mSignDuration:Int = 0

    // 总培训时长 min
    var mTotalTrainTime:Int = 0
}
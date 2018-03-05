package com.beviswang.datalibrary.model

import com.beviswang.datalibrary.Publish

/**
 * 学员信息模型
 * Created by shize on 2017/12/20.
 */
class StudentModel {
    // 学员编号
    var mNum:String = ""

    // 学员姓名
    var mName:String = ""

    // 头像地址
    var mImg:String = ""

    // 申领车型
    var mCarType:String = ""

    // 当前培训课程
    var mCurCourse:String = Publish.getInstance().mCurCourse

    // 本教学部分培训时长 min
    var mCurDuration:Int = 0

    // 本教学部分培训里程 km
    var mCurDistance:Float = 0f

    // 总培训时长 min
    var mTotalDuration:Int = 0

    // 总培训里程 km
    var mTotalDistance:Float = 0F

    // 当前培训状态
    var mCurState:String = "正在培训"

    // 当前课程总时长 s
    var mCurCourseTotalDuration:Int = 0

    // 当前课程总里程 km
    var mCurCourseTotalDistance:Float = 0F
}
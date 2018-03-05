package com.beviswang.datalibrary.model

import com.beviswang.datalibrary.Publish
import com.beviswang.datalibrary.util.SystemHelper

/**
 * 照片文字模型
 * Created by shize on 2017/12/29.
 */
class PhotoTextModel {
    var mMechanismName:String = "培训机构名称：${Publish.getInstance().mMechanismName}" // 驾驶培训机构名称
        set(value) {
            field = "培训机构名称：$value"
        }
    var mCoachName:String = "教练员姓名：王某" // 教练员姓名
        set(value) {
            field = "教练员姓名：$value"
        }
    var mStudentName:String = "学员姓名：李某" // 学员姓名
        set(value) {
            field = "学员姓名：$value"
        }
    var mPosition:String = "经纬度：117.329439E-31.837548N" // 经纬度
        set(value) {
            field = "经纬度：$value"
        }
    var mCarNo:String = "车辆号牌：${Publish.getInstance().mCarNo}" // 车辆号牌
        set(value) {
            field = "车辆号牌：$value"
        }
    var mDateTime:String = "采集时间：${SystemHelper.systemDate} ${SystemHelper.systemTime}" // 采集时间
        set(value) {
            field = "采集时间：$value"
        }
    var mSpeed:String = "行驶速度：0.00 km/h" // 行驶速度
        set(value) {
            field ="行驶速度：$value"
        }
}
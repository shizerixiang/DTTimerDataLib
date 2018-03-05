package com.beviswang.datalibrary.model

import com.beviswang.datalibrary.message.PackageMsg

/**
 * 注册应答信息模型
 * Created by shize on 2018/1/15.
 */
class RegisterModel {
    // ************************************ 注册信息 ************************************
    var mMessage:PackageMsg = PackageMsg() // 注册消息

    // ************************************ 用户输入 ************************************
    var mPlatFormIP:String = "114.215.173.239" // 计时平台 IP
    var mPlatFormPort:Int = 9001 // 计时平台端口
    var mCarNum:String = "皖A0958学" // 车牌号 -> 车辆标识
    var mPhoneNum:String = "13515666505" // 手机号
}
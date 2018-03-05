package com.beviswang.datalibrary.util

/**
 * 消息 ID
 * Created by shize on 2018/1/12.
 */
object MessageID {
    // *********************************** 计时终端 ***********************************
    val ClientAnswerID = "0001"                 // 客户端通用应答消息 ID
    val ClientHeartbeatID = "0002"              // 客户端心跳消息 ID
    val ClientLogOffID = "0003"                 // 终端注销消息 ID （终端拆除）
    val ClientRegisterID = "0100"               // 终端注册消息 ID
    val ClientLoginID = "0102"                  // 终端鉴权消息 ID
    val ClientParamAnswerID = "0104"            // 查询终端参数应答 ID
    val ClientLocationID = "0200"               // 位置信息汇报 ID
    val ClientAnswerLocationID = "0201"         // 位置信息查询应答 ID
    val ClientUpstreamID = "0900"               // 数据上行透传 ID
    // *********************************** 计时平台 ***********************************
    val ServerAnswerID = "8001"                 // 服务器通用应答消息 ID
    val ServerSupplementSubpackageID = "8003"   // 补传分包请求消息 ID
    val ServerRegisterAnswerID = "8100"         // 终端注册应答消息 ID
    val ServerSetParamID = "8103"               // 设置终端参数消息 ID
    val ServerQueryAllParamID = "8104"          // 查询所有终端参数 ID
    val ServerClientControlID = "8105"          // 终端控制 ID
    val ServerQueryAppointParamID = "8106"      // 查询指定终端参数 ID
    val ServerQueryLocationID = "8201"          // 位置信息查询 ID
    val ServerTrackLocationID = "8202"          // 临时位置跟踪控制 ID
    val ServerDownLinkID = "8900"               // 数据下行透传 ID
    // *********************************** 上行透传 ***********************************
    val ClientCoachLoginID = "0101"             // 上报教练员登录 ID
    val ClientCoachLogoutID = "0102"            // 上报教练员登出 ID
    val ClientStudentLoginID = "0201"           // 上报学员登录 ID
    val ClientStudentLogoutID = "0202"          // 上报学员登出 ID
    val ClientHoursRecodeID = "0203"            // 上报学时记录 ID
    val ClientReportAnswerID = "0205"           // 命令上报学时记录应答 ID
    val ClientTakePhotoAnswerID = "0301"        // 立即拍照应答 ID
    val ClientQueryPicAnswerID = "0302"         // 查询照片应答 ID
    val ClientReportPicID = "0303"              // 上报照片查询结果 ID
    val ClientUpAppointPicAnswerID = "0304"     // 上传指定照片应答 ID
    val ClientUpPicInitID = "0305"              // 照片上传初始化 ID
    val ClientUpPicID = "0306"                  // 上传照片数据包 ID
    val ClientSetAppParamAnswerID = "0501"      // 设置计时终端应用参数应答 ID
    val ClientSetBanStateAnswerID = "0502"      // 设置禁训状态应答 ID
    val ClientQueryAppParamAnswerID = "0503"    // 查询计时终端应用参数应答 ID
    val ClientRequestIdentityID = "0401"        // 请求身份认证信息 ID
    val ClientRequestNumberID = "0402"          // 请求统一编号信息 ID
    val ClientReportCarInfoID = "0403"          // 上报车辆绑定信息 ID
    // *********************************** 下行透传 ***********************************
    val ServerCoachLoginAnswerID = "8101"       // 教练员登录应答 ID
    val ServerCoachLogoutAnswerID = "8102"      // 教练员登出应答 ID
    val ServerStudentLoginID = "8201"           // 学员登录应答 ID
    val ServerStudentLogoutID = "8202"          // 学员登出应答 ID
    val ServerReportHoursID = "8205"            // 命令上报学时记录 ID
    val ServerTakePhotoID = "8301"              // 立即拍照 ID
    val ServerQueryPicID = "8302"               // 查询照片 ID
    val ServerReportPicAnswerID = "8303"        // 上报照片查询结果应答 ID
    val ServerUpAppointPicID = "8304"           // 上传指定照片 ID
    val ServerUpPicInitAnswerID = "8305"        // 照片上传初始化应答 ID
    val ServerSetAppParamID = "8501"            // 设置计时终端应用参数 ID
    val ServerSetBanStateID = "8502"            // 设置禁训状态 ID
    val ServerQueryAppParamID = "8503"          // 查询计时终端应用参数 ID
    val ServerResponseIdentityID = "8401"       // 请求身份认证信息应答 ID
    val ServerResponseNumberID = "8402"         // 请求统一编号信息应答 ID
    val ServerReportCarInfoAnswerID = "8403"    // 上报车辆绑定信息应答 ID
}
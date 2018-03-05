package com.beviswang.datalibrary.util

import android.annotation.SuppressLint
import android.content.Context
import com.beviswang.datalibrary.R
import com.beviswang.datalibrary.logE
import com.beviswang.datalibrary.message.ParamMsg
import com.beviswang.datalibrary.message.PasBodyMsg
import com.beviswang.datalibrary.message.passthrough.QueryAppParamData

/**
 * 参数设置工具类
 * Created by shize on 2018/1/23.
 */
object ParameterHelper {
    /** 客户端心跳发送间隔，单位为秒(s) 4 byte */
    val HEARTBEAT_DURATION_ID = 0x0001
    /** TCP 消息应答超时时间，单位为妙(s) 4 byte */
    val TCP_RESPONSE_TIMEOUT_ID = 0x0002
    /** TCP 消息重传次数 4 byte */
    val TCP_RETRANSMISSION_ID = 0x0003
    /** UDP 消息应答超时时间，单位为妙(s) 4 byte */
    val UDP_RESPONSE_TIMEOUT_ID = 0x0004
    /** UDP 消息重传次数 4 byte */
    val UDP_RETRANSMISSION_ID = 0x0005
    /** SMS 消息应答超时时间，单位为妙(s) 4 byte */
    val SMS_RESPONSE_TIMEOUT_ID = 0x0006
    /** SMS 消息重传次数 4 byte */
    val SMS_RETRANSMISSION_ID = 0x0007

    /** 主服务器 APN ，无线通信拨号访问点。若网络制式为 CDMA ，则该处为 PPP 拨号号码 String */
    val SERVER_APN_ID = 0x0010
    /** 主服务器无线通信拨号用户名 String */
    val SERVER_USER_NAME_ID = 0x0011
    /** 主服务器无线通信拨号密码 String */
    val SERVER_PASSWORD_ID = 0x0012
    /** 主服务器地址，IP 或域名 String */
    val SERVER_IP_ID = 0x0013
    /** 备份服务器 APN ，无线通信拨号访问点 String */
    val BACKUPS_SERVER_APN_ID = 0x0014
    /** 备份服务器无线通信拨号用户名 String */
    val BACKUPS_SERVER_USER_NAME_ID = 0x0015
    /** 备份服务器无线通信拨号密码 String */
    val BACKUPS_SERVER_PASSWORD_ID = 0x0016
    /** 备份服务器地址，IP 或域名 String */
    val BACKUPS_SERVER_IP_ID = 0x0017
    /** 服务器 TCP 端口 4 byte */
    val SERVER_PORT_TCP_ID = 0x0018
    /** 服务器 UDP 端口 4 byte */
    val SERVER_PORT_UDP_ID = 0x0019

    /** 位置汇报策略，0：定时汇报；1：定距汇报；2：定时和定距汇报 4 byte */
    val LOCATION_POLICY_ID = 0x0020
    /** 位置汇报方案，0：根据 ACC 状态；1：根据登录状态和 ACC 状态，先判断登录状态，若登录再根据 ACC 状态 4 byte */
    val LOCATION_SCHEME_ID = 0x0021
    /** 驾驶员未登录汇报时间间隔，单位为秒(s)，>0 4 byte */
    val NOT_REPORTED_TIME_DURATION_ID = 0x0022

    /** 休眠时汇报时间间隔，单位为秒(s)，>0 4 byte */
    val DORMANCY_TIME_DURATION_ID = 0x0027
    /** 紧急报警时汇报时间间隔，单位为秒(s)，>0 4 byte */
    val ALARM_TIME_DURATION_ID = 0x0028
    /** 缺省时间汇报间隔( 即：位置汇报间隔 )，单位为秒(s)，>0 4 byte */
    val DEFAULT_TIME_DURATION_ID = 0x0029

    /** 缺省距离汇报间隔，单位为米(m)，>0 4 byte */
    val DEFAULT_DISTANCE_DURATION_ID = 0x002c
    /** 驾驶员未登录汇报距离间隔，单位为米(m)，>0 4 byte */
    val NOT_REPORTED_DISTANCE_DURATION_ID = 0x002d
    /** 休眠时汇报距离间隔，单位为米(m)，>0 4 byte */
    val DORMANCY_DISTANCE_DURATION_ID = 0x002e
    /** 紧急报警时汇报距离间隔，单位为米(m)，>0 4 byte */
    val ALARM_DISTANCE_DURATION_ID = 0x002f
    /** 拐点补传角度，<180° 4 byte */
    val INFLEXION_SUPPLEMENTS_ANGLE_ID = 0x0030

    /** 监控平台电话号码 String */
    val MONITOR_PLATFORM_PHONE_ID = 0x0040
    /** 复位电话号码，可采用此电话号码拨打终端电话让终端复位 String */
    val RESET_PHONE_ID = 0x0041
    /** 恢复出厂设置电话号码，可采用此电话号码拨打终端电话让终端恢复出厂设置 String */
    val RESTORE_FACTORY_SETTINGS_PHONE_ID = 0x0042
    /** 监控平台 SMS 电话号码 String */
    val MONITOR_PLATFORM_SMS_PHONE_ID = 0x0043
    /** 接收终端 SMS 文本报警号码 String */
    val TEXT_ALARM_SMS_PHONE_ID = 0x0044
    /** 终端电话接听策略，0：自动接听；1：ACC ON 时自动接听，OFF 时手动接听 4 byte */
    val ANSWERING_STRATEGY_ID = 0x0045
    /** 每次最长通话时间，单位为秒(s)，0 为不允许通话，0xFFFFFFFF 为不限制 4 byte */
    val ONCE_LONGEST_CALL_TIME_ID = 0x0046
    /** 当月最长通话时间，单位为秒(s)，0 为不允许通话，0xFFFFFFFF 为不限制 4 byte */
    val MONTHLY_LONGEST_CALL_TIME_ID = 0x0047
    /** 监听电话号码 String */
    val MONITOR_PHONE_ID = 0x0048
    /** 监管平台特权短信号码 String */
    val MONITOR_PLATFORM_MSG_PHONE_ID = 0x0049

    /** 报警屏蔽字。与位置信息汇报消息中的报警标识相对应，相应位为 1 则相应报警被屏蔽 4 byte */
    val ALARM_SHIELD_WORD_ID = 0x0050
    /** 报警发送文本 SMS 开关，与位置信息汇报消息中的报警标识相对应，相应位为 1 则相应报警时发送文本 SMS 4 byte */
    val ALARM_SEND_TEXT_SWITCH_ID = 0x0051
    /** 报警拍摄开关，与位置信息汇报消息中的报警标识相对应，相应位为 1 则相应报警时摄像头拍摄 4 byte */
    val ALARM_SHOOTING_SWITCH_ID = 0x0052
    /** 报警拍摄存储标识，与位置信息汇消息中的报警标识相对应，相应位为 1 则相应报警时牌的照片进行存储，否则实时长传 4 byte */
    val ALARM_SHOOTING_FLAG_ID = 0x0053
    /** 关键标识，与位置信息汇报消息中的报警标识相对应，相应位为 1 则对相应报警为关键报警 4 byte */
    val KEY_FLAG_ID = 0x0054
    /** 最高速度，单位为公里每小时(km/h) 4 byte */
    val TOP_SPEED_ID = 0x0055
    /** 超速持续时间，单位为秒(s) 4 byte */
    val SPEEDING_DURATION_ID = 0x0056
    /** 连续驾驶时间门限，单位为秒(s) 4 byte */
    val CONTINUOUS_DRIVING_LIMIT_ID = 0x0057
    /** 当天累计驾驶时间门限，单位为秒(s) 4 byte */
    val DAY_TOTAL_TIME_LIMIT_ID = 0x0058
    /** 最小休息时间，单位为秒(s) 4 byte */
    val MINIMUM_REST_TIME_ID = 0x0059
    /** 最长停车时间，单位为秒(s) 4 byte */
    val LONGEST_PARKING_TIME_ID = 0x005a

    /** 图像/视频质量，1-10，1 最好 4 byte */
    val IMAGE_QUALITY_ID = 0x0070
    /** 亮度，0-255 4 byte */
    val BRIGHTNESS_ID = 0x0071
    /** 对比度，0-127 4 byte */
    val CONTRAST_RATIO_ID = 0x0072
    /** 饱和度，0-127 4 byte */
    val SATURATION_ID = 0x0073
    /** 色度，0-255 4 byte */
    val CHROMA_ID = 0x0074

    /** 车辆里程表读数，1/10km 4 byte */
    val ODOMETER_VALUE_ID = 0x0080
    /** 车辆所在的省域 ID 2 byte */
    val PROVINCIAL_REGION_ID = 0x0081
    /** 车辆所在的市域 ID 2 byte */
    val CITY_DOMAIN_ID = 0x0082
    /** 公安交通管理部门颁发的机动车号牌 String */
    val VEHICLE_BRAND_NUMBER_ID = 0x0083
    /** 车牌颜色，按照 JT/T415-2006 的 5.4.12 1 byte */
    val VEHICLE_BRAND_COLOR_ID = 0x0084
    /** 车辆脉冲系数，车辆行驶 1km 距离过程中产生的脉冲信号个数 4 byte */
    val PULSE_COEFFICIENT_ID = 0x0085

    // ********************************* 终端应用参数 *********************************
    /**
     * 定时拍照时间间隔 1 byte
     * 单位：min，默认值 15
     * 在学员登录后间隔固定时间拍摄照片
     */
    val APP_TAKE_PHOTO_DURATION = 0x1002
    /**
     * 照片上传设置 1 byte
     * 0：不自动请求上传
     * 1：自动请求上传
     */
    val APP_IS_AUTO_REQUEST_UP = 0x1003
    /**
     * 是否报读附加消息 1 byte
     * 1：自动报读
     * 2：不报读
     * 控制是否报读消息中的附加消息，如果下行消息中指定了是否报读，则遵循该消息的设置执行
     */
    val APP_IS_READ_MSG = 0x1004
    /**
     * 熄火后停止学时计时的延时时间 1 byte
     * 单位：min
     */
    val APP_FLAMEOUT_DELAYED_TIME = 0x1005
    /**
     * 熄火后 GNSS 数据包上传间隔 2 byte
     * 单位：s，默认值 3600，0 表示不上传
     */
    val APP_UP_GNSS_DATA_DURATION = 0x1006
    /**
     * 熄火后教练自动登出的延时时间 2 byte
     * 单位：min，默认值 150
     */
    val APP_FLAMEOUT_COACH_AUTO_OUT = 0x1007
    /**
     * 重新验证身份时间 2 byte
     * 单位：min，默认值 30
     */
    val APP_RE_VERIFICATION_TIME = 0x1008
    /**
     * 教练跨校教学 1 byte
     * 1：允许  2：禁止
     * 默认值 2 禁止
     */
    val APP_CAN_COACH_CROSS_TEACH = 0x1009
    /**
     * 学员跨校学习 1 byte
     * 1：允许  2：禁止
     * 默认值 1 允许
     */
    val APP_CAN_STU_CROSS_STUDY = 0x100a
    /**
     * 响应平台同类消息时间间隔 2 byte
     * 单位：s，在该时间间隔内对平台发送的多次相同 ID 消息可拒绝执行回复失败
     */
    val APP_RESPONSE_DURATION = 0x100b

    /**
     * 保存平台设置的参数
     *
     * @param context 上下文
     * @param params 需要保存的参数集合
     */
    @SuppressLint("ApplySharedPref")
    fun saveParams(context: Context, params: List<ParamMsg.ParamItem>) {
        val sp = context.getSharedPreferences(context.getString(R.string.str_parameter), Context.MODE_PRIVATE)
        val editor = sp.edit()
        params.forEach { editor.putString(it.paramId.toString(), it.paramValue) }
        editor.commit()
    }

    /**
     * 保存终端应用参数
     *
     * @param context 上下文
     * @param params 参数集合
     */
    @SuppressLint("ApplySharedPref")
    fun saveAppParams(context: Context, params: List<Pair<Int, String>>) {
        val sp = context.getSharedPreferences(context.getString(R.string.str_parameter), Context.MODE_PRIVATE)
        val editor = sp.edit()
        params.forEach { editor.putString(it.first.toString(), it.second) }
        editor.commit()
    }

    /**
     * 通过参数 ID 集合，读取参数对象
     *
     * @param context 上下文
     * @param paramIdList 参数 ID 集合，为 null 则获取所有参数
     * @return 参数对象集合
     */
    fun readParams(context: Context, paramIdList: List<Int>?): ArrayList<ParamMsg.ParamItem> {
        val paramItemList = ArrayList<ParamMsg.ParamItem>()
        val sp = context.getSharedPreferences(context.getString(R.string.str_parameter), Context.MODE_PRIVATE)
        if (paramIdList == null) {
            sp.all.forEach {
                val pItem = ParamMsg.ParamItem()
                pItem.paramId = it.key.toInt()
                pItem.paramValue = it.value as String
                paramItemList.add(pItem)
            }
            paramItemList.sortBy { it.paramId }
            paramItemList.forEach {
                logE("消息ID：${it.paramId} -> 消息内容：${it.paramValue}")
            }
            return paramItemList
        }
        paramIdList.forEach {
            val pItem = ParamMsg.ParamItem()
            pItem.paramId = it
            pItem.paramValue = sp.getString(it.toString(), "0")
            paramItemList.add(pItem)
        }
        return paramItemList
    }

    /**
     * 通过参数 ID 读取单个参数值
     *
     * @param context 上下文
     * @param paramId 参数 ID
     * @param def 默认值
     * @return 单个参数的值（16 进制数据，根据情况转换）
     */
    fun readParam(context: Context, paramId: Int, def: String): String {
        val sp = context.getSharedPreferences(context.getString(R.string.str_parameter), Context.MODE_PRIVATE)
        return sp.getString(paramId.toString(), def)
    }

    /**  @return 获取心跳间隔，默认 30 秒 */
    fun getHeartbeatDuration(context: Context): Long {
        val duration = readParam(context, HEARTBEAT_DURATION_ID, "1e")
        return Integer.parseInt(duration, 16) * 1000L
    }

    /** @return 获取位置汇报间隔，默认 30 秒 */
    fun getLocationDuration(context: Context): Long {
        val duration = readParam(context, DEFAULT_TIME_DURATION_ID, "1e")
        return Integer.parseInt(duration, 16) * 1000L
    }

    /** @return 获取拍照间隔，默认 15 分钟 */
    fun getTakePhotoDuration(context: Context): Int {
        val duration = readParam(context, APP_TAKE_PHOTO_DURATION, "f")
        return Integer.parseInt(duration, 16)
    }

    /** @return 获取查询计时终端应用参数消息数据 */
    fun getQueryAppParamAnswerData(context: Context): QueryAppParamData.QueryAnswerData {
        val queryAnswer = QueryAppParamData.QueryAnswerData()
        queryAnswer.result = 0
        queryAnswer.timingInterval = getTakePhotoDuration(context)
        queryAnswer.uploadSetting = readParam(context, APP_IS_AUTO_REQUEST_UP, "0").toInt(16)
        queryAnswer.isSpeakAddMsg = readParam(context, APP_IS_READ_MSG, "1").toInt(16)
        queryAnswer.flameoutDelayed = readParam(context, APP_FLAMEOUT_DELAYED_TIME, "0").toInt(16)
        queryAnswer.upGNSSDataDuration = readParam(context, APP_UP_GNSS_DATA_DURATION, "e10").toLong(16)
        queryAnswer.flameoutCoachAutoOut = readParam(context, APP_FLAMEOUT_COACH_AUTO_OUT, "96").toInt(16)
        queryAnswer.reVerificationTime = readParam(context, APP_RE_VERIFICATION_TIME, "1e").toInt(16)
        queryAnswer.canCoachCrossTeach = readParam(context, APP_CAN_COACH_CROSS_TEACH, "2").toInt(16)
        queryAnswer.canStuCrossStudy = readParam(context, APP_CAN_STU_CROSS_STUDY, "2").toInt(16)
        queryAnswer.responsePlatformDuration = readParam(context, APP_RESPONSE_DURATION, "0").toLong(16)
        return queryAnswer
    }
}
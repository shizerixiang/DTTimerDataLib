package com.beviswang.datalibrary.message

import com.beviswang.datalibrary.util.ConvertHelper

/**
 * 临时位置跟踪控制
 * 在位置跟踪有效期内，按时间间隔持续发送位置信息汇报
 * Created by shize on 2018/2/7.
 */
class TrackLocationMsg : PackageMsg.MsgBody {
    /**
     * 时间间隔 2 byte
     * 单位为 s，0 则停止跟踪
     * 停止跟踪无需带后继字段
     */
    var duration: Int = 0
    /**
     * 位置跟踪有效期 4 byte
     * 单位为 s，终端在接收到位置跟踪控制消息后，
     * 在有效期截止时间之前，依据消息中的时间间隔发送位置汇报
     */
    var cycle: Long = 0L

    override fun setBodyBytes(byteArray: ByteArray) {
        val byteList = byteArray.asList()
        duration = Integer.parseInt(ConvertHelper.byteArray2HexString(
                byteList.subList(0, 2).toByteArray()), 16)
        cycle = java.lang.Long.parseLong(ConvertHelper.byteArray2HexString(
                byteList.subList(2, 6).toByteArray()), 16)
    }

    override fun getBodyArray(): ByteArray = byteArrayOf()
}
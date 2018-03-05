package com.beviswang.datalibrary.message

import com.beviswang.datalibrary.util.ConvertHelper
import com.beviswang.datalibrary.util.MessageHelper
import java.nio.charset.Charset

/**
 * 注册请求消息体
 * Created by shize on 2018/1/16.
 */
class RegisterRequestMsg : PackageMsg.MsgBody {
    // 省域 ID 2 byte 000b
    var provinceId: Int = 11
    // 市县域 ID 2 byte 006c
    var cityId: Int = 108
    // 制造商 ID 5 byte 3938313854
    var manufacturerId: String = "9818T"
    // 终端型号 20 byte 54532d3130324100000000000000000000000000
    var terminalModel: String = "TS-102A"
    // 终端序列号 7 byte 30303032353438
    var serialNum: String = "0002548"
    /**
     * IMEI 15 byte 393631333632323330353030303530
     * 移动终端唯一标识符
     */
    var terminalIMEI: String = "961362230500050"
    /**
     * 车牌颜色 1 byte 01
     * 指车辆牌照的颜色/底色
     * 1 代表蓝色；2 代表黄色；3 代表黑色；4 代表白色；9 代表其他
     */
    var carColor: Int = 1
    /**
     * 车辆标识 无限制 byte cdee4130393538d1a7
     * 也就是车牌号
     */
    var carIdentification: String = "cdee4130393538d1a7"

    override fun getBodyArray(): ByteArray {
        val provinceIdStr = MessageHelper.supplementStr(4,
                Integer.toHexString(provinceId), 0)
        val cityIdStr: String = MessageHelper.supplementStr(4,
                Integer.toHexString(cityId), 0)
        val manufacturerIdStr = ConvertHelper.stringGBK2HexString(manufacturerId)
        val terminalModelStr = MessageHelper.supplementStr(40,
                ConvertHelper.stringGBK2HexString(terminalModel), 1)
        val serialNumStr = MessageHelper.supplementStr(14,
                ConvertHelper.stringGBK2HexString(serialNum), 1)
        val terminalIMEIStr = ConvertHelper.stringGBK2HexString(terminalIMEI)
        val carColorStr = ConvertHelper.byteArray2HexString(byteArrayOf(carColor.toByte()))
        val carIdentificationStr = ConvertHelper.byteArray2HexString(carIdentification.toByteArray(Charset.forName("GBK")))
        return ConvertHelper.string2ByteArray(provinceIdStr + cityIdStr +
                manufacturerIdStr + terminalModelStr + serialNumStr + terminalIMEIStr +
                carColorStr + carIdentificationStr)
    }

    override fun setBodyBytes(byteArray: ByteArray) {}
}
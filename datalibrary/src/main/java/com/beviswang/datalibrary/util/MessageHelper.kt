package com.beviswang.datalibrary.util

import android.annotation.SuppressLint
import android.content.Context
import com.beviswang.datalibrary.Publish
import com.beviswang.datalibrary.R
import com.beviswang.datalibrary.logD
import com.beviswang.datalibrary.logI
import com.beviswang.datalibrary.message.*
import com.beviswang.datalibrary.message.PasBodyMsg
import com.beviswang.datalibrary.model.PhotoModel
import com.beviswang.datalibrary.operator.HeartbeatOperator
import com.beviswang.datalibrary.operator.PictureUploadOperator

/**
 * 消息追加工具类
 * 消息结构：标识位 + 消息头 + 消息体 + 校验码 + 标识位
 * 标识位：采用 0x7e 表示，若其他部分出现 0x7e 则需要进行转义
 * 转义定义：0x7e <--> 0x7d 后紧跟一个 0x02
 *           0x7d <--> 0x7d 后紧跟一个 0x01
 * Created by shize on 2018/1/10.
 */
object MessageHelper {
    /**
     * 转义参数
     */
    private val IDENTIFICATION_BIT = "7e" // 固定的标识位
    private val CONVERT_BYTE_7D = "7d" // 7b 需要转义的 byte
    private val CONVERT_BYTE_7D_OVER = "7d01" // 7b 转义后的 byte
    private val CONVERT_BYTE_7E = "7e" // 7e 需要转义的 byte
    private val CONVERT_BYTE_7E_OVER = "7d02" // 7e 转义后的 byte
    /**
     * 消息头必要固定参数
     */
    val PROTOCOL_VERSION = "80" // 协议版本号：80 是 16 进制
    var TERMINAL_PHONE_NUM = Publish.getInstance().mPhoneNo // 终端手机号
        get() {
            var numStr = ""
            (field.length until 16).forEach {
                numStr += "0"
            }
            return numStr + field
        }
    val RESERVE = "3c" // 预留消息头

    /**
     * 生成校验码，是指从消息头开始到校验码前一位的范围进行异或
     * 这里是理论上的倒数第二步
     * 原理：将消息头和消息体前后一个字节异或，直至结束
     *
     * @param byteArray 消息头和消息体已经生成好的 byteArray
     * @return 添加完成的 byteArray
     */
    fun addCheckCode(byteArray: ByteArray): ByteArray {
        return byteArray.plus(getCheckCode(byteArray))
    }

    /**
     * 检测校验码
     *
     * @param byteArray 需要检测的 byteArray
     * @return 是否检测通过
     */
    fun checkCode(byteArray: ByteArray): Boolean {
        val checkCode = byteArray.last()
//        logD("提取的校验码为：${ConvertHelper.byteArray2HexString(byteArrayOf(checkCode))}")
        val checkMsg = removeCheckCode(byteArray)
        return getCheckCode(checkMsg) == checkCode
    }

    /**
     * 将 byteArray 中的校验码移除
     * 注意：此处传入的 byteArray 中不应该存在标识位
     *
     * @param byteArray 需要移除校验码的 byteArray
     * @return 移除完成的 byteArray
     */
    fun removeCheckCode(byteArray: ByteArray): ByteArray {
        return byteArray.asList().subList(0, byteArray.size - 1).toByteArray()
    }

    /**
     * 获取消息的校验码
     *
     * @param byteArray 消息
     * @return 校验码
     */
    private fun getCheckCode(byteArray: ByteArray): Byte {
        var checkCode: Byte = byteArray[0]
        for (i in 1 until byteArray.size) {
            checkCode = (checkCode.toInt() xor byteArray[i].toInt()).toByte()
        }
        logD("生成的校验码为：${ConvertHelper.byteArray2HexString(byteArrayOf(checkCode))}")
        return checkCode
    }

    /**
     * 添加标识位
     * 这里应该是构建消息的最后一步
     * 原理：将添加了校验码的 byteArray 转义，再添加标识位
     *
     * @param byteArray 除标识位以外的已经生成好的 byteArray
     * @return 添加完成的 byteArray
     */
    fun addIdentificationBit(byteArray: ByteArray): ByteArray {
        var b: String // 用于 byte 的字符表示变量
        var bytes = "" // 需要返回的 byteArray 的字符串
        val byteString: String = ConvertHelper.byteArray2HexString(byteArray)
        // 检测 byteArray 是否需要转义
        (0 until byteString.length / 2).forEach {
            b = byteString[it * 2].toString() + byteString[it * 2 + 1].toString()
            if (b == CONVERT_BYTE_7D) {
                b = CONVERT_BYTE_7D_OVER
            } else if (b == CONVERT_BYTE_7E) {
                b = CONVERT_BYTE_7E_OVER
            }
            bytes += b
        }
        // 添加标识位
        bytes = IDENTIFICATION_BIT + bytes + IDENTIFICATION_BIT
//        logD("转义及添加标识位前：$byteString \n转义及添加标识位后：$bytes ")
        logD("发送的消息：$byteString")
        return ConvertHelper.string2ByteArray(bytes)
    }

    /**
     * 将 byteArray 中的标识位移除并进行转义
     *
     * @param byteArray 需要移除校验码的 byteArray
     * @return 移除完成的 byteArray
     */
    fun removeIdentificationBit(byteArray: ByteArray): ByteArray {
        var b = "" // 用于 byte 的字符表示变量
        var bytes = "" // 需要返回的 byteArray 的字符串
        val byteString = ConvertHelper.byteArray2HexString(byteArray.asList()
                .subList(1, byteArray.size - 1).toByteArray())
//        logD("移除标识位后：$byteString")
        // 检测是否需要转义
        (0 until byteString.length / 2).forEach {
            if (b != CONVERT_BYTE_7D) {
                b = byteString[it * 2].toString() + byteString[it * 2 + 1].toString()
                if (b != CONVERT_BYTE_7D) bytes += b
            } else {
                b += byteString[it * 2].toString() + byteString[it * 2 + 1].toString()
                if (b == CONVERT_BYTE_7D_OVER) b = CONVERT_BYTE_7D
                else if (b == CONVERT_BYTE_7E_OVER) b = CONVERT_BYTE_7E
                bytes += b
                b = ""
            }
        }
//        logD("转义处理后：$bytes")
        return ConvertHelper.string2ByteArray(bytes)
    }

    /**
     * 将所有分包组成一个完整的包
     *
     * @param packages 分包集合
     * @return 由分包组成完整包的消息体
     */
    fun composeMsgPackage(packages: ArrayList<ByteArray>): ByteArray {
        val fullBytes = ArrayList<Byte>() // 拼接完整的消息包
        (0 until packages.size).forEach {
            fullBytes.addAll(getBodyFromSubPackage(packages[it]))
        }
        return fullBytes.toByteArray()
    }

    /**
     * 从分包里获取消息体，用于拼接
     *
     * @param byteArray 整个分包消息
     * @return 分包消息体
     */
    private fun getBodyFromSubPackage(byteArray: ByteArray): List<Byte> {
        var msgBytes = removeIdentificationBit(byteArray)
        // 检测校验码是否能通过
        if (!checkCode(msgBytes)) throw Exception("消息包校验码错误，未能正确解析！")
        msgBytes = removeCheckCode(msgBytes)
        if (msgBytes.size < 20) return arrayListOf()
        return msgBytes.asList().subList(20, msgBytes.size)
    }

    /**
     * 该消息是否为分包消息，并返回分包的总数
     *
     * @param bytes 接收的消息
     * @return 分包的消息数，若没有分包则返回 1
     */
    fun getSubPackagesCount(bytes: ByteArray): Int {
        // 移除标识位并转义
        val byteMsg = removeIdentificationBit(bytes)
        val msgProperty = PackageMsg.MsgProperty().setMsgPropertyHexString(
                ConvertHelper.byteArray2HexString(byteMsg.asList().subList(3, 5).toByteArray()))
        // 没有分包，返回 1
        if (!msgProperty.getIsSubPackage()) return 1
        val count = ConvertHelper.byteArray2HexString(byteMsg.asList().subList(16, 18).toByteArray())
        return Integer.parseInt(count, 16)
    }

    /**
     * 解析当前分包的序号
     *
     * @param bytes 当前分包
     * @return 分包的序号
     */
    fun getSubPackageIndex(bytes: ByteArray): Int {
        // 移除标识位并转义
        val byteMsg = removeIdentificationBit(bytes)
        val index = ConvertHelper.byteArray2HexString(byteMsg.asList().subList(18, 20).toByteArray())
        return Integer.parseInt(index, 16)
    }

    /**
     * 检测是否为心跳包
     *
     * @param bytes 当前消息包
     * @return 是否为心跳包
     */
    fun isHeartbeatMsg(bytes: ByteArray): Boolean {
        // 移除标识位并转义
        val byteMsg = removeIdentificationBit(bytes)
        // 读取消息包 ID
        val msgId = ConvertHelper.byteArray2HexString(byteMsg.asList().subList(1, 3).toByteArray())
        if (msgId != MessageID.ServerAnswerID) return false
        // 验证应答消息 ID
        val reMsgId = ConvertHelper.byteArray2HexString(byteMsg.asList().subList(18, 20).toByteArray())
        return reMsgId == MessageID.ClientHeartbeatID
    }

    /**
     * 给字符串补充 0
     *
     * @param needLength 所需字符串长度
     * @param str 需要补充的字符串
     * @param direction 是前补还是后补 0 前补；1 后补
     * @return 补充完成的字符串
     */
    fun supplementStr(needLength: Int, str: String, direction: Int): String {
        var returnStr = str
        if (direction == 0) {
            // 前补
            (0 until (needLength - returnStr.length)).forEach {
                returnStr = "0" + returnStr
            }
            return returnStr
        }
        // 后补
        (0 until (needLength - returnStr.length)).forEach {
            returnStr += "0"
        }
        return returnStr
    }

    /**
     * 移除字符串中头部的补充 0
     *
     * @param str 需要移除头部 0 的字符串
     * @return 移除完成的字符串
     */
    fun remove0(str: String): String {
        var i = -1
        str.mapIndexed { index, c ->
            if (c != '0' && i == -1) {
                i = index
            }
        }
        return str.substring(i, str.length)
    }

    /**
     * 获取客户端应答消息
     *
     * @param flowNum 流水号
     * @param messageId 应答 ID
     * @param result 结果 0 成功；1 失败；2 消息有误；3 不支持
     * @return 组合完成的消息包
     */
    fun getAnswerMsg(flowNum: Int, messageId: String, result: Int): PackageMsg {
        val header = PackageMsg.MsgHeader()
        header.flowNum = flowNum
        header.msgId = MessageID.ClientAnswerID
        val body = CurrencyMsg()
        body.result = result
        body.responseId = messageId
        body.result = result
        val pMsg = PackageMsg()
        pMsg.setMessage(header, body)
        return pMsg
    }

    /**
     * 保存流水号
     *
     * @param context 上下文
     * @param flowNum 流水号
     * @return 是否保存成功
     */
    @SuppressLint("ApplySharedPref")
    fun saveFlowNum(context: Context, flowNum: Int): Boolean {
        val sp = context.getSharedPreferences(context.getString(R.string.str_message), Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putInt(context.getString(R.string.str_flow_num), flowNum)
        editor.commit()
        return true
    }

    /**
     * 读取流水号
     *
     * @param context 上下文
     * @return 流水号
     */
    fun readFlowNum(context: Context): Int {
        val sp = context.getSharedPreferences(context.getString(R.string.str_message), Context.MODE_PRIVATE)
        return sp.getInt(context.getString(R.string.str_flow_num), 0)
    }

    /**
     * 保存驾培包序号
     *
     * @param context 上下文
     * @param serial 序号
     * @return 是否保存成功
     */
    @SuppressLint("ApplySharedPref")
    fun saveProtocolSerial(context: Context, serial: Int): Boolean {
        val sp = context.getSharedPreferences(context.getString(R.string.str_message), Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putInt(context.getString(R.string.str_protocol_serial), serial)
        editor.commit()
        return true
    }

    /**
     * 读取驾培包序号
     *
     * @param context 上下文
     * @return 驾培包序号
     */
    fun readProtocolSerial(context: Context): Int {
        val sp = context.getSharedPreferences(context.getString(R.string.str_message), Context.MODE_PRIVATE)
        return sp.getInt(context.getString(R.string.str_protocol_serial), 1)
    }

    /**
     * 保存照片编号
     *
     * @param context 上下文
     * @param number 照片编号
     * @return 是否保存成功
     */
    @SuppressLint("ApplySharedPref")
    fun savePicNumber(context: Context, number: Long): Boolean {
        val sp = context.getSharedPreferences(context.getString(R.string.str_message), Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putLong(context.getString(R.string.str_picture_number), number)
        editor.commit()
        return true
    }

    /**
     * 读取照片编号
     *
     * @param context 上下文
     * @return 照片编号
     */
    fun readPicNumber(context: Context): Long {
        val sp = context.getSharedPreferences(context.getString(R.string.str_message), Context.MODE_PRIVATE)
        return sp.getLong(context.getString(R.string.str_picture_number), 1L)
    }

    /**
     * 保存课程 ID
     *
     * @param context 上下文
     * @param classId 课程 ID
     * @return 是否保存成功
     */
    @SuppressLint("ApplySharedPref")
    fun saveClassId(context: Context, classId: Int): Boolean {
        val sp = context.getSharedPreferences(context.getString(R.string.str_message), Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putInt(context.getString(R.string.str_class_id), classId)
        editor.commit()
        return true
    }

    /**
     * 读取课程 ID
     *
     * @param context 上下文
     * @return 课程 ID
     */
    fun readClassId(context: Context): Int {
        val sp = context.getSharedPreferences(context.getString(R.string.str_message), Context.MODE_PRIVATE)
        return sp.getInt(context.getString(R.string.str_class_id), 1)
    }

    /**
     * 解析参数项
     *
     * @param byteArray 只包含参数项的字节数组
     * @return 返回参数项列表
     */
    fun parseParamItem(byteArray: ByteArray): ArrayList<ParamMsg.ParamItem> {
        val pList = ArrayList<ParamMsg.ParamItem>()
        var len = byteArray.size // 字节数组长度
        var bytes: List<Byte> = byteArray.asList() // 进入运算的字节数组
        while (len != 0) {
            val pLen = bytes[4].toInt() + 5 // 参数总长度
            val pItem = ParamMsg.ParamItem() // 参数项
            pItem.setBodyBytes(bytes.subList(0, pLen).toByteArray())
            pList.add(pItem)
            bytes = bytes.subList(pLen, bytes.size)
            len = bytes.size
            logI("消息ID：${pItem.paramId} -> 消息内容：${pItem.paramValue}")
        }
        return pList
    }

    /**
     * 解析参数 ID
     *
     * @param byteArray 需要解析字节数组
     * @return 参数 ID 列表
     */
    fun parseParamID(byteArray: ByteArray): ArrayList<Int> {
        val pIDList = ArrayList<Int>()
        var len = byteArray.size // 字节数组长度
        var bytes: List<Byte> = byteArray.asList() // 进入运算的字节数组
        while (len != 0) {
            val pID = Integer.parseInt(ConvertHelper.byteArray2HexString(bytes.subList(0, 4).toByteArray()), 16)
            pIDList.add(pID)
            bytes = bytes.subList(4, bytes.size)
            len = bytes.size
        }
        return pIDList
    }

    /**
     * 获取 byte 数组的校验串
     * @param byteString 需要生成校验串的 byte 数组字符串
     * @return 生成的校验串
     */
    fun getCheckStrand(byteString: String): String {
        val pKey = SignVerifyHelper.getDevCAInfo() ?: throw Exception("没有获取到正确的私钥！")
        return SignVerifyHelper.sign(byteString, pKey) ?: throw Exception("校验串生成失败！")
    }

    /**
     * 获取上行透传消息
     *
     * @param msgId 透传消息 ID
     * @param msgData 透传消息数据内容
     * @param isResponse 是否需要应答
     * @param isRealTime 是否为实时消息，false 为补传消息
     * @return 返回完整消息包
     */
    fun getUpstreamMsg(msgId: String, msgData: PasBodyMsg.DataContent, isResponse: Boolean,
                       isRealTime: Boolean): PackageMsg {
        val pMsg = PackageMsg()
        val msgHeader = PackageMsg.MsgHeader()
        msgHeader.msgId = MessageID.ClientUpstreamID
        val msgBody = PasBodyMsg()
        msgBody.setMsgId(msgId)
        msgBody.setDataContent(msgData)
        msgBody.setIsRealTimeMsg(isRealTime)
        msgBody.setIsResponse(isResponse)
        pMsg.setMessage(msgHeader, msgBody)
        return pMsg
    }

    /** @return 获取位置信息消息包 */
    fun getGNSSMsg(msgId: String): PackageMsg {
        val pMsg = PackageMsg()
        val msgHeader = PackageMsg.MsgHeader()
        msgHeader.msgId = msgId
        val msgBody = GNSSDataMsg()
        pMsg.setMessage(msgHeader, msgBody)
        return pMsg
    }

    /**
     * 应答消息指令
     *
     * @param msgId 透传消息 ID
     * @param dataContent 消息数据
     * @param isRealTimeMsg 是否为实时消息
     */
    fun responseCommend(msgId: String, dataContent: PasBodyMsg.DataContent, isRealTimeMsg: Boolean) {
        val rePasBodyMsg = PasBodyMsg() // 数据上行透传消息体
        rePasBodyMsg.setMsgId(msgId)
        rePasBodyMsg.setIsRealTimeMsg(isRealTimeMsg)
        rePasBodyMsg.setIsResponse(false)
        rePasBodyMsg.setDataContent(dataContent)
        HeartbeatOperator.getInstance().sendUpLinkResponseMsg(rePasBodyMsg)
    }

    /**
     * 上传图片
     *
     * @param context 上下文
     * @param picturePath 图片路径
     * @param eventType 事件类型
     * 定义如下：
     * 0：中心查询的图片
     * 1：紧急报警主动上传的图片
     * 2：关车门后达到指定车速主动上传的图片
     * 3：侧翻报警主动上传的图片
     * 4：上客
     * 5：定时拍照
     * 6：进区域
     * 7：出区域
     * 8：事故疑点（紧急刹车）
     * 9：开车门
     * 17：学员登录拍照
     * 18：学员登出拍照
     * 19：学员培训过程中拍照
     * 20：教练员登录拍照
     * 21：教练员登出拍照
     * @param uploadModel 上传模式
     * 1：平台拍照指令后上传
     * 2：平台查询后要求上传
     * 129：终端主动拍照上传
     * 255：停止拍摄并上传图片
     */
    fun uploadPicture(context: Context, picModel: PhotoModel, isRealTime: Boolean):Int {
        val uploadOp = PictureUploadOperator(context)
        uploadOp.mPhotoModel = picModel
        uploadOp.isRealTime = isRealTime
        return uploadOp.upload()
    }

    /**
     * 读取禁训状态
     *
     * @param context 上下文
     * @return 1：表示可用，默认值；2：表示禁用
     */
    fun readBanState(context: Context): Int {
        val sp = context.getSharedPreferences(context.getString(R.string.str_message), Context.MODE_PRIVATE)
        return sp.getInt(context.getString(R.string.str_ban_state), 1)
    }

    /**
     * 保存禁训状态
     *
     * @param context 上下文
     * @param banState 禁训状态
     * 1：表示可用，默认值；2：表示禁用
     * @return 是否保存成功
     */
    @SuppressLint("ApplySharedPref")
    fun saveBanState(context: Context, banState: Int): Boolean {
        val sp = context.getSharedPreferences(context.getString(R.string.str_message), Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putInt(context.getString(R.string.str_ban_state), banState)
        editor.commit()
        return true
    }
}
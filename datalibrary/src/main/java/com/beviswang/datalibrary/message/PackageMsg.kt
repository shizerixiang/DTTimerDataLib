package com.beviswang.datalibrary.message

import com.beviswang.datalibrary.Publish
import com.beviswang.datalibrary.logE
import com.beviswang.datalibrary.logI
import com.beviswang.datalibrary.util.ConvertHelper
import com.beviswang.datalibrary.util.MessageHelper
import com.beviswang.datalibrary.util.OperationHelper

/**
 * 消息包实体类
 * Created by shize on 2018/1/16.
 */
class PackageMsg {
    // 消息头 16 byte 总长度
    private var msgHeader: MsgHeader? = null
    // 消息体
    private var msgBody: MsgBody? = null
    // 分包总数
    private var subPackCount: Int = 0

    /**
     * @return 获取消息头
     */
    fun getMsgHeader(): MsgHeader? {
        return msgHeader
    }

    /**
     * @return 获取消息体
     */
    fun getMsgBody(): MsgBody? {
        return msgBody
    }

    /**
     * 设置消息头和消息体
     *
     * @param msgHeader 消息头
     * @param msgBody 消息体
     */
    fun setMessage(msgHeader: MsgHeader, msgBody: MsgBody?): PackageMsg {
        var body = msgBody
        if (body == null) body = EmptyMsg()
        // 设置消息体长度，必须在消息体赋值完成后获取!!!
        msgHeader.msgProperty.setMsgLength(body.getBodyLength())
        this.msgHeader = msgHeader
        this.msgBody = body
        return this
    }

    /**
     * @return 获取可以发送的 byteArray
     */
    fun getSendMsg(): ByteArray {
        return MessageHelper.addIdentificationBit(MessageHelper.addCheckCode(getPackageArray()))
    }

    /**
     * 直接设置整个消息包的 byteArray
     * 之后通过内部解析成需要的数据形式
     *
     * @param byteArray 整个 byteArray 的消息包
     */
    fun setReadHexMsg(byteArray: ByteArray): PackageMsg {
        // 移除标识位并转义，返回带有校验码的消息包
        var msgBodyArray = MessageHelper.removeIdentificationBit(byteArray)
        // 检测校验码是否能通过
        if (!MessageHelper.checkCode(msgBodyArray)) throw Exception("消息包校验码错误，未能正确解析！")
        msgBodyArray = MessageHelper.removeCheckCode(msgBodyArray)
        // 消息包字符串
        val msgString = ConvertHelper.byteArray2HexString(msgBodyArray)
        logI("解析后的消息内容：$msgString")
        // 解析消息头
        parseHeaderMsg(msgBodyArray)
        // 解析消息体
        parseBodyMsg(msgBodyArray)
        return this
    }

    /**
     * 直接设置多个消息包的 byteArray
     * 用于接收多个分包
     *
     * @param byteArrays 消息包集合
     */
    fun setReadHexMsgList(byteArrays: ArrayList<ByteArray>): PackageMsg? {
        if (byteArrays.size == 0) {
            logE("未接收到消息包！原因：接收消息包失败！")
            return null
        }
        // 获取转义并移除标识位和校验码的消息
        val msgBytes = MessageHelper.removeCheckCode(MessageHelper.removeIdentificationBit(byteArrays[0]))
        // 解析消息头
        parseHeaderMsg(msgBytes)
        // 解析分包中的所有消息体
        parseBodyMsgList(byteArrays)
        return this
    }

    /**
     * 将一个消息包分为多个分包
     *
     * @return 获取分包集合
     */
    fun getSubPackages(): List<PackageMsg> {
        if (msgHeader == null || msgBody == null) throw Exception("需要分包消息没有消息头或消息体！")
        val pMsgList = ArrayList<PackageMsg>()
        val bodyArray = msgBody!!.getBodyArray()
        // 计算分包数
        getSubPackageCount(bodyArray)
        var fromIndex = 0 // 起始位置
        var toIndex = BODY_MAX_LENGTH // 截止位置
        var index = 1 // 分包序号
        while (toIndex < bodyArray.size) {
            pMsgList.add(getSubPackage(bodyArray.asList().subList(
                    fromIndex, toIndex).toByteArray(), index))
            fromIndex = toIndex
            toIndex += BODY_MAX_LENGTH
            index++
        }
        if (fromIndex < bodyArray.size - 1) {
            pMsgList.add(getSubPackage(bodyArray.asList().subList(
                    fromIndex, bodyArray.size).toByteArray(), index))
        }
        return pMsgList
    }

    /**
     * 获取分包消息
     *
     * @param bodyBytes 分包消息 byte 数组
     * @param index 分包消息编号
     * @return 分包消息包
     */
    private fun getSubPackage(bodyBytes: ByteArray, index: Int): PackageMsg {
        val body = SubPackageMsg()
        val header = MsgHeader()
        val pMsg = PackageMsg()
        header.setIsSubPackage(true)
        header.setPackageCount(subPackCount)
        header.setPackageId(index)
        header.msgId = msgHeader!!.msgId
        body.setBodyBytes(bodyBytes)
        pMsg.setMessage(header, body)
        return pMsg
    }

    /**
     * @return 获取分包数量
     */
    fun getSubPackageCount(byteArray: ByteArray): Int {
        if (subPackCount != 0) return subPackCount
        subPackCount = byteArray.size / BODY_MAX_LENGTH
        if (byteArray.size % BODY_MAX_LENGTH != 0) subPackCount++
        return subPackCount
    }

    /**
     * 解析消息头
     *
     * @param msgBodyArray 消息内容 byteArray
     */
    private fun parseHeaderMsg(msgBodyArray: ByteArray) {
        // 截取消息体属性
        val msgProperty = MsgProperty().setMsgPropertyHexString(
                ConvertHelper.byteArray2HexString(msgBodyArray.asList().subList(3, 5).toByteArray()))
        // 设置消息头内容
        msgHeader = MsgHeader()
        msgHeader!!.msgProperty = msgProperty
        msgHeader!!.setHeaderBytes(msgBodyArray.asList().subList(
                0, msgHeader!!.getHeaderMsgLength()).toByteArray())
    }

    /**
     * 解析消息体
     *
     * @param msgBodyArray 消息内容 byteArray
     */
    private fun parseBodyMsg(msgBodyArray: ByteArray) {
        // 设置消息体内容
        msgBody = OperationHelper.getMsgBodyByMsgId(msgHeader!!.msgId)
        msgBody!!.setBodyBytes(msgBodyArray.asList().subList(
                msgHeader!!.getHeaderMsgLength(), msgBodyArray.size).toByteArray())
    }

    /**
     * 解析分包中所有的消息体
     *
     * @param byteArrays 分包消息的集合
     */
    private fun parseBodyMsgList(byteArrays: ArrayList<ByteArray>) {
        msgBody = OperationHelper.getMsgBodyByMsgId(msgHeader!!.msgId)
        msgBody!!.setBodyBytes(MessageHelper.composeMsgPackage(byteArrays))
    }

    /**
     * @return 获取消息包 byteArray ，单纯的消息头和消息体的字符串连接
     */
    private fun getPackageArray(): ByteArray {
        if (msgHeader == null || msgBody == null) throw Exception("消息头或消息体不能为空！")
        if (msgBody is EmptyMsg) return ConvertHelper.string2ByteArray(msgHeader!!.getHeaderString())
        return ConvertHelper.string2ByteArray(getPackageString())
    }

    /**
     * @return 获取消息包字符串
     */
    private fun getPackageString(): String {
        if (msgHeader == null || msgBody == null) throw Exception("消息头或消息体不能为空！")
        return msgHeader!!.getHeaderString() + ConvertHelper.byteArray2HexString(msgBody!!.getBodyArray())
    }

    /**
     * 消息头实体类
     */
    class MsgHeader {
        private var protocolVersion: String = MessageHelper.PROTOCOL_VERSION // 协议版本号 1 byte 固定的协议
        var msgId: String = "0100" // 消息ID 2 byte
        var msgProperty: MsgProperty = MsgProperty() // 消息体属性 2 byte 003d
        private var terminalPhone: String = MessageHelper.TERMINAL_PHONE_NUM // 终端手机号 8 byte
        var flowNum: Int = 0 // 服务器应答流水号 2 byte 0007
        var reserve: String = MessageHelper.RESERVE // 预留 1 byte
        private var msgPackageInfo: String = "" // 消息包封装项 2 byte 需要先设置总个数，再设置编号
        private var msgPackageId: String = "" // 分包编号 2 byte

        /**
         * @param count 设置分包总个数
         */
        fun setPackageCount(count: Int) {
            msgPackageInfo = MessageHelper.supplementStr(4, Integer.toHexString(count), 0)
        }

        /**
         * @param id 分包的编号
         */
        fun setPackageId(id: Int) {
            msgPackageId = MessageHelper.supplementStr(4, Integer.toHexString(id), 0)
        }

        /** @param boolean 设置分包消息 */
        fun setIsSubPackage(boolean: Boolean) {
            msgProperty.setIsSubPackage(boolean)
        }

        /**
         * @return 获取消息头 byteArray
         */
        fun getHeaderArray(): ByteArray {
            return ConvertHelper.string2ByteArray(getHeaderString())
        }

        /**
         * @return 获取消息头字符串
         */
        fun getHeaderString(): String {
            flowNum = Publish.getInstance().mFlowNum
            Publish.getInstance().mFlowNum++
            return protocolVersion + msgId + msgProperty.getMsgPropertyString() + terminalPhone +
                    MessageHelper.supplementStr(4, Integer.toHexString(
                            flowNum), 0) + reserve + msgPackageInfo + msgPackageId
        }

        /**
         * 设置消息头的 byteArray
         * 设置后可以获取对应的值
         *
         * @param byteArray 获取的 byteArray
         */
        fun setHeaderBytes(byteArray: ByteArray) {
            val byteString = ConvertHelper.byteArray2HexString(byteArray)
            protocolVersion = byteString.substring(0, 2)
            msgId = byteString.substring(2, 6)
            terminalPhone = byteString.substring(10, 26)
            flowNum = Integer.parseInt(byteString.substring(26, 30), 16)
            reserve = byteString.substring(30, 32)
            // 判断是否分包
            if (msgProperty.getIsSubPackage()) {
                msgPackageInfo = byteString.substring(32, 36)
                msgPackageId = byteString.substring(36, 40)
            }
        }

        /**
         * 获取消息头长度
         * 只有在消息体属性有值时有效
         *
         * @return 消息头长度
         */
        fun getHeaderMsgLength(): Int {
            var headerLength = 16
            // 如果有分包，则消息头有 20 byte 位
            if (msgProperty.getIsSubPackage())
                headerLength = 20
            return headerLength
        }
    }

    /**
     * 消息体属性
     * 此处为十进制中的解析原理：
     * 如果大于 8192 则记录有分包
     * 在 8192 去除后再比对是否有加密方式，如果大于 1023 则开始加密方式解析
     * 最后计算消息体长度，消息体长度最大为 1023
     */
    class MsgProperty {
        // 消息体长度，用 bit 位表示，长度为 10 bit位，消息最大长度为 1023，即 3ff
        private var msgLength: String = "0000000000"
        // 分包， 1 为分包，0 为不分包
        private var isSubPackage: String = "0"
        // 数据加密方式，001 为 RSA 加密，000 为不加密，其他保留
        var msgEncryption: String = "000"
        // 保留位
        private var msgRetain: String = "00"

        /**
         * @param length 设置消息体长度
         */
        fun setMsgLength(length: Int) {
            var lengthStr = Integer.toBinaryString(length)
            (lengthStr.length until 10).forEach {
                lengthStr = "0" + lengthStr
            }
            msgLength = lengthStr
        }

        /**
         * @param lengthB 直接赋予消息体长度二进制字符串，用于解析
         */
        private fun setMsgLength(lengthB: String) {
            msgLength = lengthB
        }

        /**
         * @return 获取消息体长度
         */
        fun getMsgLength(): Int {
            return Integer.parseInt(msgLength, 2)
        }

        /**
         * @param boolean 赋值是否分包
         */
        fun setIsSubPackage(boolean: Boolean) {
            isSubPackage = if (boolean) "1" else "0"
        }

        /**
         * @param str 直接赋予是否分包以二进制字符串，用于解析
         */
        private fun setIsSubPackage(str: String) {
            isSubPackage = str
        }

        /**
         * @return 获取是否分包
         */
        fun getIsSubPackage(): Boolean {
            return isSubPackage != "0"
        }

        /**
         * @return 直接获取消息体属性十六进制字符串，用于发送
         */
        fun getMsgPropertyString(): String {
            return ConvertHelper.binaryString2hexString(msgRetain + isSubPackage + msgEncryption + msgLength)!!
        }

        /**
         * @param msgProperty 直接设置消息体属性十六进制字符串
         */
        fun setMsgPropertyHexString(msgProperty: String): MsgProperty {
            // 将十六进制转换为二进制
            val msgBString = ConvertHelper.hexString2binaryString(msgProperty)!!
            // 解析二进制 bit 位
            setMsgLength(msgBString.substring(6, msgBString.length))
            setIsSubPackage(msgBString[2].toString())
            msgEncryption = msgBString.substring(3, 6)
            msgRetain = msgBString.substring(0, 2)
            return this
        }
    }

    /**
     * 消息体接口
     */
    interface MsgBody {
        /**
         * @param byteArray 设置读取的消息体 byteArray
         */
        fun setBodyBytes(byteArray: ByteArray)

        /**
         * @return 获取消息体 byteArray
         */
        fun getBodyArray(): ByteArray

        /**
         * @return 获取消息体长度，需要先设置完所有的数据后消息长度才能准确
         */
        fun getBodyLength(): Int = getBodyArray().size
    }

    companion object {
        // 消息体最大长度
        val BODY_MAX_LENGTH = 1023
    }
}
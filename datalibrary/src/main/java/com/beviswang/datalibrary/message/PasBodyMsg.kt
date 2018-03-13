package com.beviswang.datalibrary.message

import com.beviswang.datalibrary.Publish
import com.beviswang.datalibrary.logD
import com.beviswang.datalibrary.logI
import com.beviswang.datalibrary.util.ConvertHelper
import com.beviswang.datalibrary.util.MessageHelper
import com.beviswang.datalibrary.util.OperationHelper

/**
 * 透传消息体
 * Created by shize on 2018/1/30.
 */
class PasBodyMsg : PackageMsg.MsgBody {
    /** 透传消息类型，0x13为驾培业务 1 byte */
    private var msgType: Int = 0x13
    /** 扩展计时培训消息体数据格式 无限制 byte */
    private var msgContent: PasMsgContent = PasMsgContent()
    // 缓存的消息 byte 数组
    private var msgByteArray:ByteArray? = null

    override fun setBodyBytes(byteArray: ByteArray) {
        if (byteArray[0].toInt() != msgType) throw Exception("此透传消息不是驾培业务消息！")
        msgContent.setMsgContentByteArray(byteArray.asList().subList(1, byteArray.size).toByteArray())
    }

    override fun getBodyArray(): ByteArray {
        if (msgByteArray != null) return msgByteArray!!
        val strMsgType = MessageHelper.supplementStr(2, Integer.toHexString(msgType), 0)
        val strMsgContent = msgContent.toString()
        msgByteArray = ConvertHelper.string2ByteArray(strMsgType + strMsgContent)
        return msgByteArray!!
    }

    /** @param msgId 设置透传消息 ID */
    fun setMsgId(msgId: String) {
        msgContent.msgId = msgId
    }

    /** @return 透传消息 ID */
    fun getMsgId(): String = msgContent.msgId

    /** @param dataContent 设置数据内容 */
    fun setDataContent(dataContent: DataContent) {
        msgContent.setDataContent(dataContent)
    }

    /** @return 获取数据内容 */
    fun getDataContent(): DataContent? = msgContent.getDataContent()

    /** @param isResponse 设置是否需要应答 */
    fun setIsResponse(isResponse: Boolean) {
        msgContent.setIsResponse(isResponse)
    }

    /** @return 是否为应答消息 */
    fun getIsResponse(): Boolean {
        return msgContent.isResponse()
    }

    /** @param isRealTime 设置是否为实时消息 */
    fun setIsRealTimeMsg(isRealTime: Boolean) = msgContent.setIsRealTimeMsg(isRealTime)

    /** @return 是否为实时消息 */
    fun getIsRealTimeMsg(): Boolean = msgContent.getIsRealTimeMsg()

    /** 扩展计时培训消息体 */
    class PasMsgContent {
        /**
         * 透传消息 ID 2 byte
         * 功能编号 + 消息编号，例如：0x0102 上报教练员登出协议
         * 功能号为 0x01，消息号为 0x02
         */
        var msgId: String = "0101"
        /** 扩展消息属性 2 byte */
        private var msgProperty: PasMsgProperty = PasMsgProperty()
        /**
         * 驾培包序号 2 byte
         * 扩展驾培协议包序号，从 1 开始，除协议中特别声明外，循环递增
         */
        private var protocolSerial: Int = Publish.getInstance().mProtocolSerial
        /** 计时终端编号 16 byte */
        private var terminalNum: String = Publish.getInstance().mTerminalNo
        /**
         * 数据长度 4 byte
         * 数据内容的长度 n，没有数据内容则为 0
         */
        private var dataLength: Int = 0x00000000
        /**
         * 数据内容 n byte
         * 根据数据长度要求确定，可无此字段
         */
        private var dataContent: DataContent? = null
        /**
         * 校验串 256 byte
         * 使用计时终端证书对整个扩展驾培消息（不含校验串）进行加密后生成，采用 2048 位证书时
         * 长度为 256 byte 。平台发送的扩展消息无此字段
         */
        private var checkStrand: String = ""

        /**
         * @return 获取扩展计时培训消息体的 byte 数组
         */
        fun getMsgContentByteArray(): ByteArray {
            return ConvertHelper.string2ByteArray(toString())
        }

        /**
         * 设置扩展计时培训消息体的 byte 数组
         * @param byteArray 扩展计时培训消息体
         */
        fun setMsgContentByteArray(byteArray: ByteArray) {
            val byteList = byteArray.asList()
            msgId = ConvertHelper.byteArray2HexString(byteList.subList(0, 2).toByteArray())
            msgProperty.setMsgProperty(ConvertHelper.byteArray2HexString(byteList.subList(2, 4).toByteArray()))
            protocolSerial = Integer.parseInt(ConvertHelper.byteArray2HexString(byteList.subList(4, 6).toByteArray()), 16)
            if (terminalNum != String(byteList.subList(6, 22).toByteArray())) throw Exception("终端编号不符合！")
            dataLength = Integer.parseInt(ConvertHelper.byteArray2HexString(byteList.subList(22, 26).toByteArray()), 16)
            if (dataLength == 0) return
            dataContent = OperationHelper.getDataContentByMsgId(msgId)
            dataContent!!.setDataByteArray(byteList.subList(26, byteArray.size).toByteArray())
        }

        /** @return 是否需要应答 */
        fun isResponse(): Boolean {
            return msgProperty.isResponse()
        }

        /**
         * 设置是否需要应答
         *
         * @param isResponse 是否需要应答
         */
        fun setIsResponse(isResponse: Boolean) {
            msgProperty.setIsResponse(isResponse)
        }

        /**
         * 设置数据内容
         *
         * @param data 数据内容
         */
        fun setDataContent(data: DataContent) {
            dataLength = data.getDataLength()
            this.dataContent = data
        }

        /** @return 获取数据内容 */
        fun getDataContent(): DataContent? {
            return dataContent
        }

        /** @param isRealTime 是否为实时消息，默认为实时消息 true */
        fun setIsRealTimeMsg(isRealTime: Boolean) {
            if (!isRealTime) msgProperty.agingType = "1"
        }

        /** @return 是否是实时消息 */
        fun getIsRealTimeMsg(): Boolean = msgProperty.agingType == "0"

        override fun toString(): String {
            val strPS = MessageHelper.supplementStr(4, protocolSerial.toString(16), 0)
            protocolSerial++
            val strTN = ConvertHelper.stringGBK2HexString(terminalNum)
            val strDLen = MessageHelper.supplementStr(8, dataLength.toString(16), 0)
            var strDContent = ""
            if (dataLength != 0 && dataContent != null) strDContent = ConvertHelper.byteArray2HexString(dataContent!!.getDataByteArray())
            val strMsg = msgId + msgProperty.getMsgProperty() + strPS + strTN + strDLen + strDContent
            // 生成校验串
            checkStrand = MessageHelper.getCheckStrand(strMsg)
            logD("校验串：$checkStrand")
            return strMsg + checkStrand
        }
    }

    /**
     * 扩展消息属性
     * 0 位表示消息时效类型，应答中也应附带此内容，0：实时消息，1：补传消息
     * 1 位表示应答属性，0：不需要应答，1：需要应答
     * 4-7 位表示加密算法，0：未加密，1：SHA1，2：SHA256
     * 其他位保留
     */
    class PasMsgProperty {
        /** 消息时效类型 1 bit */
        var agingType: String = "0"
        /** 应答属性 1 bit */
        private var responseAttr: String = "0"
        /** 保留 2 bit */
        private var reserve: String = "00"
        /** 加密算法 4 bit */
        private var encryption: String = "0010"
        /** 保留 8 bit */
        private var reserve2: String = "00000000"

        /** @return 是否需要应答 */
        fun isResponse(): Boolean {
            return responseAttr == "1"
        }

        /** @param isResponse 设置是否需要应答 */
        fun setIsResponse(isResponse: Boolean) {
            responseAttr = if (isResponse) "1" else "0"
        }

        /** @return 获取加密算法，0：未加密，1：SHA1，2：SHA256 */
        fun getEncryption(): Int {
            return Integer.parseInt(encryption, 16)
        }

        /** @param encryption 设置加密算法，0：未加密，1：SHA1，2：SHA256 */
        fun setEncryption(encryption: Int) {
            this.encryption = MessageHelper.supplementStr(4, Integer.toBinaryString(encryption), 0)
        }

        /** @param hexString 设置扩展消息属性 */
        fun setMsgProperty(hexString: String) {
            val bString = ConvertHelper.hexString2binaryString(hexString)!!
            agingType = bString.substring(0, 1)
            responseAttr = bString.substring(1, 2)
            reserve = bString.substring(2, 4)
            encryption = bString.substring(4, 8)
            reserve2 = bString.substring(8, bString.length)
        }

        /** @return 获取扩展消息属性 16 进制字符串 */
        fun getMsgProperty(): String {
            return ConvertHelper.binaryString2hexString(toString())!!
        }

        override fun toString(): String {
            return reserve2 + encryption + reserve + responseAttr + agingType
        }
    }

    /**
     * 数据内容
     */
    interface DataContent {
        /**
         * @return 获取数据长度
         */
        fun getDataLength(): Int = getDataByteArray().size

        /**
         * @return 获取数据 byte 数组
         */
        fun getDataByteArray(): ByteArray

        /**
         * @param byteArray 设置数据 byte 数组
         */
        fun setDataByteArray(byteArray: ByteArray)
    }
}
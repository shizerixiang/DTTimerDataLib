package com.beviswang.datalibrary.message

import com.beviswang.datalibrary.util.ConvertHelper
import com.beviswang.datalibrary.util.MessageHelper

/**
 * 终端参数消息体
 * Created by shize on 2018/1/24.
 */
class ParamMsg : PackageMsg.MsgBody {
    // ********************************* 应答固有参数 *********************************
    /**
     *  应答流水号，对应的终端参数查询消息的流水号 2 byte
     *  注意：必须在获取到查询消息的同时单独设置该参数（该参数在消息头），否则无法应答
     */
    var flowNum: Int = 0
    // *********************************** 通用参数 ***********************************
    private var paramTotal: Int = 0                                 // 参数总数 1 byte
    private var packageParamTotal: Int = 0                          // 本数据包参数总数 1 byte
    var paramList: ArrayList<ParamItem> = ArrayList()               // 参数项列表 无限制长度

    override fun setBodyBytes(byteArray: ByteArray) {
        paramTotal = byteArray[0].toInt()
        packageParamTotal = byteArray[1].toInt()
        paramList = MessageHelper.parseParamItem(byteArray.asList().subList(2, byteArray.size).toByteArray())
    }

    override fun getBodyArray(): ByteArray {
        return ConvertHelper.string2ByteArray(toString())
    }

    override fun toString(): String {
        val fNum = MessageHelper.supplementStr(4, ConvertHelper.integer2hexString(flowNum), 0)
        var pList = ""
        paramList.forEach {
            pList += it.toString()
        }
        paramTotal = paramList.size
        val pTotal = MessageHelper.supplementStr(2, ConvertHelper.integer2hexString(paramTotal), 0)
        packageParamTotal = paramTotal
        val pPTotal = MessageHelper.supplementStr(2, ConvertHelper.integer2hexString(packageParamTotal), 0)
        return fNum + pTotal + pPTotal + pList
    }

    /** 终端参数项 */
    class ParamItem : PackageMsg.MsgBody {
        var paramId: Int = 0x0000                    // 参数 ID 4 byte
        private var paramLength: Int = 0             // 参数值长度 1 byte
        var paramValue: String = ""                  // 参数值 无限制长度 （直接保存16进制数据）

        override fun setBodyBytes(byteArray: ByteArray) {
            val byteList = byteArray.asList()
            paramId = Integer.parseInt(ConvertHelper.byteArray2HexString(
                    byteList.subList(0, 4).toByteArray()), 16)
            paramLength = Integer.parseInt(ConvertHelper.byteArray2HexString(
                    byteList.subList(4, 5).toByteArray()), 16)
            paramValue = ConvertHelper.byteArray2HexString(byteList.subList(5, byteList.size).toByteArray())
        }

        override fun getBodyArray(): ByteArray {
            return ConvertHelper.string2ByteArray(toString())
        }

        override fun toString(): String {
            val pId = MessageHelper.supplementStr(8, ConvertHelper.integer2hexString(paramId), 0)
            paramLength = paramValue.length / 2
            val pLen = MessageHelper.supplementStr(2, ConvertHelper.integer2hexString(paramLength), 0)
            return pId + pLen + paramValue
        }
    }
}
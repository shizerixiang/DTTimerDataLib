package com.beviswang.datalibrary.message

import com.beviswang.datalibrary.Publish
import com.beviswang.datalibrary.logI
import com.beviswang.datalibrary.util.ConvertHelper
import com.beviswang.datalibrary.util.FileHelper

/**
 * 注册应答数据实体类
 * 参数 byteArray 为消息体字节数组
 * Created by shize on 2018/1/17.
 */
class RegisterResponseMsg : PackageMsg.MsgBody {
    var flowNum: Int = 0 // 应答流水号
    /**
     *  结果：0 成功；1 车辆已被注册；2 数据库中无该车辆；3 终端已被注册；4 数据库中无该终端。
     *  注意：只有在成功后才返回以下内容
     */
    var result: Int = 0 // 结果
    var platformNum: String = "" // 平台编号
    var mechanismNum: String = "" // 培训机构编号
    var terminalNum: String = "" // 计时终端编号
    var certificateKey: String = "" // 证书口令
    var certificatePath: String = "" // 终端证书路径

    override fun getBodyArray(): ByteArray {
        return toString().toByteArray()
    }

    override fun setBodyBytes(byteArray: ByteArray) {
        val byteString = ConvertHelper.byteArray2HexString(byteArray)
        logI("需要解析的消息体为：$byteString")
        flowNum = Integer.parseInt(byteString.substring(0, 4), 16)
        result = Integer.parseInt(byteString.substring(4, 6), 16)
        // 检测服务器应答是否有数据
        if (byteArray.size < 52) {
            return
        }
        parseContent(byteString, byteArray)
    }

    /**
     * 解析主要内容
     */
    private fun parseContent(byteString: String, byteArray: ByteArray) {
        platformNum = String(ConvertHelper.string2ByteArray(byteString.substring(6, 16)))
        mechanismNum = String(ConvertHelper.string2ByteArray(byteString.substring(16, 48)))
        terminalNum = String(ConvertHelper.string2ByteArray(byteString.substring(48, 80)))
        certificateKey = String(ConvertHelper.string2ByteArray(byteString.substring(80, 104)))
        val fileName = "$terminalNum.pfx" // 终端编号.pfx
        FileHelper.byte2File(byteArray.asList().subList(52, byteArray.size).toByteArray(),
                filePath, fileName)
        certificatePath = filePath + fileName // 保存路径
    }

    companion object {
        // 文件路径，在扩展存储的根目录下
        val filePath = Publish.DIR_FILE_PATH
    }
}
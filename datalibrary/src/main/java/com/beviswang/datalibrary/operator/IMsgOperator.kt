package com.beviswang.datalibrary.operator

import com.beviswang.datalibrary.message.PackageMsg
import com.beviswang.datalibrary.message.PasBodyMsg
import java.io.Closeable

/**
 * 消息操作接口
 * Created by shize on 2018/1/19.
 */
interface IMsgOperator : Closeable {
    /**
     * 连接服务器
     *
     * @return 是否连接成功
     */
    fun connect(): Boolean

    /**
     * 发送消息
     *
     * @param msgPackage 发送的消息包
     * @return 是否发送成功
     */
    fun sendMsg(msgPackage: PackageMsg): Boolean

    /**
     * 接收消息
     *
     * @return 接收的消息包，接收失败返回 null
     */
    fun receiveMsg(): PackageMsg?

    /**
     * 发送业务消息结束
     * 负责复活心跳包
     */
    fun sendOver()

    /**
     * 发送客户端应答消息
     *
     * @param rePMsg 服务器发送过来的消息（需要进行应答的消息）
     * @param result 结果：0 成功；1 失败；2 消息错误；3 不支持 默认值为 0
     */
    fun sendResponseMsg(rePMsg: PackageMsg, result: Int = 0)

    /**
     * 发送数据上行透传消息
     *
     * @param rePMsg 需要上行透传的消息体
     */
    fun sendUpLinkResponseMsg(rePMsg: PasBodyMsg)
}
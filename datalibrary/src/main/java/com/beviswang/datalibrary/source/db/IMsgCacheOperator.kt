package com.beviswang.datalibrary.source.db

import com.beviswang.datalibrary.message.GNSSDataMsg
import com.beviswang.datalibrary.message.passthrough.HoursReportData
import com.beviswang.datalibrary.model.PhotoModel

/**
 * 消息缓存接口
 * Created by shize on 2018/2/9.
 */
interface IMsgCacheOperator {
    /**
     * 保存照片信息
     *
     * @param photoModel 照片信息
     */
    fun savePictureInfo(photoModel: PhotoModel): Boolean

    /**
     * 获取离线缓存的定时拍照的照片信息
     *
     * @return 离线缓存的照片信息集合
     */
    fun getOffLinePictureInfo(): ArrayList<PhotoModel>?

    /**
     * 更新补发消息
     * 在补发成功后更新为已经发送的消息
     *
     * @param picNumber 图片编号
     */
    fun updateReissuePictureInfo(picNumber: String)

    /**
     * 上传图片信息失败
     * 上传过程中断网，则调用该方法通知数据库该图片未上传
     *
     * @param picNumber 图片编号
     */
    fun uploadFailedPictureInfo(picNumber: String)

    /**
     * 通过编号获取图片信息
     *
     * @param number 图片编号
     * @return 图片信息
     */
    fun getPictureInfoByNumber(number: String): PhotoModel?

    /**
     * 通过时间获取图片信息
     *
     * @param fromDate 起始时间戳
     * @param toDate 截止时间戳
     * @return 照片信息集合
     */
    fun getPictureInfoByDate(fromDate: Long, toDate: Long): ArrayList<PhotoModel>?

    /**
     * 缓存定时上报学时记录消息数据
     *
     * @param hoursReportData 上报学时记录消息
     */
    fun cacheHoursMsg(hoursReportData: HoursReportData): Boolean

    /**
     * 获取离线未上报的学时记录消息
     *
     * @return 未上报的学时记录消息集合
     */
    fun getOffLineHoursMsg(): ArrayList<HoursReportData>?

    /**
     * 更新补发消息
     * 在补发成功后更新为已经发送的消息
     */
    fun updateReissueHoursMsg(hoursNumber: String)

    /** @return 获取定时上报学时记录消息数据缓存 */
    fun getCacheHoursMsg(): ArrayList<HoursReportData>?

    /**
     * 通过时间获取学时记录
     *
     * @param fromDate 起始时间
     * @param toDate 截止时间
     * @return 符合条件的学时记录
     */
    fun getCacheHoursMsgByDate(fromDate: Long, toDate: Long): ArrayList<HoursReportData>?

    /**
     * 通过条数获取学时记录
     *
     * @param count 学时记录条数
     * @return 最近 count 条学时记录
     */
    fun getCacheHoursMsgByCount(count: Int): ArrayList<HoursReportData>?

    /**
     * 缓存位置信息汇报消息
     *
     * @param mGNSSDataMsg 位置信息汇报消息
     */
    fun cacheGNSSMsg(mGNSSDataMsg: GNSSDataMsg): Boolean

    /**
     * 获取离线未上报的位置汇报消息
     *
     * @return 未上报的位置汇报消息集合
     */
    fun getOffLineGNSSMsg(): ArrayList<GNSSDataMsg>?

    /**
     * 更新补发消息
     * 在补发成功后更新为已经发送的消息
     */
    fun updateReissueGNSSMsg(GNSSData: GNSSDataMsg)

    /** @return 获取位置信息汇报消息缓存 */
    fun getCacheGNSSMsg(): ArrayList<GNSSDataMsg>?

//    /**
//     * 缓存照片初始化消息数据
//     *
//     * @param upInitData 上传照片初始化消息
//     */
//    fun cachePictureInitMsg(upInitData: UploadPictureData.UpInitData): Boolean
//
//    /** @return 获取照片初始化消息数据缓存 */
//    fun getCachePictureInitMsg(): ArrayList<UploadPictureData.UpInitData>?
}
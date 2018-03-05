package com.beviswang.datalibrary.operator

/**
 * 上传处理
 * Created by shize on 2018/2/2.
 */
interface IUploadOperator {
    /**
     * 初始化上传
     * 主要保存图片信息（插入数据库），用于下次平台及终端查询照片
     * 注意：如果数据上传失败，需要补发时，可以直接从数据库获取保存信息
     *
     * @param path 上传文件路径
     * @return 是否初始化成功
     */
    fun initUpload(path:String):Boolean

    /**
     * 上传图片
     * 0：上传成功
     * 1：照片编号重复或错误
     * 9：其他错误
     * 255：拒绝上传
     * -1：上传失败
     *
     * @return 返回上传结果
     */
    fun upload():Int
}
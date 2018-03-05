package com.beviswang.datalibrary

/**
 * 数据源
 * Created by shize on 2018/1/15.
 */
interface BaseDataSource<T> {
    /**
     * 获取数据
     *
     * @param callback 数据回调
     */
    fun getData(callback: LoadSourceCallback<T>)

    /**
     * 刷新数据
     * 刷新后，需要重新调用 [getData] 获取刷新后的数据
     */
    fun refreshData()

    /**
     * 保存数据
     *
     * @param data 需要保存的数据
     */
    fun saveData(data: T)

    interface LoadSourceCallback<in T> {
        /**
         * 数据加载成功
         *
         * @param dataModel 数据模型
         */
        fun onDataLoaded(dataModel: T)

        /**
         * 数据加载失败
         *
         * @param msg 失败信息
         */
        fun onDataLoadFailed(msg: String)
    }
}
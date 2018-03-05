package com.beviswang.datalibrary

/**
 * BasePresenter
 * Created by shize on 2018/1/15.
 */
interface BasePresenter {
    /**
     * 刷新页面
     */
    fun refreshPage()
    /**
     * 开始加载页面内容
     */
    fun startLoad()
}
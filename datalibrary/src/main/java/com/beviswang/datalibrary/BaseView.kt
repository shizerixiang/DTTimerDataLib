package com.beviswang.datalibrary

import android.content.Context

/**
 * BaseView
 * Created by shize on 2018/1/15.
 */
interface BaseView<in T> {
    /**
     * @return 获取上下文
     */
    fun getViewContext():Context

    /**
     * @return 检测是否安全
     */
    fun isActive():Boolean

    /**
     * 显示无网络
     */
    fun showNoNetwork()

    /**
     * @param presenter 传递 Presenter
     */
    fun setPresenter(presenter:T)
}
package com.beviswang.datalibrary.contract

import com.beviswang.datalibrary.BaseDataSource
import com.beviswang.datalibrary.BasePresenter
import com.beviswang.datalibrary.BaseView
import com.beviswang.datalibrary.model.SettingModel

/**
 * 设置契约接口
 * Created by shize on 2018/1/16.
 */
interface SettingContract {
    /**
     * 设置页面接口
     */
    interface ISettingView : BaseView<ISettingPresenter> {
        /**
         * 显示注销成功
         */
        fun showLogOffSucceed()

        /**
         * 显示注销失败
         *
         * @param msg 注销失败信息
         */
        fun showLogOffFailed(msg: String)
    }

    /**
     * 设置数据操作接口
     */
    interface ISettingPresenter : BasePresenter{
        /**
         * 注销终端
         */
        fun logOffTerminal()
    }

    /**
     * 设置数据源接口
     */
    interface ISettingDataSource : BaseDataSource<SettingModel>{
        /**
         * 请求注销终端
         *
         * @param callback 注销请求回调
         */
        fun requestLogOffTerminal(callback:BaseDataSource.LoadSourceCallback<SettingModel>)
    }
}
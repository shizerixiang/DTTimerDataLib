package com.beviswang.datalibrary.contract

import com.beviswang.datalibrary.BaseDataSource
import com.beviswang.datalibrary.BasePresenter
import com.beviswang.datalibrary.BaseView
import com.beviswang.datalibrary.model.SplashModel

/**
 * 启动界面契约接口
 * Created by shize on 2018/1/15.
 */
interface SplashContract {
    /**
     * 启动页面接口
     */
    interface ISplashView:BaseView<ISplashPresenter>{
        /**
         * 鉴权成功
         *
         * @param splashModel 启动模型
         */
        fun showAuthenticateSucceed(splashModel: SplashModel)

        /**
         * 鉴权失败，需要注册
         *
         * @param msg 失败信息
         */
        fun showAuthenticateFailed(msg:String)
    }

    /**
     * 启动数据操作接口
     */
    interface ISplashPresenter:BasePresenter{
        /**
         * 认证终端
         */
        fun authenticateTerminal()
    }

    /**
     * 启动数据源接口
     */
    interface ISplashDataSource:BaseDataSource<SplashModel>{
        /**
         * 请求鉴权
         */
        fun requestAuthenticate(callback: BaseDataSource.LoadSourceCallback<SplashModel>)
    }
}
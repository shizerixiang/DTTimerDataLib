package com.beviswang.datalibrary.contract

import com.beviswang.datalibrary.BaseDataSource
import com.beviswang.datalibrary.BasePresenter
import com.beviswang.datalibrary.BaseView
import com.beviswang.datalibrary.model.CoachModel

/**
 * 教练契约接口
 * Created by shize on 2018/1/15.
 */
interface CoachContract {
    /**
     * 教练页面接口
     */
    interface ICoachView : BaseView<ICoachPresenter> {
        /**
         * 登出成功
         */
        fun showLogoutSucceed()

        /**
         * 登出失败
         */
        fun showLogoutFailed(msg:String)
    }

    /**
     * 教练数据操作
     */
    interface ICoachPresenter : BasePresenter{
        /**
         * 登出教练
         */
        fun logoutCoach()
    }

    /**
     * 教练数据源
     */
    interface ICoachDataSource : BaseDataSource<CoachModel>{
        /**
         * 请求登出
         *
         * @param callback 请求回调
         */
        fun requestLogout(callback: BaseDataSource.LoadSourceCallback<CoachModel>)
    }
}
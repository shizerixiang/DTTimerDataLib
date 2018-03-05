package com.beviswang.datalibrary.contract

import com.beviswang.datalibrary.BaseDataSource
import com.beviswang.datalibrary.BasePresenter
import com.beviswang.datalibrary.BaseView
import com.beviswang.datalibrary.model.LoginModel

/**
 * 登录契约接口
 * Created by shize on 2018/1/15.
 */
interface LoginContract {
    /**
     * 登录页面接口
     */
    interface ILoginView:BaseView<ILoginPresenter>{
        /**
         * 显示登录成功
         *
         * @param loginModel 登录数据
         */
        fun showLoginSucceed(loginModel: LoginModel)

        /**
         * 显示登录失败
         *
         * @param msg 失败原因
         */
        fun showLoginFailed(msg:String)
    }

    /**
     * 登录数据操作接口
     */
    interface ILoginPresenter:BasePresenter{
        /**
         * 教练登录
         *
         * @param loginModel 登录信息
         */
        fun loginCoach(loginModel: LoginModel)

        /**
         * 学员登录
         *
         * @param loginModel 登录信息
         */
        fun loginStudent(loginModel: LoginModel)

        /**
         * 管理员登录
         *
         * @param loginModel 登录信息
         */
        fun loginAdmin(loginModel: LoginModel)
    }

    /**
     * 登录数据源接口
     */
    interface ILoginDataSource:BaseDataSource<LoginModel>{
        /**
         * 请求教练登录
         *
         * @param loginModel 登录信息
         * @param callback 登录回调
         */
        fun requestLoginCoach(loginModel: LoginModel, callback:BaseDataSource.LoadSourceCallback<LoginModel>)

        /**
         * 请求学员登录
         *
         * @param loginModel 登录信息
         * @param callback 登录回调
         */
        fun requestLoginStudent(loginModel: LoginModel, callback:BaseDataSource.LoadSourceCallback<LoginModel>)

        /**
         * 请求管理员登录
         *
         * @param loginModel 登录信息
         * @param callback 登录回调
         */
        fun requestLoginAdmin(loginModel: LoginModel, callback:BaseDataSource.LoadSourceCallback<LoginModel>)
    }
}
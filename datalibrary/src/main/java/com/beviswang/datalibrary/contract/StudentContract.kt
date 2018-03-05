package com.beviswang.datalibrary.contract

import com.beviswang.datalibrary.BaseDataSource
import com.beviswang.datalibrary.BasePresenter
import com.beviswang.datalibrary.BaseView
import com.beviswang.datalibrary.model.StudentModel

/**
 * 学员契约接口
 * Created by shize on 2018/1/16.
 */
interface StudentContract {
    /**
     * 学员页面接口
     */
    interface IStudentView:BaseView<IStudentPresenter>{
        /**
         * 登出成功
         */
        fun showLogoutSucceed()

        /**
         * 登出失败
         *
         * @param msg 失败信息
         */
        fun showLogoutFailed(msg:String)
    }

    /**
     * 学员数据操作接口
     */
    interface IStudentPresenter:BasePresenter{
        /**
         * 学员登出
         */
        fun logoutStudent()
    }

    /**
     * 学员数据源接口
     */
    interface IStudentDataSource:BaseDataSource<StudentModel>{
        /**
         * 请求登出
         *
         * @param callback 请求回调
         */
        fun requestLogout(callback: BaseDataSource.LoadSourceCallback<StudentModel>)
    }
}
package com.beviswang.datalibrary.contract

import com.beviswang.datalibrary.BaseDataSource
import com.beviswang.datalibrary.BasePresenter
import com.beviswang.datalibrary.BaseView
import com.beviswang.datalibrary.model.RegisterModel

/**
 * 注册契约接口
 * Created by shize on 2018/1/15.
 */
interface RegisterContract {
    /**
     * 注册页面接口
     */
    interface IRegisterView : BaseView<IRegisterPresenter> {
        /**
         * 注册成功
         *
         * @param info 注册应答信息
         */
        fun showRegisterSucceed(info: RegisterModel)

        /**
         * 注册失败
         *
         * @param msg 失败信息
         */
        fun showRegisterFailed(msg: String)
    }

    /**
     * 注册操作接口
     */
    interface IRegisterPresenter : BasePresenter {
        /**
         * 注册终端
         *
         * @param registerModel 注册信息
         */
        fun registerTerminal(registerModel: RegisterModel)
    }

    /**
     * 注册数据源接口
     */
    interface IRegisterDataSource : BaseDataSource<RegisterModel> {
        /**
         * 提交注册信息
         *
         * @param registerModel 注册信息
         */
        fun setRegisterModel(registerModel: RegisterModel)
    }
}
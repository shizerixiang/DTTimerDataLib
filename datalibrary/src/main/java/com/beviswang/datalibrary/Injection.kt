package com.beviswang.datalibrary

import android.content.Context
import com.beviswang.datalibrary.contract.*
import com.beviswang.datalibrary.source.repository.*

/**
 * 注入数据源类
 * Created by shize on 2018/1/15.
 */
object Injection {
    /**
     * 获取注册页数据源
     *
     * @return 注册页数据源
     */
    fun provideRegisterRepository(): RegisterContract.IRegisterDataSource {
        return RegisterRepository.newInstance()
    }

    /**
     * 获取登录页数据源
     *
     * @return 登录页数据源
     */
    fun provideLoginRepository():LoginContract.ILoginDataSource{
        return LoginRepository.newInstance()
    }

    /**
     * 获取启动页数据源
     *
     * @return 启动页数据源
     */
    fun provideSplashRepository():SplashContract.ISplashDataSource{
        return SplashRepository.newInstance()
    }

    /**
     * 获取设置页数据源
     *
     * @return 设置页数据源
     */
    fun provideSettingRepository():SettingContract.ISettingDataSource{
        return SettingRepository.newInstance()
    }

    /**
     * 获取教练信息页数据源
     *
     * @param context 上下文
     * @return 教练信息页数据源
     */
    fun provideCoachRepository(context:Context):CoachContract.ICoachDataSource{
        return CoachRepository.newInstance(context)
    }

    /**
     * 获取学员信息页数据源
     *
     * @param context 上下文
     * @return 学员信息页数据源
     */
    fun provideStudentRepository(context:Context):StudentContract.IStudentDataSource{
        return StudentRepository.newInstance(context)
    }
}
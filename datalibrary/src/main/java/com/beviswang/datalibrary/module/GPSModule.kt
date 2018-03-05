package com.beviswang.datalibrary.module

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.beviswang.datalibrary.model.LocationInfoModel
import com.beviswang.datalibrary.util.SystemHelper
import java.lang.ref.WeakReference

/**
 * GPS 模块
 * Created by shize on 2017/12/28.
 */
@SuppressLint("MissingPermission")
open class GPSModule(context: Context) {
    // 上下文弱引用
    private val mWeakContext = WeakReference<Context>(context)
    private lateinit var mLocationManager: LocationManager

    /**
     * 获取位置信息
     *
     * @param l 位置信息
     * @return 返回位置信息模型，GPS 设备出现异常时为空
     */
    open fun getLocationInfo(l: Location?): LocationInfoModel? {
        val locationInfo = LocationInfoModel()
        try {
            var location = l
            // 位置为空，则获取最后一次记录的位置
            if (location == null) {
                location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
            if (location == null) {
                Log.e(javaClass.simpleName, "没有获取到位置信息！")
                return null
            }
            GPS_LATITUDE = (location.latitude * 1000000).toLong()
            GPS_LONGITUDE = (location.longitude * 1000000).toLong()
            GPS_SPEED = (location.speed * 3.6f * 10).toInt()
            GPS_DIR = location.bearing.toInt()
            GPS_TIME = SystemHelper.stampToDate(location.time)

            locationInfo.mLatitude = location.latitude
            locationInfo.mLongitude = location.longitude
            locationInfo.mSpeed = location.speed
            locationInfo.mTime = GPS_TIME
            locationInfo.mDir = location.bearing

            Log.i(javaClass.simpleName, "当前位置信息：经度=${location.latitude}，" +
                    "纬度=${location.longitude}，速度=${location.speed}，" +
                    "时间=${GPS_TIME}，方向=${location.bearing}")

        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 搜索指定位置城市
//        if (mWeakContext.get() == null) return locationInfo
//        LocationHelper.searchLocationCityAdCode(mWeakContext.get()!!, locationInfo.mLatitude,
//                locationInfo.mLongitude, object : LocationHelper.OnSearchLocationListener {
//            override fun onResult(adCode: String, locationName: String) {
//                Log.i(javaClass.simpleName, " 行政区划代码：$adCode \n 详细地址： $locationName")
//            }
//        })
        return locationInfo
    }

    /**
     * 初始化位置监听器
     *
     * @return 初始化结果
     */
    open fun initPositioner(): Boolean {
        val context = mWeakContext.get() ?: return false
        mLocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
                3f, object : LocationListener {
            override fun onLocationChanged(location: Location?) {
                // 位置改变
                getLocationInfo(location)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                // 状态改变
                getLocationInfo(null)
            }

            override fun onProviderEnabled(provider: String?) {
                // 位置启用
                Toast.makeText(context, "位置恢复！", Toast.LENGTH_LONG).show()
            }

            override fun onProviderDisabled(provider: String?) {
                // 位置禁用
                Toast.makeText(context, "位置禁用，请打开系统定位！", Toast.LENGTH_LONG).show()
            }
        })
        return true
    }

    companion object {
        // 纬度乘以 10 的 6 次方
        var GPS_LATITUDE: Long = 0L
        // 经度乘以 10 的 6 次方
        var GPS_LONGITUDE: Long = 0L
        // 1/10 km/h 卫星定位获取的速度，精确一位小数，乘以十
        var GPS_SPEED: Int = 0
        // 方向，0-359，正北为 0，顺时针
        var GPS_DIR: Int = 0
        // 时间，YYMMDDhhmmss 时间格式
        var GPS_TIME: String = "000000000000"

        private var INSTANCE: GPSModule? = null

        /**
         * 获取实例
         *
         * @param context 上下文
         */
        fun getInstance(context: Context): GPSModule {
            if (INSTANCE == null)
                INSTANCE = GPSModule(context)
            return INSTANCE!!
        }

        /** 获取初始化之后的 GPS */
        fun getInstance(): GPSModule {
            return INSTANCE!!
        }

        /** 销毁实例 */
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
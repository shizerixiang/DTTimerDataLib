package com.beviswang.datalibrary.util

import android.content.Context
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.*

/**
 * 位置工具类
 * Created by shize on 2018/1/10.
 */
object LocationHelper {
    /**
     * 搜索位置城市的行政区划代码
     *
     * @param context 上下文
     * @param latitude 纬度
     * @param longitude 经度
     * @param listener 位置结果监听器
     */
    fun searchLocationCityAdCode(context: Context, latitude: Double, longitude: Double, listener: OnSearchLocationListener) {
        val gCodeSearch = GeocodeSearch(context)
        gCodeSearch.setOnGeocodeSearchListener(object : GeocodeSearch.OnGeocodeSearchListener {
            var formatAddress: String = "" // 详细位置信息

            override fun onRegeocodeSearched(p0: RegeocodeResult?, p1: Int) {
                if (p0 == null) return
                val regeocodeAddress = p0.regeocodeAddress
                formatAddress = regeocodeAddress.formatAddress
                // 搜索详细地址的行政区划代码
                val gCodeQuery = GeocodeQuery(formatAddress, null)
                gCodeSearch.getFromLocationNameAsyn(gCodeQuery)
            }

            override fun onGeocodeSearched(p0: GeocodeResult?, p1: Int) {
                if (p0 == null || p0.geocodeAddressList.size == 0) return
                val code = p0.geocodeAddressList[0].adcode
                listener.onResult(code, formatAddress)
            }
        })
        // 逆地理编码查询条件：逆地理编码查询的地理坐标点、查询范围、坐标类型。
        val llp = LatLonPoint(latitude, longitude)
        val query = RegeocodeQuery(llp, 500f, GeocodeSearch.AMAP)
        // 异步查询
        gCodeSearch.getFromLocationAsyn(query)
    }

    /**
     * 位置搜索结果监听器
     */
    interface OnSearchLocationListener {
        /**
         * 结果回调
         *
         * @param adCode 行政区划代码
         * @param locationName 详细地址
         */
        fun onResult(adCode: String, locationName: String)
    }
}
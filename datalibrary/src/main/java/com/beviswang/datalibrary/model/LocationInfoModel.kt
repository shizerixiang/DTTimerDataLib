package com.beviswang.datalibrary.model

import android.os.Parcel
import android.os.Parcelable

/**
 * 需要的位置信息
 * Created by shize on 2017/12/28.
 */
class LocationInfoModel() : Parcelable {
    var mLongitude: Double = 0.0 // 经度
    var mLatitude: Double = 0.0 // 纬度
    var mSpeed: Float = 0f // 速度 km/h
        set(value) {
            field = value * 3.6f
        }
    var mTime: String = "000000000000" // YYMMDDhhmmss 时间格式
    var mDir:Float = 0F // 方向

    constructor(parcel: Parcel) : this() {
        mLongitude = parcel.readDouble()
        mLatitude = parcel.readDouble()
        mSpeed = parcel.readFloat()
        mTime = parcel.readString()
        mDir = parcel.readFloat()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(mLongitude)
        parcel.writeDouble(mLatitude)
        parcel.writeFloat(mSpeed)
        parcel.writeString(mTime)
        parcel.writeFloat(mDir)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LocationInfoModel> {
        override fun createFromParcel(parcel: Parcel): LocationInfoModel {
            return LocationInfoModel(parcel)
        }

        override fun newArray(size: Int): Array<LocationInfoModel?> {
            return arrayOfNulls(size)
        }
    }
}
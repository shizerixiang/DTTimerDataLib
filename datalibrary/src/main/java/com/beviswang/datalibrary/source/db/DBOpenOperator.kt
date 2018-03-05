package com.beviswang.datalibrary.source.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

//import com.beviswang.datalibrary.model.TrainInfoModel.Companion.ALL_MILE
//import com.beviswang.datalibrary.model.TrainInfoModel.Companion.ALL_TIME
//import com.beviswang.datalibrary.model.TrainInfoModel.Companion.CARD_NUM
//import com.beviswang.datalibrary.model.TrainInfoModel.Companion.CLASS_ID
//import com.beviswang.datalibrary.model.TrainInfoModel.Companion.COACH_NUM
//import com.beviswang.datalibrary.model.TrainInfoModel.Companion.COURSE_ID
//import com.beviswang.datalibrary.model.TrainInfoModel.Companion.GPS_DATA
//import com.beviswang.datalibrary.model.TrainInfoModel.Companion.HOURS_ID
//import com.beviswang.datalibrary.model.TrainInfoModel.Companion.ID
//import com.beviswang.datalibrary.model.TrainInfoModel.Companion.MAX_SPEED
//import com.beviswang.datalibrary.model.TrainInfoModel.Companion.MSG_ID
//import com.beviswang.datalibrary.model.TrainInfoModel.Companion.PHOTO
//import com.beviswang.datalibrary.model.TrainInfoModel.Companion.PIC_EVENT
//import com.beviswang.datalibrary.model.TrainInfoModel.Companion.PIC_ID
//import com.beviswang.datalibrary.model.TrainInfoModel.Companion.RECORD_STATUS
//import com.beviswang.datalibrary.model.TrainInfoModel.Companion.RECORD_TIME
//import com.beviswang.datalibrary.model.TrainInfoModel.Companion.STATUS
//import com.beviswang.datalibrary.model.TrainInfoModel.Companion.STU_NUM
//import com.beviswang.datalibrary.model.TrainInfoModel.Companion.SUB_MILE
//import com.beviswang.datalibrary.model.TrainInfoModel.Companion.TYPE
//import com.beviswang.datalibrary.model.TrainInfoModel.Companion.VEH_TYPE

/**
 * 数据库创建者
 * Created by shize on 2017/12/26.
 */
class DBOpenOperator(context: Context, name: String, factory: SQLiteDatabase.CursorFactory?, version: Int)
    : SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase?) {
        // 创建培训信息表
//        db?.execSQL("create table TrainInfo(" +
//                "$ID Integer primary key autoincrement," +
//                "$TYPE text," +
//                "$VEH_TYPE text," +
//                "$RECORD_TIME text," +
//                "$HOURS_ID text," +
//                "$PIC_ID text," +
//                "$COACH_NUM text," +
//                "$CARD_NUM text," +
//                "$STU_NUM text," +
//                "$COURSE_ID text," +
//                "$CLASS_ID text," +
//                "$RECORD_STATUS text," +
//                "$MAX_SPEED double," +
//                "$SUB_MILE double," +
//                "$ALL_TIME Integer," +
//                "$ALL_MILE double," +
//                "$PIC_EVENT text," +
//                "$PHOTO text," +
//                "$GPS_DATA text," +
//                "$STATUS text," +
//                "$MSG_ID text)")
        // 创建照片信息表
        db?.execSQL("create table PhotoInfo(" +
                "id Integer primary key autoincrement," +   // 表数据编号
                "picId String unique," +                    // 照片编号
                "path text," +                              // 照片路径
                "personnelNum text," +                      // 教练或学员编号
                "uploadModel Integer," +                    // 上传模式
                "cameraChannelNum Integer," +               // 摄像头通道
                "pictureSize Integer," +                    // 照片尺寸
                "eventType Integer," +                      // 发起拍照事件类型
                "classId Integer," +                        // 培训课堂编号
                "mGNSSDataMsg text," +                      // GNSS 数据包
                "faceReliability Integer," +                // 人脸识别置信度
                "time integer," +                           // 拍摄时间
                "isUpload Integer)")                        // 是否已经上传到平台 0 未上传 1 已上传
        // 离线缓存消息
//        db?.execSQL("create table CacheMsg()")
        // 上报学时记录数据表
        db?.execSQL("create table HoursReportTable(" +
                "id Integer primary key autoincrement," +   // 表数据编号
                "hoursNum text," +                          // 学时记录编号
                "coachNum text," +                          // 教练编号
                "studentNum text," +                        // 学员编号
                "classId String," +                         // 课堂 ID
                "reportTime text," +                        // 上报时间
                "curCourse text," +                         // 当前课程
                "recodeState Integer," +                    // 记录状态
                "maxSpeed Integer," +                       // 1 min 内的最大速度
                "mileage Integer," +                        // 1 min 内的行驶里程
                "location text," +                          // GNSS 数据包
                "time Integer," +                           // 数据生成时间
                "isUpload Integer)")                        // 是否已经上传到平台 0 未上传 1 已上传
        // 位置数据表
        db?.execSQL("create table LocationTable(" +
                "id Integer primary key autoincrement," +   // 汇报位置信息消息编号
                "locationMsg text," +                       // 汇报位置信息消息包
                "isUpload Integer)")                        // 是否已经上传到平台 0 未上传 1 已上传
//        // 定时拍照照片初始化消息数据表
//        db?.execSQL("create table PhotoInitTable(" +
//                "id Integer primary key autoincrement," +   // 表数据编号
//                "photoId Integer," +                        // 照片编号
//                "personnelNum text," +                      // 教练或学员编号
//                "uploadModel Integer," +                    // 上传模式
//                "cameraChannelNum Integer," +               // 摄像头通道
//                "pictureSize Integer," +                    // 照片尺寸
//                "eventType Integer," +                      // 发起拍照事件类型
//                "packageTotal Integer," +                   // 分包总数
//                "dataSize Integer," +                       // 数据大小
//                "classId Integer," +                        // 培训课堂编号
//                "mGNSSDataMsg text," +                      // GNSS 数据包
//                "faceReliability Integer)")                 // 人脸识别置信度
//        db?.execSQL("create table LocationTable(" +
//                "id Integer primary key autoincrement," +
//                "alarmSign text," +
//                "stateBit text," +
//                "latitude text," +
//                "longitude text," +
//                "driveSpeed Integer," +
//                "satelliteSpeed Integer," +
//                "direction Integer," +
//                "time text," +
//                "additionalInfo text)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }
}
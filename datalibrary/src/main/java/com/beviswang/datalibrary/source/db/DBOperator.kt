package com.beviswang.datalibrary.source.db

import android.content.Context
import android.database.Cursor
import com.beviswang.datalibrary.Publish
import com.beviswang.datalibrary.logD
import com.beviswang.datalibrary.logE
import com.beviswang.datalibrary.message.GNSSDataMsg
import com.beviswang.datalibrary.message.PackageMsg
import com.beviswang.datalibrary.message.PasBodyMsg
import com.beviswang.datalibrary.message.passthrough.CacheData
import com.beviswang.datalibrary.message.passthrough.HoursReportData
import com.beviswang.datalibrary.model.PhotoModel
import com.beviswang.datalibrary.util.ConvertHelper
import com.beviswang.datalibrary.util.MessageHelper
import com.beviswang.datalibrary.util.SystemHelper

//import com.beviswang.datalibrary.model.TrainInfoModel
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
 * 数据库操作类
 * Created by shize on 2017/12/26.
 */
class DBOperator(context: Context, dbName: String, version: Int) : IMsgCacheOperator {
    // 数据库工具类
    private val mDBHelper: DBOpenOperator = DBOpenOperator(context, dbName, null, version)

//    /**
//     * 获取培训数据
//     *
//     * @param sql    语句
//     * @param filter 条件
//     * @return 培训数据列表
//     */
//    fun obtainTrainInfo(sql: String, filter: Array<String>?): ArrayList<TrainInfoModel> {
//        val db = mDBHelper.readableDatabase
//        val cursor = db.rawQuery(sql, filter)
//        val list = ArrayList<TrainInfoModel>()
//        var model: TrainInfoModel
//        while (cursor.moveToNext()) {
//            model = TrainInfoModel()
//            model.id = cursor.getInt(cursor.getColumnIndex(ID))
//            model.type = cursor.getString(cursor.getColumnIndex(TYPE))
//            model.vehType = cursor.getString(cursor.getColumnIndex(VEH_TYPE))
//            model.recordTime = cursor.getString(cursor.getColumnIndex(RECORD_TIME))
//            model.hoursId = cursor.getString(cursor.getColumnIndex(HOURS_ID))
//            model.picId = cursor.getString(cursor.getColumnIndex(PIC_ID))
//            model.coachNum = cursor.getString(cursor.getColumnIndex(COACH_NUM))
//            model.cardNum = cursor.getString(cursor.getColumnIndex(CARD_NUM))
//            model.stuNum = cursor.getString(cursor.getColumnIndex(STU_NUM))
//            model.courseId = cursor.getString(cursor.getColumnIndex(COURSE_ID))
//            model.classId = cursor.getString(cursor.getColumnIndex(CLASS_ID))
//            model.recordStatus = cursor.getString(cursor.getColumnIndex(RECORD_STATUS))
//            model.maxSpeed = cursor.getDouble(cursor.getColumnIndex(MAX_SPEED))
//            model.subMile = cursor.getDouble(cursor.getColumnIndex(SUB_MILE))
//            model.allTime = cursor.getInt(cursor.getColumnIndex(ALL_TIME))
//            model.allMile = cursor.getDouble(cursor.getColumnIndex(ALL_MILE))
//            model.picEvent = cursor.getString(cursor.getColumnIndex(PIC_EVENT))
//            model.photo = cursor.getString(cursor.getColumnIndex(PHOTO))
//            model.GPSData = cursor.getString(cursor.getColumnIndex(GPS_DATA))
//            model.status = cursor.getString(cursor.getColumnIndex(STATUS))
//            model.msgId = cursor.getString(cursor.getColumnIndex(MSG_ID))
//            list.add(model)
//        }
//        cursor.close()
//        db.close()
//        return list
//    }

    override fun savePictureInfo(photoModel: PhotoModel): Boolean {
        logD("$LOG_HINT 保存图片信息")
        var isCache = true
        try {
            val sql = "insert into PhotoInfo(picId,path,personnelNum,uploadModel," +
                    "cameraChannelNum,pictureSize,eventType,classId,mGNSSDataMsg,faceReliability," +
                    "time,isUpload) values(?,?,?,?,?,?,?,?,?,?,?,?)"
            execDBSQL(sql, arrayOf(photoModel.mId, photoModel.mImagePath, photoModel.personnelNum,
                    photoModel.uploadModel, photoModel.cameraChannelNum, photoModel.pictureSize,
                    photoModel.eventType, photoModel.classId, photoModel.mGNSSDataMsg!!.toString(),
                    photoModel.faceReliability, SystemHelper.systemTimeStamp, getIsCarOnLine()))
        } catch (e: Exception) {
            isCache = false
            e.printStackTrace()
        } finally {
            return isCache
        }
    }

    override fun getOffLinePictureInfo(): ArrayList<PhotoModel>? {
        logD("$LOG_HINT 获取未上传的定时拍照图片")
        val db = mDBHelper.readableDatabase
        var photoArray: ArrayList<PhotoModel>? = ArrayList()
        try {
            val sql = "select * from PhotoInfo where isUpload=0 and eventType=5"
            val cursor = db.rawQuery(sql, arrayOf())
            while (cursor.moveToNext()) {
                val picModel = getPhotoModel(cursor)
                picModel.mGNSSDataMsg?.setIsReissueMsg(true)
                photoArray?.add(picModel)
            }
            cursor.close()
        } catch (e: Exception) {
            photoArray = null
            logE("未获取到定时拍照, 且未上传的图片")
        } finally {
            db.close()
            return photoArray
        }
    }

    override fun updateReissuePictureInfo(picNumber: String) {
        logD("$LOG_HINT 更新图片信息为已上传")
        val sql = "update PhotoInfo set isUpload=1 where picId='$picNumber'"
        try {
            execDBSQL(sql, arrayOf())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun uploadFailedPictureInfo(picNumber: String) {
        logD("$LOG_HINT 上传图片过程中断网，需要通知数据库该图片未成功上传，编号：$picNumber")
        val sql = "update PhotoInfo set isUpload=0 where picId='$picNumber'"
        try {
            execDBSQL(sql, arrayOf())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getPictureInfoByNumber(number: String): PhotoModel? {
        logD("$LOG_HINT 通过图片编号获取图片信息：编号为 $number")
        val db = mDBHelper.readableDatabase
        var photoModel: PhotoModel? = null
        try {
            val sql = "select * from PhotoInfo where picId='?'"
            val cursor = db.rawQuery(sql, arrayOf(number))
            cursor.moveToNext()
            photoModel = getPhotoModel(cursor)
            cursor.close()
        } catch (e: Exception) {
            photoModel = null
            logE("未获取到图片信息！")
            e.printStackTrace()
        } finally {
            db.close()
            return photoModel
        }
    }

    override fun getPictureInfoByDate(fromDate: Long, toDate: Long): ArrayList<PhotoModel>? {
        logD("$LOG_HINT 通过日期获取图片信息")
        var photoList: ArrayList<PhotoModel>? = null
        val db = mDBHelper.readableDatabase
        try {
            photoList = ArrayList()
            val sql = "select * from PhotoInfo where time>=? and time<=?"
            val cursor = db.rawQuery(sql, arrayOf(fromDate.toString(), toDate.toString()))
            while (cursor.moveToNext()) {
                photoList.add(getPhotoModel(cursor))
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
        return photoList
    }

    /**
     * 获取照片模型
     */
    private fun getPhotoModel(cursor: Cursor): PhotoModel {
        val photoModel = PhotoModel()
        photoModel.mId = MessageHelper.supplementStr(10,
                cursor.getString(cursor.getColumnIndex("picId")), 0)
        photoModel.mImagePath = cursor.getString(cursor.getColumnIndex("path"))
        photoModel.personnelNum = cursor.getString(cursor.getColumnIndex("personnelNum"))
        photoModel.uploadModel = cursor.getInt(cursor.getColumnIndex("uploadModel"))
        photoModel.cameraChannelNum = cursor.getInt(cursor.getColumnIndex("cameraChannelNum"))
        photoModel.pictureSize = cursor.getInt(cursor.getColumnIndex("pictureSize"))
        photoModel.eventType = cursor.getInt(cursor.getColumnIndex("eventType"))
        photoModel.classId = cursor.getInt(cursor.getColumnIndex("classId"))
        photoModel.mGNSSDataMsg = GNSSDataMsg(cursor.getString(cursor.getColumnIndex("mGNSSDataMsg")))
        photoModel.faceReliability = cursor.getInt(cursor.getColumnIndex("faceReliability"))
        photoModel.mTime = cursor.getLong(cursor.getColumnIndex("time"))
        return photoModel
    }

    override fun cacheHoursMsg(hoursReportData: HoursReportData): Boolean {
        logD("$LOG_HINT 缓存学时消息")
        hoursReportData.mGNSSData.setIsReissueMsg(true)
        var isCache = true
        try {
            val sql = "insert into HoursReportTable(hoursNum,coachNum,studentNum,classId,reportTime," +
                    "curCourse,recodeState,maxSpeed,mileage,location,time,isUpload) values(?,?,?,?,?,?,?,?,?,?,?,?)"
            execDBSQL(sql, arrayOf(hoursReportData.hoursNum, hoursReportData.coachNum,
                    hoursReportData.stuNum, hoursReportData.classId, hoursReportData.reportTime,
                    hoursReportData.curCourse, hoursReportData.recodeState, hoursReportData.maxSpeed,
                    hoursReportData.mileage, hoursReportData.mGNSSData.toString(),
                    SystemHelper.systemTimeStamp, getIsCarOnLine()))
        } catch (e: Exception) {
            isCache = false
            e.printStackTrace()
        } finally {
            return isCache
        }
    }

    override fun getOffLineHoursMsg(): ArrayList<HoursReportData>? {
        logD("$LOG_HINT 获取未上传的缓存学时消息")
        val sql = "select * from HoursReportTable where isUpload=0"
        return getHoursReportData(sql, arrayOf())
    }

    override fun updateReissueHoursMsg(hoursNumber: String) {
        logD("$LOG_HINT 更新学时记录为已上传，学时编号：$hoursNumber")
        val sql = "update HoursReportTable set isUpload=1 where hoursNum='$hoursNumber'"
        try {
            execDBSQL(sql, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getCacheHoursMsg(): ArrayList<HoursReportData>? {
        logD("$LOG_HINT 获取缓存学时消息")
        val sql = "select * from HoursReportTable"
        return getHoursReportData(sql, arrayOf())
    }

    override fun getCacheHoursMsgByDate(fromDate: Long, toDate: Long): ArrayList<HoursReportData>? {
        logD("$LOG_HINT 通过日期获取学时消息")
        val sql = "select * from HoursReportTable where time>=? and time<=?"
        return getHoursReportData(sql, arrayOf(fromDate.toString(), toDate.toString()))
    }

    override fun getCacheHoursMsgByCount(count: Int): ArrayList<HoursReportData>? {
        logD("$LOG_HINT 获取最近 $count 个学时记录")
        val hours = getCacheHoursMsg()
        return hours?.subList(hours.size - count, hours.size) as ArrayList<HoursReportData>?
    }

    override fun cacheGNSSMsg(mGNSSDataMsg: GNSSDataMsg): Boolean {
        logD("$LOG_HINT 缓存 GNSS 数据包")
        var isCache = true
        try {
            val sql = "insert into LocationTable(locationMsg,isUpload) values(?,?)"
            execDBSQL(sql, arrayOf(mGNSSDataMsg.toString(), getIsCarOnLine()))
        } catch (e: Exception) {
            isCache = false
            e.printStackTrace()
        } finally {
            return isCache
        }
    }

    override fun getOffLineGNSSMsg(): ArrayList<GNSSDataMsg>? {
        logD("$LOG_HINT 获取未上传的缓存 GNSS 数据包")
        val db = mDBHelper.readableDatabase
        var dataList: ArrayList<GNSSDataMsg>? = null
        try {
            dataList = ArrayList()
            val sql = "select * from LocationTable where isUpload=0"
            val cursor = db.rawQuery(sql, arrayOf())
            while (cursor.moveToNext()) {
                val data = GNSSDataMsg(cursor.getString(cursor.getColumnIndex("locationMsg")))
                data.mId = cursor.getInt(cursor.getColumnIndex("id"))
                data.setIsReissueMsg(true)
                dataList.add(data)
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
        return dataList
    }

    override fun updateReissueGNSSMsg(GNSSData: GNSSDataMsg) {
        logD("$LOG_HINT 更新位置信息为已上传")
        val sql = "update LocationTable set isUpload=1 where id=${GNSSData.mId}"
        try {
            execDBSQL(sql, arrayOf())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getCacheGNSSMsg(): ArrayList<GNSSDataMsg>? {
        logD("$LOG_HINT 获取缓存 GNSS 数据包")
        val db = mDBHelper.readableDatabase
        var dataList: ArrayList<GNSSDataMsg>? = null
        try {
            dataList = ArrayList()
            val sql = "select * from LocationTable"
            val cursor = db.rawQuery(sql, arrayOf())
            while (cursor.moveToNext()) {
                val data = GNSSDataMsg(cursor.getString(cursor.getColumnIndex("locationMsg")))
                dataList.add(data)
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
        return dataList
    }

    override fun cacheMsg(packageMsg: PackageMsg): Boolean {
        logD("$LOG_HINT 缓存离线透传消息包")
        var isCache = true
        try {
            val sql = "insert into CacheMsgTable(msgId,msgContent) values(?,?)"
            val pasMsg = packageMsg.getMsgBody() as PasBodyMsg
            execDBSQL(sql, arrayOf(pasMsg.getMsgId(), pasMsg.getDataContent()!!.toString()))
        } catch (e: Exception) {
            isCache = false
            e.printStackTrace()
        } finally {
            return isCache
        }
    }

    override fun getOffLineMsg(): ArrayList<PackageMsg>? {
        logD("$LOG_HINT 获取缓存的离线透传消息包")
        val db = mDBHelper.readableDatabase
        var pMsgArray: ArrayList<PackageMsg>? = ArrayList()
        try {
            val sql = "select * from CacheMsgTable"
            val cursor = db.rawQuery(sql, null)
            while (cursor.moveToNext()) {
                val pMsg = MessageHelper.getUpstreamMsg(cursor.getString(
                        cursor.getColumnIndex("msgId")), CacheData(
                        cursor.getString(cursor.getColumnIndex("msgContent"))),
                        true, false)
                pMsg.mId = cursor.getInt(cursor.getColumnIndex("id"))
                pMsgArray?.add(pMsg)
            }
            cursor.close()
        } catch (e: Exception) {
            pMsgArray = null
            e.printStackTrace()
        } finally {
            db.close()
            return pMsgArray
        }
    }

    override fun deleteMsg(packageId: Int) {
        logD("$LOG_HINT 删除缓存的离线透传消息包")
        try {
            val sql = "delete from CacheMsgTable where id=?"
            execDBSQL(sql, arrayOf(packageId))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

//    override fun cachePictureInitMsg(upInitData: UploadPictureData.UpInitData): Boolean {
//        logD("$LOG_HINT 缓存图片初始化消息")
//        try {
//            val sql = "insert into PhotoInitTable(photoId,personnelNum,uploadModel,cameraChannelNum," +
//                    "pictureSize,eventType,packageTotal,dataSize,classId,mGNSSDataMsg,faceReliability)" +
//                    " values(?,?,?,?,?,?,?,?,?,?,?)"
//            execDBSQL(sql, arrayOf(upInitData.pictureNumber, upInitData.personnelNum,
//                    upInitData.uploadModel, upInitData.cameraChannelNum, upInitData.pictureSize,
//                    upInitData.eventType, upInitData.packageTotal, upInitData.dataSize,
//                    upInitData.classId, upInitData.mGNSSDataMsg, upInitData.faceReliability))
//        } catch (e: Exception) {
//            e.printStackTrace()
//            return false
//        }
//        return true
//    }
//
//    override fun getCachePictureInitMsg(): ArrayList<UploadPictureData.UpInitData>? {
//        logD("$LOG_HINT 获取缓存的图片初始化消息")
//        var dataList: ArrayList<UploadPictureData.UpInitData>? = null
//        val db = mDBHelper.readableDatabase
//        try {
//            dataList = ArrayList()
//            val sql = "select * from PhotoInitTable"
//            val cursor = db.rawQuery(sql, arrayOf())
//            while (cursor.moveToNext()) {
//                val data = UploadPictureData.UpInitData()
//                data.pictureNumber = cursor.getString(cursor.getColumnIndex("photoId"))
//                data.personnelNum = cursor.getString(cursor.getColumnIndex("personnelNum"))
//                data.uploadModel = cursor.getInt(cursor.getColumnIndex("uploadModel"))
//                data.cameraChannelNum = cursor.getInt(cursor.getColumnIndex("cameraChannelNum"))
//                data.pictureSize = cursor.getInt(cursor.getColumnIndex("pictureSize"))
//                data.eventType = cursor.getInt(cursor.getColumnIndex("eventType"))
//                data.packageTotal = cursor.getInt(cursor.getColumnIndex("packageTotal"))
//                data.dataSize = cursor.getInt(cursor.getColumnIndex("dataSize"))
//                data.classId = cursor.getInt(cursor.getColumnIndex("classId"))
//                data.mGNSSDataMsg = GNSSDataMsg(cursor.getString(cursor.getColumnIndex("mGNSSDataMsg")))
//                data.faceReliability = cursor.getInt(cursor.getColumnIndex("faceReliability"))
//                dataList.add(data)
//            }
//            cursor.close()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        } finally {
//            db.close()
//        }
//        return dataList
//    }

    /**
     * 获取上报学时记录消息数据
     *
     * @param sql 查询语句
     * @param filter 条件参数
     * @return 学时记录列表
     */
    private fun getHoursReportData(sql: String, filter: Array<String>): ArrayList<HoursReportData>? {
        val db = mDBHelper.readableDatabase
        var hoursList: ArrayList<HoursReportData>? = null
        try {
            hoursList = ArrayList()
            var hours: HoursReportData
            val cursor = db.rawQuery(sql, filter)
            while (cursor.moveToNext()) {
                hours = HoursReportData()
                hours.hoursNum = cursor.getString(cursor.getColumnIndex("hoursNum"))
                hours.coachNum = cursor.getString(cursor.getColumnIndex("coachNum"))
                hours.stuNum = cursor.getString(cursor.getColumnIndex("studentNum"))
                hours.classId = cursor.getInt(cursor.getColumnIndex("classId"))
                hours.reportTime = cursor.getString(cursor.getColumnIndex("reportTime"))
                hours.curCourse = cursor.getString(cursor.getColumnIndex("curCourse"))
                hours.recodeState = cursor.getInt(cursor.getColumnIndex("recodeState"))
                hours.maxSpeed = cursor.getInt(cursor.getColumnIndex("maxSpeed"))
                hours.mileage = cursor.getInt(cursor.getColumnIndex("mileage"))
                hours.mGNSSData = GNSSDataMsg(cursor.getString(cursor.getColumnIndex("location")))
                hoursList.add(hours)
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
        return hoursList
    }

    /**
     * 执行语句
     *
     * @param sql    语句
     * @param filter 字段值
     */
    private fun execDBSQL(sql: String, filter: Array<Any>?) {
        val db = mDBHelper.writableDatabase
        if (filter == null) {
            db.execSQL(sql)
        } else {
            db.execSQL(sql, filter)
        }
        db.close()
    }

    /**
     * @return 在线返回 1 ,不在线返回 0
     */
    private fun getIsCarOnLine(): Int = if (Publish.getInstance().mIsCarOnLine) 1 else 0

    companion object {
        //        // SQL 语句
//        val DB_ADD_TRAIN_INFO = "insert into TrainInfo(" +
//                "$TYPE,$VEH_TYPE,$RECORD_TIME,$HOURS_ID,$PIC_ID," +
//                "$COACH_NUM,$CARD_NUM,$STU_NUM,$COURSE_ID,$CLASS_ID," +
//                "$RECORD_STATUS,$MAX_SPEED,$SUB_MILE,$ALL_TIME,$ALL_MILE," +
//                "$PIC_EVENT,$PHOTO,$GPS_DATA,$STATUS,$MSG_ID)" +
//                " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
//        val DB_REMOVE_TRAIN_INFO = "delete from TrainInfo where id=?"
//        val DB_ALL_TRAIN_INFO = "select * from TrainInfo order by id asc"
        val LOG_HINT = "数据库："
    }
}
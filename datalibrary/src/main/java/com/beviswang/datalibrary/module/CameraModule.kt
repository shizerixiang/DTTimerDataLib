package com.beviswang.datalibrary.module

import android.content.Context
import android.media.MediaPlayer
import android.media.AudioManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Camera
import android.net.Uri
import android.view.SurfaceHolder
import com.beviswang.datalibrary.Publish
import com.beviswang.datalibrary.logD
import com.beviswang.datalibrary.logE
import com.beviswang.datalibrary.model.PhotoTextModel
import com.beviswang.datalibrary.util.BitmapHelper
import com.beviswang.datalibrary.util.FileHelper
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.ref.WeakReference

/**
 * 相机模块
 * Created by shize on 2018/1/4.
 */
class CameraModule(context: Context) : Camera.PictureCallback {
    private val mWeakContext = WeakReference<Context>(context)
    private var mCamera: Camera? = null
    private lateinit var mCallback: OnTakeOver
    // 需要的图片尺寸
    private var mPicSize = 0x06

    /**
     * 开启预览并初始化相机
     *
     * @param surfaceHolder 预览显示 holder
     */
    fun startPreview(surfaceHolder: SurfaceHolder) {
        if (mWeakContext.get() == null) {
            logD("startPreview will return")
            return
        }

        if (mCamera != null) {
            stopPreview()
        }

        mCamera = Camera.open(CAMERA_ID)
        val parameters = mCamera!!.parameters
        val width = mWeakContext.get()!!.resources.displayMetrics.widthPixels
        val height = mWeakContext.get()!!.resources.displayMetrics.heightPixels
        val size = getBestPreviewSize(width, height, parameters)
        if (size != null) {
            //设置预览分辨率
            parameters.setPreviewSize(size.width, size.height)
            //设置保存图片的大小
            parameters.setPictureSize(size.width, size.height)
        }

        //自动对焦
        parameters.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
        parameters.previewFrameRate = 20

        //设置相机预览方向
        mCamera!!.setDisplayOrientation(0)

        mCamera!!.parameters = parameters

        try {
            mCamera!!.setPreviewDisplay(surfaceHolder)
        } catch (e: Exception) {
            logD(e.message)
        }

        mCamera!!.startPreview()
    }

    /**
     * 停止预览并释放相机
     */
    fun stopPreview() {
        //释放Camera对象
        if (mCamera != null) {
            try {
                mCamera!!.setPreviewDisplay(null)
                mCamera!!.stopPreview()
                mCamera!!.release()
                mCamera = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 获取最佳显示尺寸
     */
    private fun getBestPreviewSize(width: Int, height: Int,
                                   parameters: Camera.Parameters): Camera.Size? {
        var result: Camera.Size? = null

        for (size in parameters.supportedPreviewSizes) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size
                } else {
                    val resultArea = result.width * result.height
                    val newArea = size.width * size.height

                    if (newArea > resultArea) {
                        result = size
                    }
                }
            }
        }
        return result
    }

    /**
     * 拍照
     *
     * @param callback 拍照回调
     * @param size 图片尺寸
     */
    fun takePhoto(callback: OnTakeOver, size: Int) {
        mCallback = callback
        mPicSize = size
        mCamera!!.takePicture(null, null, null, this@CameraModule)
        playSound()
    }

    /**
     * 播放系统拍照声音
     */
    private fun playSound() {
        if (mWeakContext.get() == null) return
        var mediaPlayer: MediaPlayer? = null
        val audioManager = mWeakContext.get()!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val volume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)

        if (volume != 0) {
            if (mediaPlayer == null)
                mediaPlayer = MediaPlayer.create(mWeakContext.get()!!,
                        Uri.parse("file:///system/media/audio/ui/camera_click.ogg"))
            if (mediaPlayer != null) {
                mediaPlayer.start()
            }
        }
    }

    override fun onPictureTaken(data: ByteArray, camera: Camera) {
        try {
            val opt = BitmapFactory.Options()
            opt.inPreferredConfig = Bitmap.Config.RGB_565
            //旋转角度，保证保存的图片方向是对的
            var bitmap = BitmapFactory.decodeByteArray(data, 0, data.size, opt)
            val matrix = Matrix()
            // 图片保存的方向
            matrix.setRotate(0f)
            // 将图片保存为指定尺寸
            bitmap = resizeBitmap(bitmap, matrix)
            // 合成图片和文字信息
            bitmap = BitmapHelper.synthesisImage(getPhotoText(), bitmap)
            val path = Publish.DIR_FILE_PATH + "photo" + File.separator
            val fileName = System.currentTimeMillis().toString() + ".jpg"
            FileHelper.bitmap2File(bitmap, path, fileName)
            mCallback.onSucceed(path + fileName)
        } catch (e: FileNotFoundException) {
            mCallback.onFailed()
            e.printStackTrace()
        } catch (e: IOException) {
            mCallback.onFailed()
            e.printStackTrace()
        }
        mCamera!!.startPreview()
    }

    /**
     * 将图片保存为指定大小
     *
     * @param bitmap 图片
     * @param matrix
     */
    private fun resizeBitmap(bitmap: Bitmap, matrix: Matrix): Bitmap {
        var bitmap1 = bitmap
        bitmap1 = Bitmap.createBitmap(bitmap1, 0, 0,
                bitmap1.width, bitmap1.height, matrix, true)
        when (mPicSize) {
            0x01 -> bitmap1 = BitmapHelper.resizeImage(bitmap1, 240, 320)!!
            0x02 -> bitmap1 = BitmapHelper.resizeImage(bitmap1, 480, 640)!!
            0x03 -> bitmap1 = BitmapHelper.resizeImage(bitmap1, 600, 800)!!
            0x04 -> bitmap1 = BitmapHelper.resizeImage(bitmap1, 768, 1024)!!
            0x05 -> bitmap1 = BitmapHelper.resizeImage(bitmap1, 144, 176)!!
            0x06 -> bitmap1 = BitmapHelper.resizeImage(bitmap1, 288, 352)!!
            0x07 -> bitmap1 = BitmapHelper.resizeImage(bitmap1, 288, 704)!!
            0x08 -> bitmap1 = BitmapHelper.resizeImage(bitmap1, 576, 704)!!
        }
        logE("处理前 ----->>>>>> 图片高：${bitmap1.height}  图片宽：${bitmap1.width} 图片大小：${bitmap1.byteCount}")
        return bitmap1
    }

    /**
     * 获取图像合成文本信息
     */
    private fun getPhotoText(): PhotoTextModel {
        val ptm = PhotoTextModel()
        // 位置信息及速度
        val positionInfo = GPSModule.getInstance(mWeakContext.get()!!).getLocationInfo(null)
        if (positionInfo == null) {
            ptm.mPosition = "无法获取位置信息"
        } else {
            ptm.mPosition = "${positionInfo.mLongitude}E-${positionInfo.mLatitude}N"
            ptm.mSpeed = positionInfo.mSpeed.toString()
        }
        return ptm
    }

    companion object {
        // 1：前置摄像头
        private val CAMERA_ID = 0 //后置摄像头
        private val TAG = "CameraModule"

        private var INSTANCE: CameraModule? = null

        fun getInstance(context: Context): CameraModule {
            if (INSTANCE == null) INSTANCE = CameraModule(context)
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE?.stopPreview()
            INSTANCE = null
        }
    }

    interface OnTakeOver {
        /**
         * 拍照成功
         *
         * @param path 路径
         */
        fun onSucceed(path: String)

        /**
         * 拍照失败
         */
        fun onFailed()
    }
}
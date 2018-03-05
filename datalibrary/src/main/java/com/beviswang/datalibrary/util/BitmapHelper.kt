package com.beviswang.datalibrary.util

import android.graphics.*
import com.beviswang.datalibrary.model.PhotoTextModel
import java.io.ByteArrayOutputStream

/**
 * 图片文字合成器
 * Created by shize on 2017/12/28.
 */
object BitmapHelper {
    /**
     * 合成图片和文本
     *
     * @param text 文本
     * @param image 图片
     * @return 返回合成完成的图片
     */
    fun synthesisImage(text: PhotoTextModel, image: Bitmap): Bitmap {
        // 获取图片尺寸
        val height = image.height
        val width = image.width
        // 创建一个需要尺寸的 Bitmap
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        // 绘制背景图片
        val imagePaint = Paint() // 图片画笔
        canvas.drawBitmap(image, 0f, 0f, imagePaint)
        // 绘制文字到背景图片上
        val textPaint = Paint() // 文字画笔
        textPaint.color = Color.RED
        textPaint.textSize = 12f // 文字大小
        textPaint.typeface = Typeface.MONOSPACE
        textPaint.isAntiAlias = true // 设置抗锯齿
        // 起始边距
        val distanceMargin = 8f
        // 每行的高度
        val textHeight = textPaint.textSize + 6
        // 绘制文字到图片上
        canvas.drawText(text.mMechanismName, distanceMargin, textHeight, textPaint)
        canvas.drawText(text.mCoachName, distanceMargin, textHeight * 2, textPaint)
        canvas.drawText(text.mStudentName, distanceMargin, textHeight * 3, textPaint)
        canvas.drawText(text.mPosition, distanceMargin, textHeight * 4, textPaint)
        canvas.drawText(text.mCarNo, distanceMargin, textHeight * 5, textPaint)
        canvas.drawText(text.mDateTime, distanceMargin, textHeight * 6, textPaint)
        canvas.drawText(text.mSpeed, distanceMargin, textHeight * 7, textPaint)
        // 保存为本地图片
        canvas.save()
        canvas.restore()
        image.recycle()
        return bitmap
    }

    /**
     * 按比例缩放bitmap图片
     *
     * @param bitmap     原图
     * @param targetSize 目标尺寸
     * @return 缩放图
     */
    fun scaleBitmap(bitmap: Bitmap?, targetSize: Float): Bitmap? {
        if (bitmap == null) {
            return null
        }
        var ratio = 1f
        val height = bitmap.height
        val width = bitmap.width
        if (height > targetSize) {
            ratio = targetSize / height
        }
        val matrix = Matrix()
        matrix.preScale(ratio, ratio)
        val scaleBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false)
        if (scaleBitmap == bitmap) {
            return scaleBitmap
        }
        bitmap.recycle()
        return scaleBitmap
    }

    /**
     * 将图片缩放到指定大小
     *
     * @param bitmap 图片
     * @param height 指定高度
     * @param width  指定宽度
     * @return 缩放后的图片
     */
    fun resizeImage(bitmap: Bitmap?, height: Int, width: Int): Bitmap? {
        if (bitmap == null) {
            return null
        }
        val bitmapHeight = bitmap.height
        val bitmapWidth = bitmap.width
        if (bitmap.height < height) {
            return bitmap
        }
        val scaleWidth = width.toFloat() / bitmapWidth
        val scaleHeight = height.toFloat() / bitmapHeight
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        val resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, false)
        if (resizeBitmap == bitmap) {
            return resizeBitmap
        }
        bitmap.recycle()
        return resizeBitmap
    }

    /** 将 bitmap 转换为 byteArray */
    fun bitmap2Bytes(bitmap: Bitmap):ByteArray{
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100,bos)
        return bos.toByteArray()
    }
}
package com.beviswang.datalibrary.util

import android.graphics.Bitmap
import android.util.Base64
import com.beviswang.datalibrary.logD
import com.beviswang.datalibrary.logE
import java.io.*

/**
 * 文件处理工具类
 * Created by shize on 2018/1/18.
 */
object FileHelper {
    /**
     * 根据byte数组，生成文件
     *
     * @param bfile 文件数组
     * @param filePath 文件存放路径
     * @param fileName 文件名称
     */
    fun byte2File(bfile: ByteArray, filePath: String, fileName: String) {
        var bos: BufferedOutputStream? = null
        var fos: FileOutputStream? = null
        val file: File
        try {
            val dir = File(filePath) // 文件夹文件
            if (!dir.exists() && !dir.isDirectory) {//判断文件目录是否存在
                dir.mkdirs()
            }
            file = File(filePath + fileName)
            fos = FileOutputStream(file)
            bos = BufferedOutputStream(fos)
            bos.write(bfile)
            bos.flush()
        } catch (e: Exception) {
            logE("文件写入失败：${e.message}")
            e.printStackTrace()
        } finally {
            try {
                bos?.close()
                fos?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 根据 bitmap ，生成图片文件
     *
     * @param bitmap bitmap
     * @param filePath 存放文件路径
     * @param fileName 文件名称
     */
    fun bitmap2File(bitmap: Bitmap, filePath: String, fileName: String) {
        var bos: BufferedOutputStream? = null
        var fos: FileOutputStream? = null
        val file: File
        try {
            val dir = File(filePath) // 文件夹文件
            if (!dir.exists() && !dir.isDirectory) {//判断文件目录是否存在
                dir.mkdirs()
            }
            file = File(filePath + fileName)
            fos = FileOutputStream(file)
            bos = BufferedOutputStream(fos)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
            bos.flush()
        } catch (e: Exception) {
            logE("文件写入失败：${e.message}")
            e.printStackTrace()
        } finally {
            try {
                bos?.close()
                fos?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 获得指定文件的byte数组
     *
     * @param filePath 文件绝对路径
     * @return 文件的 byteArray ，若读取失败则为 null
     */
    fun file2Byte(filePath: String): ByteArray? {
        var bos: ByteArrayOutputStream? = null
        var bis: BufferedInputStream? = null
        try {
            val file = File(filePath)
            if (!file.exists()) {
                throw FileNotFoundException("file not exists")
            }
            bos = ByteArrayOutputStream(file.length().toInt())
            bis = BufferedInputStream(FileInputStream(file))
            val bufSize = 1024
            val buffer = ByteArray(bufSize)
            var len: Int
            loop@ while (true) {
                len = bis.read(buffer, 0, bufSize)
                if (len == -1) break@loop
                logD("字节长度：$len")
                bos.write(buffer, 0, len)
            }
            return bos.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            try {
                bis?.close()
                bos?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * @param path 指定文件路径
     * @return 是否有指定路径的文件
     */
    fun hasFile(path: String): Boolean {
        return File(path).exists()
    }

    /**
     * @param path 指定文件路径
     * @return 是否成功删除文件
     */
    fun deleteFile(path: String): Boolean {
        return File(path).delete()
    }


    /**
     * 将文件转成 base64 ByteArray
     * @param path 文件路径
     * @return  Base64 ByteArray
     * @throws Exception
     */

    @Throws(Exception::class)
    fun encodeBase64File(path: String): ByteArray {
        val file = File(path)
        val inputFile = FileInputStream(file)
        val buffer = ByteArray(file.length().toInt())
        inputFile.read(buffer)
        inputFile.close()
        return Base64.encodeToString(buffer, Base64.DEFAULT).toByteArray()
    }

    /**
     * 将base64字符解码保存文件
     * @param base64Code
     * @param targetPath
     * @throws Exception
     */
    @Throws(Exception::class)
    fun decoderBase64File(base64Code: String, targetPath: String) {
        val buffer = Base64.decode(base64Code, Base64.DEFAULT)
        val out = FileOutputStream(targetPath)
        out.write(buffer)
        out.close()
    }
}
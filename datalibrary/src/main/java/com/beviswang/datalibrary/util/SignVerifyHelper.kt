package com.beviswang.datalibrary.util

import java.io.ByteArrayInputStream
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.cert.X509Certificate
import javax.crypto.Cipher
import java.util.*
import android.util.Base64
import com.beviswang.datalibrary.Publish

/**
 * 注册验证工具类
 * Created by shize on 2018/1/8.
 */
object SignVerifyHelper {

    /**
     * 证书签名：加密
     * @return 返回的字符串长度为 512 即：256 byte
     */
    @Throws(Exception::class)
    fun sign(data: String, timestamp: Long, key: PrivateKey): String? {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(data.toByteArray(charset("utf-8")))
        md.update(toBE(timestamp))
        val hash = md.digest()
        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encrypted = cipher.doFinal(hash)
        return HexBinHelper.encode(encrypted)
    }

    /**
     * 证书签名：加密
     * @return 返回字符串长度为 512 即：256 byte 的字符串
     */
    @Throws(Exception::class)
    fun sign(data:String, key: PrivateKey):String?{
        val md = MessageDigest.getInstance("SHA-256")
        md.update(data.toByteArray(charset("utf-8")))
        val hash = md.digest()
        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encrypted = cipher.doFinal(hash)
        return HexBinHelper.encode(encrypted)
    }

    /**
     * 证书签名：解密
     */
    @Throws(Exception::class)
    fun verify(data: String, timestamp: Long, encodedEncryptedStr: String,
               userCert: X509Certificate): Boolean {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(data.toByteArray())
        md.update(toBE(timestamp))
        val hash = md.digest()

        val encryptedStr = HexBinHelper.decode(encodedEncryptedStr)
        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.DECRYPT_MODE, userCert)
        val plain = cipher.doFinal(encryptedStr)
        return Arrays.equals(hash, plain)
    }

    private fun toBE(data: Long): ByteArray {
        val buffer: ByteArray
        val ts = data.toString()
        if (ts.length >= 13) {
            //平台http协议加密用，平台时间戳毫秒13位
            buffer = ByteArray(8)
            buffer[0] = data.ushr(56).toByte()
            buffer[1] = data.ushr(48).toByte()
            buffer[2] = data.ushr(40).toByte()
            buffer[3] = data.ushr(32).toByte()
            buffer[4] = data.ushr(24).toByte()
            buffer[5] = data.ushr(16).toByte()
            buffer[6] = data.ushr(8).toByte()
            buffer[7] = data.ushr(0).toByte()
        } else {
            //终端tcp协议加密用，终端时间戳秒10位
            buffer = ByteArray(4)
            buffer[0] = data.ushr(24).toByte()
            buffer[1] = data.ushr(16).toByte()
            buffer[2] = data.ushr(8).toByte()
            buffer[3] = data.ushr(0).toByte()
        }
        return buffer
    }

    /**
     * 获取证书信息
     *
     * @return PrivateKey
     */
    fun getDevCAInfo(): PrivateKey? {
        try {
            val cadata = FileHelper.file2Byte(Publish.getInstance().mCertificatePath) ?: return null
            val password = Publish.getInstance().mCertificateKey.toCharArray()
            val cabuf = Base64.decode(cadata, Base64.DEFAULT)
            val keyStore = KeyStore.getInstance("PKCS12")
            keyStore.load(ByteArrayInputStream(cabuf), password)
            val aliases = keyStore.aliases()
            if (!aliases.hasMoreElements()) {
                throw RuntimeException("no alias found")
            }
            val alias = aliases.nextElement()
            val privatekey = keyStore.getKey(alias, password) as PrivateKey //私钥
//            val cert = keyStore.getCertificate(alias) as X509Certificate    // X509Certificate证书对象
            return privatekey
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
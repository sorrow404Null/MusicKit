package com.eclipse.music.kit.utils

import android.annotation.SuppressLint
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object AesUtils {
    private const val KB = 1024L
    private const val MB = KB * 1024
    private const val GB = MB * 1024

    fun ByteArray.toLongLE(): Long =
        foldIndexed(0L) { index, acc, byte ->
            acc or ((byte.toLong() and 0xFF) shl (index * 8))
        }

    fun ByteArray.toIntLE(): Int =
        toLongLE().toInt()


    @SuppressLint("GetInstance")
    fun aesDecrypt(
        encryptedData: ByteArray,
        key: ByteArray
    ): ByteArray =
        Cipher.getInstance("AES/ECB/PKCS5Padding").run {
            init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"))
            doFinal(encryptedData)
        }

    @SuppressLint("DefaultLocale")
    fun Long.sizeIn(): String = when {
        this < KB -> "$this B"
        this < MB -> "${this / KB} KB"
        this < GB -> "${this / MB} MB"
        else -> String.format("%.2f GB", this.toDouble() / GB)
    }
}

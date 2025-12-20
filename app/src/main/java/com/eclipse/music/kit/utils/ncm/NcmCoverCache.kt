package com.eclipse.music.kit.utils.ncm

import android.graphics.Bitmap
import android.util.LruCache

object NcmCoverCache {
    private val bitmapCache = object : LruCache<String, Bitmap>(
        (Runtime.getRuntime().maxMemory() / 8).toInt()
    ) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount
        }
    }

    fun get(uri: String): Bitmap? =
        bitmapCache.get(uri)

    fun put(uri: String, bitmap: Bitmap) {
        bitmapCache.put(uri, bitmap)
    }

    fun clear() {
        bitmapCache.evictAll()
    }
}

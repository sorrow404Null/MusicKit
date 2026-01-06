package com.eclipse.music.kit.utils.ncm

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import androidx.core.content.edit
import kotlinx.serialization.json.Json

object NcmCache {
    private const val PREF = "ncm_cache"
    private const val KEY = "files"
    private val coverCache =
        object : LruCache<String, Bitmap>((Runtime.getRuntime().maxMemory() / 8).toInt()) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount
    }

    fun getCover(uri: String): Bitmap? = coverCache[uri]
    fun putCover(uri: String, bitmap: Bitmap): Bitmap? = coverCache.put(uri, bitmap)

    fun saveScan(context: Context, files: List<NcmUiFile>) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit {
            putString(KEY, Json.encodeToString(files))
        }
    }

    fun loadScan(context: Context): List<NcmUiFile> {
        val json = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY, null)
            ?: return emptyList()
        return runCatching { Json.decodeFromString<List<NcmUiFile>>(json) }.getOrDefault(emptyList())
    }
}

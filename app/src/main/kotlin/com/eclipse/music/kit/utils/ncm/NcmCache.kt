package com.eclipse.music.kit.utils.ncm

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import org.json.JSONArray
import org.json.JSONObject

object NcmCache {

    private const val PREF = "ncm_scan_cache"
    private const val KEY = "files"

    private val coverCache = object : LruCache<String, Bitmap>(
        (Runtime.getRuntime().maxMemory() / 8).toInt()
    ) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount
    }

    fun getCover(uri: String): Bitmap? =
        coverCache[uri]

    fun putCover(uri: String, bitmap: Bitmap) {
        coverCache.put(uri, bitmap)
    }

    fun clearCovers() {
        coverCache.evictAll()
    }

    fun saveScan(context: Context, files: List<NcmUiFile>) {
        val json = JSONArray().apply {
            files.forEach {
                put(
                    JSONObject().apply {
                        put("uri", it.uriKey)
                        put("name", it.displayName)
                    }
                )
            }
        }

        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit { putString(KEY, json.toString()) }
    }

    fun loadScan(context: Context): List<NcmUiFile> {
        val json = context
            .getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY, null)
            ?: return emptyList()

        return JSONArray(json).let { array ->
            List(array.length()) { index ->
                array.getJSONObject(index)
            }.mapNotNull { obj ->
                val file = DocumentFile.fromSingleUri(
                    context,
                    obj.getString("uri").toUri()
                ) ?: return@mapNotNull null

                if (!file.exists()) return@mapNotNull null

                NcmUiFile(
                    file = file,
                    displayName = obj.getString("name")
                )
            }
        }
    }

    fun clearAll(context: Context) {
        clearCovers()
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit { remove(KEY) }
    }
}

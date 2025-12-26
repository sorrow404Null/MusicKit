package com.eclipse.music.kit.utils.ncm

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object NcmCoverLoader {

    suspend fun loadBitmap(
        context: Context,
        file: DocumentFile
    ): Bitmap? = withContext(Dispatchers.IO) {

        val key = file.uri.toString()
        NcmCache.getCover(key)?.let { return@withContext it }

        val bytes =
            NcmMetaReader.readCover(context, file.uri)
                ?: return@withContext null

        val opts = BitmapFactory.Options().apply {
            inSampleSize = 4
        }

        val bitmap =
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
                ?: return@withContext null

        NcmCache.putCover(key, bitmap)
        bitmap
    }
}

package com.eclipse.music.kit.utils.ncm

import android.graphics.Bitmap
import androidx.documentfile.provider.DocumentFile

class NcmUiFile(
    val file: DocumentFile,
    val displayName: String,
    val cover: Bitmap? = null
) {
    fun withCover(bitmap: Bitmap) =
        NcmUiFile(file, displayName, bitmap)
}

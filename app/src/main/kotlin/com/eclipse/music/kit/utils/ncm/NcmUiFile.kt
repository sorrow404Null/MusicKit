package com.eclipse.music.kit.utils.ncm

import androidx.documentfile.provider.DocumentFile

data class NcmUiFile(
    val file: DocumentFile,
    val displayName: String,
    val extension: String
) {
    val uriKey: String = file.uri.toString()
}

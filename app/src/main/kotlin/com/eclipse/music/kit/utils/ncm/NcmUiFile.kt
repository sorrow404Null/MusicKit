package com.eclipse.music.kit.utils.ncm

import androidx.documentfile.provider.DocumentFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class NcmUiFile(
    val fileUri: String,
    val displayName: String,
    val extension: String,
    var isConverted: Boolean = false,
    var outputFileName: String? = null
) {
    @Transient
    var file: DocumentFile? = null

    val uriKey: String get() = fileUri
}

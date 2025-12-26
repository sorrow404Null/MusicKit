package com.eclipse.music.kit.utils.storage

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile


class StorageController(
    private val context: Context
) {
    companion object {
        fun scan(context: Context, dirUri: String): List<DocumentFile> =
            dirUri.takeIf { it.isNotBlank() }
                ?.let(Uri::parse)
                ?.let { DocumentFile.fromTreeUri(context, it) }
                ?.listFiles()
                ?.filter { file ->
                    file.isFile &&
                            file.name
                                ?.endsWith(".ncm", ignoreCase = true)
                            ?: false
                }
                .orEmpty()

    }

    @SuppressLint("SdCardPath")
    fun resolveDir(context: Context, uri: Uri): PickedDir {
        val doc = DocumentFile.fromTreeUri(context, uri)

        val displayPath = when {
            uri.authority == "com.android.externalstorage.documents" -> {
                val docId = DocumentsContract.getTreeDocumentId(uri)
                if (docId.startsWith("primary:")) {
                    val relative = docId.removePrefix("primary:")
                    "/sdcard/${relative}"
                } else {
                    "/storage/${docId.replace(":", "/")}"
                }
            }
            else -> doc?.name ?: "Unknown"
        }

        return PickedDir(
            uri = uri.toString(),
            name = displayPath
        )
    }

    fun scanNcm(inputDir: String): List<DocumentFile> =
        scan(context, inputDir)

    fun persistPermission(uri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }

}

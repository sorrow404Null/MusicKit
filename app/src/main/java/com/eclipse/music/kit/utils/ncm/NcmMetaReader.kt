package com.eclipse.music.kit.utils.ncm

import android.content.Context
import android.net.Uri
import com.eclipse.music.kit.utils.AesUtils.toIntLE
import kotlin.io.encoding.ExperimentalEncodingApi

object NcmMetaReader {
    private const val MAGIC = "CTENFDAM"

    @OptIn(ExperimentalEncodingApi::class)
    fun readCover(
        context: Context,
        uri: Uri
    ): ByteArray? = context.contentResolver
        .openInputStream(uri)
        ?.use { input ->

            fun readIntLE(): Int =
                ByteArray(4).also { input.read(it) }.toIntLE()

            val magic = ByteArray(8).also(input::read)
            if (String(magic) != MAGIC) return null

            input.skip(2)

            input.skip(readIntLE().toLong())
            input.skip(readIntLE().toLong())

            input.skip(5)

            input.skip(4)

            val coverSize = readIntLE()
            if (coverSize <= 0) return null

            ByteArray(coverSize).also(input::read)
        }
}

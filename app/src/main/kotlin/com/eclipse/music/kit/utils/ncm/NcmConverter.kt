package com.eclipse.music.kit.utils.ncm

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.eclipse.music.kit.utils.AesUtils.aesDecrypt
import com.eclipse.music.kit.utils.AesUtils.toIntLE
import com.eclipse.music.kit.utils.ncm.data.MusicLyrics
import com.eclipse.music.kit.utils.ncm.data.NCMMetadata
import com.kyant.taglib.Picture
import com.kyant.taglib.TagLib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.experimental.xor
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object NcmConverter {
    private const val MAGIC = "CTENFDAM"
    private val CORE_KEY = byteArrayOf(
        0x68,
        0x7A,
        0x48,
        0x52,
        0x41,
        0x6D,
        0x73,
        0x6F,
        0x35,
        0x6B,
        0x49,
        0x6E,
        0x62,
        0x61,
        0x78,
        0x57
    )
    private val META_KEY = byteArrayOf(
        0x23,
        0x31,
        0x34,
        0x6C,
        0x6A,
        0x6B,
        0x5F,
        0x21,
        0x5C,
        0x5D,
        0x26,
        0x30,
        0x55,
        0x3C,
        0x27,
        0x28
    )
    private val httpClient = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun convert(context: Context, inputUri: Uri, outputDirUri: String): Result<Uri> =
        withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(inputUri)?.use { input ->
                    val magic = ByteArray(8).also(input::read)
                    if (String(magic) != MAGIC) throw Exception("Invalid NCM")
                    input.skip(2)

                    val rc4KeyEncSize = ByteArray(4).also(input::read).toIntLE()
                    val rc4KeyEncBytes = ByteArray(rc4KeyEncSize).also(input::read).apply {
                        indices.forEach { this[it] = this[it] xor 0x64 }
                    }
                    val rc4Key = aesDecrypt(rc4KeyEncBytes, CORE_KEY)

                    val metadataSize = ByteArray(4).also(input::read).toIntLE()
                    val metadataBytes = ByteArray(metadataSize).also(input::read).apply {
                        indices.forEach { this[it] = this[it] xor 0x63 }
                    }
                    val metadata = aesDecrypt(
                        Base64.decode(metadataBytes, 22, metadataBytes.size),
                        META_KEY
                    ).let {
                        json.decodeFromString<NCMMetadata>(String(it.copyOfRange(6, it.size)))
                    }

                    input.skip(5)
                    val crcSize = ByteArray(4).also(input::read).toIntLE()
                    val coverSize = ByteArray(4).also(input::read).toIntLE()
                    val coverData =
                        if (coverSize > 0) ByteArray(coverSize).also(input::read) else null
                    (crcSize - coverSize).takeIf { it > 0 }?.let { input.skip(it.toLong()) }

                    val ext = if (metadata.format == "mp3") "mp3" else "flac"
                    val name = "${metadata.musicName} - ${metadata.artist.joinToString(", ")}.$ext"
                    val outDir = DocumentFile.fromTreeUri(context, outputDirUri.toUri())
                        ?: throw Exception("Output error")

                    outDir.findFile(name)?.delete()
                    val newFile =
                        outDir.createFile(if (ext == "mp3") "audio/mpeg" else "audio/flac", name)
                            ?: throw Exception("Create error")

                    context.contentResolver.openOutputStream(newFile.uri)?.use { out ->
                        val sBox = IntArray(256) { it }.apply {
                            var j = 0
                            val key = rc4Key.copyOfRange(17, rc4Key.size)
                            for (i in 0 until 256) {
                                j = (j + this[i] + key[i % key.size]) and 0xFF
                                val tmp = this[i]; this[i] = this[j]; this[j] = tmp
                            }
                        }

                        val keyStream = IntArray(256) { i ->
                            val v = (i + 1) and 0xFF
                            sBox[(sBox[v] + sBox[(sBox[v] + v) and 0xFF]) and 0xFF]
                        }

                        val buffer = ByteArray(64 * 1024)
                        var read: Int
                        var total = 0
                        while (input.read(buffer).also { read = it } != -1) {
                            for (i in 0 until read) {
                                val key = keyStream[(total + i) and 0xFF]
                                buffer[i] = (buffer[i].toInt() xor key).toByte()
                            }
                            out.write(buffer, 0, read)
                            total += read
                        }
                    }

                    context.contentResolver.openFileDescriptor(newFile.uri, "rw")?.use { pfd ->
                        writeTags(pfd, metadata, coverData)
                    }
                    newFile.uri
                } ?: throw Exception("Stream error")
            }
        }

    private fun writeTags(pfd: ParcelFileDescriptor, meta: NCMMetadata, cover: ByteArray?) {
        val lyrics = fetchLyrics(meta.musicId)
        val metadata = TagLib.getMetadata(pfd.dup().detachFd(), false) ?: return
        val props = HashMap<String, Array<String>>()
        metadata.propertyMap.forEach { (k, v) -> props[k] = v }

        val artists = meta.artist.joinToString(", ")
        props["TITLE"] = arrayOf(meta.musicName)
        props["ARTIST"] = arrayOf(artists)
        props["ALBUM"] = arrayOf(meta.album)
        props["ALBUMARTIST"] = arrayOf(artists)
        lyrics?.let { props["LYRICS"] = arrayOf(it) }

        cover?.let { data ->
            getImageMimeType(data)?.let { mime ->
                val artwork = Picture(data, mime, "Front Cover", "Front Cover")
                TagLib.savePictures(pfd.dup().detachFd(), arrayOf(artwork))
            }
        }

        TagLib.savePropertyMap(pfd.dup().detachFd(), props)
    }

    private fun fetchLyrics(id: Long): String? = runCatching {
        val url = "https://music.163.com/api/song/media?id=$id"
        httpClient.newCall(Request.Builder().url(url).build()).execute().use { resp ->
            if (!resp.isSuccessful) return@runCatching null
            val body = resp.body?.string() ?: return@runCatching null
            if (body.contains("nolyric")) return@runCatching null
            json.decodeFromString<MusicLyrics>(body).lyric.takeIf { it.isNotBlank() }
        }
    }.getOrNull()

    private fun getImageMimeType(data: ByteArray): String? = when {
        data.size < 4 -> null
        data[0] == 0xFF.toByte() && data[1] == 0xD8.toByte() -> "image/jpeg"
        data[0] == 0x89.toByte() && data[1] == 0x50.toByte() && data[2] == 0x4E.toByte() && data[3] == 0x47.toByte() -> "image/png"
        else -> null
    }
}

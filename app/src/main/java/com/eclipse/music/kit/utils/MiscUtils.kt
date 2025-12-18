package com.eclipse.music.kit.utils

import android.content.Intent
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.eclipse.music.kit.utils.AndroidAppContext.applicationContext

object MiscUtils {
    fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        applicationContext?.startActivity(intent)
    }

    fun DocumentFile.safeDisplayName(): String = name
        ?.removeSuffix(".ncm")
        ?.let { if (it.length > 40) it.take(35) + "..." else it }
        ?: "Unknown"


}
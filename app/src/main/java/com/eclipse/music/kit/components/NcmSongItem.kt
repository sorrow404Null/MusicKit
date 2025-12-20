package com.eclipse.music.kit.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.eclipse.music.kit.R
import com.eclipse.music.kit.utils.MiscUtils.extensionOrUnknown
import com.eclipse.music.kit.utils.MiscUtils.safeDisplayName

@Composable
fun NcmSongItem(
    file: DocumentFile,
    cover: Bitmap?,
    isCurrent: Boolean
) {
    val displayName = file.safeDisplayName()
    val extension = file.extensionOrUnknown()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isCurrent) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (cover != null) {
            val imageBitmap = remember(cover) { cover.asImageBitmap() }
            Image(
                bitmap = imageBitmap,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        0.5.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(8.dp)
                    ),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column {
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "$extension ${stringResource(R.string.text_extension_format)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) // 更低调的灰色
            )
        }
    }
}

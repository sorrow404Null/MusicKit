package com.eclipse.music.kit.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eclipse.music.kit.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricSourceSheet(
    currentIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val titles = stringArrayResource(R.array.lyric_source_titles)
    val descs = stringArrayResource(R.array.lyric_source_descriptions)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {

            Text(
                text = stringResource(R.string.text_choose_lyric_source),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            titles.indices.forEach { index ->
                LyricSourceItem(
                    title = titles[index],
                    subtitle = descs[index],
                    selected = index == currentIndex,
                    onClick = { onSelect(index) }
                )
            }
        }
    }
}

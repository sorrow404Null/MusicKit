package com.eclipse.music.kit.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eclipse.music.kit.R
import kotlinx.coroutines.delay

@Composable
fun ConvertProgressDialog(
    total: Int,
    onProgress: (Int) -> Unit,
    onFinish: () -> Unit
) {
    var current by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (current < total) {
            delay(300)
            current++
            onProgress(current - 1)
        }
    }

    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            if (current >= total) {
                TextButton(onClick = onFinish) {
                    Text(stringResource(R.string.text_done))
                }
            }
        },
        title = { Text(stringResource(R.string.text_converting)) },
        text = {
            Column {
                LinearProgressIndicator(
                    progress = {
                        if (total == 0) 0f else current / total.toFloat()
                    }
                )
                Spacer(Modifier.height(8.dp))
                Text("$current / $total")
            }
        }
    )
}

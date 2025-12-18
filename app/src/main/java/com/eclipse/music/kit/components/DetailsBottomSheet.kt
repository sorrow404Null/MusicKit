package com.eclipse.music.kit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eclipse.music.kit.R
import com.eclipse.music.kit.utils.MiscUtils.openUrl


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsBottomSheet(
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stringResource(R.string.text_app_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            InfoRow(
                icon = Icons.Outlined.Person,
                label = stringResource(R.string.text_author),
                value = stringResource(R.string.text_sorrow_404_null)
            ) {
                openUrl("https://github.com/sorrow404Null")
            }

            InfoRow(
                icon = Icons.Outlined.Link,
                label = stringResource(R.string.text_github),
                value = stringResource(R.string.text_github_description)
            ) {
                openUrl("https://github.com/sorrow404Null/MusicKit")
            }
        }
    }
}

package com.eclipse.music.kit.pages

import android.annotation.RequiresApi
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.eclipse.music.kit.R
import com.eclipse.music.kit.components.LyricSourceSheet
import com.eclipse.music.kit.components.NavigationItem
import com.eclipse.music.kit.components.SwitchItem
import com.eclipse.music.kit.components.ValueItem
import com.eclipse.music.kit.components.rememberSettingsState
import com.eclipse.music.kit.navigation.Routes
import com.eclipse.music.kit.utils.data.SettingsState
import com.eclipse.music.kit.utils.storage.StorageActions

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun SettingsPage(navController: NavHostController) {

    var showLyricSheet by remember { mutableStateOf(false) }
    val lyricTitles = stringArrayResource(R.array.lyric_source_titles)
    val (settingsState, _, storageActions) = rememberSettingsState()
    val state = settingsState.value

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.text_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.SETTINGS) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }) {
                        Icon(Icons.Outlined.ArrowBackIosNew, null)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {

            StorageSettingUI(
                state = settingsState.value,
                actions = storageActions
            )

            SettingSection(title = stringResource(R.string.text_section_lyrics)) {
                ValueItem(
                    icon = Icons.Outlined.LibraryMusic,
                    title = stringResource(R.string.text_lyric_source),
                    value = lyricTitles[state.lyricSourceIndex],
                    onClick = { showLyricSheet = true }
                )

                if (showLyricSheet) {
                    LyricSourceSheet(
                        currentIndex = state.lyricSourceIndex,
                        onSelect = {
                            settingsState.value =
                                state.copy(lyricSourceIndex = it)
                            showLyricSheet = false
                        },
                        onDismiss = { showLyricSheet = false }
                    )
                }

            }

            SettingSection(title = stringResource(R.string.text_section_advanced)) {
                SwitchItem(
                    icon = Icons.Outlined.Delete,
                    title = stringResource(R.string.text_delete_source),
                    subtitle = stringResource(R.string.text_delete_source_desc),
                    checked = state.deleteSource,
                    isDanger = true,
                    onCheckedChange = {
                        settingsState.value =
                            state.copy(deleteSource = it)
                    }
                )
            }
        }
    }
}


@Composable
private fun SettingSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 1.dp
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun StorageSettingUI(
    state: SettingsState,
    actions: StorageActions
) {
    SettingSection(title = stringResource(R.string.text_section_storage)) {
        NavigationItem(
            icon = Icons.Outlined.FolderOpen,
            title = stringResource(R.string.text_input_path),
            subtitle = state.inputDirName.ifEmpty {
                stringResource(R.string.text_not_set)
            },
            onClick = actions.selectInput
        )

        NavigationItem(
            icon = Icons.Outlined.Folder,
            title = stringResource(R.string.text_output_path),
            subtitle = state.outputDirName.ifEmpty {
                stringResource(R.string.text_not_set)
            },
            onClick = actions.selectOutput
        )
    }
}

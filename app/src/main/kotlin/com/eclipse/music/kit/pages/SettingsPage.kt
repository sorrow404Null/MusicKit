package com.eclipse.music.kit.pages

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.eclipse.music.kit.R
import com.eclipse.music.kit.components.HapticLevel
import com.eclipse.music.kit.components.LanguageSheet
import com.eclipse.music.kit.components.LyricSourceSheet
import com.eclipse.music.kit.components.NavigationItem
import com.eclipse.music.kit.components.SwitchItem
import com.eclipse.music.kit.components.ValueItem
import com.eclipse.music.kit.components.rememberHaptic
import com.eclipse.music.kit.components.rememberSettingsState
import com.eclipse.music.kit.utils.AppLanguage
import com.eclipse.music.kit.utils.data.SettingsRepository
import com.eclipse.music.kit.utils.data.SettingsState
import com.eclipse.music.kit.utils.storage.StorageActions
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun SettingsPage(navController: NavHostController) {

    var showLyricSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val (settingsState, _, storageActions) = rememberSettingsState()
    val state = settingsState.value

    val haptic = rememberHaptic(state)
    val scope = rememberCoroutineScope()
    val repo = remember { SettingsRepository(context) }

    val languageNames = stringArrayResource(R.array.language_names)
    val currentLanguageName by remember(state.language) {
        derivedStateOf {
            languageNames.getOrNull(
                AppLanguage.entries.indexOfFirst { it.code == state.language }
            ) ?: languageNames.first()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.text_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic(HapticLevel.LIGHT)
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {

            SettingSection(title = stringResource(R.string.text_section_appearance)) {
                ValueItem(
                    icon = Icons.Outlined.Language,
                    title = stringResource(R.string.text_language),
                    value = currentLanguageName,
                    onClick = {
                        haptic(HapticLevel.LIGHT)
                        showLanguageSheet = true
                    }
                )

                if (showLanguageSheet) {
                    LanguageSheet(
                        currentLanguageCode = state.language,
                        onSelect = { code ->
                            haptic(HapticLevel.LIGHT)
                            scope.launch {
                                val newState = state.copy(language = code)
                                repo.save(newState)
                                settingsState.value = newState
                                (context as? Activity)?.recreate()
                            }
                            showLanguageSheet = false
                        },
                        onDismiss = { showLanguageSheet = false }
                    )
                }
            }

            StorageSettingUI(
                state = state,
                actions = storageActions,
                onHaptic = { haptic(HapticLevel.LIGHT) }
            )

            SettingSection(title = stringResource(R.string.text_section_lyrics)) {
                val lyricTitles = stringArrayResource(R.array.lyric_source_titles)
                ValueItem(
                    icon = Icons.Outlined.LibraryMusic,
                    title = stringResource(R.string.text_lyric_source),
                    value = lyricTitles[state.lyricSourceIndex],
                    onClick = {
                        haptic(HapticLevel.LIGHT)
                        showLyricSheet = true
                    }
                )

                if (showLyricSheet) {
                    LyricSourceSheet(
                        currentIndex = state.lyricSourceIndex,
                        onSelect = {
                            haptic(HapticLevel.LIGHT)
                            settingsState.value = state.copy(lyricSourceIndex = it)
                            showLyricSheet = false
                        },
                        onDismiss = { showLyricSheet = false }
                    )
                }
            }

            SettingSection(title = stringResource(R.string.text_section_advanced)) {
                SwitchItem(
                    icon = Icons.Outlined.Vibration,
                    title = stringResource(R.string.text_haptic_feedback),
                    subtitle = stringResource(R.string.text_haptic_feedback_desc),
                    checked = state.hapticFeedbackEnabled,
                    onCheckedChange = {
                        haptic(HapticLevel.LIGHT)
                        settingsState.value = state.copy(hapticFeedbackEnabled = it)
                    }
                )

                SwitchItem(
                    icon = Icons.Outlined.Delete,
                    title = stringResource(R.string.text_delete_source),
                    subtitle = stringResource(R.string.text_delete_source_desc),
                    checked = state.deleteSource,
                    isDanger = true,
                    onCheckedChange = {
                        haptic(HapticLevel.LIGHT)
                        settingsState.value = state.copy(deleteSource = it)
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
private fun StorageSettingUI(
    state: SettingsState,
    actions: StorageActions,
    onHaptic: () -> Unit
) {
    SettingSection(title = stringResource(R.string.text_section_storage)) {

        NavigationItem(
            icon = Icons.Outlined.FolderOpen,
            title = stringResource(R.string.text_input_path),
            subtitle = state.inputDirName.ifEmpty {
                stringResource(R.string.text_not_set)
            },
            onClick = {
                onHaptic()
                actions.selectInput()
            }
        )

        NavigationItem(
            icon = Icons.Outlined.Folder,
            title = stringResource(R.string.text_output_path),
            subtitle = state.outputDirName.ifEmpty {
                stringResource(R.string.text_not_set)
            },
            onClick = {
                onHaptic()
                actions.selectOutput()
            }
        )
    }
}

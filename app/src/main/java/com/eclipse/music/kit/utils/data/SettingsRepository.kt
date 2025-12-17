package com.eclipse.music.kit.utils.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

class SettingsRepository(context: Context) {
    val Context.settingsDataStore by preferencesDataStore(name = "settings")

    private val ds = context.settingsDataStore

    suspend fun load(): SettingsState {
        val p = ds.data.first()
        return SettingsState(
            inputDir = p[SettingsKeys.INPUT_DIR] ?: "",
            inputDirName = p[SettingsKeys.INPUT_DIR_NAME] ?: "",
            outputDir = p[SettingsKeys.OUTPUT_DIR] ?: "",
            outputDirName = p[SettingsKeys.OUTPUT_DIR_NAME] ?: "",
            deleteSource = p[SettingsKeys.DELETE_SOURCE] ?: false,
            lyricSourceIndex = p[SettingsKeys.LYRIC_SOURCE_INDEX] ?: 0
        )
    }

    suspend fun save(state: SettingsState) {
        ds.edit {
            it[SettingsKeys.INPUT_DIR] = state.inputDir
            it[SettingsKeys.INPUT_DIR_NAME] = state.inputDirName
            it[SettingsKeys.OUTPUT_DIR] = state.outputDir
            it[SettingsKeys.OUTPUT_DIR_NAME] = state.outputDirName
            it[SettingsKeys.DELETE_SOURCE] = state.deleteSource
            it[SettingsKeys.LYRIC_SOURCE_INDEX] = state.lyricSourceIndex
        }
    }
}

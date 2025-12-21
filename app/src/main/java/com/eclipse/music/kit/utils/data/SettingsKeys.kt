package com.eclipse.music.kit.utils.data

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object SettingsKeys {
    val INPUT_DIR = stringPreferencesKey("input_dir")
    val INPUT_DIR_NAME = stringPreferencesKey("input_dir_name")
    val OUTPUT_DIR = stringPreferencesKey("output_dir")
    val OUTPUT_DIR_NAME = stringPreferencesKey("output_dir_name")

    val DELETE_SOURCE = booleanPreferencesKey("delete_source")
    val LYRIC_SOURCE_INDEX = intPreferencesKey("lyric_source_index")
    val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
}
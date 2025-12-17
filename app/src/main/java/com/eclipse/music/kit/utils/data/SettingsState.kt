package com.eclipse.music.kit.utils.data

data class SettingsState(
    val inputDir: String = "",
    val inputDirName: String = "",
    val outputDir: String = "",
    val outputDirName: String = "",
    val deleteSource: Boolean = false,
    val lyricSourceIndex: Int = 0
)
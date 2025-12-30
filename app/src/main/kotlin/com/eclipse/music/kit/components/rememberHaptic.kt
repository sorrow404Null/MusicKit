package com.eclipse.music.kit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.eclipse.music.kit.utils.data.SettingsState

enum class HapticLevel {
    LIGHT,
    MEDIUM,
    SUCCESS
}

@Composable
fun rememberHaptic(
    settings: SettingsState
): (HapticLevel) -> Unit {

    val haptic = LocalHapticFeedback.current
    val enabled = settings.hapticFeedbackEnabled

    return remember(enabled) {
        { level ->
            if (!enabled) return@remember

            val type = when (level) {
                HapticLevel.LIGHT ->
                    HapticFeedbackType.TextHandleMove

                HapticLevel.MEDIUM ->
                    HapticFeedbackType.LongPress

                HapticLevel.SUCCESS ->
                    HapticFeedbackType.Confirm
            }

            haptic.performHapticFeedback(type)
        }
    }
}

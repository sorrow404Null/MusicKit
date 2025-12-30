package com.eclipse.music.kit.components

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.documentfile.provider.DocumentFile
import com.eclipse.music.kit.utils.data.SettingsRepository
import com.eclipse.music.kit.utils.data.SettingsState
import com.eclipse.music.kit.utils.storage.StorageActions
import com.eclipse.music.kit.utils.storage.StorageController


@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun rememberSettingsState():
        Triple<MutableState<SettingsState>, List<DocumentFile>, StorageActions> {

    val context = LocalContext.current
    val repo = remember { SettingsRepository(context) }
    val controller = remember { StorageController(context) }

    val state = remember { mutableStateOf(SettingsState()) }
    var ncmFiles by remember { mutableStateOf(emptyList<DocumentFile>()) }
    var selectingInput by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        state.value = repo.load()
    }

    LaunchedEffect(state.value) {
        repo.save(state.value)
    }

    val dirPickerLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocumentTree()
        ) { uri ->
            uri ?: return@rememberLauncherForActivityResult

            controller.persistPermission(uri)
            val picked = controller.resolveDir(context, uri)

            state.value =
                if (selectingInput) {
                    state.value.copy(
                        inputDir = picked.uri,
                        inputDirName = picked.name
                    )
                } else {
                    state.value.copy(
                        outputDir = picked.uri,
                        outputDirName = picked.name
                    )
                }
        }

    LaunchedEffect(state.value.inputDir) {
        if (state.value.inputDir.isNotEmpty()) {
            ncmFiles = controller.scanNcm(state.value.inputDir)
        }
    }

    val actions = remember {
        StorageActions(
            selectInput = {
                selectingInput = true
                dirPickerLauncher.launch(null)
            },
            selectOutput = {
                selectingInput = false
                dirPickerLauncher.launch(null)
            }
        )
    }

    return Triple(state, ncmFiles, actions)
}

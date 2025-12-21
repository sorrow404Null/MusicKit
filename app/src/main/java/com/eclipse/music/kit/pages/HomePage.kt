package com.eclipse.music.kit.pages

import android.annotation.RequiresApi
import android.app.Application
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.eclipse.music.kit.R
import com.eclipse.music.kit.components.ConvertProgressDialog
import com.eclipse.music.kit.components.DetailsBottomSheet
import com.eclipse.music.kit.components.HapticLevel
import com.eclipse.music.kit.components.NcmSongItem
import com.eclipse.music.kit.components.rememberHaptic
import com.eclipse.music.kit.components.rememberSettingsState
import com.eclipse.music.kit.navigation.Routes
import com.eclipse.music.kit.utils.ncm.ScanState
import com.eclipse.music.kit.viewModel.home.HomeViewModel
import com.eclipse.music.kit.viewModel.home.HomeViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.R)
@Suppress("AssignedValueIsNeverRead")
@Composable
fun HomePage(
    navController: NavHostController,
) {

    val context = LocalContext.current

    val application = remember {
        context.applicationContext as Application
    }

    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(application)
    )

    val scanState by viewModel.scanState.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val selectedUris by viewModel.selectedUris.collectAsState()

    var showDetails by remember { mutableStateOf(false) }
    var showProgress by remember { mutableStateOf(false) }

    val (settingsState, ncmFiles, _) = rememberSettingsState()
    val settings = settingsState.value

    val haptic = rememberHaptic(settings)

    val chooseInputDirText =
        stringResource(R.string.text_toast_choose_input_dir)
    val chooseOutputDirText =
        stringResource(R.string.text_toast_choose_output_dir)

    fun toast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(ncmFiles) {
        viewModel.refresh(ncmFiles)
        viewModel.scan(ncmFiles)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic(HapticLevel.LIGHT)
                        navController.navigate(Routes.SETTINGS)
                    }) {
                        Icon(Icons.Outlined.Settings, null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        haptic(HapticLevel.LIGHT)
                        viewModel.refresh(ncmFiles)
                        viewModel.scan(ncmFiles)
                    }) {
                        Icon(Icons.Outlined.Refresh, null)
                    }

                    IconButton(onClick = {
                        haptic(HapticLevel.LIGHT)
                        showDetails = true
                    }) {
                        Icon(Icons.Outlined.Info, null)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            val files = (scanState as? ScanState.Done)?.files.orEmpty()
            val totalCount = files.size
            val selectedCount = selectedUris.size

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        text = stringResource(R.string.text_pending_files),
                        style = MaterialTheme.typography.labelMedium
                    )

                    Text(
                        text = stringResource(
                            R.string.text_selected_count,
                            selectedCount,
                            totalCount
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (totalCount > 0) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    haptic(HapticLevel.LIGHT)
                                    viewModel.clearSelection()
                                }
                            ) {
                                Text(stringResource(R.string.text_clear_selection))
                            }

                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    haptic(HapticLevel.MEDIUM)
                                    if (selectedCount == totalCount) {
                                        viewModel.clearSelection()
                                    } else {
                                        viewModel.selectAll()
                                    }
                                }
                            ) {
                                Text(
                                    if (selectedCount == totalCount)
                                        stringResource(R.string.text_unselect_all)
                                    else
                                        stringResource(R.string.text_select_all)
                                )
                            }
                        }
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedCount > 0,
                        onClick = {
                            when {
                                settings.inputDir.isEmpty() ->
                                    toast(chooseInputDirText)

                                settings.outputDir.isEmpty() ->
                                    toast(chooseOutputDirText)

                                else -> {
                                    haptic(HapticLevel.MEDIUM)
                                    showProgress = true
                                }
                            }
                        }
                    ) {
                        Text(
                            stringResource(
                                R.string.text_start_convert_with_count,
                                selectedCount
                            )
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 1.dp
            ) {
                when (val stateScan = scanState) {

                    ScanState.Scanning -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(12.dp))
                            Text(stringResource(R.string.text_scanning))
                        }
                    }

                    is ScanState.Done -> {
                        LazyColumn {
                            itemsIndexed(
                                items = stateScan.files,
                                key = { _, item -> item.file.uri }
                            ) { index, item ->

                                val uri = item.file.uri.toString()
                                val isSelected =
                                    selectedUris.contains(uri)

                                NcmSongItem(
                                    file = item.file,
                                    cover = item.cover,
                                    isCurrent = index == currentIndex,
                                    isSelected = isSelected,
                                    onClick = {
                                        if (!isSelected) {
                                            haptic(HapticLevel.MEDIUM)
                                        }
                                        viewModel.toggleSelect(item.file)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDetails) {
        DetailsBottomSheet { showDetails = false }
    }

    if (showProgress) {

        val selectedFiles =
            (scanState as? ScanState.Done)
                ?.files
                ?.filter {
                    selectedUris.contains(
                        it.file.uri.toString()
                    )
                }
                .orEmpty()

        ConvertProgressDialog(
            total = selectedFiles.size,
            onProgress = viewModel::setCurrentIndex,
            onFinish = {
                viewModel.setCurrentIndex(-1)
                viewModel.clearSelection()
                showProgress = false
            }
        )
    }
}

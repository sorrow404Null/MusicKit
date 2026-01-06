package com.eclipse.music.kit.pages

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.eclipse.music.kit.R
import com.eclipse.music.kit.components.DetailsBottomSheet
import com.eclipse.music.kit.components.HapticLevel
import com.eclipse.music.kit.components.NcmSongItem
import com.eclipse.music.kit.components.rememberHaptic
import com.eclipse.music.kit.components.rememberSettingsState
import com.eclipse.music.kit.navigation.Routes
import com.eclipse.music.kit.utils.ncm.ScanState
import com.eclipse.music.kit.viewModel.home.HomeViewModel
import com.eclipse.music.kit.viewModel.home.HomeViewModelFactory
import kotlinx.coroutines.delay

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
    val covers = viewModel.covers
    val convertingUris = viewModel.convertingUris

    var showDetails by remember { mutableStateOf(false) }
    var isConverting by remember { mutableStateOf(false) }

    val (settingsState) = rememberSettingsState()
    val settings = settingsState.value

    val haptic = rememberHaptic(settings)

    val chooseInputDirText = stringResource(R.string.text_toast_choose_input_dir)
    val chooseOutputDirText = stringResource(R.string.text_toast_choose_output_dir)
    val noNetworkWarning = stringResource(R.string.text_no_network_warning)

    fun toast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadCacheFirst()
    }

    LaunchedEffect(settings.inputDir, settings.outputDir) {
        if (settings.inputDir.isNotEmpty()) {
            viewModel.scan(settings.inputDir, settings.outputDir)
        }
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
                        viewModel.refresh(settings.inputDir, settings.outputDir)
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                        }

                        AnimatedVisibility(
                            visible = isConverting,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            val progress =
                                if (selectedCount == 0) 0f else currentIndex.toFloat() / selectedCount.toFloat()
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.size(36.dp),
                                    strokeWidth = 3.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                )
                                Text(
                                    text = "$currentIndex",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    if (totalCount > 0) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                modifier = Modifier.weight(1f),
                                enabled = !isConverting,
                                onClick = {
                                    haptic(HapticLevel.LIGHT)
                                    viewModel.clearSelection()
                                }
                            ) {
                                Text(stringResource(R.string.text_clear_selection))
                            }

                            Button(
                                modifier = Modifier.weight(1f),
                                enabled = !isConverting,
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
                        enabled = selectedCount > 0 && !isConverting,
                        onClick = {
                            when {
                                settings.inputDir.isEmpty() -> toast(chooseInputDirText)
                                settings.outputDir.isEmpty() -> toast(chooseOutputDirText)
                                else -> {
                                    if (!isNetworkAvailable(context)) {
                                        toast(noNetworkWarning)
                                    }
                                    haptic(HapticLevel.MEDIUM)
                                    isConverting = true
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
                                key = { _, item -> item.uriKey }
                            ) { index, item ->

                                val isSelected = selectedUris.contains(item.uriKey)

                                NcmSongItem(
                                    displayName = item.displayName,
                                    extension = item.extension,
                                    cover = covers[item.uriKey],
                                    isCurrent = convertingUris.contains(item.uriKey),
                                    isSelected = isSelected,
                                    isConverted = item.isConverted,
                                    onClick = {
                                        if (isConverting) return@NcmSongItem
                                        if (!isSelected) {
                                            haptic(HapticLevel.MEDIUM)
                                        }
                                        viewModel.toggleSelect(item.uriKey)
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

    if (isConverting) {
        val selectedFiles = (scanState as? ScanState.Done)
                ?.files
            ?.filter { selectedUris.contains(it.uriKey) }
                .orEmpty()

        LaunchedEffect(Unit) {
            viewModel.startConvert(
                uris = selectedFiles.map { it.fileUri.toUri() },
                outputDir = settings.outputDir
            )
            toast(context.getString(R.string.text_done))
            delay(800)
            isConverting = false
            viewModel.clearSelection()
            viewModel.resetProgress()
        }
    }
}

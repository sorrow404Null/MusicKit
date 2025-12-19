package com.eclipse.music.kit.pages

import android.annotation.RequiresApi
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.eclipse.music.kit.R
import com.eclipse.music.kit.components.ConvertProgressDialog
import com.eclipse.music.kit.components.DetailsBottomSheet
import com.eclipse.music.kit.components.NcmSongItem
import com.eclipse.music.kit.components.rememberSettingsState
import com.eclipse.music.kit.navigation.Routes
import com.eclipse.music.kit.utils.AndroidAppContext.applicationContext
import com.eclipse.music.kit.utils.MiscUtils.safeDisplayName
import com.eclipse.music.kit.utils.ncm.NcmUiFile
import com.eclipse.music.kit.utils.ncm.ScanState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.R)
@Suppress("AssignedValueIsNeverRead")
@Composable
fun HomePage(navController: NavHostController) {

    val context = LocalContext.current
    fun toast(msg: String) =
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    var showDetails by remember { mutableStateOf(false) }
    var showProgress by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableIntStateOf(-1) }
    var refreshKey by remember { mutableIntStateOf(0) }

    val (settingsState, ncmFiles, _) = rememberSettingsState()
    val state = settingsState.value

    var scanState by remember { mutableStateOf<ScanState>(ScanState.Scanning) }

    LaunchedEffect(ncmFiles, refreshKey) {
        scanState = ScanState.Scanning
        delay(300)

        val files = ncmFiles
            .filter { it.exists() }
            .map {
                NcmUiFile(
                    file = it,
                    displayName = it.safeDisplayName()
                )
            }
            .sortedBy { it.displayName.lowercase() }

        scanState = ScanState.Done(files)
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
                        Icon(Icons.Outlined.Settings, null)
                    }
                },
                actions = {
                    IconButton(onClick = { refreshKey++ }) {
                        Icon(Icons.Outlined.Refresh, null)
                    }

                    IconButton(onClick = { showDetails = true }) {
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

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text(
                        stringResource(R.string.text_pending_files),
                        style = MaterialTheme.typography.labelMedium
                    )

                    Text(
                        text = files.size.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        color = if (files.isEmpty())
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.primary
                    )

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            when {
                                state.inputDir.isEmpty() ->
                                    toast(applicationContext!!.getString(R.string.text_toast_choose_input_dir))

                                state.outputDir.isEmpty() ->
                                    toast(applicationContext!!.getString(R.string.text_toast_choose_output_dir))

                                files.isEmpty() ->
                                    toast(applicationContext!!.getString(R.string.text_toast_no_ncm_files))

                                else ->
                                    showProgress = true
                            }
                        }
                    ) {
                        Text(stringResource(R.string.text_action_start_convert))
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
                        if (stateScan.files.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(stringResource(R.string.text_no_files))
                            }
                        } else {
                            LazyColumn {
                                items(
                                    items = stateScan.files,
                                    key = { it.file.uri }
                                ) { item ->
                                    NcmSongItem(
                                        name = item.displayName,
                                        isCurrent = stateScan.files.indexOf(item) == currentIndex
                                    )
                                }
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
        ConvertProgressDialog(
            total = (scanState as? ScanState.Done)?.files?.size ?: 0,
            onProgress = { currentIndex = it },
            onFinish = {
                currentIndex = -1
                showProgress = false
            }
        )
    }
}
package com.eclipse.music.kit.viewModel.home

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eclipse.music.kit.utils.MiscUtils.extensionOrUnknown
import com.eclipse.music.kit.utils.MiscUtils.safeDisplayName
import com.eclipse.music.kit.utils.ncm.NcmCache
import com.eclipse.music.kit.utils.ncm.NcmConverter
import com.eclipse.music.kit.utils.ncm.NcmCoverLoader
import com.eclipse.music.kit.utils.ncm.NcmUiFile
import com.eclipse.music.kit.utils.ncm.ScanState
import com.eclipse.music.kit.utils.storage.StorageController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.text.Collator
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

class HomeViewModel(
    private val appContext: Context
) : ViewModel() {

    private val _scanState =
        MutableStateFlow<ScanState>(ScanState.Done(emptyList()))
    val scanState: StateFlow<ScanState> = _scanState

    private var lastInputDir = ""
    private var scanJob: Job? = null

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _selectedUris =
        MutableStateFlow<Set<String>>(emptySet())
    val selectedUris: StateFlow<Set<String>> = _selectedUris

    val convertingUris = mutableStateListOf<String>()
    val covers = mutableStateMapOf<String, Bitmap>()

    private val collator =
        Collator.getInstance(Locale.getDefault()).apply {
            strength = Collator.PRIMARY
        }

    private val coverSemaphore = Semaphore(3)
    private val convertSemaphore = Semaphore(8)

    fun loadCacheFirst() {
        if (_scanState.value is ScanState.Done && (_scanState.value as ScanState.Done).files.isNotEmpty()) return
        val cached = NcmCache.loadScan(appContext)
        if (cached.isNotEmpty()) {
            _scanState.value = ScanState.Done(cached)
        }
    }

    @SuppressLint("UseKtx")
    fun scan(inputDir: String, outputDir: String = "", force: Boolean = false) {
        if (inputDir.isBlank()) return
        if (lastInputDir == inputDir && !force) return
        lastInputDir = inputDir

        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _scanState.value = ScanState.Scanning
            _selectedUris.value = emptySet()
            covers.clear()

            val existingOutputFiles = withContext(Dispatchers.IO) {
                runCatching {
                    if (outputDir.isBlank()) emptySet()
                    else {
                        val treeUri =
                            if (outputDir.startsWith("content://")) Uri.parse(outputDir) else Uri.parse(
                                Uri.decode(outputDir)
                            )
                        DocumentFile.fromTreeUri(appContext, treeUri)
                            ?.listFiles()
                            ?.mapNotNull { it.name }
                            ?.toSet() ?: emptySet()
                    }
                }.getOrDefault(emptySet())
            }

            val cachedMap = NcmCache.loadScan(appContext).associateBy { it.fileUri }

            val files = withContext(Dispatchers.IO) {
                StorageController.scan(appContext, inputDir)
            }

            val base = withContext(Dispatchers.Default) {
                files.asSequence()
                    .filter { it.exists() }
                    .map { doc ->
                        val uri = doc.uri.toString()
                        val cachedItem = cachedMap[uri]

                        var isConverted = cachedItem?.isConverted ?: false
                        val outName = cachedItem?.outputFileName

                        if (isConverted) {
                            if (outName == null || !existingOutputFiles.contains(outName)) {
                                isConverted = false
                            }
                        }

                        NcmUiFile(
                            fileUri = uri,
                            displayName = doc.safeDisplayName(),
                            extension = doc.extensionOrUnknown(),
                            isConverted = isConverted,
                            outputFileName = outName
                        ).apply { file = doc }
                    }
                    .sortedWith { a, b ->
                        collator.compare(a.displayName, b.displayName)
                    }
                    .toList()
            }

            _scanState.value = ScanState.Done(base)
            NcmCache.saveScan(appContext, base)

            base.forEach { item ->
                launch(Dispatchers.IO) {
                    coverSemaphore.acquire()
                    runCatching {
                        val file = item.file ?: DocumentFile.fromSingleUri(
                            appContext,
                            item.fileUri.toUri()
                        )
                        val bitmap = file?.let {
                            NcmCoverLoader.loadBitmap(appContext, it)
                        } ?: return@launch
                        covers[item.uriKey] = bitmap
                    }.also {
                        coverSemaphore.release()
                    }
                }
            }
        }
    }

    fun refresh(inputDir: String, outputDir: String) {
        scan(inputDir, outputDir, force = true)
    }

    fun toggleSelect(uriKey: String) {
        _selectedUris.value =
            _selectedUris.value.toMutableSet().apply {
                if (contains(uriKey)) remove(uriKey) else add(uriKey)
            }
    }

    fun clearSelection() {
        _selectedUris.value = emptySet()
    }

    fun resetProgress() {
        _currentIndex.value = 0
        convertingUris.clear()
    }

    fun selectAll() {
        val files = (_scanState.value as? ScanState.Done)?.files.orEmpty()
        _selectedUris.value =
            files.filter { !it.isConverted }
                .map { it.uriKey }
                .toSet()
    }

    suspend fun startConvert(uris: List<Uri>, outputDir: String) {
        val completedCount = AtomicInteger(0)
        _currentIndex.value = 0
        withContext(Dispatchers.Default) {
            uris.forEach { uri ->
                launch {
                    val uriString = uri.toString()
                    convertSemaphore.withPermit {
                        convertingUris.add(uriString)
                        val result = NcmConverter.convert(appContext, uri, outputDir)
                        result.onSuccess { outputUri ->
                            val outputFileName =
                                DocumentFile.fromSingleUri(appContext, outputUri)?.name
                            val state = _scanState.value
                            if (state is ScanState.Done) {
                                val updatedFiles = state.files.map {
                                    if (it.fileUri == uriString) {
                                        it.copy(isConverted = true, outputFileName = outputFileName)
                                            .apply { file = it.file }
                                    } else it
                                }
                                _scanState.value = ScanState.Done(updatedFiles)
                                NcmCache.saveScan(appContext, updatedFiles)
                            }
                        }
                        convertingUris.remove(uriString)
                        _currentIndex.value = completedCount.incrementAndGet()
                    }
                }
            }
        }
    }
}

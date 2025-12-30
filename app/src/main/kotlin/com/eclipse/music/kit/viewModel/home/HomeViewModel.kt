package com.eclipse.music.kit.viewModel.home

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateMapOf
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eclipse.music.kit.utils.MiscUtils.extensionOrUnknown
import com.eclipse.music.kit.utils.MiscUtils.safeDisplayName
import com.eclipse.music.kit.utils.ncm.NcmCache
import com.eclipse.music.kit.utils.ncm.NcmCoverLoader
import com.eclipse.music.kit.utils.ncm.NcmUiFile
import com.eclipse.music.kit.utils.ncm.ScanState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import java.text.Collator
import java.util.Locale

class HomeViewModel(
    private val appContext: Context
) : ViewModel() {

    private val _scanState =
        MutableStateFlow<ScanState>(ScanState.Scanning)
    val scanState: StateFlow<ScanState> = _scanState

    private var hasScannedOnce = false

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _selectedUris =
        MutableStateFlow<Set<String>>(emptySet())
    val selectedUris: StateFlow<Set<String>> = _selectedUris

    val covers = mutableStateMapOf<String, Bitmap>()

    private val collator =
        Collator.getInstance(Locale.getDefault()).apply {
            strength = Collator.PRIMARY
        }

    private val coverSemaphore = Semaphore(3)

    fun loadCacheFirst() {
        val cached = NcmCache.loadScan(appContext)
        if (cached.isNotEmpty()) {
            _scanState.value = ScanState.Done(cached)
        }
    }

    fun scan(files: List<DocumentFile>, force: Boolean = false) {
        if (hasScannedOnce && !force) return
        hasScannedOnce = true

        viewModelScope.launch(Dispatchers.Default) {
            _scanState.value = ScanState.Scanning
            _selectedUris.value = emptySet()
            covers.clear()

            val base = files
                .asSequence()
                .filter { it.exists() }
                .map {
                    NcmUiFile(
                        file = it,
                        displayName = it.safeDisplayName(),
                        extension = it.extensionOrUnknown()
                    )
                }
                .sortedWith { a, b ->
                    collator.compare(a.displayName, b.displayName)
                }
                .toList()

            _scanState.value = ScanState.Done(base)
            NcmCache.saveScan(appContext, base)

            base.forEach { item ->
                launch(Dispatchers.IO) {
                    coverSemaphore.acquire()
                    runCatching {
                        val bitmap =
                            NcmCoverLoader.loadBitmap(
                                appContext,
                                item.file
                            ) ?: return@launch

                        covers[item.uriKey] = bitmap
                    }.also {
                        coverSemaphore.release()
                    }
                }
            }
        }
    }

    fun refresh(files: List<DocumentFile>) {
        hasScannedOnce = false
        scan(files, force = true)
    }

    fun toggleSelect(file: DocumentFile) {
        val key = file.uri.toString()
        _selectedUris.value =
            _selectedUris.value.toMutableSet().apply {
                if (contains(key)) remove(key) else add(key)
            }
    }

    fun isSelected(file: DocumentFile): Boolean =
        _selectedUris.value.contains(file.uri.toString())

    fun clearSelection() {
        _selectedUris.value = emptySet()
    }

    fun selectAll() {
        val files = (_scanState.value as? ScanState.Done)?.files.orEmpty()
        _selectedUris.value =
            files.map { it.uriKey }.toSet()
    }

    fun selectedCount(): Int = _selectedUris.value.size

    fun setCurrentIndex(index: Int) {
        _currentIndex.value = index
    }
}

package com.eclipse.music.kit.viewModel.home

import android.content.Context
import android.graphics.Bitmap
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eclipse.music.kit.utils.MiscUtils.safeDisplayName
import com.eclipse.music.kit.utils.ncm.NcmCoverLoader
import com.eclipse.music.kit.utils.ncm.NcmUiFile
import com.eclipse.music.kit.utils.ncm.ScanState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
    private val collator: Collator =
        Collator.getInstance(Locale.getDefault()).apply {
            strength = Collator.PRIMARY
        }
    private val _selectedUris =
        MutableStateFlow<Set<String>>(emptySet())
    val selectedUris: StateFlow<Set<String>> = _selectedUris

    fun scan(files: List<DocumentFile>, force: Boolean = false) {
        if (hasScannedOnce && !force) return
        hasScannedOnce = true

        viewModelScope.launch(Dispatchers.Default) {
            _scanState.value = ScanState.Scanning
            _selectedUris.value = emptySet()

            val base = files
                .asSequence()
                .filter { it.exists() }
                .map { file ->
                    NcmUiFile(
                        file = file,
                        displayName = file.safeDisplayName()
                    )
                }
                .sortedWith { a, b ->
                    collator.compare(a.displayName, b.displayName)
                }
                .toList()

            _scanState.value = ScanState.Done(base)

            base.forEachIndexed { index, item ->
                launch(Dispatchers.IO) {
                    val bitmap =
                        NcmCoverLoader.loadBitmap(appContext, item.file)
                            ?: return@launch

                    updateCover(index, bitmap)
                }
            }
        }
    }

    private fun updateCover(index: Int, bitmap: Bitmap) {
        val current = _scanState.value as? ScanState.Done ?: return
        if (index !in current.files.indices) return

        val list = current.files.toMutableList()
        list[index] = list[index].withCover(bitmap)
        _scanState.value = ScanState.Done(list)
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
            files.map { it.file.uri.toString() }.toSet()
    }

    fun selectedCount(): Int = _selectedUris.value.size

    fun setCurrentIndex(index: Int) {
        _currentIndex.value = index
    }
}

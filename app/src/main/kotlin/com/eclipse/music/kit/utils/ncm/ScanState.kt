package com.eclipse.music.kit.utils.ncm

sealed class ScanState {
    object Scanning : ScanState()
    data class Done(val files: List<NcmUiFile>) : ScanState()
}

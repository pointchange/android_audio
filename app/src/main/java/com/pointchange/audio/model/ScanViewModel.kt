package com.pointchange.audio.model

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointchange.audio.model_data.AudioMetadata
import com.pointchange.audio.service.VlcManager
import com.pointchange.audio.util.getAudioFromContentResolver
import com.pointchange.audio.util.getAudioFromFile
import com.pointchange.audio.view.page.ScanDegree
import com.pointchange.audio.view.page.ScanDegree.GENERAL
import com.pointchange.audio.view.page.ScanDegree.MEDIUM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ScanViewModel : ViewModel() {
    private val _scanState = MutableStateFlow(ScanState.PENDING)
    val scanState = _scanState.asStateFlow()
    private var scanDegree = GENERAL

    private val _fileList = MutableStateFlow<List<String>>(emptyList())

    private val _selectedFolderPath = MutableStateFlow<String?>(null)
    val selectedFolderPath = _selectedFolderPath.asStateFlow()
    private val _minDuration = MutableStateFlow(0L)
    private val _minFileSize = MutableStateFlow(0L)
    private val _filterLoading = MutableStateFlow(false)
    val filterLoading = _filterLoading.asStateFlow()
    val folderGroups = combine(
        _fileList, _minDuration, _minFileSize
    ) { paths, durationLimit, sizeLimit ->
        withContext(Dispatchers.IO) {
            paths
                .filter {
                    if (paths[0] == it) {
                        _filterLoading.emit(true)
                    }
                    val size = getFileSize(it) / 1024L
                    val duration = getDuration(it) / 1000L
                    size > sizeLimit && duration > durationLimit
                }
                .groupBy { path ->
                    val list = path.substringBeforeLast("/", missingDelimiterValue = "根目录")
                    if (paths[paths.size - 1] == path) {
                        _filterLoading.emit(false)
                    }
                    list
                }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    fun initScanUIState() {
        scanDegree = GENERAL
        _selectedFolderPath.value = null
        _fileList.value = emptyList()
        _minDuration.value = 0L
        _minFileSize.value = 0L
        _scanState.value = ScanState.PENDING
        _filterLoading.value = false
    }

    fun setScanDegree(scanDegree: ScanDegree) {
        this.scanDegree = scanDegree
    }

    fun setSelectedFolderPath(fullPath: String? = null) {
        _selectedFolderPath.value = fullPath
    }

    fun filterFileList(minDuration: Long, minFileSize: Long) {
        _minDuration.value = minDuration
        _minFileSize.value = minFileSize
    }


    private suspend fun getDuration(path: String) = VlcManager.getDuration(path)

    private fun getFileSize(path: String) = File(path).length()


    fun mediumScan(context: Context) {
        viewModelScope.launch {
            _scanState.emit(ScanState.LOADING)
            val audioFromFile = when (scanDegree) {
                GENERAL -> getAudioFromContentResolver(context)
                MEDIUM -> getAudioFromFile(Directory.STORAGE_PUBLIC.path)
            }
            _fileList.value = audioFromFile
            _scanState.emit(ScanState.ENDING)
        }
    }


    fun saveScanResultList(notSelectList: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = _fileList.value.toMutableList()
            list.removeAll(notSelectList)
            val audioMetadataList =
                list.map { AudioMetadata(uri = it, artist = null, album = null, title = null) }
            VlcManager.repository.insertOnlyNews(list = audioMetadataList)
        }
    }

}
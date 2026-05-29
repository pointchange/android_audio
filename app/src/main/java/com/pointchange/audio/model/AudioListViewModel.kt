package com.pointchange.audio.model

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.palette.graphics.Palette
import com.pointchange.audio.model_data.AudioMetadata
import com.pointchange.audio.model_data.AudioMetadataDetail
import com.pointchange.audio.model_data.SortAudio
import com.pointchange.audio.service.VlcManager
import com.pointchange.audio.util.changeColorDeepOrLight
import com.pointchange.audio.util.getAudioFromContentResolver
import com.pointchange.audio.util.getAudioFromFile
import com.pointchange.audio.util.getAudioLrcFromContentResolver
import com.pointchange.audio.util.getDuration
import com.pointchange.audio.util.getFileSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream


@OptIn(ExperimentalCoroutinesApi::class)
class AudioListViewModel(application: Application) : AndroidViewModel(application) {

    @OptIn(FlowPreview::class)
    var searchAudio = VlcManager.repository.keyword
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            VlcManager.repository.searchAudioMetadata(query)
        }.cachedIn(viewModelScope)

    //    private var _sortOrder = MutableStateFlow(SortAudio.DEFAULT)
    val sortOrder = VlcManager.repository.sortOrder

    var sortAudio = sortOrder
        .flatMapLatest { order ->
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    initialLoadSize = 20,
                    prefetchDistance = 5,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = {
                    VlcManager.repository.getSortByAscMetaData(order.description())
                }
            ).flow
        }.cachedIn(viewModelScope)


//    private var _isFavor = false
//    private var _isFavor = MutableStateFlow(false)

    val audioList: Flow<PagingData<AudioMetadata>> =
        VlcManager.repository.isFavor.flatMapLatest { isInit ->
            VlcManager.repository.getFavoriteAudioMetadataPaged(isInit)

        }.cachedIn(viewModelScope)

    val playList = VlcManager.repository.playList

    //    private val _currentPlayListState = MutableStateFlow(PlayList.DEFAULT)

    //    private val _playingInfo = MutableStateFlow(PlayingInfo())
    val playingInfo = VlcManager.repository.playingInfo


    private var _audioMetadataDetail = MutableStateFlow<AudioMetadataDetail>(AudioMetadataDetail())
    val audioMetadataDetail = _audioMetadataDetail.asStateFlow()

    private val _colorFromCover = MutableStateFlow(0xFF444444)
    val colorFromCover = _colorFromCover.asStateFlow()
    val transparencyColor = Color.Transparent
    val lrcContent = VlcManager.repository.lrcContent

    val presetList: List<String> = VlcManager.getPresetNames()
    private val _currentPresetIndex = MutableStateFlow(-1)
    val currentPresetIndex = _currentPresetIndex.asStateFlow()

    var isRefreshing by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            VlcManager.repository.isNotFileExist.collect {
                if (it) {
                    Toast.makeText(application, "文件找不到", Toast.LENGTH_SHORT).show()
                    VlcManager.repository.setIsNotFileExist()
                }
            }
        }
    }


    fun selectPreset(index: Int) {
        _currentPresetIndex.value = index
        VlcManager.setPreset(index)
    }

    fun setColorFromCover(bitmap: Bitmap? = null, color: Long = 0xFF000000) {

        viewModelScope.launch {
            if (bitmap == null) {
                _colorFromCover.value = changeColorDeepOrLight(
                    rawColorInt = color.toInt(),
                    saturability = 0.8F,
                    brightness = 0.12F
                ).toArgb()
                    .toLong()
            } else {
                Palette.from(bitmap).generate {
                    val rawColorInt =
                        it?.darkMutedSwatch?.rgb ?: it?.dominantSwatch?.rgb
                    if (rawColorInt != null) {
                        _colorFromCover.value = changeColorDeepOrLight(
                            rawColorInt = rawColorInt,
                            saturability = 0.8F,
                            brightness = 0.2F
                        ).toArgb()
                            .toLong()
                    }
                }

            }
        }
    }


    fun next() {
        VlcManager.repository.next()
    }

    fun previous() {
        VlcManager.repository.previous()
    }


    fun scanHandle(context: Context) {
        viewModelScope.launch {
            val lrcList = getAudioLrcFromContentResolver(context).associateBy {
                it.substringAfterLast("/").substringBeforeLast(".")
            }
            val sizeLimit = 1024L
            val durationLimit = 1000L * 60
            val audioList = getAudioFromContentResolver(context)
                .filter {
                    val size = getFileSize(it) / 1024L
                    val duration = getDuration(it) / 1000L
                    size > sizeLimit && duration > durationLimit
                }
                .map { uri ->
                    val name = uri.substringAfterLast("/").substringBeforeLast(".")
                    AudioMetadata(uri = uri, lrc = lrcList[name])
                }

            VlcManager.repository.saveMetadataList(audioList)
            setCurrentPlayListState(PlayList.DEFAULT)
            launch(Dispatchers.IO) {
                audioList.forEach {
                    VlcManager.repository.requestMetadata(it.uri)
                }
            }
        }
    }

    fun playHandle(uri: String) {
        val file = File(uri)
        if (file.exists()) {
            VlcManager.repository.playHandle(uri)
        } else {
            Toast.makeText(application, "文件不存在", Toast.LENGTH_SHORT).show()
            VlcManager.repository.next()
        }
    }

    fun saveInPublicPicture(context: Context, uri: String, coverPath: String) {
        viewModelScope.launch {
            val name = uri.substringAfterLast("/").substringBeforeLast(".")
            val imageDetails = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$name.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/${context.packageName}"
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            val imageUri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageDetails
            )

            imageUri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    coverPath.toUri().path?.let { path ->
                        val file = File(path)
                        if (file.exists()) {
                            FileInputStream(file).use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    imageDetails.clear()
                    imageDetails.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(it, imageDetails, null, null)
                }
            }
        }
    }

    fun setKeyword(keyword: String) {
        val kw = keyword.trim()
        if (kw.isEmpty()) return
        if (kw.isBlank()) return
        VlcManager.repository.setKeyword(keyword)
    }

    fun setSortOrder(sortOrder: SortAudio) {
//        _sortOrder.value = sortOrder
        VlcManager.repository.setSortOrder(sortOrder)
    }

    fun updateFavoriteState() {
        viewModelScope.launch {
            VlcManager.repository.updateFavoriteState()
        }
    }

    fun setFavor(isFavor: Boolean) {
        VlcManager.repository.setFavor(isFavor)
    }

    fun setCurrentPlayListState(state: PlayList) {
        VlcManager.repository.setCurrentPlayListState(state)
    }

    fun setPlayingInfo(state: PlayList, index: Int = 0) {
        VlcManager.repository.setPlayingInfo(state, index)
    }

    fun addPlayList(from: Int, item: AudioMetadata?, currentI: Int = 0) {
        if (item == null) return
        viewModelScope.launch {
            VlcManager.repository.addPlayList(from, item, currentI)
        }
    }

    fun movePlayListItem(from: Int, to: Int) {
        VlcManager.repository.movePlayListItem(from, to)
    }

    fun removeAtPlayListItem(index: Int) {
        VlcManager.repository.removeAtPlayListItem(index)
    }

    fun clearPlayList() {
        VlcManager.repository.clearPlayList()
    }

    fun setPlayingInfoMode() {
        VlcManager.repository.setPlayingInfoMode()
    }

    fun clearPlayingInfo() {
        VlcManager.repository.clearPlayingInfo()
    }

    fun parseAudioInfo(uri: String) {
        viewModelScope.launch {
            _audioMetadataDetail.emit(VlcManager.parseAudioInfo(uri))
        }
    }

    suspend fun remove(list: List<Int>, isDeleteLocal: Boolean = false): Boolean =
        VlcManager.repository.remove(list, isDeleteLocal)

    fun removeOne(uri: String, isDeleteLocal: Boolean = false) {
        viewModelScope.launch {
            val isPause = VlcManager.repository.removeOne(uri, isDeleteLocal)
            if (isPause) {
                VlcManager.mediaPlayer?.pause()
            }
        }
    }

    fun updateFavoriteListState(list: List<Int>, isFav: Boolean) {
        viewModelScope.launch {
            val res = list.fold(initial = mutableListOf<String>()) { pre, cur ->
                if (cur != -1) {
                    pre.add(playList[cur].uri)
                }
                pre
            }
            VlcManager.repository.updateFavoriteListState(res, isFav)
        }
    }

    fun addListToPlayList(list: List<Int>) {
        viewModelScope.launch {
            VlcManager.repository.addListToPlayList(list)
        }
    }

    fun selectLrc(uri: String) {
        viewModelScope.launch {
            VlcManager.repository.selectLrc(uri = uri)
        }
    }

    fun refreshList() {
        viewModelScope.launch {
            isRefreshing = true
            val list =
                getAudioFromFile(Directory.STORAGE_PUBLIC.path).map { AudioMetadata(uri = it) }
            val uris = list.map { it.uri }

            val urisSet = uris.toSet()
            val memorySet = VlcManager.repository.getMemoryCache().toSet()


            if (uris.isNotEmpty()) {
                VlcManager.repository.deleteExceptMetadata(uris)
            } else {
                VlcManager.repository.clearCache()
            }
            VlcManager.repository.insertOnlyNews(list)

            val newList = urisSet - memorySet
            newList.forEach {
                VlcManager.repository.requestMetadata(it)
            }


            isRefreshing = false
        }
    }

    fun parseOne(uri: String) {
        viewModelScope.launch {
            val file = File(uri)
            if (file.exists()) {
                VlcManager.repository.requestMetadata(uri)
            }
        }
    }
}
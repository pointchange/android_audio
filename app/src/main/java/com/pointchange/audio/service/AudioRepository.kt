package com.pointchange.audio.service

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.application
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.pointchange.audio.model.LrcLine
import com.pointchange.audio.model.PlayList
import com.pointchange.audio.model_data.AudioDatabase
import com.pointchange.audio.model_data.AudioMetadata
import com.pointchange.audio.model_data.DataStoreCacheManager
import com.pointchange.audio.model_data.PlayMode
import com.pointchange.audio.model_data.PlayingInfo
import com.pointchange.audio.model_data.SortAudio
import com.pointchange.audio.util.decreaseIndexInRange
import com.pointchange.audio.util.getIndexInEnum
import com.pointchange.audio.util.increaseIndexInRange
import com.pointchange.audio.util.nextPreHandle
import com.pointchange.audio.view.widget.AudioInfoStateDefinition
import com.pointchange.audio.view.widget.AudioInfoWidget
import com.pointchange.audio.view.widget.AudioWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.buffer
import okio.source
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class AudioRepository(context: Context) {
    private val dao = AudioDatabase.getDatabase(context).audioMetadataDao()
    private val memoryCache = mutableMapOf<String, AudioMetadata>()
    private val parsingUri = ConcurrentHashMap<String, Boolean>()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var _keyword = MutableStateFlow("")
    val keyword = _keyword.asStateFlow()
    private val _currentPlayListState = MutableStateFlow(PlayList.DEFAULT)
    val currentPlayListState = _currentPlayListState.asStateFlow()

    val playList = mutableStateListOf<AudioMetadata>()
    private var randomIntList = mutableListOf<Int>()
    private var randomCurrentIndex = 0
    private val _playingInfo = MutableStateFlow(PlayingInfo())
    val playingInfo = _playingInfo.asStateFlow()
    private val _lrcContent = MutableStateFlow<List<LrcLine>>(emptyList())
    val lrcContent = _lrcContent.asStateFlow()
    private var _sortOrder = MutableStateFlow(SortAudio.DEFAULT)
    val sortOrder = _sortOrder.asStateFlow()
    private var _isFavor = MutableStateFlow(false)
    val isFavor = _isFavor.asStateFlow()

    private var _isNotFileExist = MutableStateFlow(false)
    val isNotFileExist = _isNotFileExist.asStateFlow()


    init {
        scope.launch {
            dao.getAllAudioFlow().collect { list ->
                if (list.isEmpty()) return@collect
                list.forEach { memoryCache[it.uri] = it }
                val saveList = list.sortedBy { it.uri }.subList(
                    0,
                    if (list.size - 1 < 20) {
                        list.size - 1
                    } else {
                        20
                    }
                )

                DataStoreCacheManager.saveFirstPage(context, saveList)
            }
        }
        scope.launch {
            currentPlayListState.collect { playListState ->
                getPlayList(playListState = playListState)
            }
        }
    }

    fun getMemoryCache() = memoryCache.values.map { it.uri }

    suspend fun refreshWidget(playingInfo: PlayingInfo, isPlaying: Boolean, context: Context) {
        val audioMetadata = playingInfo.audioMetadata
        val title = audioMetadata?.title ?: ""
        val artist = audioMetadata?.artist ?: ""
        val cover = audioMetadata?.coverPath ?: ""
        val isPlaying = isPlaying
        val manager = GlanceAppWidgetManager(context)
        val glanceIdsAudioWidget = manager.getGlanceIds(AudioWidget::class.java)

        val audioInfoWidget = AudioInfoWidget(
            title = title,
            artist = artist,
            cover = cover,
            isPlaying = isPlaying
        )
        for (id in glanceIdsAudioWidget) {
            updateAppWidgetState(
                context,
                AudioInfoStateDefinition,
                id
            ) { audioInfoWidget }
            AudioWidget().update(context, id)
        }
    }

    fun savePlayInfo() = _playingInfo.value
    fun readPlayInfo(playingInfo: PlayingInfo) {
        _playingInfo.value = playingInfo
        playingInfo.audioMetadata?.let {
            VlcManager.playHandle(it.uri, isPrepare = true)
        }
    }

    fun setKeyword(keyword: String) {
        _keyword.value = keyword
    }

    suspend fun getPlayList(playListState: PlayList) {
        val allAudioList = VlcManager.repository.getAllAudio()
        playList.clear()
        val list = when (playListState) {
            PlayList.DEFAULT -> allAudioList.sortedBy { it.uri }
            PlayList.TITLE -> allAudioList.sortedBy { it.title }
            PlayList.ARTIST -> allAudioList.sortedBy { it.artist }
            PlayList.SEARCH -> {
                allAudioList.filter { data ->
                    data.uri.contains(other = _keyword.value, ignoreCase = true) ||
                            data.title?.contains(
                                other = _keyword.value,
                                ignoreCase = true
                            ) == true ||
                            data.artist?.contains(
                                other = _keyword.value,
                                ignoreCase = true
                            ) == true ||
                            data.album?.contains(
                                other = _keyword.value,
                                ignoreCase = true
                            ) == true
                }
            }

            PlayList.FAVORITE -> allAudioList.filter { it.isFavorite }
        }
        playList.addAll(list).also {
            randomIntList.clear()
            val randomList = List(playList.size) { i -> i }.shuffled()
            randomIntList.addAll(randomList)
            randomCurrentIndex = -1
        }
    }

    fun next() {
        nextPreHandle(
            mode = _playingInfo.value.mode,
            playList = playList,
            playingInfo = _playingInfo.value,
            loopHandle = { index, size ->
                _playingInfo.update {
                    it.copy(
                        index = increaseIndexInRange(
                            currentIndex = index,
                            size = size
                        )
                    )
                }
                _playingInfo.value.index
            },
            randomHandle = { index, size ->
                if (randomCurrentIndex == -1) {
                    randomCurrentIndex =
                        randomIntList.find { it == index }
                            ?: index
                }
                if (randomCurrentIndex >= randomIntList.size - 1) {
                    randomCurrentIndex = randomIntList[0]
                } else {
                    randomCurrentIndex = increaseIndexInRange(
                        currentIndex = randomCurrentIndex,
                        size = size
                    )
                }
                randomIntList[randomCurrentIndex]
            },
            execHandle = {
                playHandle(it)
            }
        )
    }

    fun previous() {
        nextPreHandle(
            mode = _playingInfo.value.mode,
            playList = playList,
            playingInfo = _playingInfo.value,
            loopHandle = { index, size ->
                _playingInfo.update {
                    it.copy(
                        index =
                            decreaseIndexInRange(
                                currentIndex = index,
                                size = size
                            )
                    )
                }
                _playingInfo.value.index
            },
            randomHandle = { index, size ->
                if (randomCurrentIndex == -1) {
                    randomCurrentIndex =
                        randomIntList.find { it == index }
                            ?: index
                }

                if (randomCurrentIndex <= 0) {
                    randomCurrentIndex = randomIntList[randomIntList.size - 1]
                } else {
                    randomCurrentIndex = decreaseIndexInRange(
                        currentIndex = randomCurrentIndex,
                        size = size
                    )
                }
                randomIntList[randomCurrentIndex]
            },
            execHandle = {
                playHandle(it)
            }
        )
    }

    fun getAudioMetadataFrom(uri: String) = memoryCache[uri] ?: AudioMetadata(uri = uri)

    fun clear() {
        scope.cancel()
    }

    fun playHandle(uri: String) {
        val file = File(uri)
        if (file.exists()) {
            scope.launch {
                val audioMetadata = VlcManager.repository.getAudioMetadataAndPlay(uri)
                _playingInfo.update {
                    it.copy(
                        audioMetadata = audioMetadata
                    )
                }
                if (playList.isEmpty()) {
                    getPlayList(playListState = _currentPlayListState.value)
                }
                val lrcUri = _playingInfo.value.audioMetadata?.lrc
                    ?: (_playingInfo.value.audioMetadata?.uri?.substringBeforeLast(".") + ".lrc")
                parseLrc(lrcUri)
            }
        } else {
            _isNotFileExist.value = true
            VlcManager.repository.next()
        }
    }

    fun setIsNotFileExist() {
        _isNotFileExist.value = false
    }

    suspend fun parseLrc(uri: String) {
        withContext(Dispatchers.IO) {
            val file = File(uri)
            if (file.exists()) {
                val list = file.source().buffer().readUtf8()

                val reg = Regex("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)")
                _lrcContent.value = list.lines().mapNotNull { line ->
                    reg.find(line)?.let { match ->
                        val min = match.groupValues[1].toLong()
                        val sec = match.groupValues[2].toLong()
                        val ms = match.groupValues[3].toLong()
                        LrcLine(min * 60 * 1000 + sec * 1000 + ms, match.groupValues[4].trim())
                    }
                }.sortedBy { it.time }
            } else {
                _lrcContent.value = emptyList()
            }
        }
    }

    suspend fun selectLrc(uri: String) {
        _playingInfo.value.audioMetadata?.let { audioMetadata ->
            updateLrc(uri = audioMetadata.uri, lrc = uri)
            parseLrc(uri)
        }
    }

    fun setSortOrder(sortOrder: SortAudio) {
        _sortOrder.value = sortOrder
    }

    suspend fun updateFavoriteState() {
        _playingInfo.value.audioMetadata?.let {
            updateFavoriteState(it.uri, true)
        }
    }

    fun setFavor(isFavor: Boolean) {
        _isFavor.value = isFavor
    }

    fun setCurrentPlayListState(state: PlayList) {
        _currentPlayListState.value = state
    }

    fun setPlayingInfo(state: PlayList, index: Int = 0) {
        _playingInfo.update { it.copy(currentList = state, index = index) }
    }

    suspend fun addPlayList(from: Int, item: AudioMetadata, currentI: Int = 0) {
        withContext(Dispatchers.Default) {
            val currentIndex = if (currentI == 0) _playingInfo.value.index else currentI
            try {
                if (playList[currentIndex].uri == playList[from].uri) return@withContext
                val exist = playList.any { it.uri == item.uri }
                if (exist) {
                    movePlayListItem(from, currentIndex + 1)
                } else {
                    playList.add(currentIndex + 1, item)

                }
            } catch (e: IndexOutOfBoundsException) {
                playList.add(item)
            }
        }
    }

    fun movePlayListItem(from: Int, to: Int) {
        val temp = playList[to]
        playList[to] = playList[from]
        playList[from] = temp

    }

    fun removeAtPlayListItem(index: Int) {
        if (index == _playingInfo.value.index) {
            _playingInfo.update { it.copy(index = 0, audioMetadata = null) }
        }
        playList.removeAt(index)
    }

    fun clearPlayList() {
        playList.clear()
    }

    fun setPlayingInfoMode(defineMode: PlayMode? = null) {
        val mode = defineMode ?: getIndexInEnum<PlayMode>(_playingInfo.value.mode.ordinal)
        _playingInfo.update { it.copy(mode = mode) }
    }

    fun clearPlayingInfo() {
        _playingInfo.update { it.copy(audioMetadata = null) }
    }

    suspend fun remove(list: List<Int>, isDeleteLocal: Boolean = false): Boolean {
        var isPause = false
        val res = mutableListOf<String>()
        list.forEach {
            if (it != -1) {
                res.add(playList[it].uri)
            }
            if (it == _playingInfo.value.index) {
                _playingInfo.update { i -> i.copy(index = 0, audioMetadata = null) }
                isPause = true
            }
        }

        res.forEach { uri ->
            playList.removeIf { it.uri == uri }
            if (isDeleteLocal) {
                File(uri).delete()
            }
        }
        deleteAudioIfUri(res.toList())
        return isPause
    }

    suspend fun removeOne(uri: String, isDeleteLocal: Boolean = false): Boolean {
        val isPause = uri == _playingInfo.value.audioMetadata?.uri

        playList.removeIf { it.uri == uri }
        if (isDeleteLocal) {
            File(uri).delete()
        }

        deleteOneMetadata(uri)
        return isPause
    }


    suspend fun addListToPlayList(list: List<Int>) {
        val index = _playingInfo.value.index
        list.filter { it != -1 }.forEachIndexed { i, item ->
            addPlayList(item, playList[item], currentI = index + i)
        }
    }

    fun getAudioMetadataAndPlay(uri: String): AudioMetadata {
        VlcManager.playHandle(uri)
        return memoryCache[uri] ?: AudioMetadata(uri = uri)
    }

//    suspend fun getAudioMetadataAndPlay(uri: String): AudioMetadata {
//        memoryCache[uri]?.let {
//            Log.d("str123","memoryCache")
//            VlcManager.playHandle(uri)
//            it
//        }
//
//        val dbData = withContext(Dispatchers.IO) { dao.getMetadata(uri) }
//        if (dbData != null) {
//            return dbData.also {
//                Log.d("str123","dbData")
//
//                memoryCache[uri] = it
//                VlcManager.playHandle(uri)
//            }
//        }
//        val vlcData = VlcManager.parseAudioMetadataAndPlay(uri)
//        withContext(Dispatchers.IO) {
//            Log.d("str123","VlcManager")
//            dao.saveMetadata(vlcData)
//            memoryCache[uri] = vlcData
//        }
//        return vlcData
//    }

    fun getAudioPagingSource() = dao.getAllAudioPaged()

    fun updateMetadata(audioMetadata: AudioMetadata) = dao.upsertMetadata(audioMetadata)

    suspend fun saveMetadataList(list: List<AudioMetadata>) = dao.saveMetadata(list)
    suspend fun saveMetadataList(audioMetadata: AudioMetadata) = dao.saveMetadata(audioMetadata)

    suspend fun requestMetadata(uri: String) {
        if (parsingUri.contains(uri)) return
        try {
            parsingUri[uri] = true
            val metadata = VlcManager.parseAudioMetadata(uri)
            dao.updateAudioItem(metadata)
        } catch (e: Exception) {

        } finally {
            parsingUri.remove(uri)
        }
    }

    //    suspend fun searchAudioMetadata(keyword: String) = dao.searchMetaData(keyword)
    fun searchAudioMetadata(keyword: String): Flow<PagingData<AudioMetadata>> =
        if (keyword.isEmpty()) {
            flowOf(PagingData.empty())
        } else {
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    prefetchDistance = 5,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = {
                    dao.searchMetaData(keyword)
                }
            ).flow
        }

    //    fun getSortByAscMetaData(sortOrder: String) = Pager(
//        config = PagingConfig(
//            pageSize = 20,
//            initialLoadSize = 20,
//            prefetchDistance = 5,
//            enablePlaceholders = false
//        ),
//        pagingSourceFactory = {
//            Log.d("str123","dao")
//            dao.getSortByAscMetaData(sortOrder)
//        }
//    ).flow
    fun getSortByAscMetaData(sortOrder: String) = dao.getSortByAscMetaData(sortOrder)

    suspend fun updateFavoriteState(uri: String, isFav: Boolean) =
        dao.updateFavoriteState(uri, isFav)

    suspend fun updateFavoriteListState(list: List<String>, isFav: Boolean) =
        dao.updateFavoriteListState(list, isFav)

    fun getFavoriteAudioMetadataPaged(isInit: Boolean): Flow<PagingData<AudioMetadata>> = Pager(
        config = PagingConfig(
            pageSize = 20,
            prefetchDistance = 5,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            dao.getFavoriteAudioMetadataPaged(isInit)
        }
    ).flow

    suspend fun getAllAudio() = dao.getAllAudio()

    suspend fun deleteAudioIfUri(list: List<String>) = dao.deleteMetadata(list)
    suspend fun deleteOneMetadata(uri: String) = dao.deleteOneMetadata(uri)

    suspend fun insertOnlyNews(list: List<AudioMetadata>) = dao.insertOnlyNews(list)

    suspend fun updateLrc(uri: String, lrc: String) = dao.updateLrc(uri, lrc)
    suspend fun clearCache() = dao.clearCache()
    suspend fun deleteExceptMetadata(list: List<String>) = dao.deleteExceptMetadata(list)
}
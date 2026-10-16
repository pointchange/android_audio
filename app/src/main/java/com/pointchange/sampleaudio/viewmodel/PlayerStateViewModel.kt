package com.pointchange.sampleaudio.viewmodel

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.palette.graphics.Palette
import com.pointchange.sampleaudio.App
import com.pointchange.sampleaudio.data.AudioInfo
import com.pointchange.sampleaudio.data.EqualizerState
import com.pointchange.sampleaudio.data.LoadLyricsState
import com.pointchange.sampleaudio.data.PlayerUiState
import com.pointchange.sampleaudio.data.QueueListName
import com.pointchange.sampleaudio.data.ScanConfig
import com.pointchange.sampleaudio.data.ScanEvent
import com.pointchange.sampleaudio.data.ScanFile
import com.pointchange.sampleaudio.data.ScanLyricsUiState
import com.pointchange.sampleaudio.data.ScanState
import com.pointchange.sampleaudio.data.ScanType
import com.pointchange.sampleaudio.data.ScanUiState
import com.pointchange.sampleaudio.data.SettingsState
import com.pointchange.sampleaudio.data.SongInfo
import com.pointchange.sampleaudio.data.SongInfoUpsert
import com.pointchange.sampleaudio.data.ThemeMode
import com.pointchange.sampleaudio.data.TimerState
import com.pointchange.sampleaudio.data.toMediaItem
import com.pointchange.sampleaudio.database.SongRepository
import com.pointchange.sampleaudio.datastore.UserPreferences
import com.pointchange.sampleaudio.media.ArtworkResolver
import com.pointchange.sampleaudio.media.CoverStorage
import com.pointchange.sampleaudio.media.MediaResolver
import com.pointchange.sampleaudio.media.PlayerConnection
import com.pointchange.sampleaudio.media.Scan
import com.pointchange.sampleaudio.player.AudioEngine
import com.pointchange.sampleaudio.player.Equalizer
import com.pointchange.sampleaudio.player.lyrics.LyricsEntity
import com.pointchange.sampleaudio.player.lyrics.LyricsRepository
import com.pointchange.sampleaudio.player.lyrics.LyricsUiState
import com.pointchange.sampleaudio.player.queue.LibraryQueue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.videolan.libvlc.Media
import org.videolan.libvlc.interfaces.IMedia
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class PlayerStateViewModel(
    private val connection: PlayerConnection,
    private val songRepository: SongRepository,
    private val libraryQueue: LibraryQueue,
    private val lyricsRepository: LyricsRepository,
    private val core: AudioEngine,
    private val equalizer: Equalizer,
    val dataStore: UserPreferences,
) : ViewModel() {
    private lateinit var controller: MediaController
    val count =
        songRepository.countFlow().stateIn(viewModelScope, SharingStarted.Lazily, initialValue = 0)
    private val _controller = MutableStateFlow<MediaController?>(null)
    val controllerFuture = _controller.asStateFlow()
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState = _uiState.asStateFlow()
    private var currentPositionJob: Job? = null
    private var scanJob: Job? = null
    private var coverJob: Job? = null
    private var timerJob: Job? = null
    private var scanLyricsJob: Job? = null
    private val _scanUiState = MutableStateFlow(ScanUiState())
    val scanUiState = _scanUiState.asStateFlow()
    private val _scanConfig = MutableStateFlow(ScanConfig())
    val scanConfig = _scanConfig.asStateFlow()
    private val pagingCache = mutableMapOf<String, Flow<PagingData<SongInfo>>>()
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()
    val searchResultByQuery: Flow<PagingData<SongInfo>> = _query
        .debounce(500.milliseconds)
        .distinctUntilChanged()
        .flatMapLatest { keyword ->
            if (keyword.isEmpty() || keyword.isBlank()) {
                flowOf(PagingData.empty())
            } else {
                songRepository.search(keyword)
            }

        }.cachedIn(viewModelScope)

    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState = _settingsState.asStateFlow()

    val libraryQueueAsFn = libraryQueue
    val songRepositoryFn = songRepository
    val equalizerFn = equalizer
    val currentState = libraryQueue.currentState
    private val _lyricsUiState = MutableStateFlow<LyricsUiState>(LyricsUiState.Loading)
    val lyricsUiState = _lyricsUiState.asStateFlow()
    private val _currentTimeMs = MutableStateFlow(0L)
    val currentTimeMs = _currentTimeMs.asStateFlow()

    private val _timerState = MutableStateFlow(TimerState())
    val timerState = _timerState.asStateFlow()

    private val _equalizerState = MutableStateFlow(EqualizerState())
    val equalizerState = _equalizerState.asStateFlow()
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()
    private val _scanLyricsUiState = MutableStateFlow(ScanLyricsUiState())
    val scanLyricsUiState = _scanLyricsUiState.asStateFlow()

    private val listener = object : Player.Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                startPositionUpdates()
            } else {
                stopPositionUpdates()
            }
            super.onIsPlayingChanged(isPlaying)
        }

        override fun onPlayerError(error: PlaybackException) {
            setErrorUiState(error.message)
        }
    }

    init {
        viewModelScope.launch {

            launch {
                controller = connection.awaitController()
                controller.addListener(listener)
                val data = dataStore.currentState.first()
                controller.let {
//                    updateState(it)
                    data?.let { currentState ->
                        libraryQueue.setHistoryState(currentState)
                        currentState.toMediaItem()?.let { mediaItem ->
                            controller.setMediaItems(listOf(mediaItem), 0, currentState.positionMs)

                        }
                    }
                    if (it.isPlaying) {
                        startPositionUpdates()
                    }
                }
                _controller.value = controller
            }
            launch {
                libraryQueue.currentState.collect {
                    it.coverPath?.let {
                        getColorFromCover(it)
                    }
                }
            }
            launch {
                dataStore.scanConfig.collect {
                    it?.let { scanConfig ->
                        _scanConfig.value = scanConfig
                    }
                }
            }
            launch {
                dataStore.equalizerState.collect {
                    it?.let { equalizerState ->
                        _equalizerState.value = equalizerState
                        setVolume(userVolume = equalizerState.userVolume)
                    }
                }
            }

        }
    }

    fun setVolume(userVolume: Int) {
        core.setUserVolume(userVolume)
    }


    fun setErrorUiState(error: String?) {
        _uiState.update { it.copy(error = error) }
    }

    fun setPlaySpeed(speed: Float) {
        controller.setPlaybackSpeed(speed)
        libraryQueue.setPlaySpeed(speed)
    }

    fun setFavor(id: Long) {
        viewModelScope.launch {
            songRepository.toggleFavorite(id)
            libraryQueue.updateCurrentFavor()
        }
    }

    fun getPaging(queueName: String, list: List<Long>): Flow<PagingData<SongInfo>> {
        return pagingCache
            .getOrPut(queueName) {
                Pager(
                    config = PagingConfig(
                        pageSize = 20,
                        prefetchDistance = 5,
                        enablePlaceholders = false
                    ),
                    pagingSourceFactory = {
                        when (queueName) {
                            QueueListName.DEFAULT.label -> songRepository.songs()
                            QueueListName.FAVOR.label -> songRepository.favorites()
                            else -> songRepository.playlist(list)
                        }
                    }
                ).flow
                    .onStart {
                        saveFirstPageData()
                    }
                    .cachedIn(viewModelScope)
            }
    }

    fun saveFirstPageData() {
        viewModelScope.launch {
            val songs = songRepository.getSongsCount(20)
            dataStore.setFirstPageData(songs)
        }
    }

    fun setQuery(keyword: String = "") {
        _query.value = keyword
    }

    private fun startPositionUpdates() {
        currentPositionJob?.cancel()
        currentPositionJob = viewModelScope.launch {
            while (isActive) {
                controller.let { player ->
                    val duration = _uiState.value.durationMs
                    if (duration == C.TIME_UNSET || duration == 0L || controller.duration != duration) {
                        _uiState.update { it.copy(durationMs = controller.duration) }
                    }
                    _currentTimeMs.value = player.currentPosition
                }
                delay(500L.milliseconds)
            }
        }
    }

    private fun stopPositionUpdates() {
        currentPositionJob?.cancel()
        currentPositionJob = null
    }

    fun seekTo(positionMs: Long) {
        controller.seekTo(positionMs)
        _currentTimeMs.value = positionMs
    }

    fun prepare(id: Long, index: Int) {
        libraryQueue.prepare(controller, id, 0)

    }


    fun scanMediaStore(context: Context) {
        var config = _scanConfig.value
        config = config.copy(type = ScanType.MEDIA_STORE)
        scanFile(context, config)
    }

    fun scanAllFile(context: Context, autoComparable: Boolean = false) {
        var config = _scanConfig.value
        config = config.copy(type = ScanType.ALL_FILE)
        scanFile(context, config, autoComparable)
    }

    //scan
    fun scanFile(context: Context, config: ScanConfig, autoComparable: Boolean = false) {
        scanJob?.cancel()
//        val config = _scanConfig.value
//        config = config.copy(type = ScanType.ALL_FILE)
        val scanner = Scan(core = core)
        scanJob = viewModelScope.launch {
            scanner
                .scan(
                    context,
                    config = config,
                    roots = getScanRoot(),
                )
                .catch { throwable ->
                    if (throwable !is CancellationException) {
                        emit(ScanEvent.Error(throwable))
                    }
                }.collect { event ->
                    when (event) {
                        ScanEvent.Start -> {
                            _scanUiState.update { ScanUiState(state = ScanState.START) }
                        }

                        is ScanEvent.Scanning -> {
                            _scanUiState.update {
                                it.copy(
                                    state = ScanState.SCANNING,
                                    scannedCount = event.count
                                )
                            }

                        }

                        is ScanEvent.DurationParsing -> {
                            _scanUiState.update {
                                it.copy(
                                    state = ScanState.DURATION,
                                    durationCurrent = event.current,
                                    durationTotal = event.total
                                )
                            }
                        }

                        ScanEvent.Filtering -> {
                            _scanUiState.update { it.copy(state = ScanState.FILTER) }

                        }

                        is ScanEvent.Result -> {
                            val list = event.files
                            _scanUiState.update { it.copy(result = list, state = ScanState.RESULT) }

                            val count = songRepository.count()
                            if (count == 0) {
                                val artworkResolver = ArtworkResolver(core)
                                val coverStorage = CoverStorage(context)
                                MediaResolver(
                                    core = core,
                                    artworkResolver = artworkResolver,
                                    coverStorage = coverStorage
                                ).resolveSongs(
                                    files = list,
                                    transform = { media ->
                                        SongInfo(
                                            path = media.file.path,
                                            title = media.title,
                                            artist = media.artist,
                                            album = media.album,
                                            durationMs = media.durationMs,
                                            coverPath = null,
                                            fileSize = media.file.size,
                                            lastModified = media.file.lastModified,
                                            isFavorite = false
                                        )
                                    },
                                    onProcess = { current, total -> },
                                    onBatch = { songs -> songRepository.insertAll(songs) }
                                )
                                startCoverResolve(context)
                                scanLyricsFile()
                            }
                            _scanUiState.update { it.copy(state = ScanState.END) }

                            getResultHandle(context, autoComparable)

                        }

                        ScanEvent.Cancelled -> {
                            _scanUiState.update { it.copy(state = ScanState.CANCELLED) }
                            getResultHandle(context, autoComparable)
                        }

                        is ScanEvent.Error -> {
                            _scanUiState.update {
                                it.copy(
                                    state = ScanState.ERROR,
                                    error = event.throwable
                                )
                            }
                            getResultHandle(context, autoComparable)
                        }
                    }
                }
        }
    }

    private suspend fun getResultHandle(context: Context, autoComparable: Boolean = false) {
        if (autoComparable) {
            diffAndSaveInDatabase(context)
            withContext(Dispatchers.Main) {
                setIsRefreshing(false)
            }
        }
        cancelScanJob()
    }

    fun diffAndSaveInDatabase(context: Context) {
        val files = _scanUiState.value.result
        if (files.isEmpty()) return
        val artworkResolver = ArtworkResolver(core)
        val coverStorage = CoverStorage(context)
        val resolver = MediaResolver(
            core = core,
            artworkResolver = artworkResolver,
            coverStorage = coverStorage
        )
        viewModelScope.launch {
            val loadedSongs = songRepository.getScanInfos()
            val loadedMap = loadedSongs.associateBy { it.path }
            val needResolve = mutableListOf<ScanFile>()
            val scannedPath = HashSet<String>()
            for (file in files) {
                scannedPath += file.path
                val old = loadedMap[file.path]
                if (old == null) {
                    needResolve += file
                } else if (old.fileSize != file.size || old.lastModified != file.lastModified) {
//                    needResolve += file
                    needResolve += file.copy(id = old.id)
                }
            }
            val deletePaths =
                loadedSongs.asSequence().filter { it.path !in scannedPath }.map { it.path }.toList()

            songRepository.deleteByPath(deletePaths)
            resolver.resolveSongs(
                files = needResolve,
                transform = {
                    SongInfoUpsert(
                        id = it.id,
                        path = it.file.path,
                        title = it.title,
                        durationMs = it.durationMs,
                        artist = it.artist,
                        album = it.album,
                        fileSize = it.file.size,
                        lastModified = it.file.lastModified
                    )
                },
                onProcess = { current, total -> },
                onBatch = { songs ->
                    songRepository.upsertScanInfos(songs)
                })
            startCoverResolve(context)
        }
    }

    fun clearScanLyricsUiState() {
        _scanLyricsUiState.value = ScanLyricsUiState()
    }

    fun cancelScanLyrics() {
        scanLyricsJob?.cancel()
        scanLyricsJob = null
    }

    fun scanLyricsFile(isUserOperate: Boolean = false) {
        scanLyricsJob?.cancel()
        if (isUserOperate) {
            _scanLyricsUiState.update { it.copy(state = LoadLyricsState.LOADING) }
        }
        scanLyricsJob = viewModelScope.launch {
            val songInfos = songRepository.getAllSongInfo()
            val pathsMap = songInfos.associateBy { it.path.substringBefore(".") }
            val result = lyricsRepository.scan(roots = getScanRoot(), pathsMap.keys.toList())
            if (isUserOperate) {
                _scanLyricsUiState.update { it.copy(state = LoadLyricsState.END, result = result) }
            }

            if (result.isEmpty()) return@launch
            val count = lyricsRepository.lyricsDao.count()
            if (count > 0) {
                lyricsRepository.lyricsDao.deleteAll()
            }
            val lyricsEntity = result.mapNotNull { lyricPath ->
                ensureActive()
                pathsMap[lyricPath.substringBefore(".")]?.let {
                    val file = File(lyricPath)
                    LyricsEntity(
                        songId = it.id,
                        path = lyricPath,
                        format = file.extension,
                        lastModified = file.lastModified(),
                    )
                }
            }
            lyricsRepository.lyricsDao.insertAll(lyricsEntity)
        }
    }

    private fun startCoverResolve(context: Context) {
        if (coverJob?.isActive == true) return
        coverJob = viewModelScope.launch {
            try {
                val artworkResolver = ArtworkResolver(core)
                val coverStorage = CoverStorage(context)
                val resolver = MediaResolver(
                    core = core,
                    artworkResolver = artworkResolver,
                    coverStorage = coverStorage
                )
                resolver.resolveCovers(
                    onGetCoverList = { songRepository.getSongWithoutCover(50) }
                ) {
                    songRepository.updateCoverPath(it)
                }
            } finally {
                coverJob = null
            }
        }
    }

    fun clearDatabase() {
        controller.stop()
        saveFirstPageData()
        libraryQueue.clearCurrentState()
        viewModelScope.launch {
            songRepository.deleteAll()
        }
    }

    private fun cancelScanJob() {
        scanJob?.cancel()
        scanJob = null
    }

    fun cancelScan() {
        cancelScanJob()
        _scanUiState.update { it.copy(state = ScanState.CANCELLED) }
    }

    private fun getScanRoot(): List<File> {
        return listOf(Environment.getExternalStorageDirectory())
    }

    override fun onCleared() {
        libraryQueue.updateCurrentState { copy(positionMs = _currentTimeMs.value) }
        pagingCache.clear()
        currentPositionJob?.cancel()
        controller.removeListener(listener)
        controller.release()
        super.onCleared()
    }


    fun getColorFromCover(path: String) {
        try {
            BitmapFactory.decodeFile(path)?.let {
                val paletteBuilder = Palette.from(it).generate()
                val mainColor = paletteBuilder.getDominantColor(
                    Color.Black.toArgb()
                )
                val color1 = Color(
                    paletteBuilder.getDarkVibrantColor(mainColor)
                )
                val color2 = Color(
                    paletteBuilder.getMutedColor(
                        paletteBuilder.getDominantColor(
                            Color.DarkGray.toArgb()
                        )
                    )
                )
                _settingsState.update {
                    it.copy(
                        currentPlayCoverColor = listOf(
                            color1,
                            color2,
                            Color(mainColor)
                        )
                    )
                }
            }
        } catch (e: Exception) {
        }
    }

    suspend fun parseOne(path: String): AudioInfo =
        withContext(Dispatchers.IO) {
            val media = Media(core.libVLC, path)

            try {
                media.parse(IMedia.Parse.ParseLocal or IMedia.Parse.FetchLocal)
                val trackCount = media.trackCount
                var audio: IMedia.Track? = null
                for (i in 0 until trackCount) {
                    val track = media.getTrack(i)
                    if (track is IMedia.Track) {
                        audio = track
                        break
                    }
                }

                val file = File(path)
                AudioInfo(
                    path = path,
                    fileName = file.name,
                    fileSize = file.length(),

                    durationMs = media.duration.takeIf { it >= 0 },

                    title = media.getMeta(IMedia.Meta.Title),
                    artist = media.getMeta(IMedia.Meta.Artist),
                    album = media.getMeta(IMedia.Meta.Album),
                    genre = media.getMeta(IMedia.Meta.Genre),
                    copyright = media.getMeta(IMedia.Meta.Copyright),
                    publisher = media.getMeta(IMedia.Meta.Publisher),
                    description = media.getMeta(IMedia.Meta.Description),
                    language = media.getMeta(IMedia.Meta.Language),
                    date = media.getMeta(IMedia.Meta.Date),

                    codec = audio?.codec,
                    codecDescription = audio?.description,

                    sampleRate = try {
                        (audio as IMedia.AudioTrack).rate
                    } catch (e: ClassCastException) {
                        0
                    },
                    channels = try {
                        (audio as IMedia.AudioTrack).channels
                    } catch (e: ClassCastException) {
                        0
                    },
                    bitrate = audio?.bitrate
                        ?.toLong()
                        ?.takeIf { it > 0 },

                    bitsPerSample = null,

                    trackNumber = parseTrackNumber(
                        media.getMeta(IMedia.Meta.TrackNumber)
                    ),

                    discNumber = media
                        .getMeta(IMedia.Meta.DiscNumber)
                        ?.toIntOrNull(),

                    artworkUri = media.getMeta(IMedia.Meta.ArtworkURL)
                )
            } finally {
                media.release()
            }
        }

    private fun parseTrackNumber(value: String?): Int? {
        return value
            ?.substringBefore("/")
            ?.trim()
            ?.toIntOrNull()
    }

    fun saveInPublicPicture(context: Context, coverPath: String) {
        viewModelScope.launch {
            val resolver = context.contentResolver
            val name = "AudioCover_${
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(
                    Date()
                )
            }.jpg"
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
            val imageUri = resolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageDetails
            )

            imageUri?.let {
                try {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        val file = File(coverPath)
                        if (file.exists()) {
                            FileInputStream(file).use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        imageDetails.clear()
                        imageDetails.put(MediaStore.Images.Media.IS_PENDING, 0)
                        resolver.update(it, imageDetails, null, null)
                    }
                    Toast.makeText(context, "复制成功", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    resolver.delete(it, null, null)
                    Toast.makeText(context, "复制失败！！！", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun deleteByIds(ids: List<Long>, deletedItem: List<String>, checked: Boolean) {
        viewModelScope.launch {
            if (checked) {
                deletedItem.forEach {
                    try {
                        val file = File(it)
                        if (file.exists()) {
                            file.delete()
                        }
                    } catch (e: Exception) {

                    }

                }
            }
            val coverPaths = songRepository.getCoverPath(deletedItem)
            coverPaths.forEach { coverPath ->
                coverPath?.let {
                    try {
                        val file = File(it)
                        if (file.exists()) {
                            file.delete()
                        }
                    } catch (e: Exception) {

                    }
                }
            }
            songRepository.deleteByIds(ids)
        }
    }


    fun loadLyrics(songId: Long) {
        _lyricsUiState.value = LyricsUiState.Loading
        viewModelScope.launch {
            runCatching {
                lyricsRepository.loadLyrics(songId)
            }.onSuccess { document ->
                _lyricsUiState.value = if (document == null) {
                    LyricsUiState.Empty
                } else {
                    LyricsUiState.Success(document)
                }
            }.onFailure { error ->
                _lyricsUiState.value = LyricsUiState.Error(error.message ?: "Failed to load lyrics")
            }
        }
    }

    /* setting */
    fun switchThemeMode() {
        var currentThemeModeIndex = _settingsState.value.themeMode.ordinal
        currentThemeModeIndex++
        _settingsState.update {
            it.copy(
                themeMode = ThemeMode.entries.getOrNull(currentThemeModeIndex)
                    ?: ThemeMode.entries.first()
            )
        }
    }

    fun timerStart(second: Int) {
        _timerState.update { it.copy(running = true) }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            _timerState.update { it.copy(second = second) }
            while (_timerState.value.second > 0 && _timerState.value.running) {
                delay(1000.milliseconds)
                var s = _timerState.value.second
                s--
                _timerState.update { it.copy(second = s) }
            }
        }
    }

    fun timerPause() {
        _timerState.update { it.copy(running = false) }
    }

    fun timerEnd() {
        timerJob?.cancel()
        _timerState.update { it.copy(second = 0, running = false) }
    }

    fun setScanConfigExcludeFolders(list: List<String>) {
        _scanConfig.update { it.copy(excludeFolders = list.toSet()) }
    }

    fun setScanConfig(scanConfig: ScanConfig) {
        _scanConfig.value = scanConfig
        viewModelScope.launch {
            dataStore.setScanConfig(scanConfig)
        }
    }

    fun resetScanState() {
        _scanUiState.update { it.copy(state = ScanState.CONFIG_PREPARE) }
    }

    fun setEqualizerConfig(equalizerState: EqualizerState) {
        viewModelScope.launch {
            dataStore.setEqualizerConfig(equalizerState)
        }
    }

    fun setIsRefreshing(isRefreshing: Boolean) {
        _isRefreshing.value = isRefreshing
    }

    fun setShowMask() {
        val isShow = _settingsState.value.showMask
        _settingsState.update { it.copy(showMask = !isShow) }
    }

    companion object {
        val factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as App
                val playerConnection by lazy {
                    PlayerConnection(app)
                }
                PlayerStateViewModel(
                    connection = playerConnection,
                    songRepository = app.databaseContainer.songRepository,
                    libraryQueue = app.databaseContainer.libraryQueue,
                    lyricsRepository = app.databaseContainer.lyricsRepository,
                    dataStore = app.databaseContainer.dataStore,
                    equalizer = app.databaseContainer.equalizer,
                    core = app.core,
                )
            }
        }
    }
}
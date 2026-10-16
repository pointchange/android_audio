package com.pointchange.sampleaudio.player.queue

import androidx.media3.common.Player
import com.pointchange.sampleaudio.data.CurrentState
import com.pointchange.sampleaudio.data.QueueListName
import com.pointchange.sampleaudio.data.SongInfo
import com.pointchange.sampleaudio.data.toMediaItem
import com.pointchange.sampleaudio.database.SongRepository
import com.pointchange.sampleaudio.utils.defineSubList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryQueue(
    private val songRepository: SongRepository,
    private val queueRepository: QueueRepository,
) {
    //    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var scope: CoroutineScope? = null
    private var currentUser = defaultCurrentUser
    private val transformString = ","
    private val shuffleList = mutableListOf<Int>()
    private var currentShuffleIndex = -1

    private val _queue = MutableStateFlow<List<QueueData>>(emptyList())
    val queue = _queue.asStateFlow()
    private val _queueUiState = MutableStateFlow(QueueUiState())
    val queueUiState = _queueUiState.asStateFlow()
    private val _currentState = MutableStateFlow(CurrentState())
    val currentState = _currentState.asStateFlow()
    var firstLoadingData: List<SongInfo> = emptyList()
        private set


    fun handleInit() {
        scope?.launch {
            launch {
                songRepository.getIds().collect { ids ->
                    if (ids.isEmpty()) {
                        val queueCount = queueRepository.count()
                        if (queueCount != 0) {
                            queueRepository.deleteAll()
                        }
                    } else {
                        val ints = ids.map { it.toInt() }
                        val listToString = ints.joinToString(transformString)
                        val count = queueRepository.count()
                        if (count != 0) {
                            queueRepository.update(
                                defaultCurrentUser,
                                QueueListName.DEFAULT.label,
                                listToString
                            )
                            val favorIds = songRepository.getFavorIds()
                            val ints = favorIds.map { it.toInt() }
                            val listToString = ints.joinToString(transformString)
                            queueRepository.update(
                                name = defaultCurrentUser,
                                queueName = QueueListName.FAVOR.label,
                                value = listToString
                            )
                        } else {
                            queueRepository.insert(
                                QueueInfo(
                                    name = defaultCurrentUser,
                                    queueName = QueueListName.DEFAULT.label,
                                    value = listToString
                                )
                            )
                            queueRepository.insert(
                                QueueInfo(
                                    name = defaultCurrentUser,
                                    queueName = QueueListName.FAVOR.label,
                                    value = listToString
                                )
                            )
                        }
                    }
                }
            }
            launch {
                queueRepository.getAll().collect { queueList ->
                    if (queueList.isEmpty()) return@collect
                    val user = _queueUiState.value.user
                    val queueName = _queueUiState.value.title
                    val list = queueList.mapNotNull { queueInfo ->
                        if (user == queueInfo.name && queueName == queueInfo.queueName) {
                            _queueUiState.update {
                                it.copy(
                                    list = queueInfo.value.split(transformString)
                                        .map { it.toLong() }
                                )
                            }
                        }
                        if (user == queueInfo.name) {
                            QueueData(
                                queueInfo.queueName,
                                if (queueInfo.value.isNotEmpty()) {
                                    queueInfo.value.split(transformString).map { it.toLong() }
                                } else {
                                    emptyList()
                                }
                            )
                        } else {
                            null
                        }
                    }
                    _queue.value = list
                }
            }

        }
    }

    fun setScope(scope: CoroutineScope) {
        this.scope = scope
        handleInit()
    }

    // player operation
    fun prepare(player: Player, position: Long, index: Int) {

        scope?.launch {

            _queueUiState.update { it.copy(index = index) }

            val item = songRepository.getById(position)

            item?.let {
                withContext(Dispatchers.Main) {
                    updateCurrentState {
                        copy(
                            id = it.id,
                            path = it.path,
                            title = it.title,
                            artist = it.artist,
                            album = it.album,
                            durationMs = it.durationMs,
                            coverPath = it.coverPath,
                            isFavorite = it.isFavorite,
                            index = index
                        )
                    }
                    _currentState.value.toMediaItem()?.let { mediaItem ->
                        player.setMediaItems(listOf(mediaItem), 0, 0L)
                        player.prepare()
                        player.play()
                    }
                }
            }
        }
    }

    fun next(player: Player?) {
        val currentIndex = _queueUiState.value.index
        if (currentIndex == -1) return
        val queueName = _queueUiState.value.title
        val list = _queueUiState.value.list.ifEmpty {
            _queue.value.find { it.name == queueName }?.list ?: emptyList()
        }
        if (list.isEmpty()) return
        val total = list.size

        val nextIndex = when (_currentState.value.mode) {
            PlayerMode.DEFAULT -> {
                var index = currentIndex + 1
                if (index >= total) {
                    index = 0
                }
                index
            }

            PlayerMode.SHUFFLE -> {
                //暂时
                if (shuffleList.isEmpty()) {
                    shuffleList.addAll(List(total) { index -> index }.shuffled())
                    currentShuffleIndex = shuffleList.indexOfFirst { it == currentIndex }
                }
                currentShuffleIndex += 1

                shuffleList.getOrNull(currentShuffleIndex)
                    ?: shuffleList[0].also { currentShuffleIndex = 0 }
            }

            PlayerMode.REPEAT -> currentIndex
        }
        player?.let {
            prepare(player, list[nextIndex], nextIndex)
        }
    }

    fun previous(player: Player?) {
        val currentIndex = _queueUiState.value.index
        if (currentIndex == -1) return

        val queueName = _queueUiState.value.title
        val list = _queueUiState.value.list.ifEmpty {
            _queue.value.find { it.name == queueName }?.list ?: emptyList()
        }
        if (list.isEmpty()) return
        val total = list.size

        val nextIndex = when (_currentState.value.mode) {
            PlayerMode.DEFAULT -> {
                var index = currentIndex - 1
                if (index < 0) {
                    index = total - 1
                }
                index
            }

            PlayerMode.SHUFFLE -> {
                if (shuffleList.isEmpty()) {
                    shuffleList.addAll(List(total) { index -> index }.shuffled())
                    currentShuffleIndex = shuffleList.indexOfFirst { it == currentIndex }
                }
                currentShuffleIndex -= 1
                shuffleList.getOrNull(currentShuffleIndex)
                    ?: shuffleList[total - 1].also { currentShuffleIndex = total - 1 }
            }

            PlayerMode.REPEAT -> currentIndex
        }

        player?.let {
            prepare(player, list[nextIndex], nextIndex)
        }
    }


    //queue operation
    fun addQueueName(queueName: String) {
        scope?.launch {
            queueRepository.insert(
                QueueInfo(
                    name = currentUser,
                    queueName = queueName,
                    value = ""
                )
            )
        }
    }

    fun deleteAll() {
        scope?.launch {
            queueRepository.deleteAll(QueueListName.entries.map { it.name })
        }
    }

    fun deleteByQueueNames(name: String = defaultCurrentUser, queueNames: List<String>) {
        scope?.launch {
            queueRepository.deleteAllByQueueNames(name, queueNames)
        }
    }

    fun deleteItem(
        user: String = defaultCurrentUser,
        queueName: String = defaultCurrentUserQueue,
    ) {
        scope?.launch {
            val queue = _queue.value
            val ints = _queueUiState.value.list.ifEmpty {
                _queue.value.find { it.name == queueName }?.list
            }
            ints?.let {
                queueRepository.update(user, queueName, it.joinToString(transformString))
            }
        }
    }

    fun updateQueueName(
        user: String = defaultCurrentUser,
        queueName: String = defaultCurrentUserQueue,
    ) {
        scope?.launch {
            queueRepository.updateQueueName(user, queueName)
        }
    }

    fun updateQueueNames(
        user: String = defaultCurrentUser,
        queue: List<QueueData>
    ) {
        scope?.launch {
            queueRepository.deleteAll()
            for ((name, ints) in queue) {
                queueRepository.insert(
                    QueueInfo(
                        name = user,
                        queueName = name,
                        value = ints.joinToString(transformString)
                    )
                )
            }
        }
    }

    fun switchPlayerMode() {
        val mode = _currentState.value.mode
        val i = PlayerMode.valueOf(mode.name).ordinal + 1
        _currentState.update {
            it.copy(
                mode = PlayerMode.entries.getOrNull(i) ?: PlayerMode.entries[0]
            )
        }
    }

    fun getPlayerMode() = _currentState.value.mode

    fun setQueueStateTitleAndList(
        title: String,
        list: List<Long> = emptyList(),
        index: Int = -1,
        query: String = ""
    ) {
        if (query.trim().isEmpty()) {
            _queueUiState.update { it.copy(title = title, list = list) }
        } else {
            scope?.launch {
                val result = withContext(Dispatchers.IO) {
                    songRepository.searchTakeIds(query)
                }
                _queueUiState.update {
                    it.copy(
                        title = "搜索结果",
                        list = defineSubList(result, index),
                    )
                }
            }
        }
    }

    fun updateQueueList(
        user: String = defaultCurrentUser,
        queueName: String = defaultCurrentUserQueue,
        list: List<Long>
    ) {
        scope?.launch {
            queueRepository.update(user, queueName, list.joinToString(transformString))
        }
    }

    fun updateCurrentList(list: List<Long>) {
        val index = _queueUiState.value.index
        val currentList = _queueUiState.value.list
        val result = currentList.toMutableList()
        result.addAll(index + 1, list)
        result.distinct()
        _queueUiState.update { it.copy(list = result) }
    }


    fun updateCurrentState(block: CurrentState.() -> CurrentState) {
        _currentState.update { it.block() }
    }

    fun clearCurrentState() {
        _currentState.value = CurrentState()
    }

    suspend fun updateCurrentFavor() {
        val id = _currentState.value.id
        val isFavor = songRepository.getFavorById(id)
        _currentState.update { it.copy(isFavorite = isFavor) }
    }

    fun setPlaySpeed(speed: Float) {
        _currentState.update { it.copy(speed = speed) }
    }

    //    suspend fun saveCurrentState(context: Context) {
//        (context as App).databaseContainer.dataStore.setCurrentStateConfig(_currentState.value)
//    }
    fun getCurrentState(): CurrentState = _currentState.value

    fun setHistoryState(currentState: CurrentState) {
        _currentState.value = currentState
        val i = _queueUiState.value.list.indexOf(currentState.id)
        if (i == -1) return
        val res = defineSubList(_queueUiState.value.list, i)
        _queueUiState.update { it.copy(list = res, index = 0) }
    }


    fun setFirstLoadingData(songInfos: List<SongInfo>) {
        firstLoadingData = songInfos
    }


//    fun release() {
//
//        scope.cancel()
//        Log.d("str123","release")
//    }


    companion object {
        val defaultCurrentUser = "a"
        val defaultCurrentUserQueue = QueueListName.DEFAULT.label
    }
}
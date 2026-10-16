package com.pointchange.sampleaudio.player.queue

import com.pointchange.sampleaudio.data.QueueListName
import kotlinx.serialization.Serializable

//data class QueueState(
//    val index: Int = -1,
//    val list: List<Int> = emptyList(),
//    val title: String = QueueListName.DEFAULT.label,
//    val user: String = "a",
//    val queue: List<Pair<String, List<Int>>> = emptyList(),
//    val queue: List<QueueInfoMemory> = emptyList(),
//    val mode: PlayerMode = PlayerMode.DEFAULT,
//)

data class QueueData(
    val name: String = QueueListName.DEFAULT.label,
    val list: List<Long> = emptyList(),
)

data class QueueUiState(
    val index: Int = -1,
    val list: List<Long> = emptyList(),
    val title: String = QueueListName.DEFAULT.label,
    val user: String = "a",
)
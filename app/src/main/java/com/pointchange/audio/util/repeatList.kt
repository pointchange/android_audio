package com.pointchange.audio.util

import android.util.Log
import com.pointchange.audio.model_data.AudioMetadata
import com.pointchange.audio.model_data.PlayMode
import com.pointchange.audio.model_data.PlayingInfo
import kotlin.enums.enumEntries

//inline fun <reified T : Enum<T>> getIndexInEnum(ordinal: Int): T {
//    val values = enumValues<T>()
//    return if (ordinal in values.indices) {
//        values[ordinal]
//    } else {
//        values[0]
//    }
//}

inline fun <reified T : Enum<T>> getIndexInEnum(currentOrdinal: Int): T {
    var i = currentOrdinal
    i = increaseIndexInRange(i, enumEntries<T>().size)
    val values = enumValues<T>()
    return if (i in values.indices) {
        values[i]
    } else {
        values[0]
    }
}

fun increaseIndexInRange(currentIndex: Int, size: Int) =
    if (currentIndex + 1 > size - 1) 0 else currentIndex + 1

//fun increaseIndexInRange(currentIndex: Int, size: Int): Int {
//    var i = currentIndex
//    if (++i > size - 1) {
//        i = 0
//    }
//    return i
//}

fun decreaseIndexInRange(currentIndex: Int, size: Int) =
    if (currentIndex - 1 < 0) size - 1 else currentIndex - 1


fun decreaseIndexInRange(currentIndex: Int, size: Int, handle: (i: Int) -> Unit = {}): Int {
    var i = currentIndex
    if (--i < 0) {
        i = size - 1
    }
    handle(i)
    return i
}

fun nextPreHandle(
    mode: PlayMode,
    playList: List<AudioMetadata>,
    playingInfo: PlayingInfo,
    loopHandle: (index: Int, size: Int) -> Int = { _, _ -> 0 },
    randomHandle: (index: Int, size: Int) -> Int = { _, _ -> 0 },
    execHandle: (uri: String) -> Unit = {},
) {
    val size = playList.size
    val index = playingInfo.index

    var itemUri = ""
    when (mode) {
        PlayMode.LOOP -> {
            val i=loopHandle(index, size)
            itemUri = playList[i].uri
        }

        PlayMode.REPEAT -> {
            itemUri = playList[index].uri
        }

        PlayMode.RANDOM -> {
            val i=  randomHandle(index, size)
            itemUri = playList[i].uri
        }
    }
    execHandle(itemUri)
}


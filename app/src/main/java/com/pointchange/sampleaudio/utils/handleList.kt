package com.pointchange.sampleaudio.utils

import com.pointchange.sampleaudio.data.LimitManager

fun defineSubList(queueList: List<Long>, currentSongIndex: Int): List<Long> {
    val maxSize =
        if (queueList.size > LimitManager.playlistSize) LimitManager.playlistSize else queueList.size
    return if (currentSongIndex == queueList.lastIndex) {
        queueList
    } else {
        queueList.subList(currentSongIndex, maxSize)
    }
}

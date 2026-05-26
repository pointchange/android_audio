package com.pointchange.audio.view.page

import androidx.annotation.StringRes
import com.pointchange.audio.R

enum class Info(@param:StringRes val text:Int) {
    TITLE(R.string.title),
    ARTIST(R.string.artist),
    ALBUM(R.string.album),
    CHANNELS(R.string.channels),
    BITRATE(R.string.bitrate),
    SAMPLE_RATE(R.string.sample_rate),
    DURATION(R.string.duration),
    FILE_TYPE(R.string.file_type),
    FILE_SIZE(R.string.file_size),
    FILE_PATH(R.string.file_path),
}
package com.pointchange.audio.model

data class LrcData(
    var showLrc: Boolean = false,
    var existLrc: Boolean = true,
    var lrcContent: List<LrcLine> = emptyList(),
    var scanLrcLoading: Boolean = true,
    var lrcList: List<String> = emptyList(),
)
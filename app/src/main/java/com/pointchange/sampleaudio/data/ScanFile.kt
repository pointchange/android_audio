package com.pointchange.sampleaudio.data

import kotlinx.serialization.Serializable


data class ScanFile(
    val id: Long,
    val path: String,
    val size: Long,
    val lastModified: Long,
    val durationMs: Long,
)

enum class ScanType {
    ALL_FILE,
    MEDIA_STORE
}

enum class ScanState {
    CONFIG_PREPARE,
    START,
    SCANNING,
    DURATION,
    END,
    FILTER,
    RESULT,
    CANCELLED,
    ERROR,


}

@Serializable
data class ScanConfig(
    val type: ScanType = ScanType.MEDIA_STORE,
    val minDurationMs: Long = 60_000L,
    val excludeFolders: Set<String> = emptySet(),
    val minFileSize: Long = 100 * 1024L,
    val extensions: Set<String> = FULL_SCAN_EXTENSIONS
)

sealed interface ScanEvent {
    data object Start : ScanEvent
    data class Scanning(val count: Int) : ScanEvent
    data class DurationParsing(val current: Int, val total: Int) : ScanEvent
    data object Filtering : ScanEvent
    data class Result(val files: List<ScanFile>) : ScanEvent

    //    data object Complete : ScanEvent
    data object Cancelled : ScanEvent
    data class Error(val throwable: Throwable) : ScanEvent
}


data class ScanUiState(
    val state: ScanState = ScanState.CONFIG_PREPARE,
    val scannedCount: Int = 0,
    val durationCurrent: Int = 0,
    val durationTotal: Int = 0,
    val result: List<ScanFile> = emptyList(),
    val error: Throwable? = null
)

enum class LoadLyricsState {
    PREPARE,
    LOADING,
    END,
}

data class ScanLyricsUiState(
    val state: LoadLyricsState = LoadLyricsState.PREPARE,
    val result: List<String> = emptyList()
)

data class AudioGroup(
    val title: String,
    val parentPath: String,
    val files: List<String>,
)

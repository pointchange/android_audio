package com.pointchange.audio.model_data

data class AudioMetadataDetail(
    val uri: String = "",
    val artist: String = "",
    val album: String = "",
    val title: String = "",
    val coverPath: String? = null,
    val channels: Int = 0,
    val duration: Long = 0L,
    val bitrate: Int = 0,
    val codec: String = "",
    val sampleRate: Int = 0,
    ){
    override fun toString(): String {
        return "uri='$uri', artist='$artist', album='$album', title='$title', coverPath=$coverPath, channels=$channels, duration=$duration, bitrate=$bitrate, codec='$codec', sampleRate=$sampleRate"
    }
}

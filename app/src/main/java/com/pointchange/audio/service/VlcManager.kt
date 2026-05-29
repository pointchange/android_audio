package com.pointchange.audio.service

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.pointchange.audio.model_data.AudioItem
import com.pointchange.audio.model_data.AudioMetadata
import com.pointchange.audio.model_data.AudioMetadataDetail
import com.pointchange.audio.model_data.DataStoreCacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.suspendCancellableCoroutine
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaList
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.interfaces.IMedia
import org.videolan.libvlc.util.HWDecoderUtil
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.abs

object VlcManager {

    private var libVLC: LibVLC? = null
    var mediaPlayer: MediaPlayer? = null
        private set
    private var vlcEqualizer: MediaPlayer.Equalizer? = null
    private var currentMedia: Media? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying get() = _isPlaying.asStateFlow()
    private var _duration = MutableStateFlow(0L)
    val duration get() = _duration.asStateFlow()

    private var _current = MutableStateFlow(0L)
    val current get() = _current.asStateFlow()

    lateinit var repository: AudioRepository
        private set
    var tempList: List<AudioMetadata> = emptyList()
    private val _playError = MutableStateFlow(false)
    val playError = _playError.asStateFlow()
    private var count = 0
    private val listener = MediaPlayer.EventListener { event ->
        when (event.type) {
            MediaPlayer.Event.EncounteredError -> {
                _playError.value = true
                mediaPlayer?.stop()
                repository.next()
            }

            MediaPlayer.Event.Playing -> {
                _isPlaying.value = true
                if (mediaPlayer?.volume != 100) {
                    mediaPlayer?.volume = 100
                }
            }

            MediaPlayer.Event.Paused -> {
                _isPlaying.value = false
            }

            MediaPlayer.Event.EndReached -> {
//                            _isEnd.value = true
                repository.next()
            }

            MediaPlayer.Event.LengthChanged -> {
                if (count > 10) return@EventListener
                count++
                _duration.value = mediaPlayer?.length ?: 0L
            }

            MediaPlayer.Event.TimeChanged -> {
                _current.value = mediaPlayer?.time ?: 0L
            }
        }
    }

    fun init(context: Context) {
        if (libVLC == null) {
            repository = AudioRepository(context)
            libVLC = LibVLC(context)
            mediaPlayer = MediaPlayer(libVLC)
//                .apply {
//                setEventListener { event ->
//
//                }
//            }
            mediaPlayer?.setEventListener(listener)
            vlcEqualizer = MediaPlayer.Equalizer.create()
            mediaPlayer?.setEqualizer(vlcEqualizer)
        }
    }

    fun setPlayError() {
        _playError.value = false
    }

    fun playHandle(uri: String, isPrepare: Boolean = false) {
        count = 0
        currentMedia?.release()
        val media = Media(libVLC, uri)
        mediaPlayer?.media = media
        currentMedia = media
        if (isPrepare) return
        mediaPlayer?.play()
    }

    fun playHandle(media: Media) {
        currentMedia?.release()
//        val media = Media(libVLC, path.toUri())
//        media.setEventListener{event->
//            when(event.type){
//                IMedia.Event.ParsedChanged->{
//                    Log.d("str123", "${media.getMeta(IMedia.Meta.Artist)}")
//                    Log.d("str123", "${media.getMeta(IMedia.Meta.Title)}")
//                }
//            }
//        }
//        media.parseAsync(IMedia.Parse.ParseLocal or IMedia.Parse.FetchLocal, 5000)
        mediaPlayer?.media = media
        currentMedia = media
        mediaPlayer?.play()
    }

//    fun getAudioMeta(media: Media) {
//        media.run {
//            val artist = getMeta(IMedia.Meta.Artist) ?: ""
//            val title = getMeta(IMedia.Meta.Title) ?: ""
//        }
//    }

    suspend fun parseAudioMetadataAndPlay(uri: String) = suspendCancellableCoroutine { scope ->
        val file = File(uri)
        if (file.exists()) {
            val media = Media(libVLC, Uri.fromFile(file))
            media.setEventListener { event ->
                when (event.type) {
                    IMedia.Event.ParsedChanged -> {
                        val title = media.getMeta(IMedia.Meta.Title)
                        val artist = media.getMeta(IMedia.Meta.Artist)
                        val album = media.getMeta(IMedia.Meta.Album)
                        val coverPath = media.getMeta(IMedia.Meta.ArtworkURL)
                        val audioMetadata = AudioMetadata(
                            uri = uri,
                            title = title,
                            artist = artist,
                            album = album,
                            coverPath = coverPath,
                        )
                        scope.resume(audioMetadata)
                    }
                }
            }
            media.parseAsync(IMedia.Parse.ParseLocal or IMedia.Parse.FetchLocal, 5000)
            playHandle(media)
        }
    }

    suspend fun parseAudioMetadata(uri: String) = suspendCancellableCoroutine { scope ->
        val file = File(uri)
        if (file.exists()) {
            val media = Media(libVLC, Uri.fromFile(file))
            media.setEventListener { event ->
                when (event.type) {
                    IMedia.Event.ParsedChanged -> {
                        val title = media.getMeta(IMedia.Meta.Title)
                        val artist = media.getMeta(IMedia.Meta.Artist)
                        val album = media.getMeta(IMedia.Meta.Album)
                        val coverPath = media.getMeta(IMedia.Meta.ArtworkURL)

                        val audioItem = AudioItem(
                            uri = uri,
                            title = title,
                            artist = artist,
                            album = album,
                            coverPath = coverPath,
                        )

                        media.release()
                        scope.resume(audioItem)
                    }
                }
            }
            media.parseAsync(IMedia.Parse.ParseLocal or IMedia.Parse.FetchLocal, 5000)
        }
    }

    suspend fun parseAudioInfo(uri: String) = suspendCancellableCoroutine { scope ->
        val file = File(uri)
        if (file.exists()) {
            val media = Media(libVLC, Uri.fromFile(file))
            media.setEventListener { event ->
                when (event.type) {
                    IMedia.Event.ParsedChanged -> {
                        try {
                            val title = media.getMeta(IMedia.Meta.Title)
                            val artist = media.getMeta(IMedia.Meta.Artist)
                            val album = media.getMeta(IMedia.Meta.Album)
                            val coverPath = media.getMeta(IMedia.Meta.ArtworkURL)

                            val trackCount = media.trackCount
                            val duration = media.duration
                            var bitrate = 0
                            var codec = ""
                            var sampleRate = 0
                            var channels = 0
                            repeat(trackCount) {
                                val meta = media.getTrack(it)
                                bitrate = meta.bitrate
                                codec = meta.codec

                                sampleRate = try {
                                    (meta as IMedia.AudioTrack).rate
                                } catch (e: ClassCastException) {
                                    0
                                }
                                channels = try {
                                    (meta as IMedia.AudioTrack).channels
                                } catch (e: ClassCastException) {
                                    0
                                }

                            }

                            val audioMetadataDetail = AudioMetadataDetail(
                                uri = uri,
                                title = title,
                                artist = artist,
                                album = album,
                                coverPath = coverPath,
                                channels = channels,
                                duration = duration,
                                bitrate = bitrate,
                                codec = codec,
                                sampleRate = sampleRate,
                            )

                            media.release()
                            scope.resume(audioMetadataDetail)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                }
            }
            media.parseAsync(IMedia.Parse.ParseLocal or IMedia.Parse.FetchLocal, 5000)
        }
    }

    fun destroy() {
        repository.clear()
        currentMedia?.release()
        mediaPlayer?.release()
        libVLC?.release()
    }

    fun seekTo(newCurrent: Long) {
        mediaPlayer?.let {
            it.time = newCurrent
        }
        _current.value = newCurrent
    }

    suspend fun getDuration(uri: String) = suspendCancellableCoroutine { scope ->
        val file = File(uri)
        if (file.exists()) {
            val media = Media(libVLC, Uri.fromFile(file))
            media.setEventListener { event ->
                when (event.type) {
                    IMedia.Event.ParsedChanged -> {
                        val duration = media.duration
                        media.release()
                        scope.resume(duration)

                    }
                }
            }
            media.parseAsync(IMedia.Parse.ParseLocal or IMedia.Parse.FetchLocal, 5000)
        }
    }

    fun getPresetNames(): List<String> {
        val count = MediaPlayer.Equalizer.getPresetCount()
        return (0 until count).map { MediaPlayer.Equalizer.getPresetName(it) }
    }

    fun setPreset(presetIndex: Int) {
        // 3.7.0 直接通过静态工厂方法从预设创建新实例
        vlcEqualizer = MediaPlayer.Equalizer.createFromPreset(presetIndex)
        // 必须应用到当前播放器才会即时生效
        mediaPlayer?.setEqualizer(vlcEqualizer)
    }

    fun getIsPlaying() = _isPlaying.value

    fun setVolume(volume: Int) {
        mediaPlayer?.volume = volume
    }

    fun getVolume() = mediaPlayer?.volume
}

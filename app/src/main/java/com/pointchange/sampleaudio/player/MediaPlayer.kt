package com.pointchange.sampleaudio.player

import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.common.util.Log
import androidx.media3.common.util.SystemClock
import androidx.media3.common.util.UnstableApi
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.pointchange.sampleaudio.player.queue.LibraryQueue
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer

@OptIn(UnstableApi::class)
class MediaPlayer(
    looper: Looper,
    private val core: AudioEngine,
    private val libraryQueue: LibraryQueue,
    private val audioManager: AudioManager
) : SimpleBasePlayer(looper) {
    private val vlcPlayer = core.vlcPlayer

    private var playbackState = STATE_IDLE
    private var playWhenReady = false
    private var durationMs = C.TIME_UNSET
    private var pendingSeekPositionMs = C.TIME_UNSET
    private var currentPositionMs = 0L
    private var currentMediaItem: MediaItem? = null
    private var playlist: List<MediaItem> = emptyList()
    private var currentIndex = C.INDEX_UNSET
    private var repeatMode = Player.REPEAT_MODE_ALL
    private var shuffleModeEnable = false
    private var playWhenReadyChangeReason = Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST
    private var playbackException: PlaybackException? = null
    private var hasAudioFocus = false
    private var resumeOnFocusGain = false
    private val androidMediaAudioAttributes = android.media.AudioAttributes
        .Builder()
        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()
    private val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(androidMediaAudioAttributes)
//        .setWillPauseWhenDucked(false)
        .setOnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    hasAudioFocus = true
                    if (resumeOnFocusGain) {
                        resumeOnFocusGain = false
                        vlcPlayer.play()
                        playWhenReady = true
                        invalidateState()
                    }
                }
                //要暂停
                AudioManager.AUDIOFOCUS_LOSS -> {
                    resumeOnFocusGain = false
                    playWhenReady = false
                    vlcPlayer.pause()
                    hasAudioFocus = false
                    invalidateState()
                }
                //暂时暂停
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    resumeOnFocusGain = playWhenReady
                    playWhenReady = false
                    vlcPlayer.pause()
                    hasAudioFocus = false
                    invalidateState()
                }
                //降低音量
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    vlcPlayer.volume = (core.volume * 0.2f).toInt()
                }
            }
        }
        .build()


    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo?>?) {
            super.onAudioDevicesRemoved(removedDevices)
            val removedOutput = removedDevices?.any { device ->
                when (device?.type) {
                    AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                    AudioDeviceInfo.TYPE_WIRED_HEADSET,
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                    AudioDeviceInfo.TYPE_BLE_HEADSET,
                    AudioDeviceInfo.TYPE_BLE_SPEAKER,
                    AudioDeviceInfo.TYPE_BLE_BROADCAST -> true

                    else -> false
                }
            } ?: false

            if (removedOutput) {
                handleAudioDeviceDisconnected()
            }

        }
    }
    private var lastPositionUpdateTime = 0L

    init {
        vlcPlayer.setEventListener { vlcEvent(it) }
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, Handler(looper))
        val audioAttributes = AudioAttributes
            .Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        setAudioAttributes(audioAttributes, true)

    }


    override fun getState(): State {
        return State
            .Builder()
            .setAvailableCommands(
                Player.Commands.Builder().addAll(
                    COMMAND_PLAY_PAUSE,
                    COMMAND_GET_CURRENT_MEDIA_ITEM,
                    COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM,
                    COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
                    COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                    COMMAND_SEEK_TO_PREVIOUS,
                    COMMAND_SEEK_TO_NEXT,
                    COMMAND_STOP,
                    COMMAND_SET_REPEAT_MODE,
                    COMMAND_SET_SHUFFLE_MODE,
                    COMMAND_CHANGE_MEDIA_ITEMS,
                    COMMAND_PREPARE,
                    COMMAND_GET_TIMELINE,
                    COMMAND_SET_SPEED_AND_PITCH,
                    COMMAND_GET_METADATA,
                    COMMAND_SET_AUDIO_ATTRIBUTES,
                    COMMAND_GET_AUDIO_ATTRIBUTES
                ).build()
            )
            .setPlaybackState(playbackState)
            .setPlayWhenReady(playWhenReady, playWhenReadyChangeReason)
            .setRepeatMode(repeatMode)
            .setShuffleModeEnabled(shuffleModeEnable)
            .setPlaylist(buildPlaylistData())
            .setCurrentMediaItemIndex(currentIndex)
            .setContentPositionMs(currentPositionMs)
            .setPlayerError(playbackException)
            .build()

    }

    override fun handlePrepare(): ListenableFuture<*> {
        if (currentMediaItem == null) {
            playbackState = Player.STATE_IDLE
            invalidateState()
            return Futures.immediateVoidFuture()
        }
        playbackState = Player.STATE_BUFFERING
        invalidateState()

        loadCurrentMedia(startPositionMs = currentPositionMs)
        return Futures.immediateVoidFuture()
    }

    override fun handleSetPlayWhenReady(playWhenReady: Boolean): ListenableFuture<*> {
        this.playWhenReady = playWhenReady
        playWhenReadyChangeReason = Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST

        if (playWhenReady) {
            if (requestAudioFocus()) {
                vlcPlayer.play()
            } else {
                this.playWhenReady = false
            }
//            vlcPlayer.play()
        } else {
            vlcPlayer.pause()
            abandonAudioFocus()
        }
        invalidateState()

        return Futures.immediateVoidFuture()
    }

    override fun handleSeek(
        mediaItemIndex: Int,
        positionMs: Long,
        seekCommand: Int
    ): ListenableFuture<*> {
        when (seekCommand) {
            COMMAND_SEEK_TO_PREVIOUS -> {
                libraryQueue.previous(this)
            }

            COMMAND_SEEK_TO_NEXT -> {
                libraryQueue.next(this)
            }

            else -> {
                if (mediaItemIndex == C.INDEX_UNSET) {
                    return Futures.immediateVoidFuture()
                }
                val targetPosition =
                    if (positionMs == C.TIME_UNSET) 0L else positionMs.coerceAtLeast(0L)
                if (mediaItemIndex != currentIndex) {
                    if (mediaItemIndex !in playlist.indices) {
                        return Futures.immediateVoidFuture()
                    }
                    currentIndex = mediaItemIndex
                    currentMediaItem = playlist[mediaItemIndex]
                    currentPositionMs = targetPosition
                    durationMs = C.TIME_UNSET
                    loadCurrentMedia(targetPosition)
                    return Futures.immediateVoidFuture()
                }

                currentPositionMs = targetPosition
                if (playbackState == STATE_IDLE) {
                    pendingSeekPositionMs = targetPosition
                } else {
                    vlcPlayer.time = targetPosition
                }
            }
        }

        invalidateState()
        return Futures.immediateVoidFuture()
    }

    override fun handleRelease(): ListenableFuture<*> {
        audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
        abandonAudioFocus()
        vlcPlayer.stop()
        vlcPlayer.release()
        core.release()
        return Futures.immediateVoidFuture()
    }


    private fun vlcEvent(event: MediaPlayer.Event) {
        when (event.type) {
            MediaPlayer.Event.Opening -> {
                playbackState = Player.STATE_BUFFERING
                refreshState()
            }

            MediaPlayer.Event.Buffering -> {
                playbackState = Player.STATE_READY
                refreshState()
            }

            MediaPlayer.Event.Playing -> {
                playbackState = Player.STATE_READY
                applyPendingSeek()
                if (!playWhenReady) {
                    vlcPlayer.pause()
                }
                vlcPlayer.volume = core.volume

                refreshState()
            }

            MediaPlayer.Event.Paused -> {
                playbackState = Player.STATE_READY
                refreshState()
            }
            MediaPlayer.Event.EndReached -> {
                libraryQueue.next(this)
                playbackState = STATE_ENDED
                playWhenReady = false
                invalidateState()
            }

            MediaPlayer.Event.Stopped -> {
                playbackState = Player.STATE_IDLE
                refreshState()
            }

            MediaPlayer.Event.EncounteredError -> {
                playbackState = Player.STATE_IDLE
                playWhenReady = false
                playbackException = PlaybackException(
                    "something wrong", null,
                    PlaybackException.ERROR_CODE_UNSPECIFIED
                )
                refreshState()
            }

            MediaPlayer.Event.TimeChanged -> {
                refreshStateTimeChange()
            }
        }
    }

    private fun applyPendingSeek() {
        val position = pendingSeekPositionMs
        if (position == C.TIME_UNSET) return
        pendingSeekPositionMs = C.TIME_UNSET
        if (position <= 0L) return
        vlcPlayer.time = position
        currentPositionMs = position
    }
    private fun refreshStateTimeChange() {
        val now = SystemClock.DEFAULT.elapsedRealtime()
        if (now - lastPositionUpdateTime < 500) {
            return
        }
        lastPositionUpdateTime = now
        val time = vlcPlayer.time.coerceAtLeast(0L)
        if (time >= 0L) currentPositionMs = time
        val len = vlcPlayer.length
        if (len > 0L) {
            durationMs = len
        }
        invalidateState()

    }
    private fun refreshState() {
        val time = vlcPlayer.time.coerceAtLeast(0L)
        if (time >= 0L) currentPositionMs = time
        val len = vlcPlayer.length
        if (len > 0L) {
            durationMs = len
        }
        invalidateState()

    }

    override fun handleSetMediaItems(
        mediaItems: List<MediaItem>,
        startIndex: Int,
        startPositionMs: Long
    ): ListenableFuture<*> {
        playlist = mediaItems
        if (playlist.isEmpty()) {
            vlcPlayer.stop()
            currentIndex = C.INDEX_UNSET
            currentMediaItem = null
            currentPositionMs = 0L
            durationMs = C.TIME_UNSET
            pendingSeekPositionMs = C.TIME_UNSET
            playbackState = Player.STATE_IDLE
            invalidateState()
            return Futures.immediateVoidFuture()
        }
        currentIndex = startIndex.coerceIn(0, playlist.lastIndex)
        currentMediaItem = playlist[currentIndex]
        currentPositionMs = if (startPositionMs != C.TIME_UNSET) startPositionMs else 0L

        playbackException = null
        durationMs = C.TIME_UNSET
        playbackState = Player.STATE_IDLE
        invalidateState()
        return Futures.immediateVoidFuture()
    }

    private fun loadCurrentMedia(startPositionMs: Long = 0L) {
        val item = currentMediaItem ?: return
        val uri = item.localConfiguration?.uri ?: run {
            playbackState = STATE_IDLE
            playWhenReady = false
            invalidateState()
            return
        }
        pendingSeekPositionMs = startPositionMs.coerceAtLeast(0L)

        vlcPlayer.stop()
        val media = Media(core.libVLC, "$uri")
        vlcPlayer.media = media
        media.release()
        currentPositionMs = pendingSeekPositionMs
        durationMs = C.TIME_UNSET
        invalidateState()
        if (playWhenReady) {
            vlcPlayer.play()
        }
    }

    private fun buildPlaylistData(): List<MediaItemData> {

        return playlist.mapIndexed { index, item ->
            val duration =
                if (index == currentIndex && durationMs != C.TIME_UNSET) durationMs * 1000L else C.TIME_UNSET
            MediaItemData
                .Builder(item.mediaId)
                .apply {
                    setMediaItem(item)
                    setDurationUs(duration)
                    setDefaultPositionUs(currentPositionMs * 1000L)
                    setIsDynamic(false)
                    setIsSeekable(true)
                    setIsPlaceholder(false)
                }
                .build()
        }
    }

    override fun handleStop(): ListenableFuture<*> {
        vlcPlayer.stop()
        currentPositionMs = 0L
        pendingSeekPositionMs = C.TIME_UNSET
        playbackState = Player.STATE_IDLE
        playWhenReady = false
        invalidateState()
        return Futures.immediateVoidFuture()
    }

    private fun playNextAfterEnd() {
        when (repeatMode) {
            Player.REPEAT_MODE_ONE -> {
                restartCurrentMedia()
            }

            else -> {
                val nextIndex = nextMediaItemIndex
                if (nextIndex != C.INDEX_UNSET) {
                    currentIndex = nextIndex
                    currentMediaItem = playlist[nextIndex]
                    currentPositionMs = 0L
                    durationMs = C.TIME_UNSET
                    loadCurrentMedia(0L)
                    return
                }
                currentPositionMs =
                    if (durationMs == C.TIME_UNSET) currentPositionMs else durationMs
                playbackState = STATE_ENDED
                playWhenReady = false
                invalidateState()
            }
        }
    }

    private fun restartCurrentMedia() {
        currentMediaItem?.let {
            currentPositionMs = 0L
            pendingSeekPositionMs = 0L
            durationMs = C.TIME_UNSET
            playbackState = Player.STATE_BUFFERING
            loadCurrentMedia(0L)
        }
    }

//    override fun handleSetRepeatMode(repeatMode: Int): ListenableFuture<*> {
////        this.repeatMode = repeatMode
//        invalidateState()
//        return Futures.immediateVoidFuture()
//    }

    override fun handleSetShuffleModeEnabled(shuffleModeEnabled: Boolean): ListenableFuture<*> {
        libraryQueue.switchPlayerMode()
//        this.shuffleModeEnable = shuffleModeEnabled
        invalidateState()
        return Futures.immediateVoidFuture()
    }

    override fun handleSetPlaybackParameters(playbackParameters: PlaybackParameters): ListenableFuture<*> {
        val speed = playbackParameters.speed
        vlcPlayer.rate = speed
        invalidateState()
        return Futures.immediateVoidFuture()
    }

    override fun handleSetAudioAttributes(
        audioAttributes: AudioAttributes,
        handleAudioFocus: Boolean
    ): ListenableFuture<*> {
        return Futures.immediateVoidFuture()
    }

    private fun requestAudioFocus(): Boolean {
        val result = audioManager.requestAudioFocus(audioFocusRequest)
        hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        return hasAudioFocus
    }

    private fun abandonAudioFocus() {
        audioManager.abandonAudioFocusRequest(audioFocusRequest)
        hasAudioFocus = false
    }

    private fun handleAudioDeviceDisconnected() {
        if (playWhenReady) {
            playWhenReady = false
            vlcPlayer.pause()
            playWhenReadyChangeReason =
                Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_BECOMING_NOISY
            invalidateState()
        }
    }

}
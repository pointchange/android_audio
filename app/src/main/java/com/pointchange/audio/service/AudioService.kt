package com.pointchange.audio.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.datastore.preferences.core.edit
import com.pointchange.audio.MainActivity
import com.pointchange.audio.R
import com.pointchange.audio.model_data.DataStoreCacheManager
import com.pointchange.audio.model_data.PlayMode
import com.pointchange.audio.model_data.dataStore
import com.pointchange.audio.util.getBitmap
import com.pointchange.audio.view.widget.EventAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class AudioService : Service() {
    private val CHANNEL_ID = "vlc_playback_channel"
    private val NOTIFICATION_ID = 101
    private var currentShuffleMode = PlaybackStateCompat.SHUFFLE_MODE_NONE
    private lateinit var mediaSession: MediaSessionCompat
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isSeek = false

    private var current: Long = 0L
    private var playMode: PlayMode = PlayMode.LOOP
    private var isPlay: Boolean = false

    override fun onBind(p0: Intent?): IBinder? = null

    private fun refreshNotification(title: String, cover: String, isPlaying: Boolean) {
        val notification = buildMediaNotification(title, cover, isPlaying)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
        if (isPlaying) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            stopForeground(STOP_FOREGROUND_DETACH)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        mediaSession = MediaSessionCompat(this, "VLCAudioService").apply {
            isActive = true
            // 设置响应控制：播放、暂停、下一首、上一首、拖动进度条
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

            // 2. 核心：绑定系统锁屏/通知栏按钮的点击事件回调
            setCallback(object : MediaSessionCompat.Callback() {

                override fun onPlay() {
                    VlcManager.mediaPlayer?.play()
                    updatePlaybackState(
                        PlaybackStateCompat.STATE_PLAYING, current
                    )
                }

                override fun onSkipToPrevious() {
                    super.onSkipToPrevious()
                    VlcManager.repository.previous()
                }

                override fun onPause() {
                    VlcManager.mediaPlayer?.pause()
                    updatePlaybackState(PlaybackStateCompat.STATE_PAUSED, current)
                }

                override fun onSkipToNext() {
                    // 业务逻辑：通过 ViewModel/Repository 切下一首
                    VlcManager.repository.next()
                }

                override fun onSeekTo(pos: Long) {
                    VlcManager.seekTo(pos)
                    isSeek = true
                    updatePlaybackState(
                        PlaybackStateCompat.STATE_PLAYING,
                        pos
                    )
                }

                override fun onSetShuffleMode(shuffleMode: Int) {
                    super.onSetShuffleMode(shuffleMode)
                    currentShuffleMode = shuffleMode
                    VlcManager.repository.setPlayingInfoMode()
                }

                override fun onCustomAction(action: String?, extras: Bundle?) {
                    super.onCustomAction(action, extras)
                    when (action) {
                        "ACTION_SHUFFLE" -> {
                            VlcManager.repository.setPlayingInfoMode()
                        }
                    }
                }
            })
        }
        scope.launch {
            VlcManager.current.collect {
                current = it
            }
        }
        scope.launch {
            combine(
                VlcManager.duration, VlcManager.isPlaying,
                VlcManager.repository.playingInfo
            ) { duration, isPlaying, playingInfo ->
                Triple(duration, isPlaying, playingInfo)
            }.collect { (duration, isPlaying, playingInfo) ->
                playMode = playingInfo.mode
                isPlay = isPlaying
                updateMediaMetadata(
                    title = playingInfo.audioMetadata?.title
                        ?: this@AudioService.getString(R.string.unknow_title),
                    artist = playingInfo.audioMetadata?.artist
                        ?: this@AudioService.getString(R.string.unknow_artist),
                    duration = duration,
                    cover = playingInfo.audioMetadata?.coverPath ?: ""
                )
                val stateInt =
                    if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
                updatePlaybackState(current = current, state = stateInt)
                refreshNotification(
                    title = playingInfo.audioMetadata?.title
                        ?: this@AudioService.getString(R.string.unknow_title),
                    cover = playingInfo.audioMetadata?.coverPath ?: "",
                    isPlaying = isPlaying
                )
                VlcManager.repository.refreshWidget(
                    playingInfo = playingInfo,
                    isPlaying = isPlaying,
                    this@AudioService
                )
            }
        }
    }

    fun createSimpleNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANNEL_ID, "音乐播放", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setContentTitle("")
            setContentText("")
            setSmallIcon(R.drawable.ic_launcher_background)
            setPriority(NotificationManager.IMPORTANCE_LOW)
        }.build()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        val actionType = intent?.action ?: ""
        if (actionType == "") return START_STICKY
        val actionEnum = try {
            EventAction.valueOf(actionType)
        } catch (e: IllegalStateException) {
            return START_STICKY
        }
        when (actionEnum) {
            EventAction.TOGGLE -> {
//                startForeground(NOTIFICATION_ID, createSimpleNotification())
                if (isPlay) {
                    VlcManager.mediaPlayer?.pause()
                } else {
                    VlcManager.mediaPlayer?.play()
                }
            }

            EventAction.Next -> {
                VlcManager.repository.next()
            }

            EventAction.Pre -> {
                VlcManager.repository.previous()
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        savePlayInfo(this)
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    fun savePlayInfo(context: Context) {
        scope.launch(Dispatchers.IO) {
            try {
                val playInfo = VlcManager.repository.savePlayInfo()
                DataStoreCacheManager.savePlayInfo(context, playInfo)
                val themeConfig = ThemeManager.getThemeConfig()
                DataStoreCacheManager.saveThemeConfig(context, themeConfig)
                VlcManager.repository.refreshWidget(
                    playingInfo = playInfo,
                    isPlaying = false,
                    context = context
                )
            } finally {
                VlcManager.destroy()
                scope.cancel()
            }
        }

    }

    private fun buildMediaNotification(
        title: String,
        cover: String,
        isPlaying: Boolean
    ): Notification {
        val context = applicationContext

        // 点击整个通知栏时，能够点开回到前端 Compose 的 MainActivity
        val clickIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val pauseIntent: Intent = Intent(this, AudioService::class.java).apply {
            action = EventAction.TOGGLE.name
        }
        val pausePendingIntent = PendingIntent.getService(
            this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playIntent: Intent = Intent(this, AudioService::class.java).apply {
            action = EventAction.TOGGLE.name
        }
        val playPendingIntent = PendingIntent.getService(
            this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent: Intent = Intent(this, AudioService::class.java).apply {
            action = EventAction.Next.name
        }
        val nextPendingIntent = PendingIntent.getService(
            this, 1, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val preIntent: Intent = Intent(this, AudioService::class.java).apply {
            action = EventAction.Pre.name
        }
        val prePendingIntent = PendingIntent.getService(
            this, 2, preIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val notificationCompatBuilder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setContentTitle(title)
            setContentText("正在播放...")
            setSmallIcon(R.drawable.launcher_audio)
            setContentIntent(pendingIntent)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 确保锁屏状态下完全可见
            setOngoing(true)
            if (cover != "") {
                val bitmap = getBitmap(cover = cover)
                if (bitmap != null) {
                    setLargeIcon(bitmap)
                }
            }
        }

        if (isPlaying) {
            notificationCompatBuilder.addAction(
                R.drawable.round_pause_24,
                "Pause",
                pausePendingIntent
            )
        } else {
            notificationCompatBuilder.addAction(
                R.drawable.round_play_arrow_24,
                "Play",
                playPendingIntent
            )
        }
        notificationCompatBuilder.addAction(
            R.drawable.round_skip_next_24,
            "Next",
            nextPendingIntent
        )
        notificationCompatBuilder.addAction(
            R.drawable.round_skip_previous_24,
            "Pre",
            prePendingIntent
        )

        notificationCompatBuilder.setStyle(
            androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(0, 1)
        )
        return notificationCompatBuilder.build()
    }


    private fun updateMediaMetadata(title: String, artist: String, duration: Long, cover: String) {
        val metadata = MediaMetadataCompat.Builder().apply {
            putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)

            if (cover != "") {
                val bitmap = getBitmap(cover = cover)
                if (bitmap != null) {
                    putBitmap(
                        MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                        bitmap
                    )
                }
            }

        }.build()

        mediaSession.setMetadata(metadata)
    }

    fun updatePlaybackState(state: Int, current: Long) {

        val stateBuilder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_SEEK_TO or
                        PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE
            )
            // 将播放状态、当前进度、播放速度传给系统，锁屏进度条就会自己动起来
            .setState(state, current, 1.0f)

        val shuffleIcon = when (playMode) {
            PlayMode.LOOP -> R.drawable.round_repeat_24
            PlayMode.REPEAT -> R.drawable.round_repeat_one_24
            PlayMode.RANDOM -> R.drawable.round_shuffle_24
        }
        val shuffleAction = PlaybackStateCompat.CustomAction.Builder(
            "ACTION_SHUFFLE",
            "Shuffle",
            shuffleIcon // 你的本地随机图标
        ).build()

        stateBuilder.addCustomAction(shuffleAction)

        mediaSession.setShuffleMode(currentShuffleMode)
        mediaSession.setPlaybackState(stateBuilder.build())
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "媒体播放服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "秒速"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}


//class AudioService : MediaSessionService() {
//    private var mediaSession: MediaSession? = null
//
//    @OptIn(UnstableApi::class)
//    override fun onCreate() {
//        super.onCreate()
//        val player: ExoPlayer = ExoPlayer.Builder(this).build()
//        // system know you play music
////        val audioAttributes = AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
////            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build()
//
//        player.setAudioAttributes(AudioAttributes.DEFAULT, true)
//        mediaSession = MediaSession.Builder(this, player).build()
//    }
//
//    override fun onGetSession(p0: ControllerInfo): MediaSession? = mediaSession
//
//    override fun onDestroy() {
//        mediaSession?.run {
//            player.release()
//            release()
//            mediaSession = null
//        }
//        super.onDestroy()
//    }
//}
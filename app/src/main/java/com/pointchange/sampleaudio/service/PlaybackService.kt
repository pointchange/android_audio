package com.pointchange.sampleaudio.service

import android.app.PendingIntent
import android.content.Intent
import android.media.AudioManager
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.pointchange.sampleaudio.App
import com.pointchange.sampleaudio.MainActivity
import com.pointchange.sampleaudio.R
import com.pointchange.sampleaudio.player.MediaPlayer
import com.pointchange.sampleaudio.player.queue.LibraryQueue
import com.pointchange.sampleaudio.player.queue.PlayerMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {
    private val audioManager by lazy { getSystemService(AudioManager::class.java) }
    private lateinit var player: MediaPlayer
    private lateinit var session: MediaSession
    private lateinit var libraryQueue: LibraryQueue
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this, 0, Intent(
                this,
                MainActivity::class.java
            ), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val app = application as App
        libraryQueue = app.databaseContainer.libraryQueue
        libraryQueue.setScope(scope)
        player = MediaPlayer(
            Looper.getMainLooper(),
            app.core,
            libraryQueue,
            audioManager,
        )
        player.addListener(object : Player.Listener {
            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                session.setMediaButtonPreferences(createOrUpdateMediaButtonsState())
            }
        })
        session = MediaSession
            .Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent)
            .build()
        session.setMediaButtonPreferences(createOrUpdateMediaButtonsState())
        val provider = DefaultMediaNotificationProvider.Builder(this).build()
        provider.setSmallIcon(R.drawable.round_music_note_24)
        setMediaNotificationProvider(provider)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        scope.launch {
            try {
                (application as App).databaseContainer.dataStore.setCurrentStateConfig(libraryQueue.getCurrentState())
            } finally {
                scope.cancel()
                pauseAllPlayersAndStopSelf()
            }
        }
    }

    override fun onDestroy() {
        session.release()
        player.release()
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = session

    private fun createOrUpdateMediaButtonsState(): List<CommandButton> {
        return listOf(
            CommandButton
                .Builder(
                    when (libraryQueue.getPlayerMode()) {
                        PlayerMode.DEFAULT -> CommandButton.ICON_REPEAT_ALL
                        PlayerMode.SHUFFLE -> CommandButton.ICON_SHUFFLE_STAR
                        PlayerMode.REPEAT -> CommandButton.ICON_REPEAT_ONE
                    }
                )
                .setDisplayName("define")
                .setPlayerCommand(
                    Player.COMMAND_SET_SHUFFLE_MODE
                )
                .build(),
        )
    }

}
package com.pointchange.sampleaudio.media

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.pointchange.sampleaudio.service.PlaybackService
import kotlinx.coroutines.guava.await

class PlayerConnection(context: Context) {
    private val controllerFuture =
        MediaController.Builder(
            context.applicationContext,
            SessionToken(
                context.applicationContext,
                ComponentName(context.applicationContext, PlaybackService::class.java)
            )
        ).buildAsync()

    suspend fun awaitController(): MediaController {
        val controller = controllerFuture.await()
        return controller
    }
}

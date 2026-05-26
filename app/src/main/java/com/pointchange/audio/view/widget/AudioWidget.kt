package com.pointchange.audio.view.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import com.pointchange.audio.MainActivity
import com.pointchange.audio.R
import com.pointchange.audio.util.getBitmap
import com.pointchange.audio.view.widget.view.BaseAudioWidget

class AudioWidget : GlanceAppWidget() {
    override val stateDefinition
        get() = AudioInfoStateDefinition

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        provideContent {
            // create your AppWidget here
//            val prefs = currentState<Preferences>()
//            val title = prefs[AudioWidgetKeys.title] ?: context.getString(R.string.unknow_title)
//
//            val artist =
//                prefs[AudioWidgetKeys.artist] ?: context.getString(R.string.unknow_artist)
//            val cover = prefs[AudioWidgetKeys.cover] ?: ""
//            val isPlaying = prefs[AudioWidgetKeys.isPlaying] ?: false
//
//            val bitmap = getBitmap(cover)
//            Log.d("str123","${title} ${artist} $isPlaying $cover")
            val state = currentState<AudioInfoWidget>()
            val title = state.title.ifEmpty { context.getString(R.string.unknow_title) }

            val artist = state.artist.ifEmpty { context.getString(R.string.unknow_artist) }
            val cover = state.cover.ifEmpty { "" }
            val isPlaying = state.isPlaying

            val bitmap = getBitmap(cover)

            val clickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            GlanceTheme {
                BaseAudioWidget(
                    title = title,
                    artist = artist,
                    bitmap = bitmap,
                    isPlaying = isPlaying,
                    clickIntent = clickIntent
                )
            }
        }
    }
}



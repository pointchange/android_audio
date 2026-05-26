package com.pointchange.audio.view.widget.view

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.pointchange.audio.MainActivity
import com.pointchange.audio.R
import com.pointchange.audio.view.widget.AudioActionsString
import com.pointchange.audio.view.widget.AudioWidgetEvent
import com.pointchange.audio.view.widget.EventAction

@Composable
fun BaseAudioWidget(
    title: String,
    artist: String,
    bitmap: Bitmap?,
    isPlaying: Boolean,
    clickIntent: Intent
) {

    Row(
        modifier = GlanceModifier.fillMaxSize().padding(8.dp)
            .background(GlanceTheme.colors.primaryContainer),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (bitmap == null){
            Image(
                provider = ImageProvider(resId = R.drawable.round_music_note_24),
                contentDescription = null,
                modifier = GlanceModifier.padding(start = 8.dp).size(60.dp).cornerRadius(8.dp).clickable(onClick = actionStartActivity(clickIntent)),
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onBackground)
            )
        }else{
            Image(
                provider = ImageProvider(bitmap = bitmap),
                contentDescription = null,
                modifier = GlanceModifier.padding(start = 8.dp).size(60.dp).cornerRadius(8.dp).clickable(onClick = actionStartActivity(clickIntent)),
            )
        }

        Spacer(modifier = GlanceModifier.size(16.dp))
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = title,
                maxLines = 2,
                style = TextStyle(
                    color = GlanceTheme.colors.onBackground,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = artist,
                maxLines = 1,
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp)
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = GlanceModifier.size(48.dp).cornerRadius(24.dp)
                .clickable(
                    actionRunCallback<AudioWidgetEvent>(
                        actionParametersOf(AudioActionsString.ACTION_TYPE_KEY to EventAction.TOGGLE.name)
                    )
                )
        ) {
            Image(
                provider = ImageProvider(if (isPlaying) R.drawable.round_pause_24 else R.drawable.round_play_arrow_24),
                contentDescription = null,
                modifier = GlanceModifier.size(30.dp),
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onBackground)
            )
        }
    }
}

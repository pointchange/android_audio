package com.pointchange.sampleaudio.data

import android.net.Uri
import androidx.compose.ui.graphics.Color
import com.pointchange.sampleaudio.data.Bands
import kotlinx.serialization.Serializable


data class SettingsState(
    val currentPlayCoverColor: List<Color> = emptyList(),
    val themeMode: ThemeMode = ThemeMode.FOLLOW_SYSTEM,
    val paperUri: Uri? = null,
    val showMask: Boolean=false
)


data class TimerState(
    val running: Boolean = false,
    val second: Int = 0
)

@Serializable
data class Bands(
    val effect: String = "",
    val frequency: String = "",
    val id: Int = -1,
    val value: Float = 0f
)

@Serializable
data class EqualizerState(
    val enable: Boolean = false,
    val selected: Int = 0,
    val presetName: String? = null,
    val bands: List<Bands> = listOf(
        Bands("低音", "60", 1, 0f),
        Bands("厚度", "250", 3, 0f),
        Bands("人声", "1k", 5, 0f),
        Bands("清晰", "4k", 7, 0f),
        Bands("通透", "16k", 9, 0f),
    ),
    val userVolume: Int = 100
)
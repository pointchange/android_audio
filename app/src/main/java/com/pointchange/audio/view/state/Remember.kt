package com.pointchange.audio.view.state

import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.pointchange.audio.model.ThemeConfig
import com.pointchange.audio.model.ThemeMode

@Composable
fun rememberIsDark(themeConfig: ThemeConfig): Boolean {
    val bool = when (themeConfig.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.FOLLOW_SYSTEM -> isSystemInDarkTheme()
    }

    return remember(bool) { bool }
}
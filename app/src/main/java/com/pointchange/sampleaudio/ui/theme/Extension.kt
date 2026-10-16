package com.pointchange.sampleaudio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Spacing(
    val xs: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
)

val LocalSpacing = staticCompositionLocalOf<Spacing> { error("No spacing") }

class AppThemeExtension(
    val spacing: Spacing = Spacing()
)

object AppTheme {
    val spacing: Spacing
        @Composable
        get() = LocalSpacing.current
}

val MaterialTheme.ext: AppThemeExtension
    @Composable
    get() = AppThemeExtension(spacing = AppTheme.spacing)
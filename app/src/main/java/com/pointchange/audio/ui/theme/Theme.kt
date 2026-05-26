package com.pointchange.audio.ui.theme

import android.os.Build
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointchange.audio.model.SettingViewModel
import com.pointchange.audio.model.ThemeMode
import com.pointchange.audio.model.ThemeName
import com.pointchange.audio.view.state.rememberIsDark

//private val DarkColorScheme = darkColorScheme(
//    primary = Purple80,
//    secondary = PurpleGrey80,
//    tertiary = Pink80
//)
//
//private val LightColorScheme = lightColorScheme(
//    primary = Purple40,
//    secondary = PurpleGrey40,
//    tertiary = Pink40
//
//    /* Other default colors to override
//    background = Color(0xFFFFFBFE),
//    surface = Color(0xFFFFFBFE),
//    onPrimary = Color.White,
//    onSecondary = Color.White,
//    onTertiary = Color.White,
//    onBackground = Color(0xFF1C1B1F),
//    onSurface = Color(0xFF1C1B1F),
//    */
//)

@Composable
fun AudioTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    settingViewModel: SettingViewModel = viewModel(),
    content: @Composable () -> Unit
) {
    val themeConfig by settingViewModel.themeConfig.collectAsStateWithLifecycle()
    val darkTheme: Boolean = rememberIsDark(themeConfig)
    val light = lightColorScheme(
        primary = themeConfig.themeColor.getColor().primaryLight,
        secondary = themeConfig.themeColor.getColor().secondaryLight,
        tertiary = themeConfig.themeColor.getColor().tertiaryLight
    )

    val dark = darkColorScheme(
        primary = themeConfig.themeColor.getColor().primaryDark,
        secondary = themeConfig.themeColor.getColor().secondaryDark,
        tertiary = themeConfig.themeColor.getColor().tertiaryDark
    )

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && themeConfig.themeColor == ThemeName.DYNAMIC_COLOR -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> dark
        else -> light
    }

//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
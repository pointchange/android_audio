package com.pointchange.audio.model

import kotlinx.serialization.Serializable

@Serializable
data class ThemeConfig(
    val themeColor:ThemeName=ThemeName.DYNAMIC_COLOR,
    val themeMode:ThemeMode=ThemeMode.FOLLOW_SYSTEM
)
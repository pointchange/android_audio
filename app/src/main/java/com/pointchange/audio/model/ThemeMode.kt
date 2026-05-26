package com.pointchange.audio.model

import androidx.annotation.StringRes
import com.pointchange.audio.R

enum class ThemeMode(@param:StringRes val text: Int) {
    FOLLOW_SYSTEM(text = R.string.theme_follow_system),
    DARK(text = R.string.theme_dark),
    LIGHT(text = R.string.theme_light),
}
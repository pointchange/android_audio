package com.pointchange.audio.service

import com.pointchange.audio.model.ThemeConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeManager {
    private val _themeConfig = MutableStateFlow(ThemeConfig())
    val themeConfig = _themeConfig.asStateFlow()
    fun setThemeConfig(themeConfig: ThemeConfig) {
        _themeConfig.value=themeConfig
    }
    fun getThemeConfig()=_themeConfig.value
}
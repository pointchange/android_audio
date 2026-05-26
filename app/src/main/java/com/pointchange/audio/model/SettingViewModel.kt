package com.pointchange.audio.model

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointchange.audio.service.ThemeManager
import com.pointchange.audio.service.VlcManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TimerConfig(
    var isRunning: Boolean = false,
    var targetTime: Long = 0L,
    var timerState: TimerState = TimerState.PENDING,
    var recordFirstTargetTime: Long = 0L,
)

data class LoadingState(
    val parseLoading: Boolean = false
)

class SettingViewModel : ViewModel() {

    val themeConfig = ThemeManager.themeConfig

    private val _timerConfig = MutableStateFlow(TimerConfig())
    val timerConfig = _timerConfig.asStateFlow()

    private val _loadingState = MutableStateFlow(LoadingState())
    val loadingState = _loadingState.asStateFlow()


    fun setThemeMode(mode: ThemeMode) {
        ThemeManager.setThemeConfig(themeConfig.value.copy(themeMode = mode))
    }

    fun setThemeColor(themeName: ThemeName) {
        ThemeManager.setThemeConfig(themeConfig.value.copy(themeColor = themeName))
    }

    fun clearTimer() {
        _timerConfig.value = TimerConfig()
    }

    fun setTimerConfigRunning(isRunning: Boolean) {
        _timerConfig.update { it.copy(isRunning = isRunning) }
    }

    fun setTimerConfigTargetTime(targetTime: Long) {
//        if(_timerConfig.value.recordStartTime==0L){
//            _timerConfig.update { it.copy(recordStartTime = targetTime) }
//        }
        _timerConfig.update { it.copy(targetTime = targetTime) }
    }

    fun setTimerConfigTimerState(timerState: TimerState) {
        _timerConfig.update { it.copy(timerState = timerState) }
    }

    fun setTimerConfigRecordFirstTargetTime(recordFirstTargetTime: Long) {
        _timerConfig.update { it.copy(recordFirstTargetTime = recordFirstTargetTime) }
    }

    fun parseUir() {
        viewModelScope.launch(Dispatchers.IO) {
            _loadingState.update { it.copy(parseLoading = true) }
            VlcManager.repository.getMemoryCache().forEach {
                VlcManager.repository.requestMetadata(it)
            }
            _loadingState.update { it.copy(parseLoading = false) }
        }
    }
}
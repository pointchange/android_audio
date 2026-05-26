package com.pointchange.audio

import android.app.Application
import android.os.StrictMode
import android.util.Log
import com.pointchange.audio.model_data.DataStoreCacheManager
import com.pointchange.audio.service.ThemeManager
import com.pointchange.audio.service.VlcManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.videolan.BuildConfig

class App : Application() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        VlcManager.init(this.applicationContext)
        scope.launch {
            val list = DataStoreCacheManager.getFirstPage(applicationContext)
            if (list.isNotEmpty()) {
                VlcManager.tempList = list
            }

            val playInfo=DataStoreCacheManager.getPlayInfo(applicationContext).first()
            VlcManager.repository.readPlayInfo(playInfo)
            val config = DataStoreCacheManager.getThemeConfig(applicationContext).first()

            ThemeManager.setThemeConfig(config)

            this.cancel()
        }

    }


}
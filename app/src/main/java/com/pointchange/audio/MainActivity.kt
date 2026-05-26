package com.pointchange.audio

import android.app.ComponentCaller
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointchange.audio.model.AudioListViewModel
import com.pointchange.audio.model_data.DataStoreCacheManager
import com.pointchange.audio.service.AudioService
import com.pointchange.audio.service.VlcManager
import com.pointchange.audio.ui.theme.AudioTheme
import com.pointchange.audio.view.MainNav
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    //    private var playbackService: AudioService? = null
//    private var isBound = false
//    private val connection = object : ServiceConnection {
//        override fun onServiceConnected(
//            p0: ComponentName?,
//            p1: IBinder?
//        ) {
//            val binder = p1 as AudioService.LocalBinder
//            playbackService = binder.onGetSession()
//            isBound = true
//        }
//
//        override fun onServiceDisconnected(p0: ComponentName?) {
//            isBound = false
//            playbackService = null
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        VlcManager.init(this.applicationContext)
//        startActivity(intent)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(intent)
//        }
        startService(Intent(this, AudioService::class.java))

//        bindService(intent, connection, Context.BIND_AUTO_CREATE)

        enableEdgeToEdge()
        setContent {
            AudioTheme {
                MainNav()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            stopService(Intent(this, AudioService::class.java))
        }

//        VlcManager.destroy()
//        if(isBound){
//            VlcManager.mediaPlayer?.detachViews()
//            unbindService(connection)
//            isBound=false
//        }
    }
}

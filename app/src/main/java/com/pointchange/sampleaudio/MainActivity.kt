package com.pointchange.sampleaudio

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointchange.sampleaudio.data.ScanState
import com.pointchange.sampleaudio.datastore.dataStore
import com.pointchange.sampleaudio.ui.nav.Nav3
import com.pointchange.sampleaudio.ui.page.Home
import com.pointchange.sampleaudio.ui.theme.SampleAudioTheme
import com.pointchange.sampleaudio.viewmodel.PlayerStateViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: PlayerStateViewModel =
                viewModel(
                    factory = PlayerStateViewModel.factory
                )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            SampleAudioTheme(viewModel = viewModel) {
                requestAllFileAccessPermission(this)
                Nav3()
            }
            if (uiState.error != null) {
                val player by viewModel.controllerFuture.collectAsStateWithLifecycle()
                val currentState by viewModel.currentState.collectAsStateWithLifecycle()
                fun handle() {
                    viewModel.libraryQueueAsFn.next(player)
                    viewModel.setErrorUiState(null)
                }
                AlertDialog(
                    onDismissRequest = {
                        handle()
                    },
                    title = {
                        Text(text = uiState.error ?: "Error!")
                    },
                    text = {
                        Text(text = currentState.path + ": 文件出错")
                    },
                    confirmButton = {
                        handle()
                    },
                    dismissButton = {
                        handle()
                    }
                )
            }
        }
    }
}

fun requestAllFileAccessPermission(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        return
    }
    if (Environment.isExternalStorageManager()) {
        return
    }
    try {
        val intent = Intent(
            Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION,
            "package:${context.packageName}".toUri()
        )
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        context.startActivity(intent)
    }
}
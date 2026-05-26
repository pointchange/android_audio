package com.pointchange.audio.view.component

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.net.toUri

fun allFilesPermission(context: Context, content: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (Environment.isExternalStorageManager()) {
            content()
        } else {
            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                data =
                    ("package:" + context.applicationContext.packageName).toUri()
                context.applicationContext.startActivity(this)
            }
        }
    }
}
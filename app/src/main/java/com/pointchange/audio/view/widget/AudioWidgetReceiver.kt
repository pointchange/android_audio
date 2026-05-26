package com.pointchange.audio.view.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

open class AudioWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = AudioWidget()
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_USER_PRESENT -> {

                val workRequestBuilder = OneTimeWorkRequestBuilder<WorkerWidget>().build()
                WorkManager.getInstance(context).enqueue(workRequestBuilder)
            }
        }
    }
}

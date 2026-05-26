package com.pointchange.audio.view.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class WorkerWidget(private val context: Context,workerParams: WorkerParameters): CoroutineWorker(context,workerParams) {
    override suspend fun doWork(): Result {
       return try {
            val manager = GlanceAppWidgetManager(context)
            val ids = manager.getGlanceIds(AudioWidget::class.java)
            for (id in ids) {
                AudioWidget().update(context, id)
            }
           Result.success()
        }catch (e: Exception){
            Result.retry()
        }
    }
}
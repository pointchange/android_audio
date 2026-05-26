package com.pointchange.audio.view.widget

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.state.updateAppWidgetState
import com.pointchange.audio.MainActivity
import com.pointchange.audio.service.AudioService

object AudioActionsString {
    val ACTION_TYPE_KEY = ActionParameters.Key<String>("audio_type_key")
}

class AudioWidgetEvent : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val actionType = parameters[AudioActionsString.ACTION_TYPE_KEY] ?: return
        val actionEnum = try {
            EventAction.valueOf(actionType)
        } catch (e: IllegalStateException) {
            return
        }
        when (actionEnum) {
            EventAction.TOGGLE -> {
                updateAppWidgetState(context, AudioInfoStateDefinition, glanceId) {
                    it.copy(isPlaying = !it.isPlaying)
                }
            }

            EventAction.Next -> {

            }

            EventAction.Pre -> {

            }

        }
        val intent = Intent(context, AudioService::class.java).apply {
            action = actionType
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if(actionType== EventAction.TOGGLE.name){

                    context.startForegroundService(intent)
                }else{
                    context.startActivity(intent)
                }
            }
        }catch (e: Exception){

        }
    }

}
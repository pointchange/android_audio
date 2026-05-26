package com.pointchange.audio.view.widget

import android.content.Context
import android.util.Log
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.glance.state.GlanceStateDefinition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object AudioInfoStateDefinition : GlanceStateDefinition<AudioInfoWidget> {
    private const val FILE_NAME = "audio_info_widget"

    private object AudioInfoSerializer : Serializer<AudioInfoWidget> {
        override val defaultValue: AudioInfoWidget
            get() = AudioInfoWidget()

        override suspend fun readFrom(input: InputStream): AudioInfoWidget {
            return try {
                Json.decodeFromString(
                    AudioInfoWidget.serializer(),
                    input.readBytes().decodeToString()
                )
            } catch (e: Exception) {
                throw CorruptionException("无法读取小组件的数据", e)
            }
        }

        override suspend fun writeTo(
            t: AudioInfoWidget,
            output: OutputStream
        ) {
            withContext(Dispatchers.IO) {
                output.write(Json.encodeToString(AudioInfoWidget.serializer(), t).toByteArray())
            }
        }

    }

    private val Context.audioInfoStore by dataStore(
        fileName = FILE_NAME,
        serializer = AudioInfoSerializer
    )

    override suspend fun getDataStore(
        context: Context,
        fileKey: String
    ): DataStore<AudioInfoWidget> {
        return context.audioInfoStore
    }

    override fun getLocation(
        context: Context,
        fileKey: String
    ): File {
        return File(context.filesDir, "datastore/$FILE_NAME")
    }
}
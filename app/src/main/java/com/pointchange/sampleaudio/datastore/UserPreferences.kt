package com.pointchange.sampleaudio.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pointchange.sampleaudio.data.CurrentState
import com.pointchange.sampleaudio.data.EqualizerState
import com.pointchange.sampleaudio.data.ScanConfig
import com.pointchange.sampleaudio.data.SongInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

val Context.dataStore by preferencesDataStore(name = "simple_audio_preferences")

class UserPreferences(
    private val dataStore: DataStore<Preferences>
) {

    val scanConfig: Flow<ScanConfig?> =
        dataStore.data.map {
            try {
                val item = it[DataStoreKeys.SCAN_CONFIG] ?: return@map null
                Json.decodeFromString<ScanConfig>(item)
            } catch (e: Exception) {
                null
            }
        }
    val equalizerState: Flow<EqualizerState?> =
        dataStore.data.map {
            try {
                val item = it[DataStoreKeys.EQUALIZER_CONFIG] ?: return@map null
                Json.decodeFromString<EqualizerState>(item)
            } catch (e: Exception) {
                null
            }
        }
    val currentState: Flow<CurrentState?> =
        dataStore.data.map {
            try {
                val item = it[DataStoreKeys.CURRENT_STATE_CONFIG] ?: return@map null
                Json.decodeFromString<CurrentState>(item)
            } catch (e: Exception) {
                null
            }
        }
    val songInfoMains: Flow<List<SongInfo>?> =
        dataStore.data.map {
            try {
                val item = it[DataStoreKeys.SONG_INFO_MAIN] ?: return@map null
                Json.decodeFromString<List<SongInfo>>(item)
            } catch (e: Exception) {
                null
            }
        }

    suspend fun setScanConfig(scanConfig: ScanConfig) {
        try {
            val str = Json.encodeToString(scanConfig)
            dataStore.edit { it[DataStoreKeys.SCAN_CONFIG] = str }
        } catch (e: Exception) {
        }
    }

    suspend fun setEqualizerConfig(equalizerState: EqualizerState) {
        try {
            val str = Json.encodeToString(equalizerState)
            dataStore.edit { it[DataStoreKeys.EQUALIZER_CONFIG] = str }
        } catch (e: Exception) {
        }
    }

    suspend fun setCurrentStateConfig(currentState: CurrentState) {
        try {
            val str = Json.encodeToString(currentState)
            dataStore.edit { it[DataStoreKeys.CURRENT_STATE_CONFIG] = str }
        } catch (e: Exception) {
        }
    }

    suspend fun setFirstPageData(songInfoMains: List<SongInfo>) {
        try {
            val str = Json.encodeToString(songInfoMains)
            dataStore.edit { it[DataStoreKeys.SONG_INFO_MAIN] = str }
        } catch (e: Exception) {
        }
    }

    companion object {
        private val QUEUE_NAMES = stringPreferencesKey("queue_names")

    }
}


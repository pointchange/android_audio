package com.pointchange.audio.model_data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pointchange.audio.model.ThemeConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "open_app_cache")

object DataStoreCacheManager {
    private val FIRST_PAGE_KEY = stringPreferencesKey("key_first_page_json")
    private val PLAY_FINO_KEY = stringPreferencesKey("key_play_info_json")
    private val THEME_CONFIG_KEY = stringPreferencesKey("key_theme_config_json")
    private val jsonConfig = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }


    suspend fun saveFirstPage(context: Context, list: List<AudioMetadata>) {
        if (list.isEmpty()) return
        try {
            val jsonString = jsonConfig.encodeToString(list)
            context.dataStore.edit { prefs -> prefs[FIRST_PAGE_KEY] = jsonString }
        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    suspend fun getFirstPage(context: Context): List<AudioMetadata> {
        try {
            val prefs = context.dataStore.data.first()
            val jsonString = prefs[FIRST_PAGE_KEY] ?: return emptyList()
            return jsonConfig.decodeFromString<List<AudioMetadata>>(jsonString)
        } catch (e: Exception) {
            return emptyList()
        }
    }

    suspend fun savePlayInfo(context: Context, playingInfo: PlayingInfo) {
        try {
            val jsonString = jsonConfig.encodeToString(playingInfo)
            context.dataStore.edit { prefs -> prefs[PLAY_FINO_KEY] = jsonString }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun getPlayInfo(context: Context) = context.dataStore.data
        .map {
            try {
                val item = it[PLAY_FINO_KEY] ?: return@map PlayingInfo()

                val i = jsonConfig.decodeFromString<PlayingInfo>(item)

                return@map i
            } catch (e: Exception) {

                PlayingInfo()
            }
        }

    suspend fun saveThemeConfig(context: Context, themeConfig: ThemeConfig) {
        try {

            val jsonString = jsonConfig.encodeToString(themeConfig)

            context.dataStore.edit { prefs -> prefs[THEME_CONFIG_KEY] = jsonString }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

//    suspend fun getAllData(context: Context) = context.dataStore.data.first()
//
//    inline fun <reified T> getDataInDetail(prefs: Preferences, key: Preferences.Key<String>): T? {
//        val jsonString = prefs[key] ?: return null
//        return Json.decodeFromString<T>(jsonString)
//    }

    fun getThemeConfig(context: Context) = context.dataStore.data
        .map {
            try {
                val item = it[THEME_CONFIG_KEY] ?: return@map ThemeConfig()

                val i = jsonConfig.decodeFromString<ThemeConfig>(item)

                return@map i
            } catch (e: Exception) {

                ThemeConfig()
            }
        }

}
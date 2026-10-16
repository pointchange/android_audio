package com.pointchange.sampleaudio.datastore

import androidx.datastore.preferences.core.stringPreferencesKey

object DataStoreKeys {
    val SCAN_CONFIG = stringPreferencesKey("scan_config")
    val EQUALIZER_CONFIG= stringPreferencesKey("equalizer_config")
    val CURRENT_STATE_CONFIG= stringPreferencesKey("current_state_config")
    val SONG_INFO_MAIN= stringPreferencesKey("song_info_main")
}
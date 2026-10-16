package com.pointchange.sampleaudio.datastore

import android.content.Context

object DataStoreProvider {

    @Volatile
    private var INSTANCE: UserPreferences? = null
    fun get(context: Context): UserPreferences {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: UserPreferences(context.applicationContext.dataStore).also { INSTANCE = it }
        }
    }
}
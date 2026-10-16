package com.pointchange.sampleaudio.remember

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.pointchange.sampleaudio.datastore.DataStoreProvider
import com.pointchange.sampleaudio.datastore.UserPreferences

@Composable
fun rememberUserPreferences(): UserPreferences {
    val context = LocalContext.current
    return remember { DataStoreProvider.get(context) }
}
package com.pointchange.sampleaudio.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.pointchange.sampleaudio.data.Route
import kotlinx.serialization.Serializable


class Nav3ViewModel : ViewModel() {
    val backStack = mutableStateListOf<Route>(Route.Home)

    fun navigate(route: Route) {
        backStack.add(route)
    }

    fun pop() {
        backStack.removeLastOrNull()
    }

}
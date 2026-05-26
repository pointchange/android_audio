package com.pointchange.audio.view

import androidx.navigation3.runtime.NavKey
import com.pointchange.audio.view.NavID

interface NavActions{
    fun add(id: NavID)
    fun removeLast()

    fun isLastOrNull(): NavKey?
}
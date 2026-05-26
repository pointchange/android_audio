package com.pointchange.audio.view.page

import androidx.annotation.StringRes
import com.pointchange.audio.R

enum class ScanDegree(@param:StringRes val text: Int) {
    GENERAL(R.string.general_scanning),
    MEDIUM(R.string.intermediate_scanning),
}
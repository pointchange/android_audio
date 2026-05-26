package com.pointchange.audio.model

import androidx.annotation.StringRes
import com.pointchange.audio.R

enum class ScanState(@param:StringRes val text:Int) {
    PENDING(R.string.start_scan),
    LOADING(R.string.loading_scan),
    ENDING(R.string.scan_result)
}
package com.pointchange.audio.util

import com.pointchange.audio.service.VlcManager
import java.io.File

 suspend fun getDuration(path: String) = VlcManager.getDuration(path)

 fun getFileSize(path: String) = File(path).length()
package com.pointchange.audio.model

data class FileNode(
    val name: String="",
    val fullPath: String="",
    val isDirectory: Boolean=false,
    val childCount:Int=0
)

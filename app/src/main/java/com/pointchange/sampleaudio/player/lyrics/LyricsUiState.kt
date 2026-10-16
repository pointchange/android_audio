package com.pointchange.sampleaudio.player.lyrics

sealed interface LyricsUiState {
    data object Loading : LyricsUiState
    data object Empty : LyricsUiState
    data class Success(val document: LyricsDocument) : LyricsUiState
    data class Error(val message: String) : LyricsUiState
}
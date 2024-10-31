package com.example.tunesync.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// View model provider interface for playlists function

// Adapted from - source: https://dev.to/theplebdev/understanding-the-viewmodelproviderfactory-in-android-with-kotlin-11dp
// Author - Tristan Elliott

class PlaylistViewModelFactory(private val repository: PlaylistRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlaylistViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlaylistViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

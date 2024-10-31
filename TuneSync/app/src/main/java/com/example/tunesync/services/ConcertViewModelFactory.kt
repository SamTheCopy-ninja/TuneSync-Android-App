package com.example.tunesync.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tunesync.models.ConcertViewModel

// View model factory for pending concert searches
// Adapted from - source: https://dev.to/theplebdev/understanding-the-viewmodelproviderfactory-in-android-with-kotlin-11dp
// Author - Tristan Elliott

class ConcertViewModelFactory(
    private val pendingSearchDao: PendingSearchDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConcertViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConcertViewModel(pendingSearchDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


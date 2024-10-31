package com.example.tunesync.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.tunesync.services.PendingConcertSearch
import com.example.tunesync.services.PendingSearchDao

// View model for handling pending concert searches
class ConcertViewModel(
    private val pendingSearchDao: PendingSearchDao
) : ViewModel() {

    val pendingSearches = pendingSearchDao.getAllPendingSearches()
        .asLiveData(viewModelScope.coroutineContext)

    suspend fun savePendingSearch(search: PendingConcertSearch) {
        pendingSearchDao.insertPendingSearch(search)
    }

    suspend fun deletePendingSearch(search: PendingConcertSearch) {
        pendingSearchDao.deletePendingSearch(search)
    }

    suspend fun clearAllPendingSearches() {
        pendingSearchDao.clearAllPendingSearches()
    }
}

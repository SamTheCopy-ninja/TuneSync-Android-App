package com.example.tunesync.services

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Database queries for handling pending searches
// Adapted from - source: https://developer.android.com/training/data-storage/room
// Author - Developer.Android

@Dao
interface PendingSearchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingSearch(search: PendingConcertSearch)

    @Query("SELECT * FROM pending_concert_searches ORDER BY timestamp DESC")
    fun getAllPendingSearches(): Flow<List<PendingConcertSearch>>

    @Delete
    suspend fun deletePendingSearch(search: PendingConcertSearch)

    @Query("DELETE FROM pending_concert_searches")
    suspend fun clearAllPendingSearches()
}
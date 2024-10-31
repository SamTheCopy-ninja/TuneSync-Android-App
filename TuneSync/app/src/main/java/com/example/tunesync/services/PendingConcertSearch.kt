package com.example.tunesync.services

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Database entity for pending concert searches
@Entity(tableName = "pending_concert_searches")
data class PendingConcertSearch(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "query")
    val query: String,

    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "isVirtual")
    val isVirtual: Boolean,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
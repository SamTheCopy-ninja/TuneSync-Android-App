package com.example.tunesync.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.tunesync.models.Song

// Playlist entity
@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
) {
    @Ignore
    var songs: List<Song> = emptyList()
}
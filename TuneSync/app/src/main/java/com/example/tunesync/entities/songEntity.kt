package com.example.tunesync.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Entity for individual songs
@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "uri") val uri: String,
    @ColumnInfo(name = "duration") val duration: Long,
    @ColumnInfo(name = "artist") val artist: String,
    @ColumnInfo(name = "album") val album: String,
    @ColumnInfo(name = "albumArtUri") val albumArtUri: String?,
    @ColumnInfo(name = "genre") val genre: String?,
    @ColumnInfo(name = "releaseYear") val releaseYear: String
)
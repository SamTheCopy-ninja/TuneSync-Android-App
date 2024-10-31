package com.example.tunesync.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.tunesync.models.Song

// Entity for handling playlist tracks
@Entity(tableName = "playlist_songs",
    foreignKeys = [
        ForeignKey(
            entity = Playlist::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["playlistId", "songId"], unique = true)
    ]
)
data class PlaylistSong(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long,
    val songId: Long
)
package com.example.tunesync.models

import android.net.Uri

// Data class for individual songs
data class Song(
    val id: Long,
    val name: String,
    val uri: Uri,
    val duration: Long,
    val artist: String,
    val album: String,
    val albumArtUri: Uri?,
    val genre: String?,
    val releaseYear: String
)

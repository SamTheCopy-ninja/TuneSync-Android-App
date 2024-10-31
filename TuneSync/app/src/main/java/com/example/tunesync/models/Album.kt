package com.example.tunesync.models

import android.net.Uri

// Data class for albums
data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val artUri: String?,
    val uri: Uri
)



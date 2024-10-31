package com.example.tunesync.models

// Data class for concert information
data class Concert(
    val name: String,
    val link: String,
    val description: String,
    val startTime: String,
    val thumbnail: String?
)
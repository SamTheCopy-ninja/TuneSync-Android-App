package com.example.tunesync.services

import android.net.Uri
import androidx.room.TypeConverter

// Class used to convert URI, to allow songs to be added to the database or fetched
class UriConverter {
    @TypeConverter
    fun fromUri(uri: Uri?): String? = uri?.toString()

    @TypeConverter
    fun toUri(uriString: String?): Uri? = uriString?.let { Uri.parse(it) }
}
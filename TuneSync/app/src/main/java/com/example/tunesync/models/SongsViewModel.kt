package com.example.tunesync.models

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tunesync.entities.SongEntity
import com.example.tunesync.services.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongsViewModel(application: Application) : AndroidViewModel(application) {

    // View model for songs
    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> = _songs

    // LiveData to track loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var isLoadingInternal = false
    private var songsLoaded = false

    // Access the database
    private val songDao = AppDatabase.getDatabase(application).songDao()

    fun loadSongsIfNeeded() {
        if (!songsLoaded && !isLoadingInternal) {
            loadSongs()
        }
    }

    // Load songs to/from database

    // Adapted from - source: https://developer.android.com/training/data-storage/room
    // Author - Developer.Android
    private fun loadSongs() {
        if (isLoadingInternal) return
        isLoadingInternal = true
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val songsFromDb = withContext(Dispatchers.IO) {
                    songDao.getAllSongsAsList()
                }

                if (songsFromDb.isNotEmpty()) {
                    val songList = songsFromDb.map { entity ->
                        Song(
                            id = entity.id,
                            name = entity.name,
                            uri = Uri.parse(entity.uri),
                            duration = entity.duration,
                            artist = entity.artist,
                            album = entity.album,
                            albumArtUri = entity.albumArtUri?.let { Uri.parse(it) },
                            genre = entity.genre,
                            releaseYear = entity.releaseYear
                        )
                    }
                    _songs.postValue(songList)
                    songsLoaded = true
                } else {
                    val songList = withContext(Dispatchers.IO) {
                        getAllSongs()
                    }

                    _songs.postValue(songList)
                    songsLoaded = true

                    withContext(Dispatchers.IO) {
                        songList.forEach { song ->
                            val songEntity = SongEntity(
                                id = song.id,
                                name = song.name,
                                uri = song.uri.toString(),
                                duration = song.duration,
                                artist = song.artist,
                                album = song.album,
                                albumArtUri = song.albumArtUri?.toString(),
                                genre = song.genre,
                                releaseYear = song.releaseYear
                            )
                            songDao.insertSong(songEntity)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SongsViewModel", "Error in loadSongs", e)
            } finally {
                isLoadingInternal = false
                _isLoading.value = false // Set loading state to false
            }
        }
    }

    // Get songs from the user's local storage using MediaStore.Audio
    // Adapted from - source: https://developer.android.com/training/data-storage/shared/media
    // Author - Developer.Android

    private fun getAllSongs(): List<Song> {
        val songList = mutableListOf<Song>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.YEAR
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        try {
            val cursor = getApplication<Application>().contentResolver.query(
                collection,
                projection,
                selection,
                null,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )

            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val yearColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val title = it.getString(titleColumn) ?: "Unknown Title"
                    val duration = it.getLong(durationColumn)
                    val artist = it.getString(artistColumn) ?: "Unknown Artist"
                    val album = it.getString(albumColumn) ?: "Unknown Album"
                    val albumId = it.getLong(albumIdColumn)
                    val year = it.getString(yearColumn) ?: "Unknown Year"
                    val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)

                    val genre = getSongGenre(id) ?: "Unknown Genre"

                    songList.add(Song(id, title, uri, duration, artist, album, albumArtUri, genre, year))
                }
            }
        } catch (e: Exception) {
            Log.e("SongsViewModel", "Error loading songs", e)
        }

        return songList
    }

    // Helper function to specifically get song genres
    private fun getSongGenre(songId: Long): String? {
        val genreUri = MediaStore.Audio.Genres.getContentUriForAudioId("external", songId.toInt())
        val projection = arrayOf(MediaStore.Audio.Genres.NAME)

        try {
            getApplication<Application>().contentResolver.query(genreUri, projection, null, null, null)?.use { genreCursor ->
                if (genreCursor.moveToFirst()) {
                    return genreCursor.getString(genreCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME))
                }
            }
        } catch (e: Exception) {
            Log.e("SongsViewModel", "Error getting song genre", e)
        }
        return null
    }
}
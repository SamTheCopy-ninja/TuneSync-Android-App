package com.example.tunesync.models

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tunesync.adapters.ArtistData
import com.example.tunesync.services.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// View model for handling and displaying artists
class ArtistsViewModel(application: Application) : AndroidViewModel(application) {
    private val songDao = AppDatabase.getDatabase(application).songDao()

    private val _artistDataList = MutableStateFlow<List<ArtistData>>(emptyList())
    val artistDataList = _artistDataList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Cache the artist details to improve loading times
    companion object {
        private var cachedArtists: List<ArtistData>? = null
        private const val CACHE_EXPIRATION_TIME = 5 * 60 * 1000L
        private var lastCacheTime: Long = 0
    }

    init {
        cachedArtists?.let {
            _artistDataList.value = it
        }
    }

    // Check if the artist data needs to be reloaded again
    fun loadArtistsIfNeeded() {
        viewModelScope.launch {
            if (isCacheValid()) {
                cachedArtists?.let {
                    _artistDataList.value = it
                    return@launch
                }
            }
            loadArtists()
        }
    }

    // Check if the cache is still valid
    private fun isCacheValid(): Boolean {
        return cachedArtists != null &&
                (System.currentTimeMillis() - lastCacheTime) < CACHE_EXPIRATION_TIME
    }

    // Load the artist information from the Room database
    private suspend fun loadArtists() {
        if (_isLoading.value) return

        _isLoading.value = true
        try {
            val artists = withContext(Dispatchers.IO) {
                songDao.getAllArtists()
            }

            val artistData = withContext(Dispatchers.Default) {
                artists.map { artist ->
                    async {
                        withContext(Dispatchers.IO) {
                            val albumCount = songDao.getAlbumsByArtist(artist).size
                            val songCount = songDao.getSongsByArtist(artist).size
                            ArtistData(name = artist, albumCount = albumCount, songCount = songCount)
                        }
                    }
                }.awaitAll()
            }

            cachedArtists = artistData
            lastCacheTime = System.currentTimeMillis()
            _artistDataList.value = artistData
        } catch (e: Exception) {
            Log.e("ArtistsViewModel", "Error loading artists", e)
        } finally {
            _isLoading.value = false
        }
    }

    // Refresh the cache if required
    fun forceRefresh() {
        cachedArtists = null
        viewModelScope.launch {
            loadArtists()
        }
    }

    // Clear the cache
    override fun onCleared() {
        super.onCleared()
        cachedArtists = null
    }
}
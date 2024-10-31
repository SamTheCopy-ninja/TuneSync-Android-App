package com.example.tunesync.services

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.tunesync.entities.Playlist
import com.example.tunesync.entities.SongEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// View model for playlists
class PlaylistViewModel(private val repository: PlaylistRepository) : ViewModel() {

    val playlists: LiveData<List<Playlist>> = repository.getPlaylists()

    fun getAllSongs(): LiveData<List<SongEntity>> = repository.getAllSongs()

    // Create a playlist
    fun addPlaylist(name: String) {
        viewModelScope.launch {
            val playlist = Playlist(name = name)
            repository.insertPlaylist(playlist)
        }
    }

    // Delete the playlist
    fun removePlaylist(id: Long) {
        viewModelScope.launch {
            repository.removePlaylist(id)
        }
    }

    // Add a song to a playlist
    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
        }
    }

    // Get songs for the current playlist
    fun getSongsInPlaylist(playlistId: Long): LiveData<List<SongEntity>> = repository.getSongsInPlaylist(playlistId)

    // Remove song from playlist
    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    // Get charts for current playlist
    fun getGenreDistribution(playlistId: Long): LiveData<Map<String, Int>> {
        return repository.getGenreDistribution(playlistId)
    }

    // Get songs to add to playlist
    fun getSongsPaginated(): Flow<PagingData<SongEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { SongPagingSource(repository.songDao) }
        ).flow
    }
}
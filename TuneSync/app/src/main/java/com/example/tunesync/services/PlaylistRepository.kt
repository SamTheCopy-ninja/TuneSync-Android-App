package com.example.tunesync.services

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.tunesync.entities.Playlist
import com.example.tunesync.entities.PlaylistSong
import com.example.tunesync.entities.SongEntity
import com.example.tunesync.models.Song

// Coroutines for performing asynchronous database operations, specifically for Playlists
// Adapted from - source: https://dnmtechs.com/using-suspend-functions-in-android-daos/
// Author - Draper Oscar

class PlaylistRepository(private val playlistDao: PlaylistDao,
                         val songDao: SongDao) {
    fun getPlaylists(): LiveData<List<Playlist>> = playlistDao.getAllPlaylists()

    fun getAllSongs(): LiveData<List<SongEntity>> = playlistDao.getAllSongs()

    // Add playlist to ROOM Database
    suspend fun insertPlaylist(playlist: Playlist): Long = playlistDao.insert(playlist)

    // Delete playlist from ROOM Database
    suspend fun removePlaylist(id: Long) = playlistDao.delete(id)

    // Add song to a playlist
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        playlistDao.addSongToPlaylist(PlaylistSong(playlistId = playlistId, songId = songId))
    }

    // Get song in current playlist
    fun getSongsInPlaylist(playlistId: Long): LiveData<List<SongEntity>> = playlistDao.getSongsInPlaylist(playlistId)

    // remove song from playlist
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) = playlistDao.removeSongFromPlaylist(playlistId, songId)

    // Map genres to the playlist charts
    fun getGenreDistribution(playlistId: Long): LiveData<Map<String, Int>> {
        return playlistDao.getGenreDistributionForPlaylist(playlistId).map { genreCounts ->
            genreCounts.associate { it.genre to it.count }
        }
    }
}
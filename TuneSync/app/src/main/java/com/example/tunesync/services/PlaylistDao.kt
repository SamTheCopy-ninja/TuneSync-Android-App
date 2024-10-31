package com.example.tunesync.services

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.tunesync.entities.Playlist
import com.example.tunesync.entities.PlaylistSong
import com.example.tunesync.entities.SongEntity

// Database access objects for playlist functions
// Adapted from - source: https://developer.android.com/training/data-storage/room
// Author - Developer.Android

@Dao
interface PlaylistDao {
   @Query("SELECT * FROM playlists")
   fun getAllPlaylists(): LiveData<List<Playlist>>

   @Insert
   suspend fun insert(playlist: Playlist): Long

   @Query("DELETE FROM playlists WHERE id = :playlistId")
   suspend fun delete(playlistId: Long)

   @Insert
   suspend fun addSongToPlaylist(playlistSong: PlaylistSong)

   @Query("SELECT s.* FROM songs s INNER JOIN playlist_songs ps ON s.id = ps.songId WHERE ps.playlistId = :playlistId")
   fun getSongsInPlaylist(playlistId: Long): LiveData<List<SongEntity>>

   @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
   suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

   @Query("SELECT * FROM songs")
   fun getAllSongs(): LiveData<List<SongEntity>>

   @Query("""
        SELECT s.genre, COUNT(*) as count 
        FROM songs s 
        INNER JOIN playlist_songs ps ON s.id = ps.songId 
        WHERE ps.playlistId = :playlistId AND s.genre IS NOT NULL 
        GROUP BY s.genre 
        ORDER BY count DESC
    """)
   fun getGenreDistributionForPlaylist(playlistId: Long): LiveData<List<GenreCount>>
}
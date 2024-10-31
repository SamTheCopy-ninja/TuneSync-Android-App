package com.example.tunesync.services

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tunesync.adapters.TimelineAdapter
import com.example.tunesync.entities.SongEntity
import com.example.tunesync.models.Song

// Database access objects for song related functions
// Adapted from - source: https://developer.android.com/training/data-storage/room
// Author - Developer.Android

@Dao
interface SongDao {

    // ROOM Database queries
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSong(song: SongEntity)

    @Query("SELECT * FROM songs WHERE album = :albumName")
    suspend fun getSongsByAlbum(albumName: String): List<Song>

    @Query("SELECT * FROM songs")
    fun getAllSongs(): LiveData<List<Song>>

    @Query("SELECT DISTINCT artist FROM songs ORDER BY artist ASC")
    suspend fun getAllArtists(): List<String>

    @Query("SELECT * FROM songs WHERE artist = :artistName ORDER BY name ASC")
    suspend fun getSongsByArtist(artistName: String): List<Song>

    @Query("""
        SELECT DISTINCT album as title, releaseYear as year, 
        MAX(albumArtUri) as albumArtUri 
        FROM songs 
        WHERE artist = :artistName 
        GROUP BY album, releaseYear 
        ORDER BY releaseYear, album
    """)
    suspend fun getAlbumsByArtist(artistName: String): List<TimelineAdapter.Album>

    // playlist song selection
    @Query("SELECT * FROM songs LIMIT :limit OFFSET :offset")
    suspend fun getSongsPaginated(limit: Int, offset: Int): List<SongEntity>


    // query to retrieve songs synchronously as a list
    @Query("SELECT * FROM songs")
    suspend fun getAllSongsAsList(): List<SongEntity>

}

// Genre for pie chart use
data class GenreCount(
    @ColumnInfo(name = "genre") val genre: String,
    @ColumnInfo(name = "count") val count: Int
)
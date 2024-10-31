package com.example.tunesync

import android.os.Build
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.tunesync.entities.SongEntity
import com.example.tunesync.services.AppDatabase
import com.example.tunesync.services.SongDao
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1], manifest = Config.NONE)
class TrackDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var trackDao: SongDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        trackDao = database.songDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun getSongsByAlbum() = runBlocking {
        // Insert two songs, one of them in a different album
        val song1 = SongEntity(1, "Song 1", "uri1", 300000, "Artist 1", "Album 1", null, "Genre 1", "2021")
        val song2 = SongEntity(2, "Song 2", "uri2", 180000, "Artist 1", "Album 2", null, "Genre 1", "2021")
        trackDao.insertSong(song1)
        trackDao.insertSong(song2)

        // Retrieve songs by album
        val songsInAlbum1 = trackDao.getSongsByAlbum("Album 1")

        // Assert we retrieved only the song in "Album 1"
        assertEquals(1, songsInAlbum1.size)
        assertEquals(song1.name, songsInAlbum1[0].name)
        println("getSongsByAlbum test passed successfully")
    }

    @Test
    fun getSongsByArtist() = runBlocking {
        // Insert two songs by the same artist
        val song1 = SongEntity(1, "Song 1", "uri1", 300000, "Artist 1", "Album 1", null, "Genre 1", "2021")
        val song2 = SongEntity(2, "Song 2", "uri2", 200000, "Artist 1", "Album 2", null, "Genre 1", "2022")
        trackDao.insertSong(song1)
        trackDao.insertSong(song2)

        // Retrieve songs by artist
        val songsByArtist = trackDao.getSongsByArtist("Artist 1")

        // Assert we retrieved both songs by the artist
        assertEquals(2, songsByArtist.size)
        assertTrue(songsByArtist.any { it.name == "Song 1" })
        assertTrue(songsByArtist.any { it.name == "Song 2" })
        println("getSongsByArtist test passed successfully")
    }
}
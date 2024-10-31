package com.example.tunesync.services

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tunesync.entities.Playlist
import com.example.tunesync.entities.PlaylistSong
import com.example.tunesync.entities.SongEntity

// Abstract class to extend ROOM Database
// Adapted from - source: https://medium.com/android-beginners/android-room-using-kotlin-798ae83b3bf0
// Author - Velmurugan Murugesan

@Database(entities = [Playlist::class, SongEntity::class, PlaylistSong::class, PendingConcertSearch::class], version = 6, exportSchema = false)
@TypeConverters(UriConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun songDao(): SongDao
    abstract fun pendingSearchDao(): PendingSearchDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Migration for playlist songs table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `playlist_songs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `playlistId` INTEGER NOT NULL,
                        `songId` INTEGER NOT NULL,
                        FOREIGN KEY(`playlistId`) REFERENCES `playlists`(`id`) ON DELETE CASCADE,
                        FOREIGN KEY(`songId`) REFERENCES `songs`(`id`) ON DELETE CASCADE,
                        UNIQUE(`playlistId`, `songId`)
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_playlist_songs_playlistId_songId` ON `playlist_songs` (`playlistId`, `songId`)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the genre column to the songs table
                database.execSQL("ALTER TABLE songs ADD COLUMN genre TEXT")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the releaseYear column to the songs table
                database.execSQL("ALTER TABLE songs ADD COLUMN releaseYear TEXT")
            }
        }
    }
}

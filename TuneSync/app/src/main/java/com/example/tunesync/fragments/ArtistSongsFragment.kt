package com.example.tunesync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tunesync.R
import com.example.tunesync.adapters.SongAdapter
import com.example.tunesync.adapters.TimelineAdapter
import com.example.tunesync.models.Song
import com.example.tunesync.services.AppDatabase
import kotlinx.coroutines.launch


class ArtistSongsFragment : Fragment() {
    // Fragment for displaying songs belonging to specific artist

    // UI for artist songs
    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var songs: List<Song>
    private var currentSongIndex = -1

    private lateinit var originalSongs: List<Song>

    // Timeline for album releases
    private lateinit var timelineRecyclerView: RecyclerView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_artist_songs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Pass the artist name which user clicked on
        val artistName = arguments?.getString("artistName") ?: return

        // List the artist
        view.findViewById<TextView>(R.id.artistNameTextView).text = artistName

        // Setup recycler view that contains songs
        recyclerView = view.findViewById(R.id.recyclerViewArtistSongs)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Setup timeline for the artist albums
        timelineRecyclerView = view.findViewById(R.id.timelineRecyclerView)
        timelineRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        // Setup ExoPlayer
        exoPlayer = ExoPlayer.Builder(requireContext()).build()

        // Include the adapter
        songAdapter = SongAdapter(emptyList()) { song ->
            playSong(song)
        }
        recyclerView.adapter = songAdapter

        // Display the album timeline, and display songs
        loadArtistSongs(artistName)
        loadArtistTimeline(artistName)
        setupPlayerControls(view)

        view.findViewById<Button>(R.id.resetFilterButton).setOnClickListener {
            resetSongFilter()
        }
    }

    // Query the database for songs featuring this artist
    private fun loadArtistSongs(artistName: String) {
        val songDao = AppDatabase.getDatabase(requireContext()).songDao()
        lifecycleScope.launch {
            songs = songDao.getSongsByArtist(artistName)
            originalSongs = songs
            songAdapter.updateSongs(songs)
            currentSongIndex = if (songs.isNotEmpty()) 0 else -1
        }
    }

    // Query database to configure the album timeline
    private fun loadArtistTimeline(artistName: String) {
        val songDao = AppDatabase.getDatabase(requireContext()).songDao()
        lifecycleScope.launch {
            val albums = songDao.getAlbumsByArtist(artistName)
            val timelineAdapter = TimelineAdapter(albums) { albumTitle ->
                filterSongsByAlbum(albumTitle)
            }
            timelineRecyclerView.adapter = timelineAdapter
        }
    }

    // Filter songs for specific albums
    private fun filterSongsByAlbum(albumTitle: String) {
        val filteredSongs = songs.filter { it.album == albumTitle }
        songAdapter.updateSongs(filteredSongs)
    }

    // Reset the filter
    private fun resetSongFilter() {
        songAdapter.updateSongs(originalSongs)
    }

    // More custom ExoPlayer controls
    // Adapted from - source: https://developer.android.com/media/media3/exoplayer/hello-world
    // Author - Developer.Android

    private fun setupPlayerControls(view: View) {
        val playPauseButton: ImageButton = view.findViewById(R.id.playPauseButton)
        val nextButton: ImageButton = view.findViewById(R.id.nextButton)
        val previousButton: ImageButton = view.findViewById(R.id.previousButton)

        playPauseButton.setOnClickListener {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
                playPauseButton.setImageResource(android.R.drawable.ic_media_play)
            } else {
                exoPlayer.play()
                playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
            }
        }

        nextButton.setOnClickListener {
            playNextSong()
        }

        previousButton.setOnClickListener {
            playPreviousSong()
        }

        // Update UI when playback state changes
        // Adapted from - source: https://developer.android.com/media/media3/exoplayer/hello-world
        // Author - Developer.Android

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> {
                        playPauseButton.isEnabled = true
                        if (exoPlayer.isPlaying) {
                            playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
                        } else {
                            playPauseButton.setImageResource(android.R.drawable.ic_media_play)
                        }
                    }
                    Player.STATE_BUFFERING -> {
                        playPauseButton.isEnabled = false
                    }
                    Player.STATE_ENDED -> {
                        playNextSong()
                    }
                }
            }
        })
    }

    // Play next song in list
    private fun playNextSong() {
        if (currentSongIndex < songs.size - 1) {
            currentSongIndex++
            playSong(songs[currentSongIndex])
        }
    }

    // Play previous song in list
    private fun playPreviousSong() {
        if (currentSongIndex > 0) {
            currentSongIndex--
            playSong(songs[currentSongIndex])
        }
    }

    // When the user clicks on a track, begin playing the song
    private fun playSong(song: Song) {
        exoPlayer.setMediaItem(MediaItem.fromUri(song.uri))
        exoPlayer.prepare()
        exoPlayer.play()
        updateNowPlaying(song)

        // Update the currently playing song in the adapter
        songAdapter.updateCurrentlyPlayingSong(song.id)
    }

    // Update UI to tell user which song is playing
    private fun updateNowPlaying(song: Song) {
        view?.findViewById<TextView>(R.id.nowPlayingTextView)?.text = getString(R.string.now_playing_format, song.name, song.artist)
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }
}
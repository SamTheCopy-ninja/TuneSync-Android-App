package com.example.tunesync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tunesync.R
import com.example.tunesync.adapters.SongAdapter
import com.example.tunesync.models.Song
import com.example.tunesync.services.AppDatabase
import kotlinx.coroutines.launch

class AlbumSongsFragment : Fragment() {
    // Dedicated fragment for album songs, based on the album card the user clicks on

    // Adapted from:
    // Source - SCALER (YouTube Channel)
    // Video title - Building a Music App using Kotlin - Part #2
    // Video link - https://www.youtube.com/watch?v=odiIBC9jl_4

    // Initialize the UI
    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var songs: List<Song>
    private var currentSongIndex = -1
    private lateinit var playerView: PlayerView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_album_songs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the album name from the arguments
        val albumName = arguments?.getString("albumName") ?: return
        view.findViewById<TextView>(R.id.albumName).text = albumName

        // Initialize PlayerView
        playerView = view.findViewById(R.id.player_view)

        // Setup RecyclerView for songs
        recyclerView = view.findViewById(R.id.recyclerViewSongs)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Setup ExoPlayer
        exoPlayer = ExoPlayer.Builder(requireContext()).build()
        playerView.player = exoPlayer

        // Include the adapter
        songAdapter = SongAdapter(emptyList()) { song ->
            playSong(song)
        }
        recyclerView.adapter = songAdapter

        // Load songs from the album
        loadSongsFromAlbum(albumName)

        // Setup player controls
        setupPlayerControls(view)
    }

    // Get the specific album songs from the ROOM database and add them to the page

    // Adapted from - source: https://developer.android.com/training/data-storage/room
    // Author - Developer.Android
    private fun loadSongsFromAlbum(albumName: String) {
        val songDao = AppDatabase.getDatabase(requireContext()).songDao()
        lifecycleScope.launch {
            songs = songDao.getSongsByAlbum(albumName)
            songAdapter.updateSongs(songs)
            currentSongIndex = if (songs.isNotEmpty()) 0 else -1
        }
    }

    // Ensure the user can access media Exo Player controls

    // Adapted from - source: https://developer.android.com/media/media3/exoplayer/hello-world
    // Author - Developer.Android

    private fun setupPlayerControls(view: View) {
        val playPauseButton: ImageButton = view.findViewById(R.id.playPauseButton)
        val nextButton: ImageButton = view.findViewById(R.id.nextButton)
        val previousButton: ImageButton = view.findViewById(R.id.previousButton)

        playPauseButton.setOnClickListener {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
            } else {
                exoPlayer.play()
            }
            // Update play/pause icon immediately when clicked
            playPauseButton.setImageResource(
                if (exoPlayer.isPlaying) android.R.drawable.ic_media_pause
                else android.R.drawable.ic_media_play
            )
        }

        nextButton.setOnClickListener {
            playNextSong()
        }

        previousButton.setOnClickListener {
            playPreviousSong()
        }

        // Update UI when playback state changes

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> {
                        playPauseButton.isEnabled = true
                        // Check if playing or paused and set the icon
                        playPauseButton.setImageResource(
                            if (exoPlayer.isPlaying) android.R.drawable.ic_media_pause
                            else android.R.drawable.ic_media_play
                        )
                    }
                    Player.STATE_BUFFERING -> {
                        playPauseButton.isEnabled = false
                    }
                    Player.STATE_ENDED -> {
                        playNextSong()
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                // Update play/pause button icon whenever the playing state changes
                playPauseButton.setImageResource(
                    if (isPlaying) android.R.drawable.ic_media_pause
                    else android.R.drawable.ic_media_play
                )
            }
        })

    }

    // Play the next song if current one ends or user clicks next button
    private fun playNextSong() {
        if (currentSongIndex < songs.size - 1) {
            currentSongIndex++
            playSong(songs[currentSongIndex])
        }
    }

    // Play previous song if current one ends or user clicks previous button
    private fun playPreviousSong() {
        if (currentSongIndex > 0) {
            currentSongIndex--
            playSong(songs[currentSongIndex])
        }
    }

    // Play song user clicks on
    private fun playSong(song: Song) {
        exoPlayer.setMediaItem(MediaItem.fromUri(song.uri))
        exoPlayer.prepare()
        exoPlayer.play()
        updateNowPlaying(song)

        // Update the currently playing song in the adapter
        songAdapter.updateCurrentlyPlayingSong(song.id)
    }

    // Update with the correct song name and title
    private fun updateNowPlaying(song: Song) {
        view?.findViewById<TextView>(R.id.songTitleTextView)?.text = song.name
        view?.findViewById<TextView>(R.id.artistNameTextView)?.text = song.artist
    }

    // Close the play, and stop music when user exits this page
    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }
}
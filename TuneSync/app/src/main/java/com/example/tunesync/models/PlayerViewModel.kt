package com.example.tunesync.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

// Dedicated Exo Player instance to be used by the SongsFragment to allow background play
class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val _exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(application).build().apply {
            addListener(playerListener)
        }
    }

    val exoPlayer: ExoPlayer
        get() = _exoPlayer

    private var currentPlaylist: List<Song> = emptyList()

    private val _currentSong = MutableLiveData<Song?>()
    val currentSong: LiveData<Song?> = _currentSong

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateCurrentSong()
        }

        override fun onPlaybackStateChanged(state: Int) {
            _isPlaying.postValue(state == Player.STATE_READY && exoPlayer.playWhenReady)
        }
    }

    // Check if playlist changed
    fun updatePlaylistIfNeeded(newPlaylist: List<Song>) {
        if (currentPlaylist != newPlaylist) {
            currentPlaylist = newPlaylist
            exoPlayer.clearMediaItems()
            exoPlayer.addMediaItems(newPlaylist.map { song -> createMediaItemFromSong(song) })
            exoPlayer.prepare()
            updateCurrentSong()
        }
    }

    // Allow user to play/pause songs
    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    // Keep track of currently playing song
    private fun updateCurrentSong() {
        val currentIndex = exoPlayer.currentMediaItemIndex
        _currentSong.postValue(currentPlaylist.getOrNull(currentIndex))
    }

    private fun createMediaItemFromSong(song: Song): MediaItem {
        return MediaItem.Builder()
            .setUri(song.uri)
            .setMediaId(song.id.toString())
            .build()
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }
}
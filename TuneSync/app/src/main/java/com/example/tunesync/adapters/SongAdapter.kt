package com.example.tunesync.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tunesync.R
import com.example.tunesync.models.Song

// Adapter for individual songs
// This adapter is based on the following tutorial
// Source - SCALER (YouTube Channel)
// Video title - Building a Music App using Kotlin - Part #2
// Video link - https://www.youtube.com/watch?v=odiIBC9jl_4

class SongAdapter(
    var songs: List<Song>,
    private val onSongClick: (Song) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    private var currentlyPlayingSongId: Long? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view, onSongClick)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        val isPlaying = song.id == currentlyPlayingSongId
        holder.bind(song, isPlaying)
    }

    override fun getItemCount(): Int = songs.size

    fun updateSongs(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }

    // Determine which song is playing
    fun updateCurrentlyPlayingSong(songId: Long?) {
        val oldPlayingPosition = songs.indexOfFirst { it.id == currentlyPlayingSongId }
        currentlyPlayingSongId = songId
        val newPlayingPosition = songs.indexOfFirst { it.id == currentlyPlayingSongId }

        if (oldPlayingPosition != -1) notifyItemChanged(oldPlayingPosition)
        if (newPlayingPosition != -1) notifyItemChanged(newPlayingPosition)
    }

    // Setup the recycler for each card
    class SongViewHolder(itemView: View, private val onSongClick: (Song) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val songNameTextView: TextView = itemView.findViewById(R.id.songNameTextView)
        private val artistTextView: TextView = itemView.findViewById(R.id.artistTextView)
        private val albumTextView: TextView = itemView.findViewById(R.id.albumTextView)
        private val albumCoverImageView: ImageView = itemView.findViewById(R.id.albumCoverImageView)
        private val songDurationTextView: TextView = itemView.findViewById(R.id.songDurationTextView)
        private val nowPlayingIndicator: View = itemView.findViewById(R.id.nowPlayingIndicator)

        fun bind(song: Song, isPlaying: Boolean) {
            songNameTextView.text = song.name
            artistTextView.text = song.artist
            albumTextView.text = song.album

            // Convert the duration from milliseconds to minutes:seconds format
            songDurationTextView.text = formatDuration(song.duration)

            // Load album art using Glide
            Glide.with(itemView.context)
                .load(song.albumArtUri)
                .placeholder(R.drawable.cvr)
                .error(R.drawable.cvr)
                .into(albumCoverImageView)

            nowPlayingIndicator.visibility = if (isPlaying) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                onSongClick(song)
            }
        }

        // Helper method to format the duration
        private fun formatDuration(durationInMillis: Long): String {
            val minutes = (durationInMillis / 1000) / 60
            val seconds = (durationInMillis / 1000) % 60
            return String.format("%d:%02d", minutes, seconds)
        }
    }
}
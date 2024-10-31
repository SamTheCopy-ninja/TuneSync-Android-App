package com.example.tunesync.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tunesync.entities.Playlist
import com.example.tunesync.R

// Adapter for playlist creation
// This adapter is based on the following tutorial
// Source - SCALER (YouTube Channel)
// Video title - Building a Music App using Kotlin - Part #2
// Video link - https://www.youtube.com/watch?v=odiIBC9jl_4

class PlaylistAdapter(
    private val playlists: List<Playlist>,
    private val onPlaylistClick: (Playlist) -> Unit,
    private val onDeleteClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    // Allow user to either click on a playlist to access tracks, or click the delete button
    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.bind(playlist)
        holder.itemView.setOnClickListener { onPlaylistClick(playlist) }
        holder.deleteButton.setOnClickListener { onDeleteClick(playlist) }
    }

    override fun getItemCount(): Int = playlists.size

    // Display the playlist titles
    class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playlistNameTextView: TextView = itemView.findViewById(R.id.playlistNameTextView)
        val deleteButton: MaterialButton = itemView.findViewById(R.id.deleteButton)

        fun bind(playlist: Playlist) {
            playlistNameTextView.text = playlist.name
        }
    }
}

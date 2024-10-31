package com.example.tunesync.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.tunesync.models.Album
import com.example.tunesync.R
import com.example.tunesync.databinding.ItemAlbumBinding

// Adapter for albums
// This adapter is based on the following tutorial
// Source - SCALER (YouTube Channel)
// Video title - Building a Music App using Kotlin - Part #2
// Video link - https://www.youtube.com/watch?v=odiIBC9jl_4

class AlbumAdapter(private val albums: List<Album>, private val onAlbumClick: (Album) -> Unit) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_album, parent, false)
        return AlbumViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = albums[position]
        holder.bind(album)
        holder.itemView.setOnClickListener {
            onAlbumClick(album)
        }
    }


    override fun getItemCount(): Int = albums.size

    // Setup the view holder to populate the recycler view with album details
    class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val albumNameTextView: TextView = itemView.findViewById(R.id.albumNameTextView)
        private val artistNameTextView: TextView = itemView.findViewById(R.id.artistNameTextView)
        private val albumArtImageView: ImageView = itemView.findViewById(R.id. albumArtImageView)

        fun bind(album: Album) {
            albumNameTextView.text = album.name
            artistNameTextView.text = album.artist
            // Load album art into ImageView
            if (album.artUri != null) {
                Glide.with(itemView.context)
                    .load(album.artUri)
                    .into(albumArtImageView)
            } else {
                albumArtImageView.setImageResource(R.drawable.cvr)
            }
        }
    }
}

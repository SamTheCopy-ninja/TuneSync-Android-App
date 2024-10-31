package com.example.tunesync.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tunesync.R


// Adapter for artists
// This adapter is based on the following tutorial
// Source - SCALER (YouTube Channel)
// Video title - Building a Music App using Kotlin - Part #2
// Video link - https://www.youtube.com/watch?v=odiIBC9jl_4

class ArtistAdapter(
    private val onArtistClick: (String) -> Unit,
    var viewType: Int = VIEW_TYPE_LIST // Default to list view
) : ListAdapter<ArtistData, RecyclerView.ViewHolder>(ArtistDiffCallback()) {

    companion object {
        const val VIEW_TYPE_LIST = 0
        const val VIEW_TYPE_GRID = 1
    }

    override fun getItemViewType(position: Int): Int {
        return viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_GRID) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_artist_grid, parent, false) // Layout for grid
            GridArtistViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_artist, parent, false) // Layout for list
            ArtistViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val artistData = getItem(position)
        if (holder is ArtistViewHolder) {
            holder.bind(artistData)
        } else if (holder is GridArtistViewHolder) {
            holder.bind(artistData)
        }
    }

    // View holder for artist "items"
    inner class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val artistNameTextView: TextView = itemView.findViewById(R.id.textViewArtistName)
        private val artistStatsTextView: TextView = itemView.findViewById(R.id.textViewArtistStats)
        private val artistAvatarImageView: ImageView = itemView.findViewById(R.id.imageViewArtistAvatar)

        fun bind(artistData: ArtistData) {
            artistNameTextView.text = artistData.name

            // Use string resource for artist stats
            val formattedStats = itemView.context.getString(R.string.artist_stats_format, artistData.albumCount, artistData.songCount)
            artistStatsTextView.text = formattedStats

            itemView.setOnClickListener { onArtistClick(artistData.name) }
        }
    }

    // New ViewHolder for Grid layout
    inner class GridArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val artistNameTextView: TextView = itemView.findViewById(R.id.textViewArtistName)
        private val artistStatsTextView: TextView = itemView.findViewById(R.id.textViewArtistStats)
        private val artistAvatarImageView: ImageView = itemView.findViewById(R.id.imageViewArtistAvatar)

        fun bind(artistData: ArtistData) {
            artistNameTextView.text = artistData.name

            // Use string resource for artist stats
            val formattedStats = itemView.context.getString(R.string.artist_stats_format, artistData.albumCount, artistData.songCount)
            artistStatsTextView.text = formattedStats

            itemView.setOnClickListener { onArtistClick(artistData.name) }
        }
    }

}


data class ArtistData(
    val name: String,
    val albumCount: Int,
    val songCount: Int,
)

class ArtistDiffCallback : DiffUtil.ItemCallback<ArtistData>() {
    override fun areItemsTheSame(oldItem: ArtistData, newItem: ArtistData): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: ArtistData, newItem: ArtistData): Boolean {
        return oldItem == newItem
    }
}

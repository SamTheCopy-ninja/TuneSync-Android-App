package com.example.tunesync.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tunesync.R
import com.github.vipulasri.timelineview.TimelineView

// Adapter for creating album timelines

// Library used fo this feature: Timeline-View
// Adapted from - https://github.com/vipulasri/Timeline-View
// Creator/Author - Vipul Asri (https://github.com/vipulasri)

class TimelineAdapter(
    private val albums: List<Album>,
    private val onAlbumClick: (String) -> Unit
) : RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>() {

    data class Album(val title: String, val year: String, val albumArtUri: String)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_timeline, parent, false)
        return TimelineViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val album = albums[position]
        holder.bind(album)
    }

    override fun getItemCount() = albums.size

    override fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position, itemCount)
    }

    // Add each album cover, title and release year to recycler, to form the timeline
    inner class TimelineViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
        private val timeline: TimelineView = itemView.findViewById(R.id.timeline)
        private val titleText: TextView = itemView.findViewById(R.id.titleText)
        private val yearText: TextView = itemView.findViewById(R.id.yearText)
        private val coverImage: ImageView = itemView.findViewById(R.id.coverImage)

        init {
            timeline.initLine(viewType)
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onAlbumClick(albums[position].title)
                }
            }
        }

        fun bind(album: Album) {
            titleText.text = album.title
            yearText.text = album.year
            Glide.with(itemView.context)
                .load(album.albumArtUri)
                .placeholder(R.drawable.cvr)
                .error(R.drawable.cvr)
                .into(coverImage)
        }
    }
}
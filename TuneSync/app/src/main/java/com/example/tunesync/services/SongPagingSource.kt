package com.example.tunesync.services

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tunesync.R
import com.example.tunesync.entities.SongEntity

// Setting up the paginated recycler view to allow users to select songs to add to their playlists

// Adapted from - source: https://medium.com/swlh/paging3-recyclerview-pagination-made-easy-333c7dfa8797
// Author - Vikus Kumar

class SongPagingSource(
    private val songDao: SongDao
) : PagingSource<Int, SongEntity>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SongEntity> {
        val page = params.key ?: 0
        return try {
            val songs = songDao.getSongsPaginated(params.loadSize, page * params.loadSize)
            LoadResult.Page(
                data = songs,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (songs.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, SongEntity>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}

// Custom adapter for the recycler
class PaginatedSongAdapter(
    private val onSongSelected: (SongEntity) -> Unit
) : PagingDataAdapter<SongEntity, PaginatedSongAdapter.SongViewHolder>(SONG_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song_selectable, parent, false)
        return SongViewHolder(view, onSongSelected)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        if (song != null) {
            holder.bind(song)
        }
    }

    // Add songs into the view holder
    class SongViewHolder(
        itemView: View,
        private val onSongSelected: (SongEntity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val songNameTextView: TextView = itemView.findViewById(R.id.songNameTextView)
        private val artistTextView: TextView = itemView.findViewById(R.id.artistTextView)
        private val albumCoverImageView: ImageView = itemView.findViewById(R.id.albumCoverImageView)
        private val durationTextView: TextView = itemView.findViewById(R.id.durationTextView)

        fun bind(song: SongEntity) {
            songNameTextView.text = song.name
            artistTextView.text = song.artist

            // Format the duration from milliseconds to mm:ss
            val durationMinutes = (song.duration / 1000) / 60
            val durationSeconds = (song.duration / 1000) % 60
            val durationText = String.format("%02d:%02d", durationMinutes, durationSeconds)

            durationTextView.text = durationText

            Glide.with(itemView.context)
                .load(song.albumArtUri)
                .placeholder(R.drawable.cvr)
                .into(albumCoverImageView)

            itemView.setOnClickListener { onSongSelected(song) }
        }
    }


    companion object {
        private val SONG_COMPARATOR = object : DiffUtil.ItemCallback<SongEntity>() {
            override fun areItemsTheSame(oldItem: SongEntity, newItem: SongEntity): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: SongEntity, newItem: SongEntity): Boolean =
                oldItem == newItem
        }
    }
}
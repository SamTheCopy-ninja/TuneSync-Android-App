package com.example.tunesync.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tunesync.R
import com.example.tunesync.databinding.ItemConcertBinding
import com.example.tunesync.models.Concert

// Adapter for concerts
// This adapter is based on the following tutorial
// Source - SCALER (YouTube Channel)
// Video title - Building a Music App using Kotlin - Part #2
// Video link - https://www.youtube.com/watch?v=odiIBC9jl_4

class ConcertAdapter : ListAdapter<Concert, ConcertAdapter.ConcertViewHolder>(ConcertDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConcertViewHolder {
        val binding = ItemConcertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ConcertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConcertViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // Add the parsed API response details to the recycler view
    class ConcertViewHolder(private val binding: ItemConcertBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(concert: Concert) {
            binding.tvName.text = concert.name
            binding.tvDescription.text = concert.description
            binding.tvStartTime.text = concert.startTime

            if (!concert.thumbnail.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(concert.thumbnail)
                    .into(binding.ivThumbnail)
            } else {
                // Load a placeholder image if the API did not provide one
                binding.ivThumbnail.setImageResource(R.drawable.nia)
            }

            // Allow users to click on the event link, to view more concert information in their browser
            binding.btnOpenLink.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(concert.link))
                binding.root.context.startActivity(intent)
            }
        }
    }

    class ConcertDiffCallback : DiffUtil.ItemCallback<Concert>() {
        override fun areItemsTheSame(oldItem: Concert, newItem: Concert): Boolean {
            return oldItem.link == newItem.link
        }

        override fun areContentsTheSame(oldItem: Concert, newItem: Concert): Boolean {
            return oldItem == newItem
        }
    }
}
package com.example.tunesync.fragments

import android.content.ContentUris
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tunesync.R
import com.example.tunesync.adapters.AlbumAdapter
import com.example.tunesync.models.Album

class AlbumsFragment : Fragment() {
    // Dedicated fragment for displaying the list of albums

    private lateinit var recyclerView: RecyclerView
    private lateinit var albumAdapter: AlbumAdapter
    private lateinit var toggleButton: ToggleButton
    private lateinit var searchView: SearchView
    private val albums = mutableListOf<Album>()
    private var filteredAlbums = mutableListOf<Album>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_albums, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewAlbums)
        toggleButton = view.findViewById(R.id.viewToggleButton)
        searchView = view.findViewById(R.id.searchView)

        // Initialize the AlbumAdapter
        albumAdapter = AlbumAdapter(filteredAlbums) { album ->
            (parentFragment as? OfflineMusicFragment)?.navigateToAlbumSongs(album.name)
        }

        // Set default layout manager
        setRecyclerViewLayoutManager(false)

        // Handle toggle button click
        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            setRecyclerViewLayoutManager(isChecked)
        }

        // Load albums
        loadAlbums()

        // Setup search functionality
        setupSearchView()
    }

    private fun setRecyclerViewLayoutManager(isGridView: Boolean) {
        if (isGridView) {
            // Switch to Grid View
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
            toggleButton.text = getString(R.string.toggle_albums2)
        } else {
            // Switch to List View
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            toggleButton.text = getString(R.string.toggle_albums1)
        }

        recyclerView.adapter = albumAdapter
    }


    private fun loadAlbums() {
        albums.clear()
        albums.addAll(getAllAlbums())
        filteredAlbums.clear()
        filteredAlbums.addAll(albums)
        albumAdapter.notifyDataSetChanged()
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterAlbums(newText)
                return true
            }
        })
    }

    // Allow users to use the search view to filter the list of albums
    private fun filterAlbums(query: String?) {
        filteredAlbums.clear()
        if (query.isNullOrEmpty()) {
            filteredAlbums.addAll(albums) // If no query, show all albums
        } else {
            val lowerCaseQuery = query.lowercase()
            filteredAlbums.addAll(albums.filter {
                it.name.lowercase().contains(lowerCaseQuery) ||
                        it.artist.lowercase().contains(lowerCaseQuery)
            })
        }
        albumAdapter.notifyDataSetChanged()
    }

    // Get list of albums from the user's local storage using MediaStore.Audio
    // Adapted from - source: https://developer.android.com/training/data-storage/shared/media
    // Author - Developer.Android

    private fun getAllAlbums(): List<Album> {
        val albumList = mutableListOf<Album>()
        val projection = arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST
        )
        val cursor = requireContext().contentResolver.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
            val albumNameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
            val artistNameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val albumName = it.getString(albumNameColumn)
                val artistName = it.getString(artistNameColumn)

                // URI for album art
                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    id
                )

                val albumUri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, id)
                albumList.add(Album(id, albumName, artistName, albumArtUri.toString(), albumUri))
            }
        }
        return albumList
    }

}

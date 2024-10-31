package com.example.tunesync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tunesync.R
import com.example.tunesync.adapters.PlaylistAdapter
import com.example.tunesync.entities.Playlist
import com.example.tunesync.services.AppDatabase
import com.example.tunesync.services.PlaylistRepository
import com.example.tunesync.services.PlaylistViewModel
import com.example.tunesync.services.PlaylistViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


class PlaylistsFragment : Fragment() {
    // Fragment for creating and viewing playlists

    private lateinit var viewModel: PlaylistViewModel
    private lateinit var adapter: PlaylistAdapter
    private val playlists = mutableListOf<Playlist>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_playlists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel for playlists
        val repository = PlaylistRepository(
            AppDatabase.getDatabase(requireContext()).playlistDao(),
            AppDatabase.getDatabase(requireContext()).songDao()
        )
        val factory = PlaylistViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[PlaylistViewModel::class.java]

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewPlaylists)
        adapter = PlaylistAdapter(
            playlists,
            { playlist ->
                // Navigate to PlaylistDetailFragment
                val action = PlaylistsFragmentDirections.actionPlaylistsFragmentToPlaylistDetailFragment(playlist.id, playlist.name)
                findNavController().navigate(action)
            },
            { playlist ->
                // Allow user to delete a playlist
                showDeleteConfirmationDialog(playlist)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Observe LiveData
        viewModel.playlists.observe(viewLifecycleOwner) { updatedPlaylists ->
            playlists.clear()
            playlists.addAll(updatedPlaylists)
            adapter.notifyDataSetChanged()
        }

        // Floating action button for creating a new playlist
        val addPlaylist: FloatingActionButton = view.findViewById(R.id.addPlaylistFab)
        addPlaylist.setOnClickListener {
            showAddPlaylistDialog()
        }
    }

    // Display dialog to allow user to create playlist
    private fun showAddPlaylistDialog() {
        // Create TextInputLayout
        val textInputLayout = TextInputLayout(
            requireContext(),
            null,
            com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox
        ).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            hint = "Playlist name"
        }

        // Create TextInputEditText
        val input = TextInputEditText(textInputLayout.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            maxLines = 1
        }

        // Create a container LinearLayout
        val container = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 0)
        }

        // Add TextInputLayout to container
        container.addView(textInputLayout)
        // Add EditText to TextInputLayout
        textInputLayout.addView(input)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Create new playlist")
            .setView(container)
            .setPositiveButton("Create") { _, _ ->
                val playlistName = input.text.toString()
                if (playlistName.isNotBlank()) {
                    viewModel.addPlaylist(playlistName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Display dialog to allow user to delete playlist
    private fun showDeleteConfirmationDialog(playlist: Playlist) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Playlist")
            .setMessage("Are you sure you want to delete \"${playlist.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.removePlaylist(playlist.id)
            }
            .setNegativeButton("Cancel", null)
            .setIcon(R.drawable.noti_logo)
            .show()
    }
}
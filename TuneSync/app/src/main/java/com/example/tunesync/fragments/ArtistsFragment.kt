package com.example.tunesync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tunesync.models.ArtistsViewModel
import com.example.tunesync.R
import com.example.tunesync.adapters.ArtistAdapter
import com.example.tunesync.databinding.FragmentArtistsBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ArtistsFragment : Fragment() {
    // Dedicated fragment for displaying artists

    private var _binding: FragmentArtistsBinding? = null
    private val binding get() = _binding!!
    private lateinit var artistAdapter: ArtistAdapter
    private lateinit var viewModel: ArtistsViewModel
    private var isGridMode: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupViewModel()
        setupSearch()
        setupSwipeRefresh()
        setupToggleButton()
    }

    // If artist data is still loading, allow the user to refresh the layout to repopulate the screen
    // Adapted from - source: https://www.digitalocean.com/community/tutorials/android-swiperefreshlayout-pull-swipe-refresh
    // Author - Anupam Chugh

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.apply {
            setColorSchemeColors(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
            setProgressBackgroundColorSchemeColor(
                ContextCompat.getColor(requireContext(), R.color.black)
            )
            setOnRefreshListener {
                viewModel.forceRefresh()
            }
        }
    }

    // Recycler view for displaying the list of artists
    private fun setupRecyclerView() {

        artistAdapter = ArtistAdapter(onArtistClick = { artist -> navigateToArtistSongs(artist) })

        binding.recyclerViewArtists.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = artistAdapter
            setHasFixedSize(true)
        }
    }

    // Monitor the view model
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[ArtistsViewModel::class.java]

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.artistDataList.collect { artistDataList ->
                        artistAdapter.submitList(artistDataList)
                    }
                }

                launch {
                    viewModel.isLoading.collect { isLoading ->
                        binding.ArtistProgressBar.isVisible = isLoading
                        binding.swipeRefreshLayout.isRefreshing = isLoading
                    }
                }
            }
        }

        viewModel.loadArtistsIfNeeded()
    }

    // Allow users to search for a specific artist
    private fun setupSearch() {
        var searchJob: Job? = null

        binding.searchViewArtists.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                searchJob?.cancel()
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(300)
                    val filteredList = viewModel.artistDataList.value.filter {
                        it.name.contains(newText ?: "", ignoreCase = true)
                    }
                    artistAdapter.submitList(filteredList)
                }
                return true
            }
        })
    }

    // Toggle between the list and grid views
    private fun setupToggleButton() {
        binding.toggleButtonViewMode.setOnCheckedChangeListener { _, isChecked ->
            isGridMode = isChecked
            artistAdapter.viewType = if (isGridMode) ArtistAdapter.VIEW_TYPE_GRID else ArtistAdapter.VIEW_TYPE_LIST
            updateRecyclerViewLayout()
        }
    }

    // Refresh the layout if a user changes the view
    private fun updateRecyclerViewLayout() {
        val layoutManager = if (isGridMode) {
            GridLayoutManager(requireContext(), 2)
        } else {
            LinearLayoutManager(requireContext())
        }
        binding.recyclerViewArtists.layoutManager = layoutManager
        artistAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Allow user to click on an artist name, to go view songs
    private fun navigateToArtistSongs(artist: String) {
        findNavController().navigate(
            R.id.action_offlineMusicFragment_to_artistSongsFragment,
            Bundle().apply { putString("artistName", artist) }
        )
    }
}

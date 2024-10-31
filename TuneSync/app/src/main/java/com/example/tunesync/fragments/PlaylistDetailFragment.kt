package com.example.tunesync.fragments

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tunesync.R
import com.example.tunesync.adapters.SongAdapter
import com.example.tunesync.entities.SongEntity
import com.example.tunesync.models.Song
import com.example.tunesync.services.AppDatabase
import com.example.tunesync.services.PaginatedSongAdapter
import com.example.tunesync.services.PlaylistRepository
import com.example.tunesync.services.PlaylistViewModel
import com.example.tunesync.services.PlaylistViewModelFactory
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class PlaylistDetailFragment : Fragment() {
    // Fragment for accessing playlists and adding songs

    private lateinit var viewModel: PlaylistViewModel
    private var playlistId: Long = -1
    private lateinit var adapter: SongAdapter
    private val songs = mutableListOf<Song>()
    private lateinit var exoPlayer: ExoPlayer
    private var currentSongIndex = -1

    // Pie chart for song genres
    private lateinit var genreChart: PieChart

    // Specialized recycler for selecting songs
    private lateinit var paginatedSongAdapter: PaginatedSongAdapter
    private lateinit var paginatedRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playlistId = it.getLong("playlistId", -1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_playlist_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Pass the playlist info
        val playlistId = arguments?.getLong("playlistId") ?: return
        val playlistName = arguments?.getString("playlistName") ?: "Unnamed Playlist"

        view.findViewById<TextView>(R.id.playlistNameTextView).text = playlistName

        // Initialize ViewModel and ExoPlayer
        val repository = PlaylistRepository(
            AppDatabase.getDatabase(requireContext()).playlistDao(),
            AppDatabase.getDatabase(requireContext()).songDao()
        )
        val factory = PlaylistViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(PlaylistViewModel::class.java)
        exoPlayer = ExoPlayer.Builder(requireContext()).build()


        // Set the player listener
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    // Move to the next song when the current song ends
                    playNextSong()
                }
            }
        })

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewPlaylistSongs)

        adapter = SongAdapter(songs) { song ->
            playSong(song)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Handle songs for the specific playlist
        viewModel.getSongsInPlaylist(playlistId).observe(viewLifecycleOwner) { playlistSongs ->
            songs.clear()
            songs.addAll(playlistSongs.map { it.toSong() })
            adapter.notifyDataSetChanged()
            if (currentSongIndex == -1 && songs.isNotEmpty()) {
                currentSongIndex = 0
                updateNowPlaying(songs[currentSongIndex])
            }
        }

        // Pie chart setup
        genreChart = view.findViewById(R.id.genreChart)
        setupGenreChart()

        viewModel.getGenreDistribution(playlistId).observe(viewLifecycleOwner) { genreDistribution ->
            updateGenreChart(genreDistribution)
        }

        // Set up player controls for ExoPlayer
        setupPlayerControls(view)
        setupSkipControls(view)

        // Setup recycler that will contain songs user can add to their playlist
        setupPaginatedSongSelector()
        val addSongButton: Button = view.findViewById(R.id.addSongButton)
        addSongButton.setOnClickListener {
            toggleSongSelector()
        }
    }

    // Function to play the next song
    private fun playNextSong() {
        if (currentSongIndex < songs.size - 1) {
            currentSongIndex++
            playSong(songs[currentSongIndex])
            //startPlayback()
        } else {
            // Stop playback when the last song finishes
            exoPlayer.pause()
            currentSongIndex = -1

            // Update adapter to clear the playing indicator
            adapter.updateCurrentlyPlayingSong(null)

            // Update the play/pause button icon to reflect the current state
            view?.findViewById<ImageButton>(R.id.playPauseButton)?.setImageResource(android.R.drawable.ic_media_play)
        }
    }


    // Get the adapter
    private fun setupPaginatedSongSelector() {
        paginatedSongAdapter = PaginatedSongAdapter { selectedSong ->
            viewModel.addSongToPlaylist(playlistId, selectedSong.id)
            toggleSongSelector()
        }

        // Add songs for displaying to user
        view?.findViewById<RecyclerView>(R.id.paginatedSongRecyclerView)?.let { recyclerView ->
            paginatedRecyclerView = recyclerView
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = paginatedSongAdapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getSongsPaginated().collectLatest { pagingData ->
                paginatedSongAdapter.submitData(pagingData)
            }
        }
    }

    // If user clicks on "Add Song" button, collapse the recycler, or make it visible
    private fun toggleSongSelector() {

        val addSongButton = requireView().findViewById<Button>(R.id.addSongButton)

        if (paginatedRecyclerView.visibility == View.VISIBLE) {
            paginatedRecyclerView.visibility = View.GONE
            addSongButton.text = getString(R.string.add_song)
        } else {
            paginatedRecyclerView.visibility = View.VISIBLE
            addSongButton.text = getString(R.string.hide_list)
        }
    }

    // Entity conversion for database query
    private fun SongEntity.toSong(): Song {
        return Song(
            id = id,
            name = name,
            uri = Uri.parse(uri),
            duration = duration,
            artist = artist,
            album = album,
            albumArtUri = albumArtUri?.let { Uri.parse(it) },
            genre = genre,
            releaseYear = releaseYear
        )
    }

    // Play the song the user clicked on
    private fun playSong(song: Song) {
        exoPlayer.setMediaItem(MediaItem.fromUri(song.uri))
        exoPlayer.prepare()
        exoPlayer.play()
        updateNowPlaying(song)

        // Update the currently playing song in the adapter
        adapter.updateCurrentlyPlayingSong(song.id)

        // Update the play/pause button icon to reflect the current state
        view?.findViewById<ImageButton>(R.id.playPauseButton)?.setImageResource(android.R.drawable.ic_media_pause)
    }


    // Update the UI with song details
    private fun updateNowPlaying(song: Song) {
        val nowPlayingTextView = view?.findViewById<TextView>(R.id.nowPlayingTextView)
        nowPlayingTextView?.text = getString(R.string.now_playing_format, song.name, song.artist)
    }

    // Only start playback when user clicks play button
    private fun startPlayback() {
        if (!exoPlayer.isPlaying && songs.isNotEmpty()) {
            exoPlayer.play()
            view?.findViewById<ImageButton>(R.id.playPauseButton)?.setImageResource(android.R.drawable.ic_media_pause)
        }
    }

    // More custom ExoPlayer controls
    private fun setupPlayerControls(view: View) {
        val playPauseButton: ImageButton = view.findViewById(R.id.playPauseButton)
        playPauseButton.setOnClickListener {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
                playPauseButton.setImageResource(android.R.drawable.ic_media_play)
            } else {
                startPlayback()
            }
        }
    }

    // Using custom controls to navigate the song list
    private fun setupSkipControls(view: View) {
        val previousButton: ImageButton = view.findViewById(R.id.previousButton)
        val nextButton: ImageButton = view.findViewById(R.id.nextButton)

        previousButton.setOnClickListener {
            if (currentSongIndex > 0) {
                currentSongIndex--
                playSong(songs[currentSongIndex])
                startPlayback()
            }
        }

        nextButton.setOnClickListener {
            playNextSong()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }

    // Display pie chart containing genre information
    // Adapted from - source: https://www.geeksforgeeks.org/how-to-add-a-pie-chart-into-an-android-application/
    // Author - GeeksForGeeks

    private fun setupGenreChart() {
        genreChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setExtraOffsets(5f, 10f, 5f, 5f)
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            animateY(1400, Easing.EaseInOutQuad)
            legend.isEnabled = false

            // Adjust the position of entry labels
            setDrawEntryLabels(false)
            setEntryLabelTextSize(14f)
            setEntryLabelColor(Color.WHITE)

            // Center text
            centerText = "Genres"
            setCenterTextSize(18f)
            setCenterTextColor(Color.BLACK)
        }
    }

    private fun updateGenreChart(genreDistribution: Map<String, Int>) {
        val entries = genreDistribution.map { (genre, count) ->
            PieEntry(count.toFloat(), genre)
        }

        val dataSet = PieDataSet(entries, "Genres").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            sliceSpace = 5f
            selectionShift = 10f

            // Enable value lines and display labels outside the chart
            setDrawValues(true)
            valueTextSize = 14f
            valueTextColor = Color.BLACK

            // Set value lines for the labels
            valueLinePart1OffsetPercentage = 80f
            valueLinePart1Length = 0.6f
            valueLinePart2Length = 0.3f
            valueLineWidth = 2f
            isUsingSliceColorAsValueLineColor = true

            // Draw labels outside of the pie slices
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE

            // Custom value formatter to show both genre names and percentages
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.toInt()}%"
                }

                override fun getPieLabel(value: Float, pieEntry: PieEntry): String {
                    // Combine genre name and percentage
                    val shortenedLabel = if (pieEntry.label.length > 10) {
                        pieEntry.label.take(10) + ".." // Shorten long genre names
                    } else {
                        pieEntry.label
                    }
                    return "$shortenedLabel: ${value.toInt()}%"
                }
            }
        }

        val data = PieData(dataSet)
        genreChart.data = data
        genreChart.invalidate()
    }

}
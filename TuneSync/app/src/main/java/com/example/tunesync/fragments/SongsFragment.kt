package com.example.tunesync.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tunesync.models.PlayerViewModel
import com.example.tunesync.R
import com.example.tunesync.models.SongsViewModel
import com.example.tunesync.adapters.SongAdapter
import com.example.tunesync.models.Song
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import kotlin.coroutines.resumeWithException


class SongsFragment : Fragment() {
    // Main landing page for music playing

    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private val songs = mutableListOf<Song>()

    private lateinit var songTitleTextView: TextView
    private lateinit var artistTextView: TextView
    private lateinit var playPauseButton: ImageButton
    private lateinit var previousButton: ImageButton
    private lateinit var nextButton: ImageButton

    // lyrics
    private lateinit var showLyricsButton: Button
    private lateinit var lyricsFragment: LyricsFragment
    private lateinit var lyricsContainer: FrameLayout

    // Search for songs
    private lateinit var searchView: SearchView
    private val allSongs = mutableListOf<Song>()

    private lateinit var progressBar: ProgressBar

    private lateinit var playerViewModel: PlayerViewModel
    private lateinit var songsViewModel: SongsViewModel

    private val originalSongIndices = mutableMapOf<Song, Int>()

    // Check if user granted app permission to access music
    // Adapted from - source: https://towardsdev.com/how-to-check-and-request-permissions-in-android-1c3c4a66f285
    // Author - Anggara Dwi kuntoro

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                songsViewModel.loadSongsIfNeeded()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playerViewModel = ViewModelProvider(requireActivity()).get(PlayerViewModel::class.java)
        songsViewModel = ViewModelProvider(requireActivity()).get(SongsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_songs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupRecyclerView()
        setupExoPlayer(view)
        setupMediaControls()

        setupSearchView()

        observeSongs()
        observeLoadingState()
        checkPermissionAndLoadSongs()
        setupLyrics()
    }

    // Check if songs are being loaded into the ROOM database
    // Display loading bar until tracks are added to database
    private fun observeLoadingState() {
        songsViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // If tracks have previously been loaded
            // Display progress bar while app retrieves songs from cached data
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    // Monitor song data for changes

    private fun observeSongs() {
        songsViewModel.songs.observe(viewLifecycleOwner) { songList ->
            songs.clear()

            // Sort songs alphabetically by name
            val sortedSongs = songList.sortedBy { it.name }

            songs.addAll(sortedSongs)
            allSongs.clear()
            allSongs.addAll(sortedSongs)
            songAdapter.updateSongs(songs)

            updateTotalSongsCount()
            setupPlaylist()
        }
    }

    private fun initializeViews(view: View) {
        recyclerView = view.findViewById(R.id.upcomingSongsRecyclerView)
        songTitleTextView = view.findViewById(R.id.songTitleTextView)
        artistTextView = view.findViewById(R.id.artistNameTextView)
        playPauseButton = view.findViewById(R.id.playPauseButton)
        previousButton = view.findViewById(R.id.previousButton)
        nextButton = view.findViewById(R.id.nextButton)
        showLyricsButton = view.findViewById(R.id.showLyricsButton)
        lyricsContainer = view.findViewById(R.id.lyricsContainer)
        searchView = view.findViewById(R.id.searchView)
        progressBar = view.findViewById(R.id.progressBar)
    }

    // Add songs from database, into the recycler view

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(songs) { song ->
            val originalIndex = originalSongIndices[song] ?: songs.indexOf(song)
            playSongAtIndex(originalIndex)
            updateCurrentSongInfo(song)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = songAdapter
    }

    // Use the PlayerViewModel to set up an Exo Player that allows audio to play in background
    // To allow users to navigate to other parts of the app while music keeps playing
    // Or if the screen is turned off

    // Adapted from - source: https://developer.android.com/media/media3/exoplayer/hello-world
    // Author - Developer.Android
    private fun setupExoPlayer(view: View) {
        val playerView = view.findViewById<PlayerView>(R.id.player_view_home)
        playerView.player = playerViewModel.exoPlayer

        playerViewModel.exoPlayer.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateCurrentSongInfo()
            }

            override fun onPlaybackStateChanged(state: Int) {
                updatePlayPauseButton(state)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                // Update the play/pause button when playback state changes
                playPauseButton.setImageResource(
                    if (isPlaying) android.R.drawable.ic_media_pause
                    else android.R.drawable.ic_media_play
                )
            }
        })
    }


    // Ensure play/previous and play/pause buttons work

    private fun setupMediaControls() {
        playPauseButton.setOnClickListener {
            playerViewModel.togglePlayPause()

            // Directly update the button icon based on whether the player is now playing or paused
            playPauseButton.setImageResource(
                if (playerViewModel.exoPlayer.isPlaying) android.R.drawable.ic_media_pause
                else android.R.drawable.ic_media_play
            )
        }
        previousButton.setOnClickListener { playerViewModel.exoPlayer.seekToPreviousMediaItem() }
        nextButton.setOnClickListener { playerViewModel.exoPlayer.seekToNextMediaItem() }
    }


    // If a user leaves this fragment, update UI when they return, if song still playing
    // Also update play/pause button when clicked
    private fun updatePlayPauseButton(state: Int) {
        when (state) {
            Player.STATE_BUFFERING -> playPauseButton.isEnabled = false
            Player.STATE_READY -> {
                playPauseButton.isEnabled = true
                playPauseButton.setImageResource(
                    if (playerViewModel.exoPlayer.isPlaying) android.R.drawable.ic_media_pause
                    else android.R.drawable.ic_media_play
                )
            }
            Player.STATE_ENDED -> playerViewModel.exoPlayer.seekTo(0, 0)
            Player.STATE_IDLE -> playPauseButton.isEnabled = false
        }
    }

    // If a user leaves this fragment, ensure Exo Player is updated when they return
    private fun updateCurrentSongInfo() {
        val currentSongIndex = playerViewModel.exoPlayer.currentMediaItemIndex
        val currentSong = songs.getOrNull(currentSongIndex)

        currentSong?.let {
            songTitleTextView.text = it.name
            artistTextView.text = it.artist
            songAdapter.updateCurrentlyPlayingSong(it.id)
        }
    }

    // Overload method to ensure users can still access the same song list
    // when using the search view to filter the songs
    private fun updateCurrentSongInfo(song: Song?) {
        song?.let {
            songTitleTextView.text = it.name
            artistTextView.text = it.artist
            songAdapter.updateCurrentlyPlayingSong(it.id)

        }
    }

    // Ensure all songs are in a playlist so they can play back-to-back
    private fun playSongAtIndex(index: Int) {
        if (index in 0 until allSongs.size) {
            setupPlaylist()
            playerViewModel.exoPlayer.seekTo(index, 0)
            playerViewModel.exoPlayer.playWhenReady = true
            updateCurrentSongInfo(allSongs[index])
        }
    }

    // Check if the playlist needs to updated, when user accesses the fragment
    private fun setupPlaylist() {
        playerViewModel.updatePlaylistIfNeeded(allSongs)
    }

    // Ensure the fragment responsible for lyrics is accessible when user clicks button
    private fun setupLyrics() {
        lyricsFragment = LyricsFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.lyricsContainer, lyricsFragment)
            .commit()

        showLyricsButton.setOnClickListener {
            if (lyricsContainer.visibility == View.VISIBLE) {
                lyricsContainer.visibility = View.GONE
                showLyricsButton.setText(R.string.show_lyrics)
            } else {
                if (isNetworkAvailable(requireContext())) {
                    lyricsContainer.visibility = View.VISIBLE
                    showLyricsButton.setText(R.string.hide_lyrics)

                    // Only fetch lyrics when showing the lyrics container
                    val currentSong = songs.getOrNull(playerViewModel.exoPlayer.currentMediaItemIndex)
                    if (currentSong == null) {
                        lyricsFragment.updateLyrics("No song currently playing")
                        return@setOnClickListener
                    }

                    // Show loading state in lyrics fragment
                    lyricsFragment.updateLyrics("Loading lyrics...")

                    lifecycleScope.launch {
                        val lyrics = fetchLyrics(currentSong.name, currentSong.artist)
                        lyricsFragment.updateLyrics(lyrics)
                    }
                } else {
                    showError(requireView(), "No internet connection. Please connect and try again.")
                }
            }
        }
    }

    // Allow user to search for specific songs/albums/artists

    // Adapted from - source: https://www.geeksforgeeks.org/searchview-in-android-with-listview/
    // Author - GeeksForGeeks

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterSongs(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterSongs(newText)
                return true
            }
        })
    }

    // Filter list based on search term

    private fun filterSongs(query: String?) {
        if (query.isNullOrBlank()) {
            songs.clear()
            songs.addAll(allSongs)
            originalSongIndices.clear()
            allSongs.forEachIndexed { index, song ->
                originalSongIndices[song] = index
            }
        } else {
            val filteredList = allSongs.filter { song ->
                song.name.contains(query, ignoreCase = true) ||
                        song.artist.contains(query, ignoreCase = true) ||
                        song.album.contains(query, ignoreCase = true)
            }
            songs.clear()
            songs.addAll(filteredList)

            originalSongIndices.clear()
            filteredList.forEach { song ->
                originalSongIndices[song] = allSongs.indexOf(song)
            }
        }
        songAdapter.updateSongs(songs)
        updateTotalSongsCount()
        setupPlaylist()
    }

    // After songs are loaded from database/device, display song total
    private fun updateTotalSongsCount() {
        val totalSongs = songs.size
        view?.findViewById<TextView>(R.id.totalSongsTextView)?.text = getString(R.string.total_songs_text, totalSongs)
    }


    // Check if user granted app permission to access music
    // Adapted from - source: https://towardsdev.com/how-to-check-and-request-permissions-in-android-1c3c4a66f285
    // Author - Anggara Dwi kuntoro

    private fun checkPermissionAndLoadSongs() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                songsViewModel.loadSongsIfNeeded()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                Toast.makeText(requireContext(), "Audio permission is required to display songs", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(permission)
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    // Fetch lyrics for current song using API
    // API source - RapidAPI
    // Endpoint documentation - https://rapidapi.com/Paxsenix0/api/musixmatch-lyrics-songs

    private suspend fun fetchLyrics(title: String?, artist: String?): String = withContext(Dispatchers.IO) {
        if (title.isNullOrBlank() || artist.isNullOrBlank()) {
            return@withContext "Lyrics not available: Missing song information"
        }

        val sanitizedTitle = title.replace(Regex("[^\\w\\s-]"), "").trim()
        val sanitizedArtist = artist.replace(Regex("[^\\w\\s-]"), "").trim()

        if (sanitizedTitle.isEmpty() || sanitizedArtist.isEmpty()) {
            return@withContext "Lyrics not available: Invalid song information"
        }

        val formattedTitle = sanitizedTitle.replace(" ", "+")
        val formattedArtist = sanitizedArtist.replace(" ", "+")

        // Fetch API credentials from Firebase
        try {
            val (apiKey, apiHost, baseUrl) = fetchApiCredentials()

            val client = OkHttpClient()
            val request = Request.Builder()
                .url("$baseUrl/songs/lyrics?t=$formattedTitle&a=$formattedArtist&type=json")
                .get()
                .addHeader("x-rapidapi-key", apiKey)
                .addHeader("x-rapidapi-host", apiHost)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext "Lyrics not available: API error (${response.code})"
            }

            val responseBody = response.body?.string()
            if (responseBody.isNullOrEmpty()) {
                return@withContext "Lyrics not available: Empty response"
            }

            val jsonArray = JSONArray(responseBody)
            if (jsonArray.length() == 0) {
                return@withContext "Lyrics not available for this song"
            }

            val formattedLyrics = StringBuilder()

            for (i in 0 until jsonArray.length()) {
                val lyricObject = jsonArray.getJSONObject(i)
                val text = lyricObject.optString("text", "")
                val time = lyricObject.optJSONObject("time")
                val minutes = time?.optInt("minutes", 0) ?: 0
                val seconds = time?.optInt("seconds", 0) ?: 0

                formattedLyrics.append(String.format("%02d:%02d   %s\n", minutes, seconds, text))
                formattedLyrics.append("\n")
            }

            return@withContext formattedLyrics.toString()

        } catch (e: Exception) {
            return@withContext "Lyrics not available: ${e.message}"
        }
    }

    // Fetch API credentials from the database
    private suspend fun fetchApiCredentials(): Triple<String, String, String> = suspendCancellableCoroutine { continuation ->
        val database = FirebaseDatabase.getInstance().reference
        val apiRef = database.child("lyricsKey").child("rapidApi")

        apiRef.get().addOnSuccessListener { snapshot ->
            val apiKey = snapshot.child("key").value.toString()
            val apiHost = snapshot.child("host").value.toString()
            val baseUrl = snapshot.child("baseUrl").value.toString()
            continuation.resume(Triple(apiKey, apiHost, baseUrl)) { }
        }.addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
    }

    private fun showError(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
    }


    // Update Exo Player when user returns to this fragment
    override fun onResume() {
        super.onResume()
        updateCurrentSongInfo()
        updatePlayPauseButton(playerViewModel.exoPlayer.playbackState)
    }

    // Check if the user device has access to the internet
    // Adapted from - source: https://medium.com/@manuchekhrdev/simple-ways-to-monitor-internet-connectivity-in-android-a3bef75bd3d9
    // Author - Manuchekhr Tursunov

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

}
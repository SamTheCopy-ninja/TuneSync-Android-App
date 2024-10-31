package com.example.tunesync.fragments

//import android.R
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tunesync.models.ConcertViewModel
import com.example.tunesync.services.ConcertViewModelFactory
import com.example.tunesync.services.PendingConcertSearch
import com.example.tunesync.adapters.ConcertAdapter
import com.example.tunesync.databinding.FragmentConcertBinding
import com.example.tunesync.models.Concert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import com.example.tunesync.R
import com.example.tunesync.services.AppDatabase
import com.google.firebase.database.FirebaseDatabase


class ConcertFragment : Fragment() {
    // Fragment used to search for concert events, via API

    private lateinit var binding: FragmentConcertBinding
    private lateinit var concertAdapter: ConcertAdapter
    private lateinit var viewModel: ConcertViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentConcertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtain an instance of PendingSearchDao
        val pendingSearchDao = AppDatabase.getDatabase(requireContext()).pendingSearchDao()

        // Create the ViewModelFactory
        val factory = ConcertViewModelFactory(pendingSearchDao)

        // Initialize the ViewModel using the factory
        viewModel = ViewModelProvider(this, factory).get(ConcertViewModel::class.java)

        setupDateSpinner()
        setupRecyclerView()
        setupSearchButton()
        setupPendingSearchButton()
        observePendingSearches()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    // Setup date spinner according the URL parameters for the API
    // API source - RapidAPI
    // Endpoint documentation - https://rapidapi.com/letscrape-6bRBa3QguO5/api/real-time-events-search

    private fun setupDateSpinner() {
        val dateOptions = arrayOf("any", "today", "tomorrow", "week", "weekend", "next_week", "month", "next_month")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dateOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDate.adapter = adapter
    }

    // Recycler view for for formatted API responses
    private fun setupRecyclerView() {
        concertAdapter = ConcertAdapter()
        binding.recyclerViewConcerts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = concertAdapter
        }
    }

    // Allow users to search for a concert
    private fun setupSearchButton() {
        binding.btnSearch.setOnClickListener {
            val query = binding.etQuery.text.toString()
            if (query.isBlank()) {
                Toast.makeText(context, "Please enter a search query", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val date = binding.spinnerDate.selectedItem.toString()
            val isVirtual = binding.switchVirtual.isChecked

            if (isNetworkAvailable()) {
                searchConcerts(query, date, isVirtual)
            } else {
                lifecycleScope.launch {
                    viewModel.savePendingSearch(
                        PendingConcertSearch(
                            query = query,
                            date = date,
                            isVirtual = isVirtual
                        )
                    )
                    Toast.makeText(
                        context,
                        getString(R.string.no_internet_search_saved),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // Check if any searches are pending, and allow users to execute the search if they have an internet connection
    // Display the pending searches button if needed
    private fun setupPendingSearchButton() {
        binding.btnExecutePendingSearch.setOnClickListener {
            if (isNetworkAvailable()) {
                viewModel.pendingSearches.value?.firstOrNull()?.let { pendingSearch ->
                    searchConcerts(pendingSearch.query, pendingSearch.date, pendingSearch.isVirtual)
                    lifecycleScope.launch {
                        viewModel.deletePendingSearch(pendingSearch)
                    }
                }
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.no_internet_available),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Display information about the pending search
    private fun observePendingSearches() {
        viewModel.pendingSearches.observe(viewLifecycleOwner) { searches ->
            val hasPendingSearches = searches.isNotEmpty()
            binding.layoutPendingSearch.isVisible = hasPendingSearches

            if (hasPendingSearches) {
                val latestSearch = searches.first()
                binding.tvPendingSearchDetails.text = getString(
                    R.string.pending_search_details,
                    latestSearch.query,
                    latestSearch.date,
                    if (latestSearch.isVirtual) "Yes" else "No"
                )
            }
        }
    }

    // Search for concerts using API
    // API source - RapidAPI
    // Endpoint documentation - https://rapidapi.com/letscrape-6bRBa3QguO5/api/real-time-events-search

    private fun searchConcerts(query: String, date: String, isVirtual: Boolean) {
        // Show the progress bar
        binding.progressBarSearch.visibility = View.VISIBLE

        fetchApiCredentials { apiKey, apiHost, baseUrl ->
            val client = OkHttpClient()

            val url = "$baseUrl/search-events?" +
                    "query=${URLEncoder.encode(query, "UTF-8")}" +
                    "&date=$date" +
                    "&is_virtual=$isVirtual" +
                    "&start=0"

            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("x-rapidapi-key", apiKey)
                .addHeader("x-rapidapi-host", apiHost)
                .build()

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string()

                    withContext(Dispatchers.Main) {
                        // Hide the progress bar
                        binding.progressBarSearch.visibility = View.GONE
                        updateUI(responseBody)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        // Hide the progress bar
                        binding.progressBarSearch.visibility = View.GONE
                        showError(e.message ?: "An error occurred")
                    }
                }
            }
        }
    }

    // Fetch the API credentials from the database, to search for concerts
    private fun fetchApiCredentials(onComplete: (String, String, String) -> Unit) {
        val database = FirebaseDatabase.getInstance().reference
        val apiRef = database.child("concertKey").child("rapidApi")

        apiRef.get().addOnSuccessListener { snapshot ->
            val apiKey = snapshot.child("key").value.toString()
            val apiHost = snapshot.child("host").value.toString()
            val baseUrl = snapshot.child("baseUrl").value.toString()
            onComplete(apiKey, apiHost, baseUrl)
        }.addOnFailureListener {
            showError("Failed to retrieve API details from Firebase")
        }
    }

    // Update the UI after searches have been performed
    private fun updateUI(responseBody: String?) {
        responseBody?.let {
            try {
                val concerts = parseConcerts(it)
                concertAdapter.submitList(concerts)

                // Clear all pending searches after a successful search
                lifecycleScope.launch {
                    viewModel.clearAllPendingSearches()
                }
            } catch (e: Exception) {
                showError("Failed to parse concert data")
            }
        }
    }


    // Parse API response details so they can be formatted for displaying to the user

    private fun parseConcerts(json: String): List<Concert> {
        val jsonObject = JSONObject(json)
        val dataArray = jsonObject.getJSONArray("data")
        val concerts = mutableListOf<Concert>()

        for (i in 0 until dataArray.length()) {
            val concertObject = dataArray.getJSONObject(i)
            concerts.add(
                Concert(
                    name = concertObject.getString("name"),
                    link = concertObject.getString("link"),
                    description = concertObject.optString("description", "No description available"),
                    startTime = concertObject.optString("start_time", "Time not specified"),
                    thumbnail = concertObject.optString("thumbnail")
                )
            )
        }
        return concerts
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
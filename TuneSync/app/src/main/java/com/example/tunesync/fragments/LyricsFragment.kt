package com.example.tunesync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.tunesync.R

class LyricsFragment : Fragment() {
    // Fragment used to hold lyrics text from API

    // Text view to contain lyrics returned by the API
    private lateinit var lyricsTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lyrics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Text view for lyrics
        lyricsTextView = view.findViewById(R.id.lyricsTextView)
    }

    // Added lyrics to text view
    fun updateLyrics(lyrics: String) {
        lyricsTextView.text = lyrics
    }
}
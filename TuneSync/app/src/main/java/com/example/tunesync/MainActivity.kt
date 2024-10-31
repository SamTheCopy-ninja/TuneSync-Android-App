package com.example.tunesync

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.tunesync.models.PlayerViewModel
import com.example.tunesync.models.SongsViewModel

class MainActivity : AppCompatActivity() {

    // Initialize the models so can be used throughout the app
    private lateinit var playerViewModel: PlayerViewModel
    private lateinit var songsViewModel: SongsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Ensure entire app has context so users can navigate to other fragments without music stopping
        playerViewModel = ViewModelProvider(this).get(PlayerViewModel::class.java)
        songsViewModel = ViewModelProvider(this).get(SongsViewModel::class.java)
    }
}
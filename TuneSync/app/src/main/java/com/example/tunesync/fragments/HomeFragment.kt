package com.example.tunesync.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.tunesync.R
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {
    // Home fragment for main navigation

    // Firebase for logging out
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Go to the music hub
        view.findViewById<Button>(R.id.goToOfflineMusicButton).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_offlineMusicFragment)
        }

        // Go to the playlists page
        view.findViewById<Button>(R.id.playlistsButton).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_playlistsFragment)
        }

        // Go to the concerts page
        view.findViewById<Button>(R.id.btnConcertSearch).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_concertFragment)
        }

        // Redirect user to the Android OS audio settings page
        view.findViewById<Button>(R.id.audioSettingsButton).setOnClickListener {
            openAudioSettings()
        }

        // Logout button
        view.findViewById<Button>(R.id.logOutBtn).setOnClickListener {
            auth.signOut() // Sign out the user
            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()

            // Set NavOptions to clear the back stack
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.homeFragment, true)
                .build()

            // Navigate back to loginFragment with back stack cleared
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment, null, navOptions)
        }


    }

    private fun openAudioSettings() {
        // Check if Android version provides equalizer settings for music --- NB!!
        // Try to open the "Audio Effects" panel, based on the version of Android
        val intent = Intent(Settings.ACTION_SOUND_SETTINGS)
        intent.putExtra(":settings:fragment", "com.android.settings.DevelopmentSettings")
        intent.putExtra(":settings:fragment_args_key", "audio_effects_panel")
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // If the "Audio Effects" panel for equalizer settings is not available, open the general sound settings
            Toast.makeText(context, "No equalizer found in your version of Android", Toast.LENGTH_SHORT).show()
            intent.removeExtra(":settings:fragment")
            intent.removeExtra(":settings:fragment_args_key")
            startActivity(intent)
        }
    }
}

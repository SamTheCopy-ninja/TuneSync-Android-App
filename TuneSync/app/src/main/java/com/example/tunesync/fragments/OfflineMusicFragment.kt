package com.example.tunesync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.tunesync.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OfflineMusicFragment : Fragment() {
    // Main parent fragment for albums, songs and artists

    // View pager to enable tab layout
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_offline_music, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = view.findViewById(R.id.viewPager)
        tabLayout = view.findViewById(R.id.tabLayout)

        val adapter = ViewPagerAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = adapter

        // Tab layout
        // Adapted from - source: https://www.geeksforgeeks.org/android-viewpager-in-kotlin/
        // Author - GeeksForGeeks
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> requireContext().getString(R.string.tab_songs)
                1 -> requireContext().getString(R.string.tab_albums)
                2 -> requireContext().getString(R.string.tab_artists)
                else -> null
            }
        }.attach()

        viewPager.setCurrentItem(0, false)
    }

    // View pager for tabs
    // Adapted from - source: https://www.geeksforgeeks.org/android-viewpager-in-kotlin/
    // Author - GeeksForGeeks

    inner class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
        FragmentStateAdapter(fragmentManager, lifecycle) {

        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> SongsFragment()
                1 -> AlbumsFragment()
                2 -> ArtistsFragment()
                else -> throw IllegalStateException("Unexpected position $position")
            }
        }
    }

    fun navigateToAlbumSongs(albumName: String) {
        // Navigate to the album songs (without crashing app)
        findNavController().navigate(
            R.id.action_offlineMusicFragment_to_albumSongsFragment,
            Bundle().apply {
                putString("albumName", albumName)
            }
        )
    }
}
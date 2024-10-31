package com.example.tunesync

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.tunesync.services.PendingSearchWorker
import java.util.concurrent.TimeUnit

// Check if user has pending searches, and set a notification if confirmed
// Adapted from - source: https://dev.to/charfaouiyounes/learn-how-to-display-and-manage-notifications-in-android-4k8
// Author - Younes Charfaoui

class ConcertApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        schedulePendingSearchWorker()
    }

    // Create the notification channel
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "pending_search_channel",
                "Pending Searches",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for pending concert searches"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    // Schedule how frequently the app will check for notification status
    private fun schedulePendingSearchWorker() {
        val workRequest = PeriodicWorkRequestBuilder<PendingSearchWorker>(30, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "PendingSearchWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
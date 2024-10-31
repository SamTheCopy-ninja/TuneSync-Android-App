package com.example.tunesync.services

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tunesync.R
import kotlinx.coroutines.flow.first

// Check if user has pending searches, and set a notification if confirmed
// Adapted from - source: https://dev.to/charfaouiyounes/learn-how-to-display-and-manage-notifications-in-android-4k8
// Author - Younes Charfaoui

class PendingSearchWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val pendingSearchDao = AppDatabase.getDatabase(applicationContext).pendingSearchDao()

        // Get pending searches in a coroutine
        val pendingSearches = pendingSearchDao.getAllPendingSearches().first()

        if (pendingSearches.isNotEmpty()) {
            // Send a notification if there are pending searches
            sendNotification(pendingSearches)
        }

        return Result.success()
    }

    // Setup the notification structure
    private fun sendNotification(searches: List<PendingConcertSearch>) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(applicationContext, "pending_search_channel")
            .setSmallIcon(R.drawable.noti_logo)
            .setContentTitle("Sync Pending Concert Searches")
            .setContentText("You have pending event searches. Current Count: ${searches.size}")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}

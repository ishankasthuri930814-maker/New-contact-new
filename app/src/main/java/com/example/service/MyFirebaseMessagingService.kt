package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.aistudio.policedirectory.zxklm.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM token: $token")
        // Token can be sent to backend or saved if needed
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        try {
            Log.d(TAG, "From: ${remoteMessage.from}")

            // Check if message contains a notification payload
            val title = remoteMessage.notification?.title
                ?: remoteMessage.data["title"]
                ?: "Police Directory Alert"

            val body = remoteMessage.notification?.body
                ?: remoteMessage.data["body"]
                ?: "නව දැනුම්දීමක් (New Notification)"

            sendNotification(title, body)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling FCM message", e)
        }
    }

    private fun sendNotification(title: String, messageBody: String) {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

            val channelId = CHANNEL_ID
            val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create Notification Channel for Android O and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for Police Directory updates and emergency alerts"
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val notificationId = System.currentTimeMillis().toInt()
            notificationManager.notify(notificationId, notificationBuilder.build())
        } catch (e: Exception) {
            Log.e(TAG, "Error sending notification", e)
        }
    }

    companion object {
        private const val TAG = "FCMService"
        const val CHANNEL_ID = "police_directory_channel"
        const val CHANNEL_NAME = "Police Directory Notifications"

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for Police Directory updates and emergency alerts"
                    enableVibration(true)
                }
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}

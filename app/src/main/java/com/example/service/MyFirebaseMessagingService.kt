package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.aistudio.policedirectory.zxklm.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        try {
            Log.d(TAG, "FCM message received from: ${remoteMessage.from}")

            // Check if message contains a notification payload or data payload
            val title = remoteMessage.notification?.title
                ?: remoteMessage.data["title"]
                ?: remoteMessage.data["heading"]
                ?: "Police Directory Alert"

            val body = remoteMessage.notification?.body
                ?: remoteMessage.data["body"]
                ?: remoteMessage.data["message"]
                ?: remoteMessage.data["text"]
                ?: "නව දැනුම්දීමක් (New Notification)"

            sendNotification(title, body, remoteMessage.data)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling FCM message", e)
        }
    }

    private fun sendNotification(title: String, messageBody: String, data: Map<String, String>) {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra("extra_notification_title", title)
                putExtra("extra_notification_body", messageBody)
                for ((key, value) in data) {
                    putExtra(key, value)
                }
            }

            val pendingIntent = PendingIntent.getActivity(
                this,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val channelId = CHANNEL_ID
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(this, R.color.police_navy))
                .setContentTitle(title)
                .setContentText(messageBody)
                .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVibrate(longArrayOf(0, 250, 250, 250))
                .setContentIntent(pendingIntent)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Ensure channel exists
            createNotificationChannel(this)

            val notificationId = System.currentTimeMillis().toInt()
            notificationManager.notify(notificationId, notificationBuilder.build())
            Log.d(TAG, "Notification posted successfully [id=$notificationId, title=$title]")
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
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()

                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for Police Directory updates and emergency alerts"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 250, 250, 250)
                    enableLights(true)
                    setSound(soundUri, audioAttributes)
                }
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}

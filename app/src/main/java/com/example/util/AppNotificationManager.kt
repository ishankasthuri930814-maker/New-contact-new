package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.CallSession
import com.aistudio.policedirectory.zxklm.R

object AppNotificationManager {
    private const val TAG = "AppNotificationMgr"

    const val CHANNEL_MESSAGES_ID = "channel_direct_messages"
    const val CHANNEL_MESSAGES_NAME = "පණිවිඩ දැනුම්දීම් (Direct Messages)"

    const val CHANNEL_CALLS_ID = "channel_inapp_calls"
    const val CHANNEL_CALLS_NAME = "ඇමතුම් දැනුම්දීම් (In-App Voice Calls)"

    private const val CALL_NOTIFICATION_ID = 2001
    private const val MESSAGE_NOTIFICATION_ID_BASE = 3000

    private var activeRingtone: Ringtone? = null

    fun initializeChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Messages Channel
            val msgSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val msgAudioAttr = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                .build()

            val msgChannel = NotificationChannel(
                CHANNEL_MESSAGES_ID,
                CHANNEL_MESSAGES_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Chat messages from registered members and officers"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 180, 100, 180)
                setSound(msgSound, msgAudioAttr)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(msgChannel)

            // Calls Channel (High Priority with Ringtone)
            val callSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val callAudioAttr = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()

            val callChannel = NotificationChannel(
                CHANNEL_CALLS_ID,
                CHANNEL_CALLS_NAME,
                NotificationManager.IMPORTANCE_MAX
            ).apply {
                description = "Incoming voice calls from citizens and emergency response"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 800, 1000, 800, 1000)
                setSound(callSound, callAudioAttr)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(callChannel)
        }
    }

    /**
     * Start playing phone ringtone and vibration for incoming in-app call
     */
    fun startIncomingCallRingtone(context: Context) {
        try {
            stopIncomingCallRingtone(context)

            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val ringtone = RingtoneManager.getRingtone(context.applicationContext, ringtoneUri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone.isLooping = true
            }
            ringtone.play()
            activeRingtone = ringtone

            // Trigger vibration
            vibratePhone(context, longArrayOf(0, 800, 600, 800, 600, 800), 0)
        } catch (e: Exception) {
            Log.e(TAG, "Error playing incoming call ringtone", e)
        }
    }

    /**
     * Stop playing ringtone and stop vibration
     */
    fun stopIncomingCallRingtone(context: Context) {
        try {
            activeRingtone?.let {
                if (it.isPlaying) {
                    it.stop()
                }
            }
            activeRingtone = null

            val vibrator = getVibrator(context)
            vibrator.cancel()

            // Dismiss call notification if showing
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(CALL_NOTIFICATION_ID)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping ringtone", e)
        }
    }

    /**
     * Play brief notification chime for messages
     */
    fun playMessageNotificationSound(context: Context) {
        try {
            val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, alertUri)
            ringtone?.play()
            vibratePhone(context, longArrayOf(0, 150, 80, 150), -1)
        } catch (e: Exception) {
            Log.e(TAG, "Error playing message sound", e)
        }
    }

    /**
     * Show heads-up notification for incoming in-app voice call
     */
    fun showIncomingCallNotification(context: Context, callSession: CallSession) {
        try {
            initializeChannels(context)

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra("action_incoming_call_id", callSession.callId)
                putExtra("caller_name", callSession.callerName)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                101,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_CALLS_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("📞 ලැබෙන ඇමතුම (Incoming Voice Call)")
                .setContentText("${callSession.callerName} ඔබව අමතයි (Calling...)")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setAutoCancel(true)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, true)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(CALL_NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            Log.e(TAG, "Error showing incoming call notification", e)
        }
    }

    /**
     * Show notification for incoming direct chat message
     */
    fun showDirectMessageNotification(
        context: Context,
        senderName: String,
        messageText: String,
        conversationId: String
    ) {
        try {
            initializeChannels(context)

            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra("action_open_chat", conversationId)
                putExtra("sender_name", senderName)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                conversationId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_MESSAGES_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("💬 $senderName")
                .setContentText(messageText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(messageText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notifId = MESSAGE_NOTIFICATION_ID_BASE + (conversationId.hashCode() % 1000)
            notificationManager.notify(notifId, builder.build())
        } catch (e: Exception) {
            Log.e(TAG, "Error showing direct message notification", e)
        }
    }

    private fun getVibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun vibratePhone(context: Context, pattern: LongArray, repeat: Int) {
        try {
            val vibrator = getVibrator(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeat))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, repeat)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Vibration failed: ${e.message}")
        }
    }
}

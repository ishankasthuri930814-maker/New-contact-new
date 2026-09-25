package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.CallSession
import com.example.data.model.DirectChatMessage
import com.example.data.model.UserProfile
import com.example.util.AppNotificationManager
import com.aistudio.policedirectory.zxklm.R
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CallBackgroundService : Service() {

    companion object {
        private const val TAG = "CallBackgroundService"
        const val FOREGROUND_NOTIF_ID = 4001
        const val CHANNEL_FOREGROUND_ID = "channel_police_background_sync"
        const val CHANNEL_FOREGROUND_NAME = "පොලිස් සහ හදිසි සේවා සක්‍රීය සේවාව (Police Active Service)"

        const val ACTION_START_MONITORING = "com.example.service.ACTION_START_MONITORING"
        const val ACTION_STOP_MONITORING = "com.example.service.ACTION_STOP_MONITORING"
        const val ACTION_DECLINE_CALL_FROM_NOTIF = "com.example.service.ACTION_DECLINE_CALL_FROM_NOTIF"
        const val ACTION_ANSWER_CALL_FROM_NOTIF = "com.example.service.ACTION_ANSWER_CALL_FROM_NOTIF"

        const val EXTRA_CALL_ID = "extra_call_id"
        const val EXTRA_USER_EMAIL = "extra_user_email"
        const val EXTRA_USER_ID = "extra_user_id"
        const val EXTRA_USER_PHONE = "extra_user_phone"
        const val EXTRA_USER_NAME = "extra_user_name"

        fun start(context: Context, profile: UserProfile) {
            try {
                val intent = Intent(context, CallBackgroundService::class.java).apply {
                    action = ACTION_START_MONITORING
                    putExtra(EXTRA_USER_EMAIL, profile.email)
                    putExtra(EXTRA_USER_ID, profile.userId)
                    putExtra(EXTRA_USER_PHONE, profile.phoneNumber)
                    putExtra(EXTRA_USER_NAME, profile.displayName)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Cannot start CallBackgroundService: ${e.message}")
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, CallBackgroundService::class.java).apply {
                    action = ACTION_STOP_MONITORING
                }
                context.startService(intent)
            } catch (e: Exception) {
                Log.w(TAG, "Cannot stop CallBackgroundService: ${e.message}")
            }
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var incomingCallsListener: ListenerRegistration? = null
    private var incomingMessagesListener: ListenerRegistration? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var myKeys: List<String> = emptyList()
    private var lastNotifiedMessageTs = System.currentTimeMillis() - 5000L

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(this).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        try {
            createForegroundNotificationChannel()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val hasMic = androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.RECORD_AUDIO
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                val serviceType = if (hasMic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC or android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                } else {
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                }
                startForeground(FOREGROUND_NOTIF_ID, buildForegroundNotification(), serviceType)
            } else {
                startForeground(FOREGROUND_NOTIF_ID, buildForegroundNotification())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting foreground service: ${e.message}", e)
            try {
                startForeground(FOREGROUND_NOTIF_ID, buildForegroundNotification())
            } catch (t: Throwable) {
                Log.w(TAG, "Fallback startForeground failed: ${t.message}")
            }
        }
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            ACTION_STOP_MONITORING -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_DECLINE_CALL_FROM_NOTIF -> {
                val callId = intent.getStringExtra(EXTRA_CALL_ID) ?: ""
                if (callId.isNotBlank()) {
                    declineCall(callId)
                }
            }
            ACTION_ANSWER_CALL_FROM_NOTIF -> {
                val callId = intent.getStringExtra(EXTRA_CALL_ID) ?: ""
                AppNotificationManager.stopIncomingCallRingtone(this)
                AppNotificationManager.dismissIncomingCallNotification(this)
                val launchIntent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra("action_incoming_call_id", callId)
                    putExtra("action_auto_accept", true)
                }
                startActivity(launchIntent)
            }
            else -> {
                val email = intent?.getStringExtra(EXTRA_USER_EMAIL) ?: ""
                val userId = intent?.getStringExtra(EXTRA_USER_ID) ?: ""
                val phone = intent?.getStringExtra(EXTRA_USER_PHONE) ?: ""
                val name = intent?.getStringExtra(EXTRA_USER_NAME) ?: ""

                val keys = listOf(
                    DirectChatMessage.normalizeUserKey(email),
                    DirectChatMessage.normalizeUserKey(userId),
                    DirectChatMessage.normalizeUserKey(phone),
                    email.trim().lowercase(),
                    userId.trim(),
                    phone.trim()
                ).filter { it.isNotBlank() && it != "unknown_user" }.distinct()

                if (keys.isNotEmpty()) {
                    myKeys = keys
                    startBackgroundListeners(keys, name)
                }
            }
        }

        return START_STICKY
    }

    private fun startBackgroundListeners(keys: List<String>, currentUserName: String) {
        val db = firestore ?: return

        // 1. Listen for incoming calls
        incomingCallsListener?.remove()
        incomingCallsListener = db.collection("active_calls")
            .whereEqualTo("status", CallSession.STATUS_RINGING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val now = System.currentTimeMillis()
                val incoming = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    CallSession.fromMap(data).copy(callId = doc.id)
                }.firstOrNull { session ->
                    if (session.status != CallSession.STATUS_RINGING) return@firstOrNull false
                    // Ignore my own outgoing calls
                    if (keys.contains(session.callerId) || keys.contains(session.callerEmail) || keys.contains(session.callerUserId)) {
                        return@firstOrNull false
                    }
                    if (Math.abs(now - session.startedAt) > 90000L) return@firstOrNull false

                    // Destined for me?
                    keys.contains(session.receiverId) ||
                            session.targetKeys.any { keys.contains(it) } ||
                            (session.receiverEmail.isNotBlank() && keys.contains(session.receiverEmail.lowercase())) ||
                            (session.receiverUserId.isNotBlank() && keys.contains(session.receiverUserId)) ||
                            (session.receiverPhone.isNotBlank() && keys.contains(session.receiverPhone))
                }

                if (incoming != null) {
                    Log.d(TAG, "Background incoming call detected from: ${incoming.callerName}")
                    wakeScreen()
                    AppNotificationManager.startIncomingCallRingtone(this)
                    showIncomingCallNotificationWithActions(incoming)
                }
            }

        // 2. Listen for incoming direct messages
        incomingMessagesListener?.remove()
        val primaryKey = keys.firstOrNull() ?: return
        incomingMessagesListener = db.collection("direct_chats")
            .whereArrayContains("participants", primaryKey)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val messages = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    DirectChatMessage.fromMap(data).copy(id = doc.id)
                }.filter {
                    it.timestamp > lastNotifiedMessageTs &&
                            !keys.contains(it.senderId) &&
                            it.messageType != DirectChatMessage.TYPE_CALL_LOG
                }

                val latest = messages.maxByOrNull { it.timestamp }
                if (latest != null && latest.timestamp > lastNotifiedMessageTs) {
                    lastNotifiedMessageTs = latest.timestamp
                    AppNotificationManager.playMessageNotificationSound(this)
                    AppNotificationManager.showDirectMessageNotification(
                        context = this,
                        senderName = latest.senderName,
                        messageText = latest.text,
                        conversationId = latest.conversationId
                    )
                }
            }
    }

    private fun showIncomingCallNotificationWithActions(callSession: CallSession) {
        AppNotificationManager.initializeChannels(this)

        // Intent to open app
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("action_incoming_call_id", callSession.callId)
            putExtra("caller_name", callSession.callerName)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            101,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Answer Call
        val answerIntent = Intent(this, CallBackgroundService::class.java).apply {
            action = ACTION_ANSWER_CALL_FROM_NOTIF
            putExtra(EXTRA_CALL_ID, callSession.callId)
        }
        val answerPendingIntent = PendingIntent.getService(
            this,
            102,
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Decline Call
        val declineIntent = Intent(this, CallBackgroundService::class.java).apply {
            action = ACTION_DECLINE_CALL_FROM_NOTIF
            putExtra(EXTRA_CALL_ID, callSession.callId)
        }
        val declinePendingIntent = PendingIntent.getService(
            this,
            103,
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, AppNotificationManager.CHANNEL_CALLS_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("📞 ලැබෙන ඇමතුම (Incoming Voice Call)")
            .setContentText("${callSession.callerName} ඔබව අමතයි (Calling...)")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)
            .setFullScreenIntent(contentPendingIntent, true)
            .addAction(android.R.drawable.ic_menu_call, "🟢 පිළිගන්න (Answer)", answerPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "🔴 ප්‍රතික්ෂේප (Decline)", declinePendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2001, notification)
    }

    private fun declineCall(callId: String) {
        AppNotificationManager.stopIncomingCallRingtone(this)
        AppNotificationManager.dismissIncomingCallNotification(this)

        serviceScope.launch {
            try {
                firestore?.collection("active_calls")?.document(callId)?.update(
                    mapOf(
                        "status" to CallSession.STATUS_DECLINED,
                        "endedAt" to System.currentTimeMillis()
                    )
                )?.await()
            } catch (e: Exception) {
                Log.w(TAG, "Error declining call in background", e)
            }
        }
    }

    private fun createForegroundNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_FOREGROUND_ID,
                CHANNEL_FOREGROUND_NAME,
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Keeps voice calling and direct messaging listeners active in background"
                setShowBadge(false)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_FOREGROUND_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🛡️ ශ්‍රී ලංකා පොලිස් ආරක්ෂණ සේවාව සක්‍රීයයි")
            .setContentText("ඇමතුම් සහ පණිවිඩ සඳහා පසුබිමින් සූදානම්ව ඇත (Ready in background)")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    @SuppressLint("WakelockTimeout")
    private fun acquireWakeLock() {
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PoliceApp:CallBackgroundServiceLock")
            wakeLock?.acquire()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to acquire WakeLock", e)
        }
    }

    @Suppress("DEPRECATION")
    private fun wakeScreen() {
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager
            val screenLock = pm?.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "PoliceApp:IncomingCallWakeLock"
            )
            screenLock?.acquire(15000L)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to wake screen", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        incomingCallsListener?.remove()
        incomingMessagesListener?.remove()
        try {
            wakeLock?.let {
                if (it.isHeld) it.release()
            }
        } catch (e: Exception) {
            // WakeLock release error
        }
        Log.d(TAG, "CallBackgroundService destroyed")
    }
}

package com.example.data.repository

import android.content.Context
import android.media.AudioManager
import android.util.Log
import com.example.data.model.CallSession
import com.example.data.model.DirectChatMessage
import com.example.data.model.UserProfile
import com.example.util.AppNotificationManager
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class CallRepository(
    private val context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val TAG = "CallRepository"

    private val _activeCall = MutableStateFlow<CallSession?>(null)
    val activeCall: StateFlow<CallSession?> = _activeCall.asStateFlow()

    private val _incomingCall = MutableStateFlow<CallSession?>(null)
    val incomingCall: StateFlow<CallSession?> = _incomingCall.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(true)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                null
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Firestore not initialized for calls", t)
            null
        }
    }

    private var incomingCallListener: ListenerRegistration? = null
    private var activeCallListener: ListenerRegistration? = null
    private var currentListeningUserKey: String? = null
    private var myKeys: List<String> = emptyList()

    init {
        AppNotificationManager.initializeChannels(context)
    }

    fun startListeningForIncomingCalls(userProfile: UserProfile) {
        val keys = listOf(
            DirectChatMessage.normalizeUserKey(userProfile.email),
            DirectChatMessage.normalizeUserKey(userProfile.userId),
            DirectChatMessage.normalizeUserKey(userProfile.phoneNumber),
            userProfile.email.trim().lowercase(),
            userProfile.userId.trim(),
            userProfile.phoneNumber.trim()
        ).filter { it.isNotBlank() && it != "unknown_user" }.distinct()

        if (keys.isEmpty()) return
        myKeys = keys
        val primaryKey = keys.first()
        if (currentListeningUserKey == primaryKey && incomingCallListener != null) return

        currentListeningUserKey = primaryKey
        incomingCallListener?.remove()

        try {
            val db = firestore ?: return
            incomingCallListener = db.collection("active_calls")
                .whereEqualTo("status", CallSession.STATUS_RINGING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Error in incoming call listener", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val now = System.currentTimeMillis()
                        val activeIncoming = snapshot.documents.mapNotNull { doc ->
                            val data = doc.data ?: return@mapNotNull null
                            CallSession.fromMap(data).copy(callId = doc.id)
                        }.firstOrNull { session ->
                            if (session.status != CallSession.STATUS_RINGING) return@firstOrNull false
                            // Ignore if started by myself
                            if (myKeys.contains(session.callerId) ||
                                (session.callerEmail.isNotBlank() && myKeys.contains(session.callerEmail)) ||
                                (session.callerUserId.isNotBlank() && myKeys.contains(session.callerUserId))
                            ) {
                                return@firstOrNull false
                            }
                            // Ringing must be recent (within 90 seconds)
                            if (Math.abs(now - session.startedAt) > 90000L) return@firstOrNull false

                            // Check if this call is destined for me
                            val isForMe = myKeys.contains(session.receiverId) ||
                                    session.targetKeys.any { myKeys.contains(it) } ||
                                    (session.receiverEmail.isNotBlank() && userProfile.email.isNotBlank() && session.receiverEmail.equals(userProfile.email, ignoreCase = true)) ||
                                    (session.receiverUserId.isNotBlank() && userProfile.userId.isNotBlank() && session.receiverUserId == userProfile.userId) ||
                                    (session.receiverPhone.isNotBlank() && userProfile.phoneNumber.isNotBlank() && session.receiverPhone == userProfile.phoneNumber)

                            isForMe
                        }

                        if (activeIncoming != null) {
                            if (_incomingCall.value?.callId != activeIncoming.callId) {
                                Log.d(TAG, "Incoming in-app call detected from: ${activeIncoming.callerName} (ID: ${activeIncoming.callId})")
                                _incomingCall.value = activeIncoming
                                AppNotificationManager.startIncomingCallRingtone(context)
                                AppNotificationManager.showIncomingCallNotification(context, activeIncoming)
                                listenToActiveCall(activeIncoming.callId)
                            }
                        } else {
                            val prevIncoming = _incomingCall.value
                            if (prevIncoming != null && _activeCall.value?.status != CallSession.STATUS_CONNECTED) {
                                AppNotificationManager.stopIncomingCallRingtone(context)
                                AppNotificationManager.dismissIncomingCallNotification(context)
                                if (prevIncoming.connectedAt == 0L) {
                                    AppNotificationManager.showMissedCallNotification(context, prevIncoming.callerName)
                                    _activeCall.value = prevIncoming.copy(
                                        status = CallSession.STATUS_MISSED,
                                        endedAt = System.currentTimeMillis()
                                    )
                                }
                                _incomingCall.value = null
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start incoming call listener", e)
        }
    }

    fun listenToActiveCall(callId: String) {
        activeCallListener?.remove()
        try {
            val db = firestore ?: return
            activeCallListener = db.collection("active_calls").document(callId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        return@addSnapshotListener
                    }
                    val data = snapshot.data ?: return@addSnapshotListener
                    val updatedSession = CallSession.fromMap(data).copy(callId = snapshot.id)

                    val isCaller = myKeys.contains(updatedSession.callerId) ||
                            (updatedSession.callerEmail.isNotBlank() && myKeys.contains(updatedSession.callerEmail)) ||
                            (updatedSession.callerUserId.isNotBlank() && myKeys.contains(updatedSession.callerUserId))

                    if (updatedSession.status == CallSession.STATUS_RINGING) {
                        if (!isCaller) {
                            _incomingCall.value = updatedSession
                        } else {
                            _activeCall.value = updatedSession
                        }
                    } else {
                        _activeCall.value = updatedSession
                    }

                    if (updatedSession.status == CallSession.STATUS_CONNECTED) {
                        AppNotificationManager.stopIncomingCallRingtone(context)
                        _incomingCall.value = null
                    } else if (updatedSession.status == CallSession.STATUS_ENDED ||
                        updatedSession.status == CallSession.STATUS_DECLINED ||
                        updatedSession.status == CallSession.STATUS_MISSED
                    ) {
                        AppNotificationManager.stopIncomingCallRingtone(context)
                        AppNotificationManager.dismissIncomingCallNotification(context)
                        if (updatedSession.connectedAt == 0L && updatedSession.status != CallSession.STATUS_DECLINED) {
                            val isReceiver = !isCaller
                            if (isReceiver) {
                                AppNotificationManager.showMissedCallNotification(context, updatedSession.callerName)
                            }
                        }
                        _incomingCall.value = null
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error listening to active call $callId", e)
        }
    }

    suspend fun initiateCall(caller: UserProfile, receiver: UserProfile): CallSession? = withContext(Dispatchers.IO) {
        val callerKey = DirectChatMessage.normalizeUserKey(caller.email.ifBlank { caller.userId })
        val receiverKey = DirectChatMessage.normalizeUserKey(receiver.email.ifBlank { receiver.userId })

        val targetKeys = listOf(
            DirectChatMessage.normalizeUserKey(receiver.email),
            DirectChatMessage.normalizeUserKey(receiver.userId),
            DirectChatMessage.normalizeUserKey(receiver.phoneNumber),
            receiver.email.trim().lowercase(),
            receiver.userId.trim(),
            receiver.phoneNumber.trim()
        ).filter { it.isNotBlank() && it != "unknown_user" }.distinct()

        val callId = UUID.randomUUID().toString()
        val session = CallSession(
            callId = callId,
            callerId = callerKey,
            callerEmail = caller.email.trim().lowercase(),
            callerUserId = caller.userId.trim(),
            callerName = caller.displayName.takeIf { it.isNotBlank() && !it.equals("Citizen", ignoreCase = true) }
                ?: caller.email.substringBefore("@").replaceFirstChar { it.uppercase() }.ifBlank { "User" },
            callerAvatarIndex = caller.avatarIndex,
            callerPhone = caller.phoneNumber,
            receiverId = receiverKey,
            receiverEmail = receiver.email.trim().lowercase(),
            receiverUserId = receiver.userId.trim(),
            receiverName = receiver.displayName.takeIf { it.isNotBlank() && !it.equals("Citizen", ignoreCase = true) }
                ?: receiver.email.substringBefore("@").replaceFirstChar { it.uppercase() }.ifBlank { "User" },
            receiverAvatarIndex = receiver.avatarIndex,
            receiverPhone = receiver.phoneNumber,
            targetKeys = targetKeys,
            status = CallSession.STATUS_RINGING,
            callType = "VOICE",
            startedAt = System.currentTimeMillis()
        )

        _activeCall.value = session
        _isMuted.value = false
        _isSpeakerOn.value = true
        applyAudioHardwareSettings()

        try {
            val db = firestore
            if (db != null) {
                db.collection("active_calls").document(callId).set(session.toMap()).await()
                Log.d(TAG, "Call initiated successfully in Firestore: $callId")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore call initiation error, continuing local session", e)
        }

        listenToActiveCall(callId)

        // Outgoing call ringing timeout (35 seconds)
        externalScope.launch {
            kotlinx.coroutines.delay(35000L)
            val current = _activeCall.value
            if (current != null && current.callId == callId && current.status == CallSession.STATUS_RINGING) {
                Log.d(TAG, "Outgoing call timed out after 35s with no answer: $callId")
                endCall(callId)
            }
        }

        return@withContext session
    }

    suspend fun acceptCall(callId: String) = withContext(Dispatchers.IO) {
        AppNotificationManager.stopIncomingCallRingtone(context)
        AppNotificationManager.dismissIncomingCallNotification(context)
        _incomingCall.value = null

        val current = _activeCall.value ?: CallSession(callId = callId)
        val connected = current.copy(
            status = CallSession.STATUS_CONNECTED,
            connectedAt = System.currentTimeMillis()
        )
        _activeCall.value = connected
        applyAudioHardwareSettings()

        try {
            val db = firestore
            db?.collection("active_calls")?.document(callId)?.update(
                mapOf(
                    "status" to CallSession.STATUS_CONNECTED,
                    "connectedAt" to System.currentTimeMillis()
                )
            )?.await()
        } catch (e: Exception) {
            Log.w(TAG, "Error accepting call in Firestore", e)
        }
    }

    suspend fun declineCall(callId: String) = withContext(Dispatchers.IO) {
        AppNotificationManager.stopIncomingCallRingtone(context)
        AppNotificationManager.dismissIncomingCallNotification(context)
        _incomingCall.value = null

        val current = _activeCall.value ?: _incomingCall.value
        val endedAtTime = System.currentTimeMillis()
        if (current != null && current.callId == callId) {
            _activeCall.value = current.copy(status = CallSession.STATUS_DECLINED, endedAt = endedAtTime)
        }

        try {
            val db = firestore
            db?.collection("active_calls")?.document(callId)?.update(
                mapOf(
                    "status" to CallSession.STATUS_DECLINED,
                    "endedAt" to endedAtTime
                )
            )?.await()
        } catch (e: Exception) {
            Log.w(TAG, "Error declining call in Firestore", e)
        }
    }

    suspend fun endCall(callId: String) = withContext(Dispatchers.IO) {
        AppNotificationManager.stopIncomingCallRingtone(context)
        AppNotificationManager.dismissIncomingCallNotification(context)
        _incomingCall.value = null

        val current = _activeCall.value
        val finalStatus = if (current != null && current.connectedAt == 0L) {
            CallSession.STATUS_MISSED
        } else {
            CallSession.STATUS_ENDED
        }
        val endedAtTime = System.currentTimeMillis()
        if (current != null && current.callId == callId) {
            _activeCall.value = current.copy(status = finalStatus, endedAt = endedAtTime)
        }

        try {
            val db = firestore
            db?.collection("active_calls")?.document(callId)?.update(
                mapOf(
                    "status" to finalStatus,
                    "endedAt" to endedAtTime
                )
            )?.await()
        } catch (e: Exception) {
            Log.w(TAG, "Error ending call in Firestore", e)
        }
    }

    fun dismissCallDialog() {
        AppNotificationManager.stopIncomingCallRingtone(context)
        _activeCall.value = null
        _incomingCall.value = null
        resetAudioHardwareSettings()
    }

    fun toggleMute() {
        val newMute = !_isMuted.value
        _isMuted.value = newMute
        try {
            audioManager?.isMicrophoneMute = newMute
        } catch (e: Exception) {
            Log.w(TAG, "Mute toggle failed: ${e.message}")
        }
    }

    fun toggleSpeaker() {
        val newSpeaker = !_isSpeakerOn.value
        _isSpeakerOn.value = newSpeaker
        try {
            audioManager?.isSpeakerphoneOn = newSpeaker
        } catch (e: Exception) {
            Log.w(TAG, "Speaker toggle failed: ${e.message}")
        }
    }

    private fun applyAudioHardwareSettings() {
        try {
            audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager?.isSpeakerphoneOn = _isSpeakerOn.value
            audioManager?.isMicrophoneMute = _isMuted.value
        } catch (e: Exception) {
            Log.w(TAG, "Audio hardware mode setup error: ${e.message}")
        }
    }

    private fun resetAudioHardwareSettings() {
        try {
            audioManager?.mode = AudioManager.MODE_NORMAL
            audioManager?.isMicrophoneMute = false
        } catch (e: Exception) {
            Log.w(TAG, "Audio reset error: ${e.message}")
        }
    }

    fun onCleared() {
        incomingCallListener?.remove()
        activeCallListener?.remove()
        AppNotificationManager.stopIncomingCallRingtone(context)
        resetAudioHardwareSettings()
    }
}

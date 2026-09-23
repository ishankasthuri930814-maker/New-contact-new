package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.ChatMessage
import com.example.data.model.DirectChatMessage
import com.example.data.model.UserProfile
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class ChatRepository(
    private val context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    // Community Broadcast Messages
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _selectedChannel = MutableStateFlow("general")
    val selectedChannel: StateFlow<String> = _selectedChannel.asStateFlow()

    // 1-on-1 Direct Private Messages
    private val _directMessages = MutableStateFlow<Map<String, List<DirectChatMessage>>>(emptyMap())
    val directMessages: StateFlow<Map<String, List<DirectChatMessage>>> = _directMessages.asStateFlow()

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                null
            }
        } catch (t: Throwable) {
            Log.w("ChatRepo", "Firestore not initialized", t)
            null
        }
    }

    private var communityListener: ListenerRegistration? = null
    private var directListener: ListenerRegistration? = null
    private val incomingDirectListeners = mutableListOf<ListenerRegistration>()
    private var currentActiveConversationId: String? = null
    private val appStartTime = System.currentTimeMillis() - 5000L
    private var lastNotifiedMessageTimestamp = System.currentTimeMillis()

    init {
        seedInitialSampleMessages()
        startRealtimeCommunityListener()
    }

    private fun seedInitialSampleMessages() {
        val initialList = listOf(
            ChatMessage(
                id = "init_1",
                senderId = "admin_police_support",
                senderName = "ශ්‍රී ලංකා පොලිස් තොරතුරු සහය (Admin)",
                senderEmail = "info@police.lk",
                senderAvatarIndex = 0,
                senderBadge = "පොලිස් සහකාර (Verified Support)",
                text = "ආයුබෝවන්! ශ්‍රී ලංකා පොලිස් සහ හදිසි සේවා සජීවී ප්‍රජා සංවාද මණ්ඩපය වෙත සාදරයෙන් පිළිගනිමු. ඕනෑම හදිසි අවශ්‍යතාවයක් හෝ විමසීමක් මෙහිදී සාකච්ඡා කළ හැක.",
                timestamp = System.currentTimeMillis() - 3600000L,
                channelId = "general",
                isEmergency = false
            ),
            ChatMessage(
                id = "init_2",
                senderId = "system_emergency_alert",
                senderName = "හදිසි ඇමතුම් මධ්‍යස්ථානය (119 Unit)",
                senderEmail = "emergency119@police.lk",
                senderAvatarIndex = 1,
                senderBadge = "119 Emergency Desk",
                text = "හදිසි අවස්ථාවකදී කරුණාකර 'හදිසි උපකාර' ටැබය තෝරා පණිවිඩයක් තබන්න හෝ ක්ෂණිකව 119 අමතන්න.",
                timestamp = System.currentTimeMillis() - 1800000L,
                channelId = "emergency_help",
                isEmergency = true
            ),
            ChatMessage(
                id = "init_3",
                senderId = "traffic_patrol",
                senderName = "රථවාහන මෙහෙයුම් ඒකකය (Traffic Division)",
                senderEmail = "traffic@police.lk",
                senderAvatarIndex = 2,
                senderBadge = "Traffic Safety Team",
                text = "ප්‍රධාන මාර්ග තදබද හෝ මාර්ග බාධක පිළිබඳ ක්ෂණික තොරතුරු 'මාර්ග / රථවාහන' නාලිකාවෙන් බෙදාගන්න.",
                timestamp = System.currentTimeMillis() - 900000L,
                channelId = "traffic_alerts",
                isEmergency = false
            )
        )
        _messages.value = initialList
    }

    fun setChannel(channelId: String) {
        _selectedChannel.value = channelId
    }

    fun startRealtimeCommunityListener() {
        try {
            val db = firestore ?: return
            communityListener?.remove()

            communityListener = db.collection("chat_messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limitToLast(120)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("ChatRepo", "Chat listener error", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                val data = doc.data ?: return@mapNotNull null
                                ChatMessage.fromMap(data).copy(id = doc.id)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (list.isNotEmpty()) {
                            _messages.value = list
                        }
                    }
                }
        } catch (t: Throwable) {
            Log.w("ChatRepo", "Could not attach Firestore chat listener", t)
        }
    }

    fun listenToDirectConversation(conversationId: String) {
        if (currentActiveConversationId == conversationId && directListener != null) return
        currentActiveConversationId = conversationId
        directListener?.remove()

        try {
            val db = firestore ?: return
            // Query by conversationId without composite orderBy to avoid index requirement
            directListener = db.collection("direct_chats")
                .whereEqualTo("conversationId", conversationId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("ChatRepo", "Direct chat listener error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            val data = doc.data ?: return@mapNotNull null
                            DirectChatMessage.fromMap(data).copy(id = doc.id)
                        }.sortedBy { it.timestamp }

                        val currentMap = _directMessages.value.toMutableMap()
                        currentMap[conversationId] = list
                        _directMessages.value = currentMap
                        Log.d("ChatRepo", "Live direct messages updated for $conversationId: ${list.size} messages")
                    }
                }
        } catch (t: Throwable) {
            Log.w("ChatRepo", "Error listening to direct messages", t)
        }
    }

    suspend fun sendMessage(
        userProfile: UserProfile,
        text: String,
        channelId: String = _selectedChannel.value,
        isEmergency: Boolean = false
    ): Boolean = withContext(Dispatchers.IO) {
        val cleanText = text.trim()
        if (cleanText.isBlank()) return@withContext false

        val senderId = userProfile.userId.ifBlank { userProfile.email }
        val msgId = UUID.randomUUID().toString()
        val resolvedSenderName = userProfile.displayName.takeIf { it.isNotBlank() && !it.equals("Citizen", ignoreCase = true) }
            ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.displayName?.takeIf { it.isNotBlank() }
            ?: userProfile.email.substringBefore("@").replaceFirstChar { it.uppercase() }.takeIf { it.isNotBlank() }
            ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "පරිශීලක (User)"

        val newMessage = ChatMessage(
            id = msgId,
            senderId = senderId,
            senderName = resolvedSenderName,
            senderEmail = userProfile.email,
            senderAvatarIndex = userProfile.avatarIndex,
            senderBadge = userProfile.badge,
            text = cleanText,
            timestamp = System.currentTimeMillis(),
            channelId = channelId,
            isEmergency = isEmergency
        )

        _messages.value = _messages.value + newMessage

        try {
            val db = firestore
            if (db != null) {
                db.collection("chat_messages").document(msgId).set(newMessage.toMap()).await()
                Log.d("ChatRepo", "Message successfully sent to Firestore")
                return@withContext true
            }
        } catch (t: Throwable) {
            Log.w("ChatRepo", "Firestore message send error, stored locally", t)
        }

        return@withContext true
    }

    suspend fun deleteCommunityMessage(messageId: String): Boolean = withContext(Dispatchers.IO) {
        _messages.value = _messages.value.filter { it.id != messageId }
        try {
            firestore?.collection("chat_messages")?.document(messageId)?.delete()?.await()
            Log.d("ChatRepo", "Deleted community message: $messageId")
            return@withContext true
        } catch (t: Throwable) {
            Log.w("ChatRepo", "Error deleting community message", t)
        }
        return@withContext true
    }

    suspend fun deleteMultipleCommunityMessages(messageIds: Collection<String>): Boolean = withContext(Dispatchers.IO) {
        if (messageIds.isEmpty()) return@withContext true
        _messages.value = _messages.value.filter { !messageIds.contains(it.id) }
        try {
            val db = firestore
            if (db != null) {
                for (id in messageIds) {
                    db.collection("chat_messages").document(id).delete()
                }
            }
            return@withContext true
        } catch (t: Throwable) {
            Log.w("ChatRepo", "Error deleting multiple community messages", t)
        }
        return@withContext true
    }

    suspend fun sendDirectMessage(
        sender: UserProfile,
        receiver: UserProfile,
        text: String
    ): Boolean = withContext(Dispatchers.IO) {
        val cleanText = text.trim()
        if (cleanText.isBlank()) return@withContext false

        val senderKey = sender.email.ifBlank { sender.userId }
        val receiverKey = receiver.email.ifBlank { receiver.userId }
        if (senderKey.isBlank() || receiverKey.isBlank()) return@withContext false

        val convId = DirectChatMessage.createConversationId(senderKey, receiverKey)
        val msgId = UUID.randomUUID().toString()

        val normSenderKey = DirectChatMessage.normalizeUserKey(senderKey)
        val normReceiverKey = DirectChatMessage.normalizeUserKey(receiverKey)

        val resolvedSenderName = sender.displayName.takeIf { it.isNotBlank() && !it.equals("Citizen", ignoreCase = true) }
            ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.displayName?.takeIf { it.isNotBlank() }
            ?: sender.email.substringBefore("@").replaceFirstChar { it.uppercase() }.takeIf { it.isNotBlank() }
            ?: "පරිශීලක (User)"

        val participants = listOfNotNull(
            normSenderKey,
            normReceiverKey,
            sender.email.takeIf { it.isNotBlank() }?.lowercase(),
            receiver.email.takeIf { it.isNotBlank() }?.lowercase(),
            sender.userId.takeIf { it.isNotBlank() },
            receiver.userId.takeIf { it.isNotBlank() }
        ).distinct()

        val newDirect = DirectChatMessage(
            id = msgId,
            conversationId = convId,
            senderId = normSenderKey,
            senderEmail = sender.email,
            senderUserId = sender.userId,
            senderName = resolvedSenderName,
            senderAvatarIndex = sender.avatarIndex,
            receiverId = normReceiverKey,
            receiverEmail = receiver.email,
            receiverUserId = receiver.userId,
            participants = participants,
            text = cleanText,
            timestamp = System.currentTimeMillis()
        )

        val currentMap = _directMessages.value.toMutableMap()
        val list = (currentMap[convId] ?: emptyList()) + newDirect
        currentMap[convId] = list
        _directMessages.value = currentMap

        try {
            val db = firestore
            if (db != null) {
                db.collection("direct_chats").document(msgId).set(newDirect.toMap()).await()
                Log.d("ChatRepo", "Direct message saved to Firestore for $convId")
                return@withContext true
            }
        } catch (t: Throwable) {
            Log.w("ChatRepo", "Direct message send error in Firestore", t)
        }
        return@withContext true
    }

    suspend fun logCallMessage(
        callId: String = "",
        callerKey: String,
        callerName: String,
        callerAvatarIndex: Int,
        receiverKey: String,
        callStatus: String,
        durationSeconds: Int,
        timestamp: Long = System.currentTimeMillis()
    ): Boolean = withContext(Dispatchers.IO) {
        val convId = DirectChatMessage.createConversationId(callerKey, receiverKey)
        val msgId = if (callId.isNotBlank()) "call_log_$callId" else ("call_" + UUID.randomUUID().toString())

        val textSummary = when (callStatus) {
            "CONNECTED" -> {
                val mins = durationSeconds / 60
                val secs = durationSeconds % 60
                val durStr = if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
                "📞 හඬ ඇමතුම (Voice Call) • කාලය: $durStr"
            }
            "DECLINED" -> "📞 ප්‍රතික්ෂේප කළ ඇමතුම (Declined Call)"
            else -> "📞 මඟහැරුණු ඇමතුම (Missed Call)"
        }

        val resolvedCallerName = callerName.takeIf { it.isNotBlank() && !it.equals("Citizen", ignoreCase = true) }
            ?: callerKey.substringBefore("@").replaceFirstChar { it.uppercase() }.ifBlank { "User" }

        val normCallerKey = DirectChatMessage.normalizeUserKey(callerKey)
        val normReceiverKey = DirectChatMessage.normalizeUserKey(receiverKey)

        val callLogMsg = DirectChatMessage(
            id = msgId,
            conversationId = convId,
            senderId = normCallerKey,
            senderName = resolvedCallerName,
            senderAvatarIndex = callerAvatarIndex,
            receiverId = normReceiverKey,
            participants = listOf(normCallerKey, normReceiverKey),
            text = textSummary,
            timestamp = timestamp,
            messageType = DirectChatMessage.TYPE_CALL_LOG,
            callDurationSeconds = durationSeconds,
            callStatus = callStatus
        )

        val currentMap = _directMessages.value.toMutableMap()
        val existingList = currentMap[convId] ?: emptyList()
        val updatedList = if (existingList.any { it.id == msgId }) {
            existingList.map { if (it.id == msgId) callLogMsg else it }
        } else {
            existingList + callLogMsg
        }
        currentMap[convId] = updatedList
        _directMessages.value = currentMap

        try {
            val db = firestore
            if (db != null) {
                db.collection("direct_chats").document(msgId).set(callLogMsg.toMap()).await()
                Log.d("ChatRepo", "Call log successfully recorded for $convId")
                return@withContext true
            }
        } catch (t: Throwable) {
            Log.w("ChatRepo", "Error recording call log in Firestore", t)
        }
        return@withContext true
    }

    suspend fun deleteDirectMessage(messageId: String, conversationId: String): Boolean = withContext(Dispatchers.IO) {
        val currentMap = _directMessages.value.toMutableMap()
        val list = currentMap[conversationId]?.filter { it.id != messageId } ?: emptyList()
        currentMap[conversationId] = list
        _directMessages.value = currentMap

        try {
            firestore?.collection("direct_chats")?.document(messageId)?.delete()?.await()
            Log.d("ChatRepo", "Deleted direct message: $messageId")
            return@withContext true
        } catch (t: Throwable) {
            Log.w("ChatRepo", "Error deleting direct message in Firestore", t)
        }
        return@withContext true
    }

    suspend fun deleteMultipleDirectMessages(messageIds: Collection<String>, conversationId: String): Boolean = withContext(Dispatchers.IO) {
        if (messageIds.isEmpty()) return@withContext true
        val currentMap = _directMessages.value.toMutableMap()
        val list = currentMap[conversationId]?.filter { !messageIds.contains(it.id) } ?: emptyList()
        currentMap[conversationId] = list
        _directMessages.value = currentMap

        try {
            val db = firestore
            if (db != null) {
                for (id in messageIds) {
                    db.collection("direct_chats").document(id).delete()
                }
            }
            return@withContext true
        } catch (t: Throwable) {
            Log.w("ChatRepo", "Error deleting multiple direct messages", t)
        }
        return@withContext true
    }

    suspend fun clearDirectConversation(conversationId: String): Boolean = withContext(Dispatchers.IO) {
        val currentMap = _directMessages.value.toMutableMap()
        val toDelete = currentMap[conversationId] ?: emptyList()
        currentMap[conversationId] = emptyList()
        _directMessages.value = currentMap

        try {
            val db = firestore
            if (db != null) {
                for (msg in toDelete) {
                    db.collection("direct_chats").document(msg.id).delete()
                }
            }
            return@withContext true
        } catch (t: Throwable) {
            Log.w("ChatRepo", "Error clearing direct conversation in Firestore", t)
        }
        return@withContext true
    }

    fun startListeningForIncomingDirectMessages(currentUserProfile: UserProfile) {
        incomingDirectListeners.forEach { it.remove() }
        incomingDirectListeners.clear()

        val myKeys = listOfNotNull(
            currentUserProfile.email.takeIf { it.isNotBlank() }?.let { DirectChatMessage.normalizeUserKey(it) },
            currentUserProfile.userId.takeIf { it.isNotBlank() }?.let { DirectChatMessage.normalizeUserKey(it) },
            currentUserProfile.email.takeIf { it.isNotBlank() }?.lowercase()?.trim(),
            currentUserProfile.userId.takeIf { it.isNotBlank() }?.trim()
        ).filter { it.isNotBlank() && it != "unknown_user" }.distinct()

        if (myKeys.isEmpty()) return

        try {
            val db = firestore ?: return
            val primaryKey = myKeys.first()

            val listener = db.collection("direct_chats")
                .whereEqualTo("receiverId", primaryKey)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    val newMessages = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        DirectChatMessage.fromMap(data).copy(id = doc.id)
                    }.filter { it.timestamp > appStartTime && !myKeys.contains(it.senderId) && it.messageType != DirectChatMessage.TYPE_CALL_LOG }

                    val latest = newMessages.maxByOrNull { it.timestamp }
                    if (latest != null && latest.timestamp > lastNotifiedMessageTimestamp) {
                        lastNotifiedMessageTimestamp = latest.timestamp
                        com.example.util.AppNotificationManager.playMessageNotificationSound(context)
                        com.example.util.AppNotificationManager.showDirectMessageNotification(
                            context = context,
                            senderName = latest.senderName,
                            messageText = latest.text,
                            conversationId = latest.conversationId
                        )
                    }
                }
            incomingDirectListeners.add(listener)
        } catch (e: Exception) {
            Log.w("ChatRepo", "Error listening for incoming user direct messages", e)
        }
    }

    fun onCleared() {
        communityListener?.remove()
        directListener?.remove()
        incomingDirectListeners.forEach { it.remove() }
        incomingDirectListeners.clear()
    }
}

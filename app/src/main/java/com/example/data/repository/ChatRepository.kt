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
    private var incomingDirectMessagesListener: ListenerRegistration? = null
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
                .limitToLast(100)
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
            // Query by conversationId without composite orderBy to avoid Firestore index requirement
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
        val newMessage = ChatMessage(
            id = msgId,
            senderId = senderId,
            senderName = userProfile.displayName.ifBlank { "Citizen" },
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

    suspend fun sendDirectMessage(
        sender: UserProfile,
        receiverUserId: String,
        text: String
    ): Boolean = withContext(Dispatchers.IO) {
        val cleanText = text.trim()
        if (cleanText.isBlank() || receiverUserId.isBlank()) return@withContext false

        val senderKey = sender.email.ifBlank { sender.userId }
        val convId = DirectChatMessage.createConversationId(senderKey, receiverUserId)
        val msgId = UUID.randomUUID().toString()

        val newDirect = DirectChatMessage(
            id = msgId,
            conversationId = convId,
            senderId = DirectChatMessage.normalizeUserKey(senderKey),
            senderName = sender.displayName.ifBlank { "Citizen" },
            senderAvatarIndex = sender.avatarIndex,
            receiverId = DirectChatMessage.normalizeUserKey(receiverUserId),
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

    fun startListeningForIncomingDirectMessages(currentUserProfile: UserProfile) {
        val myKey = DirectChatMessage.normalizeUserKey(currentUserProfile.email.ifBlank { currentUserProfile.userId })
        if (myKey.isBlank() || myKey == "unknown_user") return
        incomingDirectMessagesListener?.remove()

        try {
            val db = firestore ?: return
            incomingDirectMessagesListener = db.collection("direct_chats")
                .whereEqualTo("receiverId", myKey)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    val newMessages = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        DirectChatMessage.fromMap(data).copy(id = doc.id)
                    }.filter { it.timestamp > appStartTime && it.senderId != myKey }

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
        } catch (e: Exception) {
            Log.w("ChatRepo", "Error listening for incoming user direct messages", e)
        }
    }

    fun onCleared() {
        communityListener?.remove()
        directListener?.remove()
        incomingDirectMessagesListener?.remove()
    }
}

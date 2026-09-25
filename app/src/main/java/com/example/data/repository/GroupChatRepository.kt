package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.GroupChat
import com.example.data.model.GroupChatMessage
import com.example.data.model.UserProfile
import com.example.util.AppNotificationManager
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class GroupChatRepository(
    private val context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    companion object {
        private const val TAG = "GroupChatRepo"
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else null
        } catch (e: Exception) {
            Log.w(TAG, "Firestore initialization error", e)
            null
        }
    }

    private val _userGroups = MutableStateFlow<List<GroupChat>>(emptyList())
    val userGroups: StateFlow<List<GroupChat>> = _userGroups.asStateFlow()

    private val _currentGroupMessages = MutableStateFlow<List<GroupChatMessage>>(emptyList())
    val currentGroupMessages: StateFlow<List<GroupChatMessage>> = _currentGroupMessages.asStateFlow()

    private var groupsListener: ListenerRegistration? = null
    private var activeGroupMessagesListener: ListenerRegistration? = null
    private var currentActiveGroupId: String? = null

    /**
     * Start listening to all groups where current user is a participant.
     */
    fun startListeningToUserGroups(userProfile: UserProfile) {
        val db = firestore ?: return
        val myKeys = listOfNotNull(
            userProfile.email.takeIf { it.isNotBlank() }?.lowercase()?.trim(),
            userProfile.userId.takeIf { it.isNotBlank() }?.trim(),
            userProfile.phoneNumber.takeIf { it.isNotBlank() }?.trim()
        ).distinct()

        if (myKeys.isEmpty()) return

        groupsListener?.remove()
        try {
            // Listen for groups containing user in members list
            groupsListener = db.collection("groups")
                .whereArrayContainsAny("members", myKeys)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Groups snapshot error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            val data = doc.data ?: return@mapNotNull null
                            GroupChat.fromMap(data)
                        }.sortedByDescending { it.lastMessageTs }
                        _userGroups.value = list
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start user groups listener", e)
        }
    }

    /**
     * Start listening to messages in a specific group.
     */
    fun startListeningToGroupMessages(groupId: String) {
        val db = firestore ?: return
        if (currentActiveGroupId == groupId && activeGroupMessagesListener != null) return

        activeGroupMessagesListener?.remove()
        currentActiveGroupId = groupId
        _currentGroupMessages.value = emptyList()

        try {
            activeGroupMessagesListener = db.collection("groups")
                .document(groupId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(250)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Group messages snapshot error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val messages = snapshot.documents.mapNotNull { doc ->
                            val data = doc.data ?: return@mapNotNull null
                            GroupChatMessage.fromMap(data)
                        }
                        _currentGroupMessages.value = messages
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to listen to group messages", e)
        }
    }

    fun stopListeningToGroupMessages() {
        activeGroupMessagesListener?.remove()
        activeGroupMessagesListener = null
        currentActiveGroupId = null
        _currentGroupMessages.value = emptyList()
    }

    /**
     * Create a new group chat.
     */
    suspend fun createGroup(
        name: String,
        description: String,
        creator: UserProfile,
        selectedMembers: List<UserProfile>
    ): GroupChat? = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext null
        try {
            val groupId = UUID.randomUUID().toString()
            val allMembers = (selectedMembers + creator).distinctBy { it.userId.ifBlank { it.email } }

            val memberKeys = allMembers.flatMap { user ->
                listOfNotNull(
                    user.email.takeIf { it.isNotBlank() }?.lowercase()?.trim(),
                    user.userId.takeIf { it.isNotBlank() }?.trim(),
                    user.phoneNumber.takeIf { it.isNotBlank() }?.trim()
                )
            }.distinct()

            val memberNames = allMembers.associate { user ->
                val key = user.userId.ifBlank { user.email }
                key to user.displayName
            }

            val group = GroupChat(
                id = groupId,
                name = name.trim().ifBlank { "Police Community Group" },
                description = description.trim(),
                creatorId = creator.userId.ifBlank { creator.email },
                creatorName = creator.displayName,
                members = memberKeys,
                memberNames = memberNames,
                createdAt = System.currentTimeMillis(),
                lastMessage = "Group created by ${creator.displayName}",
                lastMessageSender = creator.displayName,
                lastMessageTs = System.currentTimeMillis()
            )

            db.collection("groups").document(groupId).set(group.toMap()).await()

            // Send initial welcome message
            val welcomeMsg = GroupChatMessage(
                id = UUID.randomUUID().toString(),
                groupId = groupId,
                senderId = creator.userId.ifBlank { creator.email },
                senderName = creator.displayName,
                senderPhotoUrl = creator.profilePhotoUrl,
                senderAvatarIndex = creator.avatarIndex,
                text = "🎉 ${creator.displayName} විසින් මෙම කණ්ඩායම නිර්මාණය කරන ලදී (Group Created).",
                timestamp = System.currentTimeMillis()
            )
            db.collection("groups").document(groupId).collection("messages").document(welcomeMsg.id).set(welcomeMsg.toMap()).await()

            return@withContext group
        } catch (e: Exception) {
            Log.e(TAG, "Error creating group chat", e)
            null
        }
    }

    /**
     * Send a message to a group (supports Text, Photos, Links).
     */
    suspend fun sendGroupMessage(
        groupId: String,
        sender: UserProfile,
        text: String,
        imageUrl: String = "",
        linkUrl: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false
        try {
            val msgId = UUID.randomUUID().toString()
            val msg = GroupChatMessage(
                id = msgId,
                groupId = groupId,
                senderId = sender.userId.ifBlank { sender.email },
                senderName = sender.displayName,
                senderPhotoUrl = sender.profilePhotoUrl,
                senderAvatarIndex = sender.avatarIndex,
                text = text.trim(),
                imageUrl = imageUrl.trim(),
                linkUrl = linkUrl.trim(),
                timestamp = System.currentTimeMillis()
            )

            // 1. Add to messages sub-collection
            db.collection("groups").document(groupId).collection("messages").document(msgId).set(msg.toMap()).await()

            // 2. Update parent group's last message info
            val displayPreview = if (imageUrl.isNotBlank()) "📷 Photo" else text.take(60)
            db.collection("groups").document(groupId).update(
                mapOf(
                    "lastMessage" to displayPreview,
                    "lastMessageSender" to sender.displayName,
                    "lastMessageTs" to System.currentTimeMillis()
                )
            ).await()

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error sending group message", e)
            false
        }
    }
}

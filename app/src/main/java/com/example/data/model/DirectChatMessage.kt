package com.example.data.model

data class DirectChatMessage(
    val id: String = "",
    val conversationId: String = "", // e.g. userA__userB (normalized)
    val senderId: String = "",
    val senderEmail: String = "",
    val senderUserId: String = "",
    val senderName: String = "",
    val senderAvatarIndex: Int = 0,
    val receiverId: String = "",
    val receiverEmail: String = "",
    val receiverUserId: String = "",
    val participants: List<String> = emptyList(),
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val messageType: String = TYPE_TEXT, // "TEXT" or "CALL_LOG"
    val callDurationSeconds: Int = 0,
    val callStatus: String = "" // "CONNECTED", "MISSED", "DECLINED"
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "conversationId" to conversationId,
            "senderId" to senderId,
            "senderEmail" to senderEmail,
            "senderUserId" to senderUserId,
            "senderName" to senderName,
            "senderAvatarIndex" to senderAvatarIndex,
            "receiverId" to receiverId,
            "receiverEmail" to receiverEmail,
            "receiverUserId" to receiverUserId,
            "participants" to participants,
            "text" to text,
            "timestamp" to timestamp,
            "isRead" to isRead,
            "messageType" to messageType,
            "callDurationSeconds" to callDurationSeconds,
            "callStatus" to callStatus
        )
    }

    companion object {
        const val TYPE_TEXT = "TEXT"
        const val TYPE_CALL_LOG = "CALL_LOG"

        fun normalizeUserKey(raw: String): String {
            return raw.trim().lowercase()
                .replace(".", "_")
                .replace("@", "_at_")
                .ifBlank { "unknown_user" }
        }

        fun createConversationId(userA: String, userB: String): String {
            val keyA = normalizeUserKey(userA)
            val keyB = normalizeUserKey(userB)
            val list = listOf(keyA, keyB).sorted()
            return "${list[0]}__${list[1]}"
        }

        fun fromMap(map: Map<String, Any?>): DirectChatMessage {
            @Suppress("UNCHECKED_CAST")
            val rawParticipants = map["participants"] as? List<String> ?: emptyList()
            return DirectChatMessage(
                id = map["id"] as? String ?: "",
                conversationId = map["conversationId"] as? String ?: "",
                senderId = map["senderId"] as? String ?: "",
                senderEmail = map["senderEmail"] as? String ?: "",
                senderUserId = map["senderUserId"] as? String ?: "",
                senderName = map["senderName"] as? String ?: "",
                senderAvatarIndex = (map["senderAvatarIndex"] as? Number)?.toInt() ?: 0,
                receiverId = map["receiverId"] as? String ?: "",
                receiverEmail = map["receiverEmail"] as? String ?: "",
                receiverUserId = map["receiverUserId"] as? String ?: "",
                participants = rawParticipants,
                text = map["text"] as? String ?: "",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                isRead = map["isRead"] as? Boolean ?: false,
                messageType = map["messageType"] as? String ?: TYPE_TEXT,
                callDurationSeconds = (map["callDurationSeconds"] as? Number)?.toInt() ?: 0,
                callStatus = map["callStatus"] as? String ?: ""
            )
        }
    }
}

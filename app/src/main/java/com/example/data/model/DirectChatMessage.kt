package com.example.data.model

data class DirectChatMessage(
    val id: String = "",
    val conversationId: String = "", // e.g. userA__userB (normalized)
    val senderId: String = "",
    val senderName: String = "",
    val senderAvatarIndex: Int = 0,
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "conversationId" to conversationId,
            "senderId" to senderId,
            "senderName" to senderName,
            "senderAvatarIndex" to senderAvatarIndex,
            "receiverId" to receiverId,
            "text" to text,
            "timestamp" to timestamp,
            "isRead" to isRead
        )
    }

    companion object {
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
            return DirectChatMessage(
                id = map["id"] as? String ?: "",
                conversationId = map["conversationId"] as? String ?: "",
                senderId = map["senderId"] as? String ?: "",
                senderName = map["senderName"] as? String ?: "",
                senderAvatarIndex = (map["senderAvatarIndex"] as? Number)?.toInt() ?: 0,
                receiverId = map["receiverId"] as? String ?: "",
                text = map["text"] as? String ?: "",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                isRead = map["isRead"] as? Boolean ?: false
            )
        }
    }
}

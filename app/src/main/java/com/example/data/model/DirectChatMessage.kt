package com.example.data.model

data class DirectChatMessage(
    val id: String = "",
    val conversationId: String = "", // e.g. min(userId1, userId2) + "_" + max(userId1, userId2)
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
        fun createConversationId(userA: String, userB: String): String {
            val list = listOf(userA.trim().lowercase(), userB.trim().lowercase()).sorted()
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

package com.example.data.model

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "සාමාජිකයා (Member)",
    val senderEmail: String = "",
    val senderAvatarIndex: Int = 0,
    val senderBadge: String = "Verified Citizen",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val channelId: String = "general",
    val isEmergency: Boolean = false,
    val likeCount: Int = 0
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "senderId" to senderId,
            "senderName" to senderName,
            "senderEmail" to senderEmail,
            "senderAvatarIndex" to senderAvatarIndex,
            "senderBadge" to senderBadge,
            "text" to text,
            "timestamp" to timestamp,
            "channelId" to channelId,
            "isEmergency" to isEmergency,
            "likeCount" to likeCount
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): ChatMessage {
            val rawName = map["senderName"] as? String ?: ""
            val rawEmail = map["senderEmail"] as? String ?: ""
            val resolvedName = if (rawName.isNotBlank() && !rawName.equals("Citizen", ignoreCase = true)) {
                rawName
            } else if (rawEmail.isNotBlank()) {
                rawEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
            } else {
                "සාමාජිකයා (Member)"
            }

            return ChatMessage(
                id = map["id"] as? String ?: "",
                senderId = map["senderId"] as? String ?: "",
                senderName = resolvedName,
                senderEmail = rawEmail,
                senderAvatarIndex = (map["senderAvatarIndex"] as? Number)?.toInt() ?: 0,
                senderBadge = map["senderBadge"] as? String ?: "Verified Citizen",
                text = map["text"] as? String ?: "",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                channelId = map["channelId"] as? String ?: "general",
                isEmergency = map["isEmergency"] as? Boolean ?: false,
                likeCount = (map["likeCount"] as? Number)?.toInt() ?: 0
            )
        }
    }
}

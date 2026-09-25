package com.example.data.model

data class GroupChatMessage(
    val id: String = "",
    val groupId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhotoUrl: String = "",
    val senderAvatarIndex: Int = 0,
    val text: String = "",
    val imageUrl: String = "",
    val linkUrl: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "groupId" to groupId,
            "senderId" to senderId,
            "senderName" to senderName,
            "senderPhotoUrl" to senderPhotoUrl,
            "senderAvatarIndex" to senderAvatarIndex,
            "text" to text,
            "imageUrl" to imageUrl,
            "linkUrl" to linkUrl,
            "timestamp" to timestamp
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): GroupChatMessage {
            return GroupChatMessage(
                id = map["id"] as? String ?: "",
                groupId = map["groupId"] as? String ?: "",
                senderId = map["senderId"] as? String ?: "",
                senderName = map["senderName"] as? String ?: "Member",
                senderPhotoUrl = map["senderPhotoUrl"] as? String ?: "",
                senderAvatarIndex = (map["senderAvatarIndex"] as? Number)?.toInt() ?: 0,
                text = map["text"] as? String ?: "",
                imageUrl = map["imageUrl"] as? String ?: "",
                linkUrl = map["linkUrl"] as? String ?: "",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

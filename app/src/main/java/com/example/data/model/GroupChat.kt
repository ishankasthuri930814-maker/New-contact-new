package com.example.data.model

data class GroupChat(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val iconUrl: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val members: List<String> = emptyList(), // list of user normalized keys or ids
    val memberNames: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastMessage: String = "",
    val lastMessageSender: String = "",
    val lastMessageTs: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "description" to description,
            "iconUrl" to iconUrl,
            "creatorId" to creatorId,
            "creatorName" to creatorName,
            "members" to members,
            "memberNames" to memberNames,
            "createdAt" to createdAt,
            "lastMessage" to lastMessage,
            "lastMessageSender" to lastMessageSender,
            "lastMessageTs" to lastMessageTs
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): GroupChat {
            @Suppress("UNCHECKED_CAST")
            val membersList = (map["members"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val memberNamesMap = (map["memberNames"] as? Map<String, *>)?.mapValues { it.value?.toString() ?: "" } ?: emptyMap()

            return GroupChat(
                id = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "Group Chat",
                description = map["description"] as? String ?: "",
                iconUrl = map["iconUrl"] as? String ?: "",
                creatorId = map["creatorId"] as? String ?: "",
                creatorName = map["creatorName"] as? String ?: "",
                members = membersList,
                memberNames = memberNamesMap,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                lastMessage = map["lastMessage"] as? String ?: "",
                lastMessageSender = map["lastMessageSender"] as? String ?: "",
                lastMessageTs = (map["lastMessageTs"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

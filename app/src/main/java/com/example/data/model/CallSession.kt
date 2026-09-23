package com.example.data.model

data class CallSession(
    val callId: String = "",
    val callerId: String = "",
    val callerEmail: String = "",
    val callerUserId: String = "",
    val callerName: String = "Citizen",
    val callerAvatarIndex: Int = 0,
    val callerPhone: String = "",
    val receiverId: String = "",
    val receiverEmail: String = "",
    val receiverUserId: String = "",
    val receiverName: String = "Citizen",
    val receiverAvatarIndex: Int = 0,
    val receiverPhone: String = "",
    val targetKeys: List<String> = emptyList(),
    val status: String = STATUS_RINGING,
    val callType: String = "VOICE",
    val startedAt: Long = System.currentTimeMillis(),
    val connectedAt: Long = 0L,
    val endedAt: Long = 0L
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "callId" to callId,
            "callerId" to callerId,
            "callerEmail" to callerEmail,
            "callerUserId" to callerUserId,
            "callerName" to callerName,
            "callerAvatarIndex" to callerAvatarIndex,
            "callerPhone" to callerPhone,
            "receiverId" to receiverId,
            "receiverEmail" to receiverEmail,
            "receiverUserId" to receiverUserId,
            "receiverName" to receiverName,
            "receiverAvatarIndex" to receiverAvatarIndex,
            "receiverPhone" to receiverPhone,
            "targetKeys" to targetKeys,
            "status" to status,
            "callType" to callType,
            "startedAt" to startedAt,
            "connectedAt" to connectedAt,
            "endedAt" to endedAt
        )
    }

    companion object {
        const val STATUS_RINGING = "RINGING"
        const val STATUS_CONNECTED = "CONNECTED"
        const val STATUS_ENDED = "ENDED"
        const val STATUS_DECLINED = "DECLINED"
        const val STATUS_MISSED = "MISSED"

        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any?>): CallSession {
            return CallSession(
                callId = map["callId"] as? String ?: "",
                callerId = map["callerId"] as? String ?: "",
                callerEmail = map["callerEmail"] as? String ?: "",
                callerUserId = map["callerUserId"] as? String ?: "",
                callerName = map["callerName"] as? String ?: "Citizen",
                callerAvatarIndex = (map["callerAvatarIndex"] as? Number)?.toInt() ?: 0,
                callerPhone = map["callerPhone"] as? String ?: "",
                receiverId = map["receiverId"] as? String ?: "",
                receiverEmail = map["receiverEmail"] as? String ?: "",
                receiverUserId = map["receiverUserId"] as? String ?: "",
                receiverName = map["receiverName"] as? String ?: "Citizen",
                receiverAvatarIndex = (map["receiverAvatarIndex"] as? Number)?.toInt() ?: 0,
                receiverPhone = map["receiverPhone"] as? String ?: "",
                targetKeys = (map["targetKeys"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                status = map["status"] as? String ?: STATUS_RINGING,
                callType = map["callType"] as? String ?: "VOICE",
                startedAt = (map["startedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                connectedAt = (map["connectedAt"] as? Number)?.toLong() ?: 0L,
                endedAt = (map["endedAt"] as? Number)?.toLong() ?: 0L
            )
        }
    }
}

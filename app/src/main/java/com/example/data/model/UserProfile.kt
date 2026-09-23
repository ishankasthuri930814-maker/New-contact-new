package com.example.data.model

data class UserProfile(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val phoneNumber: String = "",
    val district: String = "Colombo",
    val emergencyNote: String = "",
    val avatarIndex: Int = 0,
    val badge: String = "සත්‍යාපිත පුරවැසියා (Verified Citizen)",
    val joinedTimestamp: Long = System.currentTimeMillis(),
    val isVerified: Boolean = true,
    val isPhonePublic: Boolean = false,
    val allowDirectMessages: Boolean = true,
    val allowDirectCalls: Boolean = true
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "email" to email,
            "displayName" to displayName,
            "phoneNumber" to phoneNumber,
            "district" to district,
            "emergencyNote" to emergencyNote,
            "avatarIndex" to avatarIndex,
            "badge" to badge,
            "joinedTimestamp" to joinedTimestamp,
            "isVerified" to isVerified,
            "isPhonePublic" to isPhonePublic,
            "allowDirectMessages" to allowDirectMessages,
            "allowDirectCalls" to allowDirectCalls
        )
    }

    /**
     * Sanitized public view of this profile to protect sensitive information (email, medical note, private phone).
     */
    fun toPublicSafeProfile(): UserProfile {
        return copy(
            email = "", // Hide email completely from others
            emergencyNote = "", // Hide private medical/emergency note
            phoneNumber = if (isPhonePublic) phoneNumber else "" // Only show phone if explicitly allowed
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): UserProfile {
            return UserProfile(
                userId = map["userId"] as? String ?: "",
                email = map["email"] as? String ?: "",
                displayName = map["displayName"] as? String ?: "",
                phoneNumber = map["phoneNumber"] as? String ?: "",
                district = map["district"] as? String ?: "Colombo",
                emergencyNote = map["emergencyNote"] as? String ?: "",
                avatarIndex = (map["avatarIndex"] as? Number)?.toInt() ?: 0,
                badge = map["badge"] as? String ?: "සත්‍යාපිත පුරවැසියා (Verified Citizen)",
                joinedTimestamp = (map["joinedTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                isVerified = map["isVerified"] as? Boolean ?: true,
                isPhonePublic = map["isPhonePublic"] as? Boolean ?: false,
                allowDirectMessages = map["allowDirectMessages"] as? Boolean ?: true,
                allowDirectCalls = map["allowDirectCalls"] as? Boolean ?: true
            )
        }
    }
}

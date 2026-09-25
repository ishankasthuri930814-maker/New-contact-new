package com.example.data.model

data class DeviceContactMatch(
    val name: String = "",
    val phoneNumber: String = "",
    val isAppUser: Boolean = false,
    val registeredProfile: UserProfile? = null
)

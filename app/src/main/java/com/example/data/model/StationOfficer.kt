package com.example.data.model

data class StationOfficer(
    val id: String,
    val name: String,
    val rank: String,
    val designation: String,
    val station: String,
    val primaryPhone: String,
    val secondaryPhone: String = "",
    val email: String = ""
) {
    val distinctPhones: List<String>
        get() = com.example.util.PhoneUtils.getDistinctPhones(primaryPhone, secondaryPhone)

    val cleanEmail: String
        get() = com.example.util.PhoneUtils.cleanEmail(email)
}

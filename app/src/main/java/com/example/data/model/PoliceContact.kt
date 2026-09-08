package com.example.data.model

data class PoliceContact(
    val id: String,
    val stationOrDesignation: String,
    val rank: String = "",
    val officerName: String = "",
    val generalPhone: String = "",
    val mobilePhone: String = "",
    val officePhone2: String = "",
    val officePhone3: String = "",
    val pvtNumber: String = "",
    val fax: String = "",
    val email: String = "",
    val oicTraffic: String = "",
    val oicCrime: String = "",
    val oicVice: String = "",
    val oicCommunityPolicing: String = "",
    val locationCoordinates: String = "",
    val locationAddress: String = "",
    val category: ContactCategory = ContactCategory.POLICE,
    val isFavorite: Boolean = false
)

enum class ContactCategory(val displayName: String, val sinhalaName: String, val iconRes: String) {
    POLICE("Police Contacts", "පොලිස් ඇමතුම්", "local_police"),
    ALL("All", "සියල්ල", "all"),
    EMERGENCY("Emergency", "හදිසි ඇමතුම්", "emergency"),
    DIVISIONS("Divisions", "කොට්ඨාස", "domain"),
    RANGES("DIG Ranges", "කලාප", "shield"),
    SENIOR_OFFICERS("Senior Officers", "ජ්‍යෙෂ්ඨ නිලධාරීන්", "badge"),
    FAVORITES("Favorites", "ප්‍රියතම", "star"),
    FIRE_STATIONS("Fire Service", "ගිනි නිවීම් සේවා", "fire"),
    SHORT_CODES("Short Codes", "කෙටි සංකේත", "dialpad"),
    HOSPITALS("Hospitals", "රෝහල්", "hospital"),
    GOVT_SERVICES("Govt & Depts", "රජයේ සේවා", "account_balance"),
    TRAVEL("Travel & Transport", "ගමන් බිමන්", "directions_bus")
}

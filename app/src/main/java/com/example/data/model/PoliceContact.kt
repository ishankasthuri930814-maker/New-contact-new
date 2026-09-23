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

enum class ContactCategory(
    val displayName: String,
    val sinhalaName: String,
    val tamilName: String,
    val iconRes: String
) {
    POLICE("Police Contacts", "පොලිස් ඇමතුම්", "காவல்துறை தொடர்புகள்", "local_police"),
    ALL("All", "සියල්ල", "அனைத்தும்", "all"),
    EMERGENCY("Emergency", "හදිසි ඇමතුම්", "அவசர அழைப்புகள்", "emergency"),
    DIVISIONS("Divisions", "කොට්ඨාස", "பிரிவுகள்", "domain"),
    RANGES("DIG Ranges", "කලාප", "டிஐஜி வலயங்கள்", "shield"),
    SENIOR_OFFICERS("Senior Officers", "ජ්‍යෙෂ්ඨ නිලධාරීන්", "சிரேஷ்ட அதிகாரிகள்", "badge"),
    FAVORITES("Favorites", "ප්‍රියතම", "விருப்பமானவை", "star"),
    FIRE_STATIONS("Fire Service", "ගිනි නිවීම් සේවා", "தீயணைப்பு சேவை", "fire"),
    SHORT_CODES("Short Codes", "කෙටි සංකේත", "குறுக்கு எண்கள்", "dialpad"),
    HOSPITALS("Hospitals", "රෝහල්", "வைத்தியசாலைகள்", "hospital"),
    GOVT_SERVICES("Govt & Depts", "රජයේ සේවා", "அரசு சேவைகள்", "account_balance"),
    TRAVEL("Travel & Transport", "ගමන් බිමන්", "பயணம் மற்றும் போக்குவரத்து", "directions_bus");

    fun getLocalizedName(lang: com.example.util.AppLanguage): String {
        return when (lang) {
            com.example.util.AppLanguage.SINHALA -> "$displayName ($sinhalaName)"
            com.example.util.AppLanguage.TAMIL -> "$displayName ($tamilName)"
            com.example.util.AppLanguage.ENGLISH -> displayName
        }
    }
}

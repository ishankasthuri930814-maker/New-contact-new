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

fun PoliceContact.sanitized(): PoliceContact {
    var station = this.stationOrDesignation.trim()
    var r = this.rank.trim()
    var officer = this.officerName.trim()
    var genPhone = this.generalPhone.trim()
    var mobPhone = this.mobilePhone.trim()

    val isNumericStation = station.matches(Regex("^[0-9\\s\\-\\.\\#]+$"))
    val isPhoneRegex = Regex("^[0-9\\+\\-\\s\\(\\)\\.\\/]{7,}$")

    // 1. If station title is purely numeric (serial number/ID from sheet), swap with rank or officerName if valid text
    if (isNumericStation) {
        if (r.isNotBlank() && !r.matches(isPhoneRegex)) {
            station = r
            r = ""
        } else if (officer.isNotBlank() && !officer.matches(isPhoneRegex)) {
            station = officer
            officer = ""
        }
    }

    // 2. If officerName is actually a phone number, move to phone field
    if (officer.matches(isPhoneRegex)) {
        if (genPhone.isBlank()) {
            genPhone = officer
        } else if (mobPhone.isBlank() && officer != genPhone) {
            mobPhone = officer
        }
        officer = ""
    }

    // 3. If rank is actually a phone number, move to phone field
    if (r.matches(isPhoneRegex)) {
        if (genPhone.isBlank()) {
            genPhone = r
        } else if (mobPhone.isBlank() && r != genPhone) {
            mobPhone = r
        }
        r = ""
    }

    return this.copy(
        stationOrDesignation = station,
        rank = r,
        officerName = officer,
        generalPhone = genPhone,
        mobilePhone = mobPhone
    )
}

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

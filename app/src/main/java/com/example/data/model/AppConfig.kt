package com.example.data.model

import androidx.compose.ui.graphics.Color

data class AppConfig(
    val theme: AppThemeConfig = AppThemeConfig(),
    val header: AppHeaderConfig = AppHeaderConfig(),
    val quickActionsTitle: String = "⚡ Quick Emergency Hotlines / හදිසි ඇමතුම්",
    val quickActions: List<QuickActionConfig> = defaultQuickActions,
    val features: AppFeaturesConfig = AppFeaturesConfig()
)

data class AppThemeConfig(
    val primaryColorHex: String = "#0D2240",
    val accentColorHex: String = "#F59E0B",
    val emergencyRedHex: String = "#DC2626",
    val headerTextColorHex: String = "#FFFFFF"
) {
    fun getPrimaryColor(): Color = parseColorSafe(primaryColorHex, Color(0xFF0D2240))
    fun getAccentColor(): Color = parseColorSafe(accentColorHex, Color(0xFFF59E0B))
    fun getEmergencyRed(): Color = parseColorSafe(emergencyRedHex, Color(0xFFDC2626))
    fun getHeaderTextColor(): Color = parseColorSafe(headerTextColorHex, Color.White)
}

data class AppHeaderConfig(
    val badgeText: String = "OFFICIAL DIRECTORY",
    val title: String = "Sri Lanka Police Directory",
    val subtitle: String = "ශ්‍රී ලංකා පොලිස් නිල ඇමතුම් සහ විද්‍යුත් තැපැල් නාමාවලිය",
    val emergencyBadgeText: String = "119 EMERGENCY",
    val emergencyBadgeNumber: String = "119",
    val bannerImageUrl: String = ""
)

data class QuickActionConfig(
    val id: String,
    val title: String,
    val subtitle: String,
    val phoneNumber: String,
    val iconType: String = "call", // call, warning, police, medical, fire, shield
    val badgeColorHex: String = "#0D2240",
    val enabled: Boolean = true
) {
    fun getBadgeColor(): Color = parseColorSafe(badgeColorHex, Color(0xFF0D2240))
}

data class AppFeaturesConfig(
    val showEmergencyBanner: Boolean = true,
    val showQuickActions: Boolean = true,
    val showCategoryTabs: Boolean = true,
    val showGpsMap: Boolean = true,
    val showFloatingSos: Boolean = true,
    val showAnnouncementTicker: Boolean = false,
    val announcementTickerText: String = ""
)

private fun parseColorSafe(hex: String, fallback: Color): Color {
    return try {
        val clean = hex.trim().removePrefix("#")
        when (clean.length) {
            6 -> Color(android.graphics.Color.parseColor("#$clean"))
            8 -> Color(android.graphics.Color.parseColor("#$clean"))
            else -> fallback
        }
    } catch (_: Exception) {
        fallback
    }
}

val defaultQuickActions = listOf(
    QuickActionConfig(
        id = "119",
        title = "119",
        subtitle = "Police Hotline",
        phoneNumber = "119",
        iconType = "warning",
        badgeColorHex = "#DC2626"
    ),
    QuickActionConfig(
        id = "118",
        title = "118",
        subtitle = "National Hotline",
        phoneNumber = "118",
        iconType = "call",
        badgeColorHex = "#0D2240"
    ),
    QuickActionConfig(
        id = "1990",
        title = "1990",
        subtitle = "Suwa Seriya",
        phoneNumber = "1990",
        iconType = "medical",
        badgeColorHex = "#059669"
    ),
    QuickActionConfig(
        id = "hq",
        title = "Police HQ",
        subtitle = "0112421111",
        phoneNumber = "0112421111",
        iconType = "police",
        badgeColorHex = "#D97706"
    )
)

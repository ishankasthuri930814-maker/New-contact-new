package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.policedirectory.zxklm.R
import com.example.data.model.AppConfig
import com.example.data.model.QuickActionConfig
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.PoliceGold
import com.example.ui.theme.PoliceNavy

@Composable
fun EmergencyHeader(
    onEmergencyCall: (String) -> Unit,
    modifier: Modifier = Modifier,
    appConfig: AppConfig = AppConfig(),
    selectedLanguage: com.example.util.AppLanguage = com.example.util.AppLanguage.SINHALA
) {
    val theme = appConfig.theme
    val header = appConfig.header
    val primaryColor = theme.getPrimaryColor()
    val accentColor = theme.getAccentColor()
    val emergencyRed = theme.getEmergencyRed()
    val headerTextColor = theme.getHeaderTextColor()

    val badgeLabel = when (selectedLanguage) {
        com.example.util.AppLanguage.SINHALA -> "නිල නාමාවලිය"
        com.example.util.AppLanguage.TAMIL -> "அதிகாரபூர்வ விபரம்"
        com.example.util.AppLanguage.ENGLISH -> header.badgeText
    }

    val bannerTitle = when (selectedLanguage) {
        com.example.util.AppLanguage.SINHALA -> "ශ්‍රී ලංකා පොලිස් තොරතුරු නාමාවලිය"
        com.example.util.AppLanguage.TAMIL -> "இலங்கை பொலிஸ் தொடர்பு விபரங்கள்"
        com.example.util.AppLanguage.ENGLISH -> header.title
    }

    val bannerSubtitle = when (selectedLanguage) {
        com.example.util.AppLanguage.SINHALA -> "පොලිස් ස්ථාන, නිලධාරීන් සහ හදිසි සේවා සබඳතා"
        com.example.util.AppLanguage.TAMIL -> "பொலிஸ் நிலையங்கள், அதிகாரிகள் மற்றும் அவசர சேவைகள்"
        com.example.util.AppLanguage.ENGLISH -> header.subtitle
    }

    val emergencyBadge = when (selectedLanguage) {
        com.example.util.AppLanguage.SINHALA -> "119 හදිසි ඇමතුම"
        com.example.util.AppLanguage.TAMIL -> "119 அவசரம்"
        com.example.util.AppLanguage.ENGLISH -> header.emergencyBadgeText
    }

    val quickActionsLabel = when (selectedLanguage) {
        com.example.util.AppLanguage.SINHALA -> "⚡ ක්ෂණික හදිසි ඇමතුම් (Quick Hotlines)"
        com.example.util.AppLanguage.TAMIL -> "⚡ அவசர துரித அழைப்புகள் (Quick Hotlines)"
        com.example.util.AppLanguage.ENGLISH -> "⚡ Quick Emergency Hotlines"
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Hero Gradient Banner Card (If enabled in remote config)
        if (appConfig.features.showEmergencyBanner) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = primaryColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    primaryColor,
                                    Color(0xFF1E293B),
                                    primaryColor
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = accentColor.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, accentColor)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalPolice,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = badgeLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = accentColor
                                    )
                                }
                            }

                            // Emergency Call Action inside Banner
                            Button(
                                onClick = { onEmergencyCall(header.emergencyBadgeNumber) },
                                colors = ButtonDefaults.buttonColors(containerColor = emergencyRed),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("banner_119_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = emergencyBadge,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = emergencyBadge,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color.White
                                )
                            }
                        }

                        Column {
                            Text(
                                text = bannerTitle,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = headerTextColor
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = bannerSubtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = headerTextColor.copy(alpha = 0.85f)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Ticker / Live Announcement banner if enabled
        if (appConfig.features.showAnnouncementTicker && appConfig.features.announcementTickerText.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = accentColor.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = appConfig.features.announcementTickerText,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Quick Emergency Actions Row (If enabled)
        if (appConfig.features.showQuickActions && appConfig.quickActions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = quickActionsLabel,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                appConfig.quickActions.forEach { action ->
                    val icon = getQuickActionIcon(action.iconType)
                    val badgeColor = action.getBadgeColor()

                    EmergencyHotlineCard(
                        title = action.title,
                        subtitle = action.subtitle,
                        icon = icon,
                        badgeColor = badgeColor,
                        onClick = { onEmergencyCall(action.phoneNumber) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private fun getQuickActionIcon(iconType: String): ImageVector {
    return when (iconType.lowercase().trim()) {
        "warning", "alert", "sos" -> Icons.Default.Warning
        "police", "badge" -> Icons.Default.LocalPolice
        "shield", "security" -> Icons.Default.Shield
        else -> Icons.Default.Call
    }
}

@Composable
private fun EmergencyHotlineCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                ),
                color = badgeColor,
                maxLines = 1
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}


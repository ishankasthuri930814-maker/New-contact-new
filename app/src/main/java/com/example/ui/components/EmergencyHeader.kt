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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.policedirectory.zxklm.R
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.PoliceGold
import com.example.ui.theme.PoliceNavy

@Composable
fun EmergencyHeader(
    onEmergencyCall: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Hero Gradient Banner Card
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PoliceNavy),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                PoliceNavy,
                                Color(0xFF1E293B),
                                PoliceNavy
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
                            color = PoliceGold.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PoliceGold)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalPolice,
                                    contentDescription = null,
                                    tint = PoliceGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "OFFICIAL DIRECTORY",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = PoliceGold
                                )
                            }
                        }

                        // Emergency Call FAB Button inside Banner
                        Button(
                            onClick = { onEmergencyCall("119") },
                            colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("banner_119_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "119 Call",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "119 EMERGENCY",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Sri Lanka Police Directory",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "ශ්‍රී ලංකා පොලිස් නිල ඇමතුම් සහ විද්‍යුත් තැපැල් නාමාවලිය",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Emergency Hotlines Row
        Text(
            text = "⚡ Quick Emergency Hotlines / හදිසි ඇමතුම්",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 119 Emergency Box
            EmergencyHotlineCard(
                title = "119",
                subtitle = "Police Hotline",
                icon = Icons.Default.Warning,
                badgeColor = EmergencyRed,
                onClick = { onEmergencyCall("119") },
                modifier = Modifier.weight(1f)
            )

            // 118 Security Box
            EmergencyHotlineCard(
                title = "118",
                subtitle = "National Hotline",
                icon = Icons.Default.Call,
                badgeColor = PoliceNavy,
                onClick = { onEmergencyCall("118") },
                modifier = Modifier.weight(1f)
            )

            // Police HQ Box
            EmergencyHotlineCard(
                title = "Police HQ",
                subtitle = "0112421111",
                icon = Icons.Default.LocalPolice,
                badgeColor = PoliceGold,
                onClick = { onEmergencyCall("0112421111") },
                modifier = Modifier.weight(1.1f)
            )
        }
    }
}

@Composable
private fun EmergencyHotlineCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                ),
                color = badgeColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

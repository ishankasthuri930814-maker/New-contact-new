package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PoliceContact
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.PoliceGold
import com.example.ui.theme.PoliceNavy

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContactDetailBottomSheet(
    contact: PoliceContact,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onCallClick: (String) -> Unit,
    onEmailClick: (String, String) -> Unit,
    onCopyClick: (String, String) -> Unit,
    onShareClick: (PoliceContact) -> Unit,
    onFavoriteToggle: (PoliceContact) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isEmergency = contact.rank == "HOTLINE" || contact.generalPhone == "119" || contact.generalPhone == "118"
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.testTag("contact_detail_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Top Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isEmergency) EmergencyRed else PoliceNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalPolice,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    if (contact.rank.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isEmergency) EmergencyRed.copy(alpha = 0.15f) else PoliceNavy.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = contact.rank,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isEmergency) EmergencyRed else PoliceNavy,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = { onShareClick(contact) }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { onFavoriteToggle(contact) }) {
                        Icon(
                            imageVector = if (contact.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Favorite",
                            tint = if (contact.isFavorite) PoliceGold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Full Station Title (Wrapped naturally so no text is cut off on real phones)
            Text(
                text = contact.stationOrDesignation,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp,
                    lineHeight = 26.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (contact.officerName.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "👮 ${contact.officerName}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 15.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Section Header
            Text(
                text = "සම්බන්ධතා විස්තර සහ සේවාවන් / Contact Details",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // General Telephone Card
            if (contact.generalPhone.isNotBlank()) {
                ContactDetailActionRow(
                    label = "ප්‍රධාන දුරකථන / Telephone",
                    value = contact.generalPhone,
                    icon = Icons.Default.Phone,
                    onCall = { onCallClick(contact.generalPhone) },
                    onCopy = { onCopyClick(contact.generalPhone, "Telephone Number") },
                    isPrimary = true,
                    isEmergency = isEmergency
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Mobile Telephone Card
            if (contact.mobilePhone.isNotBlank() && contact.mobilePhone != contact.generalPhone) {
                ContactDetailActionRow(
                    label = "ජංගම දුරකථන / Mobile",
                    value = contact.mobilePhone,
                    icon = Icons.Default.PhoneAndroid,
                    onCall = { onCallClick(contact.mobilePhone) },
                    onCopy = { onCopyClick(contact.mobilePhone, "Mobile Number") },
                    isPrimary = false,
                    isEmergency = false
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Additional Office Phone 2
            if (contact.officePhone2.isNotBlank()) {
                ContactDetailActionRow(
                    label = "අතිරේක මාර්ගය 2 / Office Line 2",
                    value = contact.officePhone2,
                    icon = Icons.Default.Phone,
                    onCall = { onCallClick(contact.officePhone2) },
                    onCopy = { onCopyClick(contact.officePhone2, "Office Line 2") },
                    isPrimary = false,
                    isEmergency = false
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Additional Office Phone 3
            if (contact.officePhone3.isNotBlank()) {
                ContactDetailActionRow(
                    label = "අතිරේක මාර්ගය 3 / Office Line 3",
                    value = contact.officePhone3,
                    icon = Icons.Default.Phone,
                    onCall = { onCallClick(contact.officePhone3) },
                    onCopy = { onCopyClick(contact.officePhone3, "Office Line 3") },
                    isPrimary = false,
                    isEmergency = false
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Fax Number if present
            if (contact.fax.isNotBlank()) {
                ContactDetailActionRow(
                    label = "ෆැක්ස් / Fax Number",
                    value = contact.fax,
                    icon = Icons.Default.Phone,
                    onCall = null,
                    onCopy = { onCopyClick(contact.fax, "Fax Number") },
                    isPrimary = false,
                    isEmergency = false
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Email Action
            if (contact.email.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "විද්‍යුත් තැපෑල / Email Address",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = contact.email,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { onEmailClick(contact.email, contact.stationOrDesignation) },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Email යවන්න", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            IconButton(
                                onClick = { onCopyClick(contact.email, "Email Address") },
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color.White)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // OIC Direct Contact Lines Section
            if (contact.oicTraffic.isNotBlank() || contact.oicCrime.isNotBlank() || contact.oicVice.isNotBlank() || contact.oicCommunityPolicing.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "OIC ස්ථානාධිපති සෘජු අංශ / OIC Emergency Direct Lines",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (contact.oicTraffic.isNotBlank()) {
                        OicDirectButton(
                            title = "🚦 Traffic OIC",
                            number = contact.oicTraffic,
                            onCall = { onCallClick(contact.oicTraffic) },
                            onCopy = { onCopyClick(contact.oicTraffic, "OIC Traffic") }
                        )
                    }
                    if (contact.oicCrime.isNotBlank()) {
                        OicDirectButton(
                            title = "🔍 Crime OIC",
                            number = contact.oicCrime,
                            onCall = { onCallClick(contact.oicCrime) },
                            onCopy = { onCopyClick(contact.oicCrime, "OIC Crime") }
                        )
                    }
                    if (contact.oicVice.isNotBlank()) {
                        OicDirectButton(
                            title = "🛡️ Vice OIC",
                            number = contact.oicVice,
                            onCall = { onCallClick(contact.oicVice) },
                            onCopy = { onCopyClick(contact.oicVice, "OIC Vice") }
                        )
                    }
                    if (contact.oicCommunityPolicing.isNotBlank()) {
                        OicDirectButton(
                            title = "🤝 Community OIC",
                            number = contact.oicCommunityPolicing,
                            onCall = { onCallClick(contact.oicCommunityPolicing) },
                            onCopy = { onCopyClick(contact.oicCommunityPolicing, "OIC Community") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Location Address Card
            if (contact.locationAddress.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Address",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ලිපිනය / Address",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = contact.locationAddress,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactDetailActionRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onCall: (() -> Unit)?,
    onCopy: () -> Unit,
    isPrimary: Boolean,
    isEmergency: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (isEmergency) EmergencyRed.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                color = if (isEmergency) EmergencyRed else MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onCall != null) {
                    Button(
                        onClick = onCall,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEmergency) EmergencyRed else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "ඇමතුමක් ගන්න / Call", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OicDirectButton(
    title: String,
    number: String,
    onCall: () -> Unit,
    onCopy: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = number,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = onCall,
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

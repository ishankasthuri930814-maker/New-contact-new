package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
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
import com.example.ui.theme.WhatsAppDarkGreen
import com.example.ui.theme.WhatsAppGreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContactDetailBottomSheet(
    contact: PoliceContact,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onCallClick: (String) -> Unit,
    onWhatsAppClick: (String) -> Unit,
    onEmailClick: (String, String) -> Unit,
    onCopyClick: (String, String) -> Unit,
    onShareClick: (PoliceContact) -> Unit,
    onFavoriteToggle: (PoliceContact) -> Unit,
    onNavigationClick: (PoliceContact) -> Unit = {},
    onQrCodeClick: ((PoliceContact) -> Unit)? = null,
    modifier: Modifier = Modifier,
    selectedLanguage: com.example.util.AppLanguage = com.example.util.AppLanguage.SINHALA
) {
    val context = LocalContext.current
    val isEmergency = contact.rank == "HOTLINE" || contact.generalPhone == "119" || contact.generalPhone == "118"
    val scrollState = rememberScrollState()

    val callBtnLabel = com.example.util.AppStrings.call(selectedLanguage)
    val shareBtnLabel = when (selectedLanguage) {
        com.example.util.AppLanguage.SINHALA -> "සම්පූර්ණ තොරතුරු Share කරන්න (Share All)"
        com.example.util.AppLanguage.TAMIL -> "அனைத்து விபரங்களையும் பகிரவும் (Share All)"
        com.example.util.AppLanguage.ENGLISH -> "Share Full Details"
    }
    val qrBtnLabel = when (selectedLanguage) {
        com.example.util.AppLanguage.SINHALA -> "QR කේතය සහ Phone එකේ Save කරන්න"
        com.example.util.AppLanguage.TAMIL -> "QR குறியீடு & சேமிக்கவும் (QR & Save)"
        com.example.util.AppLanguage.ENGLISH -> "View QR & Save to Contacts"
    }
    val emailHeading = when (selectedLanguage) {
        com.example.util.AppLanguage.SINHALA -> "විද්‍යුත් තැපෑල / Email Address"
        com.example.util.AppLanguage.TAMIL -> "மின்னஞ்சல் முகவரி / Email Address"
        com.example.util.AppLanguage.ENGLISH -> "Email Address"
    }
    val sendEmailText = when (selectedLanguage) {
        com.example.util.AppLanguage.SINHALA -> "Email යවන්න"
        com.example.util.AppLanguage.TAMIL -> "மின்னஞ்சல் அனுப்புக"
        com.example.util.AppLanguage.ENGLISH -> "Send Email"
    }
    val generalLabel = when (selectedLanguage) {
        com.example.util.AppLanguage.SINHALA -> "ප්‍රධාන දුරකථනය / General Phone"
        com.example.util.AppLanguage.TAMIL -> "பொது தொலைபேசி / General Phone"
        com.example.util.AppLanguage.ENGLISH -> "General Phone Line"
    }
    val mobileLabel = when (selectedLanguage) {
        com.example.util.AppLanguage.SINHALA -> "ජංගම දුරකථනය / Mobile Phone"
        com.example.util.AppLanguage.TAMIL -> "கைபேசி எண் / Mobile Phone"
        com.example.util.AppLanguage.ENGLISH -> "Mobile Phone"
    }

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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onNavigationClick(contact) }) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = "Navigate to Station",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (onQrCodeClick != null) {
                        IconButton(onClick = { onQrCodeClick(contact) }) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "Show QR Code",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

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
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (contact.officerName.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "👮 ${contact.officerName}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155),
                        fontSize = 14.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Section Header
            Text(
                text = "සම්බන්ධතා විස්තර සහ සේවාවන් / Contact Details",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                ),
                color = PoliceNavy
            )

            Spacer(modifier = Modifier.height(12.dp))

            // General Telephone Card
            if (contact.generalPhone.isNotBlank()) {
                ContactDetailActionRow(
                    label = "ප්‍රධාන දුරකථන / Telephone",
                    value = contact.generalPhone,
                    icon = Icons.Default.Phone,
                    onCall = { onCallClick(contact.generalPhone) },
                    onWhatsApp = if (contact.generalPhone.startsWith("07") || contact.generalPhone.startsWith("+947") || contact.generalPhone.startsWith("947")) {
                        { onWhatsAppClick(contact.generalPhone) }
                    } else null,
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
                    onWhatsApp = { onWhatsAppClick(contact.mobilePhone) },
                    onCopy = { onCopyClick(contact.mobilePhone, "Mobile Number") },
                    isPrimary = false,
                    isEmergency = false
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // PVT Private Number Card
            if (contact.pvtNumber.isNotBlank()) {
                ContactDetailActionRow(
                    label = "පුද්ගලික අංකය / PVT Number",
                    value = contact.pvtNumber,
                    icon = Icons.Default.PhoneAndroid,
                    onCall = { onCallClick(contact.pvtNumber) },
                    onWhatsApp = { onWhatsAppClick(contact.pvtNumber) },
                    onCopy = { onCopyClick(contact.pvtNumber, "PVT Number") },
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
                    onWhatsApp = if (contact.officePhone2.startsWith("07") || contact.officePhone2.startsWith("+947") || contact.officePhone2.startsWith("947")) {
                        { onWhatsAppClick(contact.officePhone2) }
                    } else null,
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
                    onWhatsApp = if (contact.officePhone3.startsWith("07") || contact.officePhone3.startsWith("+947") || contact.officePhone3.startsWith("947")) {
                        { onWhatsAppClick(contact.officePhone3) }
                    } else null,
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
                    color = Color(0xFFFFFBEB),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "විද්‍යුත් තැපෑල / Email Address",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                            color = Color(0xFFB45309)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = contact.email,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.5.sp),
                            color = Color(0xFF0F172A)
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
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Email යවන්න", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            }
                            IconButton(
                                onClick = { onCopyClick(contact.email, "Email Address") },
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color.White)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
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
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                    color = PoliceNavy
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
                            onWhatsApp = if (contact.oicTraffic.startsWith("07") || contact.oicTraffic.startsWith("+947") || contact.oicTraffic.startsWith("947")) {
                                { onWhatsAppClick(contact.oicTraffic) }
                            } else null,
                            onCopy = { onCopyClick(contact.oicTraffic, "OIC Traffic") }
                        )
                    }
                    if (contact.oicCrime.isNotBlank()) {
                        OicDirectButton(
                            title = "🔍 Crime OIC",
                            number = contact.oicCrime,
                            onCall = { onCallClick(contact.oicCrime) },
                            onWhatsApp = if (contact.oicCrime.startsWith("07") || contact.oicCrime.startsWith("+947") || contact.oicCrime.startsWith("947")) {
                                { onWhatsAppClick(contact.oicCrime) }
                            } else null,
                            onCopy = { onCopyClick(contact.oicCrime, "OIC Crime") }
                        )
                    }
                    if (contact.oicVice.isNotBlank()) {
                        OicDirectButton(
                            title = "🛡️ Vice OIC",
                            number = contact.oicVice,
                            onCall = { onCallClick(contact.oicVice) },
                            onWhatsApp = if (contact.oicVice.startsWith("07") || contact.oicVice.startsWith("+947") || contact.oicVice.startsWith("947")) {
                                { onWhatsAppClick(contact.oicVice) }
                            } else null,
                            onCopy = { onCopyClick(contact.oicVice, "OIC Vice") }
                        )
                    }
                    if (contact.oicCommunityPolicing.isNotBlank()) {
                        OicDirectButton(
                            title = "🤝 Community OIC",
                            number = contact.oicCommunityPolicing,
                            onCall = { onCallClick(contact.oicCommunityPolicing) },
                            onWhatsApp = if (contact.oicCommunityPolicing.startsWith("07") || contact.oicCommunityPolicing.startsWith("+947") || contact.oicCommunityPolicing.startsWith("947")) {
                                { onWhatsAppClick(contact.oicCommunityPolicing) }
                            } else null,
                            onCopy = { onCopyClick(contact.oicCommunityPolicing, "OIC Community") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Google Maps GPS Navigation & Location Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "GPS ස්ථානය සහ සිතියම / Station Location",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (contact.locationCoordinates.isNotBlank()) {
                                Text(
                                    text = "GPS: ${contact.locationCoordinates}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (contact.locationAddress.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = contact.locationAddress,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onNavigationClick(contact) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = "Navigate to Station",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (contact.locationCoordinates.isNotBlank()) "Google Maps Navigation ඔස්සේ යන්න" else "Google Maps හි සොයන්න",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Full Details Share Action Button
                    Button(
                        onClick = { onShareClick(contact) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = shareBtnLabel,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    if (onQrCodeClick != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onQrCodeClick(contact) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PoliceNavy),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("detail_qr_code_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "QR Code",
                                tint = PoliceGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = qrBtnLabel,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
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
    onWhatsApp: (() -> Unit)? = null,
    onCopy: () -> Unit,
    isPrimary: Boolean,
    isEmergency: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (isEmergency) EmergencyRed.copy(alpha = 0.08f) else Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, if (isEmergency) EmergencyRed.copy(alpha = 0.3f) else Color(0xFFCBD5E1))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                color = if (isEmergency) EmergencyRed else Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 15.sp),
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onCall != null) {
                    Button(
                        onClick = onCall,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEmergency) EmergencyRed else MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Call", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                if (onWhatsApp != null) {
                    Button(
                        onClick = onWhatsApp,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WhatsAppDarkGreen
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = "WhatsApp", tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "WhatsApp", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = Color(0xFF334155),
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
    onWhatsApp: (() -> Unit)? = null,
    onCopy: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.5.sp),
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = number,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp),
                    color = Color(0xFF0F172A)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onCall,
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Call", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                if (onWhatsApp != null) {
                    Button(
                        onClick = onWhatsApp,
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppDarkGreen),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "WhatsApp", tint = Color.White, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("WhatsApp", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF334155), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

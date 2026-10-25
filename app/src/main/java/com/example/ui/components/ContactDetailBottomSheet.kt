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
import androidx.compose.material.icons.filled.Edit
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
    onEditClick: ((PoliceContact) -> Unit)? = null,
    modifier: Modifier = Modifier,
    selectedLanguage: com.example.util.AppLanguage = com.example.util.AppLanguage.ENGLISH
) {
    val context = LocalContext.current
    val displayContact = androidx.compose.runtime.remember(contact, selectedLanguage) {
        com.example.util.MultiLanguageSearchHelper.translateContactForDisplay(contact, selectedLanguage)
    }
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

                    if (displayContact.rank.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isEmergency) EmergencyRed.copy(alpha = 0.15f) else PoliceNavy.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = displayContact.rank,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isEmergency) EmergencyRed else PoliceNavy,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onEditClick != null) {
                        IconButton(onClick = { onEditClick(contact) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Contact",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

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
                text = displayContact.stationOrDesignation,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (displayContact.officerName.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "👮 ${displayContact.officerName}",
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

            // Deduplicated Phone Numbers (strictly no identical number is ever shown twice!)
            val distinctPhones = contact.distinctPhones
            if (distinctPhones.isNotEmpty()) {
                distinctPhones.forEachIndexed { index, phone ->
                    val isPrimary = index == 0
                    val isMobile = com.example.util.PhoneUtils.isWhatsAppSupported(phone)
                    val label = when (index) {
                        0 -> if (isMobile) "ප්‍රධාන ජංගම / Mobile" else "ප්‍රධාන දුරකථන / Primary Telephone"
                        1 -> if (isMobile) "අතිරේක ජංගම / Mobile 2" else "අතිරේක මාර්ගය 1 / Additional Line 1"
                        2 -> "අතිරේක මාර්ගය 2 / Additional Line 2"
                        else -> "අතිරේක අංකය / Line ${index + 1}"
                    }
                    ContactDetailActionRow(
                        label = label,
                        value = phone,
                        icon = if (isMobile) Icons.Default.PhoneAndroid else Icons.Default.Phone,
                        onCall = { onCallClick(phone) },
                        onWhatsApp = if (isMobile) {
                            { onWhatsAppClick(phone) }
                        } else null,
                        onCopy = { onCopyClick(phone, label) },
                        isPrimary = isPrimary,
                        isEmergency = isEmergency && isPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Fax Number (ONLY shown if genuinely distinct from all voice telephone numbers)
            if (contact.isFaxDistinct && contact.fax.isNotBlank()) {
                ContactDetailActionRow(
                    label = "ෆැක්ස් / Fax Number",
                    value = contact.fax,
                    icon = Icons.Default.Print,
                    onCall = null,
                    onWhatsApp = null,
                    onCopy = { onCopyClick(contact.fax, "Fax Number") },
                    isPrimary = false,
                    isEmergency = false
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Email Action (strictly suppressed if "0", "null", or empty)
            if (contact.cleanEmail.isNotBlank()) {
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
                            text = contact.cleanEmail,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.5.sp),
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { onEmailClick(contact.cleanEmail, contact.stationOrDesignation) },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Email යවන්න", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            }
                            IconButton(
                                onClick = { onCopyClick(contact.cleanEmail, "Email Address") },
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color.White)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // ATTACHED OFFICERS UNDER THIS STATION ("adala police walatama athulath karala hadann")
            if (contact.attachedOfficers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalPolice,
                        contentDescription = null,
                        tint = PoliceNavy,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "👮 මෙම ස්ථානයේ නිලධාරීන් / Station Officers (${contact.attachedOfficers.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = PoliceNavy
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                contact.attachedOfficers.forEach { officer ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = officer.name.ifBlank { "Officer" },
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val subtitle = listOf(officer.rank, officer.designation)
                                .filter { it.isNotBlank() }
                                .joinToString(" • ")
                            if (subtitle.isNotBlank()) {
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }

                            val offPhones = officer.distinctPhones
                            if (offPhones.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                offPhones.forEach { ph ->
                                    val isMob = com.example.util.PhoneUtils.isWhatsAppSupported(ph)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = if (isMob) Icons.Default.PhoneAndroid else Icons.Default.Phone,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = ph,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.5.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Button(
                                                onClick = { onCallClick(ph) },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(32.dp),
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                                            ) {
                                                Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Call", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            if (isMob) {
                                                Button(
                                                    onClick = { onWhatsAppClick(ph) },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.height(32.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppDarkGreen),
                                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                                                ) {
                                                    Text("WhatsApp", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            IconButton(
                                                onClick = { onCopyClick(ph, "Officer Phone") },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            if (officer.cleanEmail.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = officer.cleanEmail,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { onEmailClick(officer.cleanEmail, officer.station) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Email, contentDescription = "Send Email", modifier = Modifier.size(14.dp), tint = Color(0xFFB45309))
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // OIC Direct Contact Lines Section (ONLY if genuinely distinct and not already listed above)
            val oicDirects = listOfNotNull(
                if (contact.oicTraffic.isNotBlank() && !distinctPhones.contains(contact.oicTraffic)) "🚦 Traffic OIC" to contact.oicTraffic else null,
                if (contact.oicCrime.isNotBlank() && !distinctPhones.contains(contact.oicCrime)) "🔍 Crime OIC" to contact.oicCrime else null,
                if (contact.oicVice.isNotBlank() && !distinctPhones.contains(contact.oicVice)) "🛡️ Vice OIC" to contact.oicVice else null,
                if (contact.oicCommunityPolicing.isNotBlank() && !distinctPhones.contains(contact.oicCommunityPolicing)) "🤝 Community OIC" to contact.oicCommunityPolicing else null
            )
            if (oicDirects.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "OIC ස්ථානාධිපති සෘජු අංශ / OIC Direct Lines",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                    color = PoliceNavy
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    oicDirects.forEach { (label, number) ->
                        OicDirectButton(
                            title = label,
                            number = number,
                            onCall = { onCallClick(number) },
                            onWhatsApp = if (com.example.util.PhoneUtils.isWhatsAppSupported(number)) {
                                { onWhatsAppClick(number) }
                            } else null,
                            onCopy = { onCopyClick(number, label) }
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

                    if (displayContact.locationAddress.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = displayContact.locationAddress,
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

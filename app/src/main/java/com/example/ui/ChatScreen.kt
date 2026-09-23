package com.example.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.Verified
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material3.Checkbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChatMessage
import com.example.data.model.DirectChatMessage
import com.example.data.model.UserProfile
import com.example.ui.components.InAppCallDialog
import com.example.ui.components.UserAvatarView
import com.example.util.AppLanguage
import com.example.util.AppStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    selectedLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var pendingCallUser by remember { mutableStateOf<UserProfile?>(null) }
    var pendingAcceptCallId by remember { mutableStateOf<String?>(null) }
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingCallUser?.let { user ->
                viewModel.startInAppCallWith(user)
                pendingCallUser = null
            }
            pendingAcceptCallId?.let { callId ->
                viewModel.acceptIncomingCall(callId)
                pendingAcceptCallId = null
            }
        } else {
            Toast.makeText(context, "ඇමතුම් සඳහා මයික්‍රෆෝන අවසරය (Microphone permission) අවශ්‍ය වේ", Toast.LENGTH_SHORT).show()
            pendingCallUser = null
            pendingAcceptCallId = null
        }
    }

    val checkAudioPermissionAndCall: (UserProfile) -> Unit = { targetUser ->
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.startInAppCallWith(targetUser)
        } else {
            pendingCallUser = targetUser
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val checkAudioPermissionAndAcceptCall: (String) -> Unit = { callId ->
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.acceptIncomingCall(callId)
        } else {
            pendingAcceptCallId = callId
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Main Mode Navigation: Modern Segmented Pill Switcher
        if (uiState.chatMode != ChatMode.DIRECT_CHAT_ROOM) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 1. පළමුව: පුද්ගලික සංවාද (Private 1-on-1)
                    val isDirect = uiState.chatMode == ChatMode.REGISTERED_USERS
                    Surface(
                        onClick = { viewModel.setChatMode(ChatMode.REGISTERED_USERS) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isDirect) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (isDirect) 2.dp else 0.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp),
                                tint = if (isDirect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "පුද්ගලික (1-on-1)",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (isDirect) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isDirect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }

                    // 2. දෙවනුව: පොදු සංවාද (Public Community)
                    val isCommunity = uiState.chatMode == ChatMode.COMMUNITY_ROOM
                    Surface(
                        onClick = { viewModel.setChatMode(ChatMode.COMMUNITY_ROOM) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isCommunity) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (isCommunity) 2.dp else 0.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forum,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp),
                                tint = if (isCommunity) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "පොදු සංවාද",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (isCommunity) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCommunity) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        when (uiState.chatMode) {
            ChatMode.COMMUNITY_ROOM -> {
                CommunityChatContent(
                    viewModel = viewModel,
                    uiState = uiState,
                    selectedLanguage = selectedLanguage,
                    onOpenDirectChatWithUser = { msg ->
                        viewModel.openDirectChatWithUser(
                            targetUserId = msg.senderId,
                            targetDisplayName = msg.senderName,
                            targetEmail = msg.senderEmail,
                            targetAvatarIndex = msg.senderAvatarIndex
                        )
                    }
                )
            }
            ChatMode.REGISTERED_USERS -> {
                RegisteredUsersDirectoryContent(
                    viewModel = viewModel,
                    uiState = uiState,
                    onStartDirectChat = { user -> viewModel.openDirectChatWith(user) },
                    onCallUser = { user ->
                        makePhoneCall(context, user)
                    },
                    onInAppCallUser = { user ->
                        checkAudioPermissionAndCall(user)
                    }
                )
            }
            ChatMode.DIRECT_CHAT_ROOM -> {
                DirectPrivateChatContent(
                    viewModel = viewModel,
                    uiState = uiState,
                    selectedLanguage = selectedLanguage,
                    onBack = { viewModel.closeDirectChat() },
                    onCallUser = { user ->
                        makePhoneCall(context, user)
                    },
                    onInAppCallUser = { user ->
                        checkAudioPermissionAndCall(user)
                    }
                )
            }
        }
    }
}

private fun makePhoneCall(context: Context, user: UserProfile) {
    val phone = user.phoneNumber.trim()
    if (phone.isBlank()) {
        Toast.makeText(context, "මෙම සාමාජිකයා දුරකථන අංකය ලබාදී නොමැත (No phone number in profile)", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "ඇමතුම ලබාගත නොහැක: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun RegisteredUsersDirectoryContent(
    viewModel: ChatViewModel,
    uiState: ChatUiState,
    onStartDirectChat: (UserProfile) -> Unit,
    onCallUser: (UserProfile) -> Unit,
    onInAppCallUser: (UserProfile) -> Unit
) {
    val myEmail = uiState.currentUserProfile.email
    val myId = uiState.currentUserProfile.userId

    val distinctUsers = uiState.registeredUsers
        .groupBy {
            val key = DirectChatMessage.normalizeUserKey(it.email).ifBlank {
                DirectChatMessage.normalizeUserKey(it.userId)
            }
            if (key != "unknown_user" && key.isNotBlank()) key else it.userId
        }
        .map { (_, list) ->
            list.maxByOrNull { user ->
                (if (user.phoneNumber.isNotBlank()) 10 else 0) +
                (if (user.displayName.isNotBlank() && !user.displayName.equals("Citizen", ignoreCase = true)) 5 else 0) +
                (if (user.avatarIndex > 0) 2 else 0) +
                user.joinedTimestamp
            } ?: list.first()
        }

    val filteredList = distinctUsers.filter { user ->
        val notMe = (user.email.isBlank() || user.email != myEmail) &&
                (user.userId.isBlank() || user.userId != myId)
        val query = uiState.searchQuery.trim().lowercase()
        notMe && (query.isEmpty() ||
                user.displayName.lowercase().contains(query) ||
                user.district.lowercase().contains(query) ||
                user.badge.lowercase().contains(query))
    }

    var selectedDirectoryTab by remember { mutableStateOf(0) } // 0: Contacts, 1: Call Logs

    // Extract all call logs across all conversations
    val allCallLogs = remember(uiState.directMessagesMap, myEmail, myId) {
        uiState.directMessagesMap.values.flatten()
            .filter { it.messageType == DirectChatMessage.TYPE_CALL_LOG }
            .sortedByDescending { it.timestamp }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sub-Tab Switcher: 📇 සාමාජික ලැයිස්තුව (Contact List) | 📞 ඇමතුම් ලැයිස්තුව (Call Logs)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tab 1: Contact List
                FilterChip(
                    selected = selectedDirectoryTab == 0,
                    onClick = { selectedDirectoryTab = 0 },
                    label = {
                        Text(
                            text = "📇 සාමාජික ලැයිස්තුව (${distinctUsers.size})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedDirectoryTab == 0) FontWeight.ExtraBold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Contacts,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                )

                // Tab 2: Call Logs
                FilterChip(
                    selected = selectedDirectoryTab == 1,
                    onClick = { selectedDirectoryTab = 1 },
                    label = {
                        Text(
                            text = "📞 Call Logs (${allCallLogs.size})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedDirectoryTab == 1) FontWeight.ExtraBold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.PhoneCallback,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE8F5E9),
                        selectedLabelColor = Color(0xFF1B5E20),
                        selectedLeadingIconColor = Color(0xFF2E7D32)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (selectedDirectoryTab == 1) {
            // === CALL LOGS TAB ===
            CallLogsListContent(
                callLogs = allCallLogs,
                registeredUsers = distinctUsers,
                myEmail = myEmail,
                myId = myId,
                onInAppCallUser = onInAppCallUser,
                onStartDirectChat = onStartDirectChat,
                onDeleteCallLog = { msg ->
                    viewModel.deleteDirectMessage(msg.id)
                }
            )
        } else {
            // === CONTACT LIST TAB ===
            // Search & Privacy Banner
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        placeholder = {
                            Text(
                                "සාමාජිකයින් හෝ නිලධාරීන් සොයන්න...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🔒 පරිශීලකයින්ගේ Contact List එක සහ සජීවී Call Log ආරක්ෂිතව පිහිටුවා ඇත.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (uiState.searchQuery.isNotBlank()) "සොයන ලද සාමාජිකයින් හමු නොවීය" else "තවමත් වෙනත් ලියාපදිංචි සාමාජිකයින් නොමැත",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (uiState.searchQuery.isNotBlank()) "වෙනත් නමක් හෝ දිස්ත්‍රික්කයක් සොයන්න." else "Firebase ගිණුම් හරහා ලියාපදිංචි වන සැබෑ සාමාජිකයින් මෙහි සජීවීව (Live) දිස්වනු ඇත.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.userId.ifBlank { it.email } }) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatarView(
                                avatarIndex = user.avatarIndex,
                                displayName = user.displayName,
                                size = 48.dp
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.displayName.ifBlank { "Citizen" },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = Color(0xFF00897B),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                Text(
                                    text = "📍 ${user.district} • ${user.badge}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )

                                if (user.isPhonePublic && user.phoneNumber.isNotBlank()) {
                                    Text(
                                        text = "📞 ${user.phoneNumber}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            // Action buttons: In-App Voice Call, Regular Phone Call, Direct Chat
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1. In-App Call Button (Vibrant Emerald Gradient)
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .shadow(elevation = 3.dp, shape = CircleShape)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color(0xFF00C853), Color(0xFF1B5E20))
                                            )
                                        )
                                        .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                        .clickable { onInAppCallUser(user) }
                                        .testTag("in_app_call_user_${user.userId}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhoneInTalk,
                                        contentDescription = "In-App Voice Call",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // 2. Regular Phone Call (Vibrant Ocean/Cyan Gradient)
                                if (user.phoneNumber.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .shadow(elevation = 3.dp, shape = CircleShape)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color(0xFF00B0FF), Color(0xFF0277BD))
                                                )
                                            )
                                            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                            .clickable { onCallUser(user) }
                                            .testTag("phone_call_user_${user.userId}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = "Regular Phone Call",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                // 3. Direct Chat (Vibrant Royal Indigo Gradient)
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .shadow(elevation = 3.dp, shape = CircleShape)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color(0xFF2979FF), Color(0xFF0D47A1))
                                            )
                                        )
                                        .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                        .clickable { onStartDirectChat(user) }
                                        .testTag("chat_user_${user.userId}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = "Direct Chat",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun CallLogsListContent(
    callLogs: List<DirectChatMessage>,
    registeredUsers: List<UserProfile>,
    myEmail: String,
    myId: String,
    onInAppCallUser: (UserProfile) -> Unit,
    onStartDirectChat: (UserProfile) -> Unit,
    onDeleteCallLog: (DirectChatMessage) -> Unit
) {
    var selectedCallLogIds by remember { mutableStateOf(setOf<String>()) }
    val isSelectionMode = selectedCallLogIds.isNotEmpty()
    var showBatchDeleteDialog by remember { mutableStateOf(false) }

    val myNormKey = remember(myEmail, myId) { DirectChatMessage.normalizeUserKey(myEmail.ifBlank { myId }) }

    if (showBatchDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteDialog = false },
            title = { Text("තෝරාගත් Call Logs මකන්නද?") },
            text = { Text("තෝරාගත් ඇමතුම් සටහන් ${selectedCallLogIds.size} ක් මකා දැමීමට අවශ්‍ය බව තහවුරු කරන්න.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBatchDeleteDialog = false
                        callLogs.filter { selectedCallLogIds.contains(it.id) }.forEach {
                            onDeleteCallLog(it)
                        }
                        selectedCallLogIds = emptySet()
                    }
                ) {
                    Text("මකන්න (Delete Selected)", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteDialog = false }) {
                    Text("අවලංගු කරන්න (Cancel)")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isSelectionMode) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "සටහන් ${selectedCallLogIds.size} ක් තෝරාගෙන ඇත",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                    Row {
                        IconButton(onClick = { showBatchDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Selected",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        IconButton(onClick = { selectedCallLogIds = emptySet() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Selection",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }

        if (callLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneCallback,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "තවමත් ඇමතුම් සටහන් (Call Logs) නොමැත",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "සාමාජිකයින් අතර සිදුකෙරෙන සියලුම In-App Voice Calls වල සටහන් මෙහි සුරැකේ.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(callLogs, key = { it.id }) { log ->
                    val isMe = log.senderId == myNormKey ||
                            log.senderId == myId ||
                            (log.senderId.isNotBlank() && log.senderId == myEmail)

                    val otherUserKey = if (isMe) log.receiverId else log.senderId
                    val fallbackName = if (isMe) log.text.ifBlank { "User" } else log.senderName.ifBlank { "User" }
                    val otherUser = registeredUsers.firstOrNull {
                        (it.email.isNotBlank() && DirectChatMessage.normalizeUserKey(it.email) == DirectChatMessage.normalizeUserKey(otherUserKey)) ||
                        (it.userId.isNotBlank() && it.userId == otherUserKey)
                    } ?: UserProfile(
                        userId = otherUserKey,
                        displayName = fallbackName,
                        email = if (otherUserKey.contains("@")) otherUserKey else ""
                    )

                    val isSelected = selectedCallLogIds.contains(log.id)

                    val toggleSelect = {
                        selectedCallLogIds = if (isSelected) {
                            selectedCallLogIds - log.id
                        } else {
                            selectedCallLogIds + log.id
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isSelectionMode) {
                                    toggleSelect()
                                } else {
                                    onStartDirectChat(otherUser)
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSelectionMode) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { toggleSelect() },
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }

                            UserAvatarView(
                                avatarIndex = otherUser.avatarIndex,
                                displayName = otherUser.displayName,
                                size = 44.dp
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = otherUser.displayName.ifBlank { "User" },
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )

                                val isConnected = log.callStatus == "CONNECTED"
                                val isDeclined = log.callStatus == "DECLINED"
                                val isMissed = log.callStatus == "MISSED" || (!isConnected && !isDeclined)

                                val (statusLabel, statusColor, statusIcon) = when {
                                    isMe && isConnected -> Triple("↗ ගනු ලැබූ ඇමතුම (Outgoing)", Color(0xFF2E7D32), Icons.AutoMirrored.Filled.CallMade)
                                    isMe && isDeclined -> Triple("↗ ප්‍රතික්ෂේප වූ ඇමතුම", Color(0xFFC62828), Icons.Default.CallEnd)
                                    isMe -> Triple("↗ පිළිතුරු නොලැබුණි", Color(0xFFE65100), Icons.AutoMirrored.Filled.CallMade)
                                    isConnected -> Triple("↙ ලැබුණු ඇමතුම (Incoming)", Color(0xFF2E7D32), Icons.AutoMirrored.Filled.CallReceived)
                                    isDeclined -> Triple("↙ ප්‍රතික්ෂේප කළ ඇමතුම", Color(0xFFC62828), Icons.Default.CallEnd)
                                    else -> Triple("↙🔴 මඟහැරුණු ඇමතුම (Missed)", Color(0xFFD32F2F), Icons.AutoMirrored.Filled.CallMissed)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = statusIcon,
                                        contentDescription = null,
                                        tint = statusColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = statusLabel,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = statusColor,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                val timeFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
                                val formattedTime = timeFormat.format(Date(log.timestamp))

                                Text(
                                    text = formattedTime + if (isConnected && log.callDurationSeconds > 0) " • කාලය: ${log.callDurationSeconds}s" else "",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                )
                            }

                            // Action Menu (Call Back & Options)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onInAppCallUser(otherUser) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF1B5E20), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhoneInTalk,
                                        contentDescription = "Call Back",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                var showLogMenu by remember { mutableStateOf(false) }
                                Box {
                                    IconButton(onClick = { showLogMenu = true }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Options",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showLogMenu,
                                        onDismissRequest = { showLogMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("තෝරන්න (Select)") },
                                            leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                                            onClick = {
                                                showLogMenu = false
                                                toggleSelect()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("සංවාදය ආරම්භ කරන්න") },
                                            leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                            onClick = {
                                                showLogMenu = false
                                                onStartDirectChat(otherUser)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("සටහන මකන්න (Delete Log)", color = MaterialTheme.colorScheme.error) },
                                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                            onClick = {
                                                showLogMenu = false
                                                onDeleteCallLog(log)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DirectPrivateChatContent(
    viewModel: ChatViewModel,
    uiState: ChatUiState,
    selectedLanguage: AppLanguage,
    onBack: () -> Unit,
    onCallUser: (UserProfile) -> Unit,
    onInAppCallUser: (UserProfile) -> Unit
) {
    val activeUser = uiState.activeDirectUser ?: return
    val messages = uiState.activeDirectMessages
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showBatchDeleteConfirmDialog by remember { mutableStateOf(false) }
    var selectedMessageIds by remember { mutableStateOf(setOf<String>()) }
    val isSelectionMode = selectedMessageIds.isNotEmpty()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty() && !isSelectionMode) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("සංවාදය හිස් කරන්නද?") },
            text = { Text("මෙම පුද්ගලික සංවාදයේ සියලුම පණිවිඩ සහ ඇමතුම් සටහන් මකා දැමීමට අවශ්‍ය බව තහවුරු කරන්න.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmDialog = false
                        viewModel.clearCurrentDirectConversation()
                        selectedMessageIds = emptySet()
                    }
                ) {
                    Text("මකන්න (Clear All)", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("අවලංගු කරන්න (Cancel)")
                }
            }
        )
    }

    if (showBatchDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteConfirmDialog = false },
            title = { Text("තෝරාගත් පණිවිඩ මකන්නද?") },
            text = { Text("තෝරාගත් පණිවිඩ/සටහන් ${selectedMessageIds.size} ක් මකා දැමීමට අවශ්‍ය බව තහවුරු කරන්න.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBatchDeleteConfirmDialog = false
                        viewModel.deleteSelectedDirectMessages(selectedMessageIds)
                        selectedMessageIds = emptySet()
                    }
                ) {
                    Text("මකන්න (Delete)", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteConfirmDialog = false }) {
                    Text("අවලංගු කරන්න (Cancel)")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Direct Chat Header Bar (Switches to Selection Bar when items selected)
        Surface(
            color = if (isSelectionMode) Color(0xFFB71C1C) else Color(0xFF003366),
            shadowElevation = 4.dp
        ) {
            if (isSelectionMode) {
                // Multi-Selection Action Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedMessageIds = emptySet() }) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Cancel Selection",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "${selectedMessageIds.size} ක් තෝරා ඇත (${selectedMessageIds.size} Selected)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )

                    // Select All / Deselect All Button
                    IconButton(
                        onClick = {
                            if (selectedMessageIds.size == messages.size) {
                                selectedMessageIds = emptySet()
                            } else {
                                selectedMessageIds = messages.map { it.id }.toSet()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (selectedMessageIds.size == messages.size) Icons.Default.Security else Icons.Default.Forum,
                            contentDescription = "Select All",
                            tint = Color.White
                        )
                    }

                    // Delete Selected Button
                    IconButton(
                        onClick = { showBatchDeleteConfirmDialog = true },
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.25f), CircleShape)
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Selected",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                // Normal Direct Chat Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    UserAvatarView(
                        avatarIndex = activeUser.avatarIndex,
                        displayName = activeUser.displayName,
                        size = 38.dp
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeUser.displayName.ifBlank { "User" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "🔒 Direct Private Chat • ${activeUser.district}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            ),
                            maxLines = 1
                        )
                    }

                    // Action Buttons: In-App Voice Call, Phone Call, & More Options
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 1. In-App Voice Call (Internet Call)
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .shadow(elevation = 2.dp, shape = CircleShape)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF00C853), Color(0xFF1B5E20))
                                    )
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                .clickable { onInAppCallUser(activeUser) }
                                .testTag("in_app_call_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneInTalk,
                                contentDescription = "In-App Voice Call",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // 2. Regular Phone Call (if has number)
                        if (activeUser.phoneNumber.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .shadow(elevation = 2.dp, shape = CircleShape)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF00B0FF), Color(0xFF0277BD))
                                        )
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                    .clickable { onCallUser(activeUser) }
                                    .testTag("regular_phone_call_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Regular Phone Call",
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        // 3. Overflow Menu (Clear Conversation / Select Messages)
                        Box {
                            IconButton(
                                onClick = { showOptionsMenu = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More Options",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showOptionsMenu,
                                onDismissRequest = { showOptionsMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("පණිවිඩ තෝරන්න (Select Messages)") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Delete, contentDescription = null)
                                    },
                                    onClick = {
                                        showOptionsMenu = false
                                        if (messages.isNotEmpty()) {
                                            selectedMessageIds = setOf(messages.last().id)
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("සංවාදය හිස් කරන්න (Clear Chat)", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = {
                                        Icon(Icons.Default.CleaningServices, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    },
                                    onClick = {
                                        showOptionsMenu = false
                                        showClearConfirmDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Direct Messages Body
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "දෙදෙනෙකු අතර පමණක් සංවාදය (1-on-1 End-to-End Chat)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "මෙම සංවාදය ඔබ සහ ${activeUser.displayName} අතර පමණක් සීමා වේ.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        val myNormKey = DirectChatMessage.normalizeUserKey(uiState.currentUserProfile.email.ifBlank { uiState.currentUserProfile.userId })
                        val isMe = msg.senderId == myNormKey ||
                                msg.senderId == uiState.currentUserProfile.userId ||
                                (msg.senderId.isNotBlank() && msg.senderId == uiState.currentUserProfile.email)
                        val isSelected = selectedMessageIds.contains(msg.id)

                        val toggleSelection = {
                            selectedMessageIds = if (isSelected) {
                                selectedMessageIds - msg.id
                            } else {
                                selectedMessageIds + msg.id
                            }
                        }

                        if (msg.messageType == DirectChatMessage.TYPE_CALL_LOG) {
                            DirectCallLogItem(
                                message = msg,
                                isMe = isMe,
                                isSelectionMode = isSelectionMode,
                                isSelected = isSelected,
                                onToggleSelection = toggleSelection,
                                onCallBack = { onInAppCallUser(activeUser) },
                                onDelete = { viewModel.deleteDirectMessage(msg.id) }
                            )
                        } else {
                            DirectMessageItem(
                                message = msg,
                                isMe = isMe,
                                isSelectionMode = isSelectionMode,
                                isSelected = isSelected,
                                onToggleSelection = toggleSelection,
                                onDelete = { viewModel.deleteDirectMessage(msg.id) }
                            )
                        }
                    }
                }
            }
        }

        // Input Box
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.inputText,
                    onValueChange = { viewModel.onInputTextChange(it) },
                    placeholder = { Text("පුද්ගලික පණිවිඩය ලියන්න...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        viewModel.sendMessage()
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                    enabled = uiState.inputText.isNotBlank() && !uiState.isSending,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (uiState.inputText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (uiState.isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (uiState.inputText.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

data class CallLogVisualConfig(
    val titleText: String,
    val statusText: String,
    val iconVector: androidx.compose.ui.graphics.vector.ImageVector,
    val containerBg: Color,
    val borderClr: Color,
    val mainColor: Color
)

@Composable
fun DirectCallLogItem(
    message: DirectChatMessage,
    isMe: Boolean,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelection: () -> Unit = {},
    onCallBack: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))
    var showMenu by remember { mutableStateOf(false) }

    val isConnected = message.callStatus == "CONNECTED"
    val isDeclined = message.callStatus == "DECLINED"
    val isMissed = message.callStatus == "MISSED" || (!isConnected && !isDeclined)

    val config = when {
        isMe -> {
            when {
                isConnected -> CallLogVisualConfig(
                    "ගනු ලැබූ ඇමතුම (Outgoing Call)",
                    "🟢 පිළිගන්නා ලදී (Answered)",
                    Icons.AutoMirrored.Filled.CallMade,
                    Color(0xFFE8F5E9),
                    Color(0xFF81C784),
                    Color(0xFF2E7D32)
                )
                isDeclined -> CallLogVisualConfig(
                    "ගනු ලැබූ ඇමතුම (Outgoing Call)",
                    "🔴 ප්‍රතික්ෂේප විය (Declined)",
                    Icons.Default.CallEnd,
                    Color(0xFFFFEBEE),
                    Color(0xFFEF9A9A),
                    Color(0xFFC62828)
                )
                else -> CallLogVisualConfig(
                    "ගනු ලැබූ ඇමතුම (Outgoing Call)",
                    "⚠️ පිළිතුරු නොලැබුණි (Unanswered)",
                    Icons.AutoMirrored.Filled.CallMade,
                    Color(0xFFFFF3E0),
                    Color(0xFFFFCC80),
                    Color(0xFFE65100)
                )
            }
        }
        else -> {
            when {
                isConnected -> CallLogVisualConfig(
                    "ලැබුණු ඇමතුම (Incoming Call)",
                    "🟢 ඔබ විසින් පිළිගන්නා ලදී",
                    Icons.AutoMirrored.Filled.CallReceived,
                    Color(0xFFE8F5E9),
                    Color(0xFF81C784),
                    Color(0xFF2E7D32)
                )
                isDeclined -> CallLogVisualConfig(
                    "ප්‍රතික්ෂේප කළ ඇමතුම (Declined Call)",
                    "ඔබ විසින් ප්‍රතික්ෂේප කළ",
                    Icons.Default.CallEnd,
                    Color(0xFFFAFAFA),
                    Color(0xFFE0E0E0),
                    Color(0xFF616161)
                )
                else -> CallLogVisualConfig(
                    "මඟහැරුණු ඇමතුම (Missed Call)",
                    "🔴 මඟහැරුණි (Missed Call)",
                    Icons.AutoMirrored.Filled.CallMissed,
                    Color(0xFFFFEBEE),
                    Color(0xFFEF5350),
                    Color(0xFFD32F2F)
                )
            }
        }
    }

    val titleText = config.titleText
    val statusText = config.statusText
    val iconVector = config.iconVector
    val containerBg = config.containerBg
    val borderClr = config.borderClr
    val mainColor = config.mainColor

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            androidx.compose.material3.Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection() },
                modifier = Modifier.padding(end = 4.dp)
            )
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else containerBg
            ),
            border = BorderStroke(
                if (isSelected) 2.dp else 1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else borderClr
            ),
            modifier = Modifier
                .widthIn(max = 340.dp)
                .clickable {
                    if (isSelectionMode) {
                        onToggleSelection()
                    }
                }
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Top Direction Indicator Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = if (isMe) "↗ ගනිපු ඇමතුම (Outgoing)" else if (isMissed) "↙🔴 මඟහැරුණු ඇමතුම (Missed)" else "↙ ආපු ඇමතුම (Incoming)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = mainColor,
                            fontSize = 11.sp
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(mainColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = mainColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = mainColor,
                                fontSize = 13.sp
                            )
                        )

                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = mainColor,
                                fontSize = 11.sp
                            )
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formattedTime,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            if (isConnected && message.callDurationSeconds > 0) {
                                val mins = message.callDurationSeconds / 60
                                val secs = message.callDurationSeconds % 60
                                val durStr = if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• කාලය: $durStr",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B5E20)
                                    )
                                )
                            }
                        }
                    }

                    if (!isSelectionMode) {
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("තෝරන්න (Select)") },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        onToggleSelection()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("මකන්න (Delete Log)", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        onDelete()
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onCallBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = mainColor),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isMissed) "නැවත අමතන්න (Call Back)" else "නැවත ඇමතුමක් ගන්න (Call Again)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun DirectMessageItem(
    message: DirectChatMessage,
    isMe: Boolean,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelection: () -> Unit = {},
    onDelete: (() -> Unit)? = null
) {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            androidx.compose.material3.Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection() },
                modifier = Modifier.padding(end = 4.dp)
            )
        }

        if (isMe && !isSelectionMode) {
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("තෝරන්න (Select)") },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onToggleSelection()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("පිටපත් කරන්න (Copy)") },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            clipboardManager.setText(AnnotatedString(message.text))
                            Toast.makeText(context, "පිටපත් කරන ලදී (Copied)", Toast.LENGTH_SHORT).show()
                        }
                    )
                    if (onDelete != null) {
                        DropdownMenuItem(
                            text = { Text("මකන්න (Delete)", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
        }

        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else if (isMe) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                modifier = Modifier.clickable {
                    if (isSelectionMode) {
                        onToggleSelection()
                    }
                }
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else if (isMe) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            lineHeight = 20.sp
                        )
                    )
                }
            }
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        if (!isMe && !isSelectionMode) {
            Spacer(modifier = Modifier.width(4.dp))
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("තෝරන්න (Select)") },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onToggleSelection()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("පිටපත් කරන්න (Copy)") },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            clipboardManager.setText(AnnotatedString(message.text))
                            Toast.makeText(context, "පිටපත් කරන ලදී (Copied)", Toast.LENGTH_SHORT).show()
                        }
                    )
                    if (onDelete != null) {
                        DropdownMenuItem(
                            text = { Text("මකන්න (Delete)", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityChatContent(
    viewModel: ChatViewModel,
    uiState: ChatUiState,
    selectedLanguage: AppLanguage,
    onOpenDirectChatWithUser: (ChatMessage) -> Unit
) {
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val filteredMessages = uiState.messages.filter {
        it.channelId == uiState.selectedChannel
    }

    LaunchedEffect(filteredMessages.size) {
        if (filteredMessages.isNotEmpty()) {
            listState.animateScrollToItem(filteredMessages.size - 1)
        }
    }

    val channels = listOf(
        Triple("general", "සමස්ත සාකච්ඡා (General)", Icons.Default.Forum),
        Triple("emergency_help", "හදිසි උපකාර (Emergency)", Icons.Default.Security),
        Triple("traffic_alerts", "මාර්ග තොරතුරු (Traffic)", Icons.Default.Traffic)
    )

    val quickPrompts = when (uiState.selectedChannel) {
        "emergency_help" -> listOf(
            "🚨 119 හදිසි අංශයේ සහය අවශ්‍යයි",
            "🚑 ගිලන් රථ සේවාව (1990) අමතන්න",
            "🛡️ ළඟම පොලිස් ස්ථානය කොහෙද?"
        )
        "traffic_alerts" -> listOf(
            "🚗 මාර්ග තදබදයක් පවතී",
            "⚠️ මාර්ග අනතුරක් සිදුවී ඇත",
            "🛑 මාර්ග බාධක පරීක්ෂාවක් ක්‍රියාත්මකයි"
        )
        else -> listOf(
            "👋 ආයුබෝවන් සියලු දෙනාටම!",
            "ℹ️ පොලිස් නාමාවලිය ඉතා ප්‍රයෝජනවත්",
            "👮 OIC දුරකථන අංක නිවැරදිව ලැබුණි"
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Channel Tabs
        PrimaryTabRow(
            selectedTabIndex = channels.indexOfFirst { it.first == uiState.selectedChannel }.coerceAtLeast(0),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            channels.forEachIndexed { _, (id, title, icon) ->
                val isSelected = uiState.selectedChannel == id
                Tab(
                    selected = isSelected,
                    onClick = { viewModel.onChannelChange(id) },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }

        // Channel Info Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (uiState.selectedChannel) {
                        "emergency_help" -> "හදිසි අවස්ථාවලදී ප්‍රජාව දැනුවත් කර සහය ලබාගන්න"
                        "traffic_alerts" -> "මාර්ග සහ රථවාහන තොරතුරු ක්ෂණිකව බෙදාගන්න"
                        else -> "ශ්‍රී ලංකා පොලිස් නාමාවලිය සාමාජිකයින්ගේ සජීවී සංවාද මණ්ඩපය"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // Message List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (filteredMessages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Forum,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "තවමත් පණිවිඩ නොමැත",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "පළමු පණිවිඩය යවා සාකච්ඡාව ආරම්භ කරන්න!",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredMessages, key = { it.id }) { msg ->
                        val isMe = (msg.senderEmail.isNotBlank() && msg.senderEmail == uiState.currentUserProfile.email) ||
                                (msg.senderId.isNotBlank() && msg.senderId == uiState.currentUserProfile.userId)
                        ChatMessageItem(
                            message = msg,
                            isMe = isMe,
                            registeredUsers = uiState.registeredUsers,
                            onOpenDirectChat = { onOpenDirectChatWithUser(it) },
                            onDeleteMessage = { viewModel.deleteCommunityMessage(it.id) }
                        )
                    }
                }
            }
        }

        // Quick Prompt Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickPrompts) { prompt ->
                FilterChip(
                    selected = false,
                    onClick = { viewModel.sendQuickPrompt(prompt) },
                    label = {
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        // Message Input Box
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                AnimatedVisibility(visible = uiState.isEmergencyFlag) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Emergency,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🚨 හදිසි පණිවිඩයක් ලෙස යවනු ලැබේ (High Priority Alert)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFD32F2F),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.toggleEmergencyFlag() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (uiState.isEmergencyFlag) Color(0xFFD32F2F) else MaterialTheme.colorScheme.surfaceVariant
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Emergency Alert",
                            tint = if (uiState.isEmergencyFlag) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = { viewModel.onInputTextChange(it) },
                        placeholder = {
                            Text(
                                text = AppStrings.typeMessage(selectedLanguage),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            viewModel.sendMessage()
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        },
                        enabled = uiState.inputText.isNotBlank() && !uiState.isSending,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (uiState.inputText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                    ) {
                        if (uiState.isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (uiState.inputText.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    isMe: Boolean,
    registeredUsers: List<UserProfile> = emptyList(),
    onOpenDirectChat: ((ChatMessage) -> Unit)? = null,
    onDeleteMessage: ((ChatMessage) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val displaySenderName = remember(message.senderName, message.senderEmail, message.senderId, registeredUsers) {
        if (message.senderName.isNotBlank() &&
            !message.senderName.equals("Citizen", ignoreCase = true) &&
            !message.senderName.equals("citizen", ignoreCase = true) &&
            !message.senderName.equals("සාමාජිකයා (Member)", ignoreCase = true)
        ) {
            message.senderName
        } else {
            val registered = registeredUsers.firstOrNull {
                (it.email.isNotBlank() && it.email.equals(message.senderEmail, ignoreCase = true)) ||
                (it.userId.isNotBlank() && it.userId == message.senderId)
            }
            registered?.displayName?.takeIf { it.isNotBlank() && !it.equals("Citizen", ignoreCase = true) }
                ?: message.senderEmail.substringBefore("@").replaceFirstChar { it.uppercase() }.takeIf { it.isNotBlank() }
                ?: "සාමාජිකයා"
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("පණිවිඩය මකන්නද?") },
            text = { Text("මෙම පණිවිඩය සාකච්ඡා මණ්ඩපයෙන් ඉවත් කිරීමට අවශ්‍ය බව තහවුරු කරන්න.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteMessage?.invoke(message)
                    }
                ) {
                    Text("මකන්න (Delete)", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("අවලංගු කරන්න (Cancel)")
                }
            }
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isMe) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(enabled = onOpenDirectChat != null) {
                        onOpenDirectChat?.invoke(message)
                    }
            ) {
                UserAvatarView(
                    avatarIndex = message.senderAvatarIndex,
                    displayName = displaySenderName,
                    size = 36.dp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 3.dp, start = 4.dp, end = 4.dp)
            ) {
                if (isMe) {
                    Text(
                        text = "ඔබ (You)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                            .clickable(enabled = onOpenDirectChat != null) {
                                onOpenDirectChat?.invoke(message)
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = displaySenderName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Tap to chat privately",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }

                if (message.senderBadge.isNotBlank() && !isMe) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = Color(0xFF00897B),
                        modifier = Modifier.size(12.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                // 3-dots Action Menu for options
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (!isMe && onOpenDirectChat != null) {
                            DropdownMenuItem(
                                text = { Text("පුද්ගලිකව කතා කරන්න (Direct Chat)") },
                                leadingIcon = {
                                    Icon(Icons.Default.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                },
                                onClick = {
                                    showMenu = false
                                    onOpenDirectChat.invoke(message)
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("පිටපත් කරන්න (Copy)") },
                            leadingIcon = {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                clipboardManager.setText(AnnotatedString(message.text))
                                Toast.makeText(context, "පිටපත් කරන ලදී (Copied)", Toast.LENGTH_SHORT).show()
                            }
                        )

                        if (isMe && onDeleteMessage != null) {
                            DropdownMenuItem(
                                text = { Text("මකන්න (Delete)", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                },
                                onClick = {
                                    showMenu = false
                                    showDeleteConfirmDialog = true
                                }
                            )
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        message.isEmergency -> Color(0xFFFFEBEE)
                        isMe -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    if (message.isEmergency) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Emergency,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "🚨 හදිසි ඇඟවීම (Emergency Alert)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFD32F2F),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = when {
                                message.isEmergency -> Color(0xFFB71C1C)
                                isMe -> MaterialTheme.colorScheme.onPrimary
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            lineHeight = 20.sp
                        )
                    )
                }
            }
        }

        if (isMe) {
            Spacer(modifier = Modifier.width(8.dp))
            UserAvatarView(
                avatarIndex = message.senderAvatarIndex,
                displayName = "ඔබ",
                size = 36.dp
            )
        }
    }
}

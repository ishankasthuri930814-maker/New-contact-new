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
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
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
        // Main Mode Navigation: Community vs Registered Citizens 1-on-1
        if (uiState.chatMode != ChatMode.DIRECT_CHAT_ROOM) {
            PrimaryTabRow(
                selectedTabIndex = if (uiState.chatMode == ChatMode.COMMUNITY_ROOM) 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                val isCommunity = uiState.chatMode == ChatMode.COMMUNITY_ROOM
                Tab(
                    selected = isCommunity,
                    onClick = { viewModel.setChatMode(ChatMode.COMMUNITY_ROOM) },
                    text = {
                        Text(
                            text = "පොදු සංවාද (Community)",
                            fontWeight = FontWeight.Bold,
                            color = if (isCommunity) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (isCommunity) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                val isDirect = uiState.chatMode == ChatMode.REGISTERED_USERS
                Tab(
                    selected = isDirect,
                    onClick = { viewModel.setChatMode(ChatMode.REGISTERED_USERS) },
                    text = {
                        Text(
                            text = "පුද්ගලික සංවාද (1-on-1)",
                            fontWeight = FontWeight.Bold,
                            color = if (isDirect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (isDirect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        when (uiState.chatMode) {
            ChatMode.COMMUNITY_ROOM -> {
                CommunityChatContent(
                    viewModel = viewModel,
                    uiState = uiState,
                    selectedLanguage = selectedLanguage
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

    // In-App Call Overlay
    if (uiState.activeCall != null || uiState.incomingCall != null) {
        InAppCallDialog(
            incomingCall = uiState.incomingCall,
            activeCall = uiState.activeCall,
            isMuted = uiState.isMuted,
            isSpeakerOn = uiState.isSpeakerOn,
            onAcceptCall = { callId -> checkAudioPermissionAndAcceptCall(callId) },
            onDeclineCall = { callId -> viewModel.declineIncomingCall(callId) },
            onEndCall = { callId -> viewModel.endActiveCall(callId) },
            onToggleMute = { viewModel.toggleMute() },
            onToggleSpeaker = { viewModel.toggleSpeaker() },
            onDismiss = { viewModel.dismissCallDialog() }
        )
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

    val filteredList = uiState.registeredUsers.filter { user ->
        val notMe = (user.email.isBlank() || user.email != myEmail) &&
                (user.userId.isBlank() || user.userId != myId)
        val query = uiState.searchQuery.trim().lowercase()
        notMe && (query.isEmpty() ||
                user.displayName.lowercase().contains(query) ||
                user.district.lowercase().contains(query) ||
                user.badge.lowercase().contains(query))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Privacy Banner
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
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
                        text = "🔒 පරිශීලකයින්ගේ Email සහ රහස්‍ය තොරතුරු ආරක්ෂා කර ඇත. සෘජුව Chat සහ Call ලබාගත හැක.",
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // 1. In-App Call Button (Green phone icon)
                                IconButton(
                                    onClick = { onInAppCallUser(user) },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(Color(0xFF2E7D32).copy(alpha = 0.2f), CircleShape)
                                        .testTag("in_app_call_user_${user.userId}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = "In-App Voice Call",
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))

                                // 2. Regular Phone Call (if has number)
                                if (user.phoneNumber.isNotBlank()) {
                                    IconButton(
                                        onClick = { onCallUser(user) },
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(Color(0xFF0288D1).copy(alpha = 0.18f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = "Regular Phone Call",
                                            tint = Color(0xFF0288D1),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }

                                // 3. Direct Chat
                                IconButton(
                                    onClick = { onStartDirectChat(user) },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = "Direct Chat",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(18.dp)
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

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Direct Chat Header Bar
        Surface(
            color = Color(0xFF003366),
            shadowElevation = 4.dp
        ) {
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
                        text = activeUser.displayName.ifBlank { "Citizen" },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    )
                    Text(
                        text = "🔒 Direct Private Chat • ${activeUser.district}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    )
                }

                // Action Buttons: In-App Voice Call & Phone Call
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 1. In-App Voice Call (Internet Call)
                    IconButton(
                        onClick = { onInAppCallUser(activeUser) },
                        modifier = Modifier
                            .background(Color(0xFF2E7D32), CircleShape)
                            .size(38.dp)
                            .testTag("in_app_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "In-App Voice Call",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // 2. Regular Phone Call (if has number)
                    if (activeUser.phoneNumber.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { onCallUser(activeUser) },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                .size(38.dp)
                                .testTag("regular_phone_call_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Regular Phone Call",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
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
                        DirectMessageItem(message = msg, isMe = isMe)
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

@Composable
fun DirectMessageItem(
    message: DirectChatMessage,
    isMe: Boolean
) {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
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
                    containerColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityChatContent(
    viewModel: ChatViewModel,
    uiState: ChatUiState,
    selectedLanguage: AppLanguage
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
                            isMe = isMe
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
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isMe) {
            UserAvatarView(
                avatarIndex = message.senderAvatarIndex,
                displayName = message.senderName,
                size = 36.dp
            )
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
                Text(
                    text = if (isMe) "ඔබ (You)" else message.senderName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                )

                if (message.senderBadge.isNotBlank()) {
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
                displayName = message.senderName,
                size = 36.dp
            )
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CallSession
import com.example.data.model.DirectChatMessage
import com.example.data.model.UserProfile
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun InAppCallDialog(
    incomingCall: CallSession?,
    activeCall: CallSession?,
    currentUserProfile: UserProfile = UserProfile(),
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    isAudioConnected: Boolean = false,
    micAmplitude: Float = 0f,
    speakerAmplitude: Float = 0f,
    onAcceptCall: (String) -> Unit,
    onDeclineCall: (String) -> Unit,
    onEndCall: (String) -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onDismiss: () -> Unit
) {
    val currentCall = incomingCall ?: activeCall ?: return
    var isMinimized by remember { mutableStateOf(false) }

    // Normalize user keys to accurately identify caller vs receiver
    val myKeys = listOfNotNull(
        currentUserProfile.email.takeIf { it.isNotBlank() },
        currentUserProfile.userId.takeIf { it.isNotBlank() },
        currentUserProfile.phoneNumber.takeIf { it.isNotBlank() },
        currentUserProfile.email.takeIf { it.isNotBlank() }?.let { DirectChatMessage.normalizeUserKey(it) },
        currentUserProfile.userId.takeIf { it.isNotBlank() }?.let { DirectChatMessage.normalizeUserKey(it) },
        currentUserProfile.phoneNumber.takeIf { it.isNotBlank() }?.let { DirectChatMessage.normalizeUserKey(it) }
    ).map { it.lowercase().trim() }.distinct()

    val isMeCaller = if (myKeys.isNotEmpty()) {
        myKeys.contains(currentCall.callerId.lowercase().trim()) ||
                (currentCall.callerEmail.isNotBlank() && myKeys.contains(currentCall.callerEmail.lowercase().trim())) ||
                (currentCall.callerUserId.isNotBlank() && myKeys.contains(currentCall.callerUserId.lowercase().trim()))
    } else {
        incomingCall == null && activeCall != null
    }

    val isIncomingRinging = !isMeCaller && (incomingCall != null || currentCall.status == CallSession.STATUS_RINGING)

    // Call duration timer for connected call
    var callDurationSeconds by remember { mutableLongStateOf(0L) }

    LaunchedEffect(currentCall.status, currentCall.connectedAt) {
        if (currentCall.status == CallSession.STATUS_CONNECTED) {
            val baseTime = if (currentCall.connectedAt > 0L) currentCall.connectedAt else System.currentTimeMillis()
            while (true) {
                callDurationSeconds = ((System.currentTimeMillis() - baseTime) / 1000L).coerceAtLeast(0L)
                delay(1000L)
            }
        }
    }

    // Auto dismiss after call terminates
    LaunchedEffect(currentCall.status) {
        if (currentCall.status == CallSession.STATUS_ENDED ||
            currentCall.status == CallSession.STATUS_DECLINED ||
            currentCall.status == CallSession.STATUS_MISSED
        ) {
            delay(1600L)
            onDismiss()
        }
    }

    // Pulsing transition for radar circles
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse1"
    )
    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse2"
    )

    // Partner info
    val partnerName = if (isMeCaller) {
        currentCall.receiverName.ifBlank { "User" }
    } else {
        currentCall.callerName.ifBlank { "User" }
    }

    val partnerPhone = if (isMeCaller) currentCall.receiverPhone else currentCall.callerPhone

    // If Minimized: Show floating WhatsApp-style banner at top
    if (isMinimized && currentCall.status == CallSession.STATUS_CONNECTED) {
        MinimizedCallBanner(
            partnerName = partnerName,
            durationSeconds = callDurationSeconds,
            isMuted = isMuted,
            onExpand = { isMinimized = false },
            onEndCall = { onEndCall(currentCall.callId) }
        )
        return
    }

    Dialog(
        onDismissRequest = { /* Prevent dismiss on click outside */ },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A), // Dark Slate Navy (WhatsApp Call Theme)
                            Color(0xFF0A192F),
                            Color(0xFF030712)
                        )
                    )
                )
                .testTag("in_app_call_screen")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top App Bar: Minimize, Encryption, Live Status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentCall.status == CallSession.STATUS_CONNECTED) {
                        IconButton(
                            onClick = { isMinimized = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Minimize Call",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(40.dp))
                    }

                    // Security & Encryption Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "End-to-End Encrypted",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Live Audio Diagnostic Badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isAudioConnected) Color(0xFF1B5E20).copy(alpha = 0.6f) else Color(0xFF37474F).copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isAudioConnected) Color(0xFF00E676).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.15f)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isAudioConnected) Color(0xFF00E676) else Color(0xFFFFB300))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isAudioConnected) "Agora HD Voice" else "Connecting",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Middle: Partner Name, Phone, Dynamic Status, and Large Pulsing Avatar
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = partnerName.ifBlank { "User" },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            textAlign = TextAlign.Center
                        )
                    )

                    if (partnerPhone.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = partnerPhone,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status Indicator
                    val statusText = when (currentCall.status) {
                        CallSession.STATUS_RINGING -> {
                            if (isIncomingRinging) "📞 ලැබෙන හඬ ඇමතුම (Incoming Call)..." else "අමතමින්... (Calling...)"
                        }
                        CallSession.STATUS_CONNECTED -> {
                            val mins = callDurationSeconds / 60
                            val secs = callDurationSeconds % 60
                            "🟢 %02d:%02d".format(mins, secs)
                        }
                        CallSession.STATUS_DECLINED -> "🔴 ඇමතුම ප්‍රතික්ෂේප විය (Declined)"
                        CallSession.STATUS_MISSED -> "🔴 පිළිතුරක් නැත (Missed Call)"
                        CallSession.STATUS_ENDED -> "⏹️ ඇමතුම අවසන් විය (Call Ended)"
                        else -> "සක්‍රියයි (Active)"
                    }

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = when (currentCall.status) {
                                CallSession.STATUS_CONNECTED -> Color(0xFF00E676)
                                CallSession.STATUS_DECLINED, CallSession.STATUS_MISSED, CallSession.STATUS_ENDED -> Color(0xFFFF5252)
                                else -> Color.White.copy(alpha = 0.85f)
                            },
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // WhatsApp / Messenger 3-layer Pulsing Radar Waves
                    Box(
                        modifier = Modifier.size(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer Radar Ring
                        Box(
                            modifier = Modifier
                                .size(210.dp)
                                .scale(if (currentCall.status == CallSession.STATUS_RINGING) pulseScale1 else 1f)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            if (isIncomingRinging) Color(0xFF00E676).copy(alpha = 0.15f) else Color(0xFF29B6F6).copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Middle Radar Ring
                        Box(
                            modifier = Modifier
                                .size(175.dp)
                                .scale(if (currentCall.status == CallSession.STATUS_RINGING) pulseScale2 else 1f)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            if (isIncomingRinging) Color(0xFF00E676).copy(alpha = 0.25f) else Color(0xFF0288D1).copy(alpha = 0.25f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Center Avatar Card
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .shadow(elevation = 12.dp, shape = CircleShape)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFF00897B),
                                            Color(0xFF004D40)
                                        )
                                    )
                                )
                                .border(3.5.dp, Color.White.copy(alpha = 0.9f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = partnerName.take(1).uppercase().ifBlank { "👤" },
                                style = MaterialTheme.typography.displayMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 50.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Live Sound Waveform Equalizer (reacting to audio amplitude!)
                    if (currentCall.status == CallSession.STATUS_CONNECTED) {
                        AudioWaveformEqualizer(
                            micAmp = micAmplitude,
                            speakerAmp = speakerAmplitude,
                            isMuted = isMuted
                        )
                    }
                }

                // Bottom Control Dock: WhatsApp / Messenger Style
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    if (isIncomingRinging && currentCall.status == CallSession.STATUS_RINGING) {
                        // Incoming Call Controls: Green Answer (pulsing) & Red Decline
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Decline Button
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .shadow(10.dp, CircleShape)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD32F2F))
                                        .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                        .clickable { onDeclineCall(currentCall.callId) }
                                        .testTag("decline_call_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CallEnd,
                                        contentDescription = "Decline Call",
                                        tint = Color.White,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("ප්‍රතික්ෂේප (Decline)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }

                            // Answer Button (Pulsing Green with halo)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .scale(pulseScale2)
                                        .size(76.dp)
                                        .shadow(14.dp, CircleShape)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color(0xFF00E676), Color(0xFF1B5E20))
                                            )
                                        )
                                        .border(3.dp, Color.White, CircleShape)
                                        .clickable { onAcceptCall(currentCall.callId) }
                                        .testTag("accept_call_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Accept Call",
                                        tint = Color.White,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("පිළිගන්න (Answer)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        SwipeToAnswerBar(onAnswer = { onAcceptCall(currentCall.callId) })
                    } else {
                        // Connected or Outgoing Controls: Glassmorphic Dock
                        Surface(
                            shape = RoundedCornerShape(36.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(28.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Mute Toggle
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isMuted) Color(0xFFFF5252) else Color.White.copy(alpha = 0.2f)
                                            )
                                            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                            .clickable { onToggleMute() }
                                            .testTag("toggle_mute_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                            contentDescription = "Mute",
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isMuted) "Muted" else "Mute",
                                        color = if (isMuted) Color(0xFFFF8A80) else Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // 2. End Call Button (Prominent Red Circular Button)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .shadow(12.dp, CircleShape)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color(0xFFFF1744), Color(0xFFC62828))
                                                )
                                            )
                                            .border(2.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                            .clickable { onEndCall(currentCall.callId) }
                                            .testTag("end_call_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CallEnd,
                                            contentDescription = "End Call",
                                            tint = Color.White,
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "අවසන් (End)",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                // 3. Speaker Toggle
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSpeakerOn) Color(0xFF00E676) else Color.White.copy(alpha = 0.2f)
                                            )
                                            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                            .clickable { onToggleSpeaker() }
                                            .testTag("toggle_speaker_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                                            contentDescription = "Speaker",
                                            tint = if (isSpeakerOn) Color.Black else Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isSpeakerOn) "Speaker" else "Earpiece",
                                        color = if (isSpeakerOn) Color(0xFF00E676) else Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
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

/**
 * Animated Sound Wave Equalizer reacting to mic and incoming speaker audio amplitude
 */
@Composable
private fun AudioWaveformEqualizer(
    micAmp: Float,
    speakerAmp: Float,
    isMuted: Boolean
) {
    val totalAmp = max(if (!isMuted) micAmp else 0f, speakerAmp)
    val animatedAmp by animateFloatAsState(targetValue = totalAmp, label = "amp")

    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(34.dp)
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(17.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        val baseHeights = listOf(8, 16, 24, 30, 22, 14, 10)
        baseHeights.forEachIndexed { index, base ->
            val scaleFactor = 0.35f + (animatedAmp * 0.9f)
            val barHeight = (base * scaleFactor).coerceIn(4f, 30f).dp
            val barColor = if (isMuted && speakerAmp < 0.05f) {
                Color(0xFFFF8A80)
            } else if (animatedAmp > 0.05f) {
                Color(0xFF00E676)
            } else {
                Color.White.copy(alpha = 0.5f)
            }

            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(barHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (animatedAmp > 0.08f) "කථා කරමින්... (Speaking)" else "සම්බන්ධයි (Connected)",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Minimized Call Pill Banner shown at top when user is in chat or directory while on call
 */
@Composable
fun MinimizedCallBanner(
    partnerName: String,
    durationSeconds: Long,
    isMuted: Boolean,
    onExpand: () -> Unit,
    onEndCall: () -> Unit
) {
    val mins = durationSeconds / 60
    val secs = durationSeconds % 60
    val timeFormatted = "%02d:%02d".format(mins, secs)

    Surface(
        onClick = onExpand,
        color = Color(0xFF1B5E20),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E676)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneInTalk,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "📞 $partnerName",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "සම්බන්ධයි (Tap to open) • $timeFormatted" + if (isMuted) " • [Muted]" else "",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD32F2F))
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "Hang Up",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SwipeToAnswerBar(
    onAnswer: () -> Unit
) {
    val density = LocalDensity.current
    val trackWidthDp = 280.dp
    val thumbSizeDp = 48.dp
    val maxDragPx = with(density) { (trackWidthDp - thumbSizeDp - 6.dp).toPx() }
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(targetValue = offsetX, label = "swipeOffset")

    Box(
        modifier = Modifier
            .width(trackWidthDp)
            .height(54.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(27.dp))
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "ස්වයිප් කර පිළිගන්න ➔",
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.Center)
        )

        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .size(thumbSizeDp)
                .clip(CircleShape)
                .background(Color(0xFF00E676))
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX >= maxDragPx * 0.65f) {
                                onAnswer()
                            }
                            offsetX = 0f
                        },
                        onDragCancel = {
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val newOffset = (offsetX + dragAmount).coerceIn(0f, maxDragPx)
                            offsetX = newOffset
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Swipe Answer",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

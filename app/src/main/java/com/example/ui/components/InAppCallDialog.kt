package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CallSession
import com.example.data.model.DirectChatMessage
import com.example.data.model.UserProfile
import com.example.ui.theme.PoliceNavy
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun InAppCallDialog(
    incomingCall: CallSession?,
    activeCall: CallSession?,
    currentUserProfile: UserProfile = UserProfile(),
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    onAcceptCall: (String) -> Unit,
    onDeclineCall: (String) -> Unit,
    onEndCall: (String) -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onDismiss: () -> Unit
) {
    val currentCall = incomingCall ?: activeCall ?: return

    // Normalizing user keys to accurately differentiate caller vs receiver
    val myKeys = listOf(
        DirectChatMessage.normalizeUserKey(currentUserProfile.email),
        DirectChatMessage.normalizeUserKey(currentUserProfile.userId),
        DirectChatMessage.normalizeUserKey(currentUserProfile.phoneNumber),
        currentUserProfile.email.trim().lowercase(),
        currentUserProfile.userId.trim(),
        currentUserProfile.phoneNumber.trim()
    ).filter { it.isNotBlank() && it != "unknown_user" }.distinct()

    val isMeCaller = if (myKeys.isNotEmpty()) {
        myKeys.contains(currentCall.callerId) ||
                (currentCall.callerEmail.isNotBlank() && myKeys.contains(currentCall.callerEmail)) ||
                (currentCall.callerUserId.isNotBlank() && myKeys.contains(currentCall.callerUserId))
    } else {
        incomingCall == null && activeCall != null
    }

    val isIncomingRinging = !isMeCaller && (incomingCall != null || currentCall.status == CallSession.STATUS_RINGING)

    // Call duration timer for connected calls
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

    // Auto dismiss when call ends, declines, or misses
    LaunchedEffect(currentCall.status) {
        if (currentCall.status == CallSession.STATUS_ENDED ||
            currentCall.status == CallSession.STATUS_DECLINED ||
            currentCall.status == CallSession.STATUS_MISSED
        ) {
            delay(1500L)
            onDismiss()
        }
    }

    // Auto timeout for unanswered ringing call (35 seconds)
    LaunchedEffect(currentCall.status) {
        if (currentCall.status == CallSession.STATUS_RINGING) {
            delay(35000L)
            if (isIncomingRinging) {
                onDeclineCall(currentCall.callId)
            } else {
                onEndCall(currentCall.callId)
            }
        }
    }

    // Pulsing animation for avatar and answer button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Name and info of the other person in the call
    val partnerName = if (isMeCaller) {
        currentCall.receiverName.ifBlank { "User" }
    } else {
        currentCall.callerName.ifBlank { "User" }
    }

    val partnerPhone = if (isMeCaller) currentCall.receiverPhone else currentCall.callerPhone

    Dialog(
        onDismissRequest = { /* Prevent accidental dismiss during active call */ },
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
                            Color(0xFF031B33),
                            PoliceNavy,
                            Color(0xFF0A192F)
                        )
                    )
                )
                .testTag("in_app_call_screen"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header: Status & Call Type
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (currentCall.status) {
                                            CallSession.STATUS_CONNECTED -> Color(0xFF4CAF50)
                                            CallSession.STATUS_ENDED, CallSession.STATUS_DECLINED, CallSession.STATUS_MISSED -> Color(0xFFF44336)
                                            else -> Color(0xFFFFB300)
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (currentCall.status) {
                                    CallSession.STATUS_CONNECTED -> "🔒 සජීවී හඬ ඇමතුම (Voice Call)"
                                    else -> if (isIncomingRinging) "📞 ලැබෙන ඇමතුම (Incoming Call)" else "📞 පිටතට යන ඇමතුම (Outgoing Call)"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Display Name of the other party
                    Text(
                        text = partnerName.ifBlank { "User" },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    )

                    if (partnerPhone.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = partnerPhone,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Call Status Line
                    Text(
                        text = when (currentCall.status) {
                            CallSession.STATUS_RINGING -> {
                                if (isIncomingRinging) {
                                    "📞 ලැබෙන ඇමතුම (Incoming Voice Call)..."
                                } else {
                                    "අමතමින්... (Calling...)"
                                }
                            }
                            CallSession.STATUS_CONNECTED -> {
                                val minutes = callDurationSeconds / 60
                                val seconds = callDurationSeconds % 60
                                "🟢 සම්බන්ධයි • %02d:%02d".format(minutes, seconds)
                            }
                            CallSession.STATUS_DECLINED -> "🔴 ඇමතුම ප්‍රතික්ෂේප විය (Declined)"
                            CallSession.STATUS_MISSED -> "🔴 පිළිතුරක් නැත (No Answer)"
                            CallSession.STATUS_ENDED -> "⏹️ ඇමතුම අවසන් විය (Call Ended)"
                            else -> "ඇමතුම සක්‍රියයි"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = when (currentCall.status) {
                                CallSession.STATUS_CONNECTED -> Color(0xFF81C784)
                                CallSession.STATUS_DECLINED, CallSession.STATUS_ENDED, CallSession.STATUS_MISSED -> Color(0xFFFF8A80)
                                else -> Color.White.copy(alpha = 0.9f)
                            },
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    )
                }

                // Center: Avatar with Pulsing Effect
                Box(
                    modifier = Modifier.size(190.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer animated pulse ring (active when ringing)
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .scale(if (currentCall.status == CallSession.STATUS_RINGING) pulseScale else 1f)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    )

                    // Inner pulse ring
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .scale(if (currentCall.status == CallSession.STATUS_RINGING) (pulseScale * 0.95f) else 1f)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                    )

                    // Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF1976D2),
                                        Color(0xFF0D47A1)
                                    )
                                )
                            )
                            .border(3.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = partnerName.take(1).uppercase().ifBlank { "👤" },
                            style = MaterialTheme.typography.displaySmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 44.sp
                            )
                        )
                    }
                }

                // Equalizer Bar when Connected
                if (currentCall.status == CallSession.STATUS_CONNECTED) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(28.dp)
                    ) {
                        val heights = listOf(14.dp, 24.dp, 10.dp, 28.dp, 18.dp, 26.dp, 12.dp)
                        heights.forEach { h ->
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(h)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFF81C784))
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(28.dp))
                }

                // Bottom: Action Controls
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isIncomingRinging && currentCall.status == CallSession.STATUS_RINGING) {
                        // INCOMING CALL CONTROLS: Decline & Answer Buttons + Swipe Option
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 🔴 Decline Button
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(
                                        onClick = { onDeclineCall(currentCall.callId) },
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFD32F2F))
                                            .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                            .testTag("decline_call_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CallEnd,
                                            contentDescription = "Decline Call",
                                            tint = Color.White,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "ප්‍රතික්ෂේප (Decline)",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // 🟢 Answer Button (Pulsing)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(
                                        onClick = { onAcceptCall(currentCall.callId) },
                                        modifier = Modifier
                                            .scale(pulseScale)
                                            .size(76.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2E7D32))
                                            .border(3.dp, Color.White, CircleShape)
                                            .testTag("accept_call_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = "Accept Call",
                                            tint = Color.White,
                                            modifier = Modifier.size(38.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "පිළිගන්න (Answer)",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Quick Swipe-to-Answer Bar
                            SwipeToAnswerBar(
                                onAnswer = { onAcceptCall(currentCall.callId) }
                            )
                        }
                    } else {
                        // OUTGOING / ACTIVE CALL CONTROLS: Mute, End Call (Red FAB), Speaker
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mute Toggle
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = onToggleMute,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(if (isMuted) Color.White else Color.White.copy(alpha = 0.2f))
                                ) {
                                    Icon(
                                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                        contentDescription = "Mute",
                                        tint = if (isMuted) Color.Black else Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isMuted) "Unmute" else "Mute",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }

                            // End Call Button (Red FAB)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = { onEndCall(currentCall.callId) },
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD32F2F))
                                        .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                        .testTag("end_call_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CallEnd,
                                        contentDescription = "End Call",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "අවසන් (End)",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Speakerphone Toggle
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = onToggleSpeaker,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(if (isSpeakerOn) Color(0xFF1976D2) else Color.White.copy(alpha = 0.2f))
                                ) {
                                    Icon(
                                        imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                                        contentDescription = "Speaker",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isSpeakerOn) "Speaker On" else "Speaker Off",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
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
        // Label in background
        Text(
            text = "ස්වයිප් කර පිළිගන්න ➔",
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.Center)
        )

        // Draggable green thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .pointerInput(maxDragPx) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            offsetX = (offsetX + dragAmount).coerceIn(0f, maxDragPx)
                            if (offsetX >= maxDragPx * 0.85f) {
                                offsetX = maxDragPx
                                onAnswer()
                            }
                        },
                        onDragEnd = {
                            if (offsetX < maxDragPx * 0.85f) {
                                offsetX = 0f
                            }
                        },
                        onDragCancel = {
                            offsetX = 0f
                        }
                    )
                }
                .size(thumbSizeDp)
                .clip(CircleShape)
                .background(Color(0xFF2E7D32)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhoneInTalk,
                contentDescription = "Swipe to answer",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

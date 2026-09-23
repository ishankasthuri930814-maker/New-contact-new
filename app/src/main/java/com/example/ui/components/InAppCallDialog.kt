package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CallSession
import com.example.ui.theme.PoliceNavy
import kotlinx.coroutines.delay

@Composable
fun InAppCallDialog(
    incomingCall: CallSession?,
    activeCall: CallSession?,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    onAcceptCall: (String) -> Unit,
    onDeclineCall: (String) -> Unit,
    onEndCall: (String) -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onDismiss: () -> Unit
) {
    val currentCall = activeCall ?: incomingCall ?: return
    val isIncoming = incomingCall != null && activeCall == null

    // Call duration timer
    var callDurationSeconds by remember { mutableLongStateOf(0L) }

    LaunchedEffect(currentCall.status, currentCall.connectedAt) {
        if (currentCall.status == CallSession.STATUS_CONNECTED) {
            val baseTime = if (currentCall.connectedAt > 0L) currentCall.connectedAt else System.currentTimeMillis()
            while (true) {
                callDurationSeconds = (System.currentTimeMillis() - baseTime) / 1000L
                delay(1000L)
            }
        }
    }

    // Auto dismiss when ended or declined
    LaunchedEffect(currentCall.status) {
        if (currentCall.status == CallSession.STATUS_ENDED || currentCall.status == CallSession.STATUS_DECLINED) {
            delay(1500L)
            onDismiss()
        }
    }

    // Pulsing animation for avatar
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val partnerName = if (isIncoming) currentCall.callerName else currentCall.receiverName
    val partnerAvatar = if (isIncoming) currentCall.callerAvatarIndex else currentCall.receiverAvatarIndex

    Dialog(
        onDismissRequest = { /* Prevent accidental dismiss */ },
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
                            Color(0xFF0D253A)
                        )
                    )
                )
                .testTag("in_app_call_screen"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header: Status & Call Type
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 24.dp)
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
                                            CallSession.STATUS_ENDED, CallSession.STATUS_DECLINED -> Color(0xFFF44336)
                                            else -> Color(0xFFFFB300)
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🌐 ඇප් සජීවී ඇමතුම (In-App Voice Call)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Text(
                        text = partnerName.ifBlank { "Citizen" },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = when (currentCall.status) {
                            CallSession.STATUS_RINGING -> {
                                if (isIncoming) "📞 ඔබව අමතයි (Incoming Voice Call)..." else "සම්බන්ධ වෙමින්... (Calling...)"
                            }
                            CallSession.STATUS_CONNECTED -> {
                                val minutes = callDurationSeconds / 60
                                val seconds = callDurationSeconds % 60
                                "🟢 සම්බන්ධයි • %02d:%02d".format(minutes, seconds)
                            }
                            CallSession.STATUS_DECLINED -> "🔴 ඇමතුම ප්‍රතික්ෂේප විය (Declined)"
                            CallSession.STATUS_ENDED -> "⏹️ ඇමතුම අවසන් විය (Call Ended)"
                            else -> "ඇමතුම සක්‍රියයි"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = when (currentCall.status) {
                                CallSession.STATUS_CONNECTED -> Color(0xFF81C784)
                                CallSession.STATUS_DECLINED, CallSession.STATUS_ENDED -> Color(0xFFFF8A80)
                                else -> Color.White.copy(alpha = 0.85f)
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
                    // Outer animated pulse ring
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
                            text = partnerName.take(1).uppercase().ifBlank { "👮" },
                            style = MaterialTheme.typography.displaySmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 44.sp
                            )
                        )
                    }
                }

                // Equalizer Bar / Voice Transmission Wave when Connected
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

                // Bottom: Action Buttons
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isIncoming && currentCall.status == CallSession.STATUS_RINGING) {
                        // Incoming Call: Answer & Decline Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Decline Button (Red)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = { onDeclineCall(currentCall.callId) },
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD32F2F))
                                        .testTag("decline_call_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CallEnd,
                                        contentDescription = "Decline Call",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "ප්‍රතික්ෂේප කරන්න",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Answer Button (Green)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = { onAcceptCall(currentCall.callId) },
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2E7D32))
                                        .testTag("accept_call_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Accept Call",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "පිළිගන්න",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // Active / Connected / Outgoing Call Controls
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
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(if (isMuted) Color.White else Color.White.copy(alpha = 0.2f))
                                ) {
                                    Icon(
                                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                        contentDescription = "Mute",
                                        tint = if (isMuted) Color.Black else Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isMuted) "Unmute" else "Mute",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }

                            // End Call Button (Red FAB)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = { onEndCall(currentCall.callId) },
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD32F2F))
                                        .testTag("end_call_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CallEnd,
                                        contentDescription = "End Call",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "ඇමතුම අවසන්",
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
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(if (isSpeakerOn) Color(0xFF1976D2) else Color.White.copy(alpha = 0.2f))
                                ) {
                                    Icon(
                                        imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                                        contentDescription = "Speaker",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isSpeakerOn) "Speaker On" else "Speaker Off",
                                    color = Color.White.copy(alpha = 0.8f),
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

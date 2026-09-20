package com.example.ui.components

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.PoliceGold
import com.example.ui.theme.PoliceNavy
import com.example.util.AppUpdateManager
import com.example.util.GitHubRelease
import com.example.util.UpdateStatus
import kotlinx.coroutines.launch

@Composable
fun AppUpdateDialog(
    status: UpdateStatus,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = {
            if (status !is UpdateStatus.Downloading) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = status !is UpdateStatus.Downloading,
            dismissOnClickOutside = status !is UpdateStatus.Downloading,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 12.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PoliceNavy)
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PoliceGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = PoliceNavy,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "නව යාවත්කාලීනයක් ඇත!",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            )
                            Text(
                                text = "යෙදුමේ නව සංස්කරණයක් නිකුත් කර ඇත",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PoliceGold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    if (status !is UpdateStatus.Downloading) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(32.dp)
                                .testTag("update_dialog_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Content Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (status) {
                        is UpdateStatus.UpdateAvailable -> {
                            val release = status.release
                            UpdateAvailableContent(
                                release = release,
                                onUpdateNow = {
                                    scope.launch {
                                        AppUpdateManager.downloadApk(context, release)
                                    }
                                },
                                onLater = onDismiss
                            )
                        }
                        is UpdateStatus.Downloading -> {
                            DownloadingContent(
                                progress = status.progressPercent,
                                bytesRead = status.bytesRead,
                                totalBytes = status.totalBytes
                            )
                        }
                        is UpdateStatus.ReadyToInstall -> {
                            ReadyToInstallContent(
                                apkFile = status.apkFile,
                                release = status.release,
                                onInstall = {
                                    if (activity != null) {
                                        AppUpdateManager.installApk(activity, status.apkFile)
                                    }
                                }
                            )
                        }
                        is UpdateStatus.Error -> {
                            ErrorContent(
                                message = status.message,
                                onRetry = {
                                    scope.launch {
                                        AppUpdateManager.checkForUpdates(context, isManualCheck = true)
                                    }
                                },
                                onDismiss = onDismiss
                            )
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateAvailableContent(
    release: GitHubRelease,
    onUpdateNow: () -> Unit,
    onLater: () -> Unit
) {
    val currentVer = AppUpdateManager.getCurrentVersionName()

    // Version Comparison Card
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "දැනට ඇති Version",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = "v$currentVer",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = PoliceNavy,
                modifier = Modifier.size(20.dp)
            )

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF10B981), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "NEW",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = release.tagName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    )
                }
                if (release.apkSize > 0) {
                    val mb = String.format("%.1f MB", release.apkSize / (1024f * 1024f))
                    Text(
                        text = "ප්‍රමාණය: $mb",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }

    // Release Notes / Description from GitHub
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "නව සංස්කරණයේ වෙනස්කම් (What's New):",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                fontSize = 12.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp, max = 150.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (release.releaseNotes.isNotBlank()) release.releaseNotes else "නව විශේෂාංග සහ වැඩිදියුණු කිරීම් ඇතුළත් කර ඇත.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF334155),
                        lineHeight = 18.sp,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }

    // Special User Note: If update fails, uninstall current app and install the downloaded APK
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFF1D4ED8),
                modifier = Modifier
                    .size(18.dp)
                    .padding(top = 1.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "විශේෂ සටහන:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E40AF),
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = "යම් හෙයකින් App එක කෙලින්ම Update නොවුවහොත්, දැනට ඇති App එක Uninstall කර ඔබගේ Download ෆෝල්ඩරයේ බාගත වී ඇති නව APK එක Install කරගන්න.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF1E3A8A),
                        lineHeight = 16.sp,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }

    // Buttons: Direct Update button & Later text button (removed GitHub / Browser buttons)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onUpdateNow,
            colors = ButtonDefaults.buttonColors(
                containerColor = PoliceNavy,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("update_now_button")
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = null,
                tint = PoliceGold,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "දැන් Update කරන්න (Direct APK)",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        TextButton(
            onClick = onLater,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .testTag("update_later_button")
        ) {
            Text(
                text = "පසුව (Later)",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Normal
                )
            )
        }
    }
}

@Composable
private fun DownloadingContent(
    progress: Int,
    bytesRead: Long,
    totalBytes: Long
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(
            progress = { progress / 100f },
            color = PoliceNavy,
            trackColor = Color(0xFFE2E8F0),
            modifier = Modifier.size(54.dp),
            strokeWidth = 5.dp
        )

        Text(
            text = "නව APK එක බාගත වෙමින් පවතී...",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
        )

        LinearProgressIndicator(
            progress = { progress / 100f },
            color = PoliceNavy,
            trackColor = Color(0xFFE2E8F0),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$progress%",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PoliceNavy
                )
            )
            if (totalBytes > 0) {
                val downloadedMb = String.format("%.1f", bytesRead / (1024f * 1024f))
                val totalMb = String.format("%.1f MB", totalBytes / (1024f * 1024f))
                Text(
                    text = "$downloadedMb / $totalMb",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B))
                )
            }
        }

        Text(
            text = "බාගත වූ වහාම Package Installer එක ස්වයංක්‍රීයව විවෘත වනු ඇත.",
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                fontSize = 11.sp
            )
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFF1D4ED8),
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 1.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "සටහන: App එක update නොවුවහොත්, පැරණි app එක uninstall කර Download ෆෝල්ඩරයේ ඇති අලුත් apk එක install කරගන්න.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF1E3A8A),
                        lineHeight = 15.sp,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun ReadyToInstallContent(
    apkFile: java.io.File,
    release: GitHubRelease,
    onInstall: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFDCFCE7)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF16A34A),
                modifier = Modifier.size(34.dp)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "බාගත කිරීම සාර්ථකයි!",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            )
            Text(
                text = "${release.tagName} ස්ථාපනය කිරීමට සූදානම්.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFF1D4ED8),
                    modifier = Modifier
                        .size(20.dp)
                        .padding(top = 1.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "වැදගත් සටහන:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E40AF),
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "යම් හෙයකින් App එක Update නොවී දෝෂයක් ආවහොත්, දැනට ඇති App එක Uninstall කර ඔබගේ Download ෆෝල්ඩරයේ ඇති අලුත් APK එක Install කරගන්න.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF1E3A8A),
                            lineHeight = 16.sp,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "දුරකථනය විසින් 'Install unknown apps' අවසරය ඉල්ලුවහොත් කරුණාකර එයට අවසර (Allow) ලබා දෙන්න.",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF92400E),
                        fontSize = 11.sp
                    )
                )
            }
        }

        Button(
            onClick = onInstall,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF16A34A),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("install_now_button")
        ) {
            Icon(
                imageVector = Icons.Default.SystemUpdate,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "දැන් Install කරන්න (Install Now)",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color(0xFFFEE2E2)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFDC2626),
                modifier = Modifier.size(28.dp)
            )
        }

        Text(
            text = "යාවත්කාලීන කිරීමේ දෝෂයක්",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF991B1B)
            )
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
        )

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = PoliceNavy),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("නැවත උත්සාහ කරන්න")
        }

        TextButton(onClick = onDismiss) {
            Text("වසන්න (Close)", color = Color(0xFF64748B))
        }
    }
}

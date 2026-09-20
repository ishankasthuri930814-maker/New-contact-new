package com.example.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.PoliceGold
import com.example.ui.theme.PoliceNavy
import com.example.util.AppUpdateManager
import com.example.util.AppNoticeManager
import com.example.util.UpdateStatus
import kotlinx.coroutines.launch

@Composable
fun AdminUpdateSettingsCard(
    modifier: Modifier = Modifier,
    onShowMessage: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val updateStatus by AppUpdateManager.updateStatus.collectAsStateWithLifecycle()

    var repoInput by remember { mutableStateOf(AppUpdateManager.getGitHubRepo(context)) }
    var showGuide by remember { mutableStateOf(false) }
    var isChecking by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PoliceNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = PoliceGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Auto-Update පද්ධතිය",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                fontSize = 15.sp
                            )
                        )
                        Text(
                            text = "GitHub Releases In-App Updates",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Current version badge
                Box(
                    modifier = Modifier
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "v${AppUpdateManager.getCurrentVersionName()}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PoliceNavy,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Description
            Text(
                text = "Play Store නොමැතිව, ඔබ GitHub Releases වෙත Upload කරන නව APK පරිශීලකයින්ගේ දුරකථනවලට ස්වයංක්‍රීයව ලැබෙන පද්ධතියයි.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF475569),
                    lineHeight = 18.sp,
                    fontSize = 12.sp
                )
            )

            // GitHub Repository Config
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "GitHub Repository (owner/repo):",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        fontSize = 12.sp
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = repoInput,
                        onValueChange = { repoInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_github_repo_input"),
                        placeholder = { Text("උදා: ishankasthuri930814-maker/New-contact-new", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PoliceNavy,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    Button(
                        onClick = {
                            AppUpdateManager.setGitHubRepo(context, repoInput)
                            onShowMessage("GitHub Repository සුරැකිණි!")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PoliceNavy),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("admin_save_repo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                            modifier = Modifier.size(16.dp),
                            tint = PoliceGold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save", fontSize = 12.sp)
                    }
                }
            }

            // Action Buttons: Check Now & Open GitHub
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        isChecking = true
                        scope.launch {
                            val res = AppUpdateManager.checkForUpdates(context, isManualCheck = true)
                            isChecking = false
                            when (res) {
                                is UpdateStatus.UpdateAvailable -> {
                                    onShowMessage("නව Update එකක් හමු විය: ${res.release.tagName}")
                                }
                                is UpdateStatus.NoUpdate -> {
                                    onShowMessage("ඔබගේ App එක දැනටමත් අලුත්ම Version (v${res.checkedVersion}) එකයි.")
                                }
                                is UpdateStatus.Error -> {
                                    onShowMessage(res.message)
                                }
                                else -> {}
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_check_update_now_button"),
                    enabled = !isChecking
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = PoliceGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isChecking) "පරීක්ෂා කරමින්..." else "Check Updates",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        val repo = AppUpdateManager.getGitHubRepo(context)
                        AppUpdateManager.openInBrowser(context, "https://github.com/$repo/releases")
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(0.9f)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = PoliceNavy
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Releases", fontSize = 12.sp, color = PoliceNavy)
                }
            }

            // Status feedback banner
            when (val s = updateStatus) {
                is UpdateStatus.UpdateAvailable -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "නව Update එකක් සූදානම්: ${s.release.tagName} (${s.release.title})",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF065F46),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
                is UpdateStatus.NoUpdate -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PoliceNavy,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "යෙදුම නවතම සංස්කරණයෙන් පවතී (v${s.checkedVersion})",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF334155),
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
                is UpdateStatus.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = s.message,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF991B1B),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
                else -> {}
            }

            // Expandable Step-by-Step Guide
            Column(modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = { showGuide = !showGuide },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = PoliceNavy,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showGuide) "උපදෙස් සඟවන්න (Hide Guide)" else "Update එකක් Release කරන ආකාරය බලන්න",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = PoliceNavy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = if (showGuide) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = PoliceNavy,
                        modifier = Modifier.size(18.dp)
                    )
                }

                AnimatedVisibility(visible = showGuide) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GuideStep(
                                number = "1",
                                title = "නව APK එක සාදා ගැනීම",
                                desc = "App එකේ build.gradle.kts හි versionName (උදා: 3.6) සහ versionCode වැඩි කර නව APK එකක් Export කරගන්න."
                            )
                            GuideStep(
                                number = "2",
                                title = "GitHub Releases වෙත යන්න",
                                desc = "ඔබගේ GitHub Repo එකට ගොස් දකුණු පස ඇති 'Releases' ➔ 'Draft a new release' ඔබන්න."
                            )
                            GuideStep(
                                number = "3",
                                title = "Tag එක සහ Title ලබාදීම",
                                desc = "Tag එකට 'v3.6' (හෝ නව අංකය) ලබා දී Title සහ නව වෙනස්කම් (Notes) සටහන් කරන්න."
                            )
                            GuideStep(
                                number = "4",
                                title = "APK එක Upload කිරීම",
                                desc = "'Attach binaries by dropping them here' කොටසට ඔබ සකස් කළ .apk file එක Drag කර ඇතුළත් කරන්න."
                            )
                            GuideStep(
                                number = "5",
                                title = "'Publish release' ක්ලික් කරන්න",
                                desc = "එපමණයි! සියලුම Users ලා App එක Open කරන විට නව Update එක Screen එක මත Popup වී කෙලින්ම Install කරගත හැක."
                            )
                        }
                    }
                }
            }

            // GitHub Instant Announcement / Notice Guide & Controls
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF4FF)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0ABFC)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF86198F)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "GitHub ක්ෂණික නිවේදන (Instant Notices)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF701A75),
                                    fontSize = 13.sp
                                )
                            )
                            Text(
                                text = "APK එකක් නැතුව පරිශීලකයින්ට Popup පණිවිඩ යැවීම",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF86198F),
                                    fontSize = 10.5.sp
                                )
                            )
                        }
                    }

                    Text(
                        text = "ඔබගේ GitHub Repo එක තුළ 'notice.json' නමින් file එකක් සාදා එහි පහත ආකාරයට සටහන් කළ විට සියලුම App users ලාට එය Popup එකක් ලෙස ලැබේ:\n\n{\n  \"id\": \"notice_1\",\n  \"active\": true,\n  \"title\": \"විශේෂ නිවේදනයයි\",\n  \"message\": \"ඔබගේ පණිවිඩය මෙහි ලියන්න...\",\n  \"type\": \"info\",\n  \"date\": \"2026-09-20\"\n}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF3B0764),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    AppNoticeManager.clearDismissedNotice(context)
                                    AppNoticeManager.checkForNotices(context, forceShow = true)
                                    onShowMessage("Notice පරීක්ෂා කරමින් පවතී...")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86198F))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color(0xFF86198F),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Notice Check",
                                fontSize = 11.sp,
                                color = Color(0xFF86198F)
                            )
                        }

                        Button(
                            onClick = {
                                val repo = AppUpdateManager.getGitHubRepo(context)
                                AppUpdateManager.openInBrowser(context, "https://github.com/$repo")
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF86198F)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open GitHub", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideStep(
    number: String,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(PoliceNavy),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    fontSize = 12.sp
                )
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF475569),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            )
        }
    }
}

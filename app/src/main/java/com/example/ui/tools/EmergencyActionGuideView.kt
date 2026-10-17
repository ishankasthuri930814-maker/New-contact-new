package com.example.ui.tools

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.PoliceNavy
import com.example.util.AppLanguage
import kotlinx.coroutines.delay

data class EmergencyGuideItem(
    val id: String,
    val titleSinhala: String,
    val titleEnglish: String,
    val emergencyPhone: String,
    val icon: ImageVector,
    val themeColor: Color,
    val urgentNotice: String,
    val steps: List<String>,
    val donts: List<String>
)

@Composable
fun EmergencyActionGuideView(
    selectedLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expandedId by remember { mutableStateOf<String?>("cpr") }

    val guides = remember {
        listOf(
            EmergencyGuideItem(
                id = "cpr",
                titleSinhala = "හදවත් සම්බාහනය (CPR / Cardiopulmonary Resuscitation)",
                titleEnglish = "Cardiopulmonary Resuscitation (CPR Guide)",
                emergencyPhone = "1990",
                icon = Icons.Default.Favorite,
                themeColor = Color(0xFFDC2626),
                urgentNotice = "සිහිය නොමැති නම් හා සාමාන්‍ය පරිදි හුස්ම නොගන්නේ නම් වහාම 1990 අමතා CPR ආරම්භ කරන්න!",
                steps = listOf(
                    "1. ප්‍රතිචාරය පරීක්ෂා කරන්න: රෝගියාගේ උරහිස් සොලවා ශබ්ද නඟා කතා කරන්න.",
                    "2. වහාම 1990 සුවසැරිය ගිලන්රථ සේවය හෝ 119 අමතන්න.",
                    "3. දෑත් ස්ථානගත කරන්න: එක් අතක් රෝගියාගේ පපුව මධ්‍යයේ තබා අනෙක් අතින් එය අල්ලා ඇඟිලි පටලවා ගන්න.",
                    "4. පපුව තදින් හා වේගයෙන් ඔබන්න (මිනිත්තුවකට වාර 100-120 ක්, අඟල් 2ක් පමණ ගැඹුරට).",
                    "5. වාර 30ක් එබීමෙන් පසු මුඛයට මුඛය තබා හුස්ම 2ක් ලබා දෙන්න (හැකි නම්). වෛද්‍ය ආධාර පැමිණෙන තෙක් නොනවත්වා කරගෙන යන්න."
                ),
                donts = listOf(
                    "හුස්ම ගන්නා හෝ සිහිය ඇති පුද්ගලයෙකුට CPR නොකරන්න.",
                    "පපුව එබීම අතරතුර රෝගියාගේ පපුව සම්පූර්ණයෙන් නැවත ඉහළට ඒමට ඉඩ හැරීම අමතක නොකරන්න."
                )
            ),
            EmergencyGuideItem(
                id = "snake_bite",
                titleSinhala = "සර්ප දෂ්ටනය (Snake Bite First Aid)",
                titleEnglish = "Snake Bite Emergency First Aid",
                emergencyPhone = "1990",
                icon = Icons.Default.Healing,
                themeColor = Color(0xFFB45309),
                urgentNotice = "රෝගියා කලබල නොකර සන්සුන්ව තබා ගන්න. දෂ්ට කළ ස්ථානය චලනය නොකරන්න!",
                steps = listOf(
                    "1. රෝගියා නිශ්චලව හා සන්සුන්ව වාඩි කරවන්න හෝ නිදි කරවන්න (හෘද ස්පන්දනය වැඩිවීම විස ශරීරගත වීම වේගවත් කරයි).",
                    "2. දෂ්ට කළ අත හෝ පාදය සන්ධි නොනැමෙන සේ පතුරක් තබා වෙළන්න (කැඩුණු අතක් බඳින ආකාරයට).",
                    "3. මුදු, වළලු, හෝ තද ඇඳුම් ඉදිමීමට පෙර ගලවා ඉවත් කරන්න.",
                    "4. දෂ්ට කළ ස්ථානය හදවතට වඩා පහළින් තබන්න.",
                    "5. වහාම ප්‍රතිවිස (Anti-venom) සහිත ළඟම ඇති රජයේ රෝහල වෙත රෝගියා රැගෙන යන්න."
                ),
                donts = listOf(
                    "දෂ්ට කළ ස්ථානය කැපීම, පිලිස්සීම හෝ ලේ මිරිකීම නොකරන්න.",
                    "කටින් විස උරා බීම කිසිසේත් නොකරන්න.",
                    "ලේ ගමනාගමනය නතර වන සේ තදින් ලණු හෝ රෙදි පටි ගැට නොගසන්න (කළු වී අවයවය අහිමි විය හැක).",
                    "තෙල්, කොළ බෙහෙත් හෝ අයිස් නොතබන්න."
                )
            ),
            EmergencyGuideItem(
                id = "electric_shock",
                titleSinhala = "විදුලි සැර වැදීම (Electric Shock Emergency)",
                titleEnglish = "Electric Shock Rescue & Safety",
                emergencyPhone = "1990",
                icon = Icons.Default.FlashOn,
                themeColor = Color(0xFFEAB308),
                urgentNotice = "ප්‍රධාන ස්විචය (Main Switch / Trip Switch) ක්‍රියා විරහිත කිරීමට පෙර කිසිවිටෙක රෝගියා ඇල්ලීම නොකරන්න!",
                steps = listOf(
                    "1. ප්‍රධාන ස්විචය හෝ පේනුව ගලවා විදුලි සැපයුම වහාම අක්‍රීය කරන්න.",
                    "2. ස්විචය විසන්ධි කළ නොහැකි නම්, වියළි ලීයක්, ප්ලාස්ටික් හෝ රබර් ද්‍රව්‍යයක් භාවිතයෙන් රෝගියා විදුලි රැහැනෙන් ඈත් කරන්න.",
                    "3. විදුලි සැර වැදුණු පුද්ගලයා ස්පර්ශ කිරීමට පෙර වියළි රබර් සෙරෙප්පු හෝ පුවත්පත් තට්ටුවක් මත සිටගන්න.",
                    "4. විදුලියෙන් මුදාගත් පසු හුස්ම ගැනීම පරීක්ෂා කර අවශ්‍ය නම් වහාම CPR ආරම්භ කරන්න.",
                    "5. 1990 හෝ 119 අමතා රෝගියා හදිසි ප්‍රතිකාර සඳහා යොමු කරන්න."
                ),
                donts = listOf(
                    "තෙතමනය සහිත දෑතින් හෝ තෙත පොළොවේ සිට විදුලි රැහැන් ස්පර්ශ නොකරන්න.",
                    "ලෝහ හෝ සන්නායක උපකරණ ආධාරයෙන් රෝගියා මුදා ගැනීමට උත්සාහ නොකරන්න."
                )
            ),
            EmergencyGuideItem(
                id = "burns",
                titleSinhala = "පිළිස්සුම් තුවාල (Burns & Scalds Treatment)",
                titleEnglish = "Burns & Scalds Care Guide",
                emergencyPhone = "1990",
                icon = Icons.Default.Fireplace,
                themeColor = Color(0xFFEA580C),
                urgentNotice = "විනාඩි 10 - 20 ක් යනතුරු ගලා යන සිසිල් සාමාන්‍ය ජලයෙන් තුවාලය සෝදන්න!",
                steps = listOf(
                    "1. රෝගියා තාප ප්‍රභවයෙන් (ගින්දර, උණුසුම් දියර, වාෂ්ප) ඈත් කරන්න.",
                    "2. පිළිස්සුණු ස්ථානයට ගලා යන සිසිල් නළ ජලය විනාඩි 15ක් පමණ අඛණ්ඩව වත් කරන්න.",
                    "3. පිළිස්සුණු ප්‍රදේශයේ තුවාලයට නොඇලී ඇති ආභරණ හෝ ඇඳුම් ඉවත් කරන්න.",
                    "4. තුවාලය පිරිසිදු තෙත රෙදිකඩකින් හෝ පිරිසිදු පොලිතින් ආවරණයකින් ලිහිල්ව ආවරණය කරන්න.",
                    "5. තුවාලය විශාල නම් හෝ මුහුණ/අත්/සන්ධි ආශ්‍රිත නම් වහාම රෝහලකට යොමු වන්න."
                ),
                donts = listOf(
                    "අයිස් කැට හෝ අයිස් වතුර කෙලින්ම තුවාලය මත නොතබන්න (පටක හානි වේ).",
                    "දන්තාලේප (Toothpaste), බටර්, පොල්තෙල්, කෝපි කුඩු ආලේප නොකරන්න.",
                    "පිළිස්සුම් නිසා හටගන්නා දියපට්ටා (Blisters) නොකඩන්න."
                )
            ),
            EmergencyGuideItem(
                id = "bleeding",
                titleSinhala = "අධික රුධිර වහනය හා රිය අනතුරු (Severe Bleeding & Trauma)",
                titleEnglish = "Severe Bleeding & Road Accidents",
                emergencyPhone = "1990",
                icon = Icons.Default.Warning,
                themeColor = Color(0xFF991B1B),
                urgentNotice = "තුවාලය මත සෘජුව තද පීඩනයක් යොදමින් රුධිරය පිටවීම පාලනය කරන්න!",
                steps = listOf(
                    "1. පිරිසිදු රෙදිකඩක් හෝ ගෝස් පටියක් තුවාලය මත තබා ඔබේ අත්ලෙන් තදින් ඔබාගෙන සිටින්න.",
                    "2. හැකි නම් තුවාල වූ අත හෝ පාදය රෝගියාගේ හදවතේ මට්ටමට වඩා ඉහළට ඔසවා තබන්න.",
                    "3. පළමු රෙදිකඩ රුධිරයෙන් පෙඟුණහොත් එය ඉවත් නොකර, ඒ මත තවත් රෙදිකඩක් තබා තදින් වෙළන්න.",
                    "4. රෝගියා බිම නිදි කරවා උණුසුම්ව තබන්න (කම්පනය / Shock වැළැක්වීමට).",
                    "5. රිය අනතුරක් නම් ගෙල හා කොඳුඇට පෙළ චලනය නොවන සේ ආරක්ෂා කර 1990 ගිලන්රථය එනතෙක් රැඳෙන්න."
                ),
                donts = listOf(
                    "තුවාලය තුළට කිඳා බැස ඇති වීදුරු කැබලි හෝ ලෝහ කැබලි ගලවා ඉවත් නොකරන්න.",
                    "තුවාලය සේදීමට වතුර හෝ දියර භාවිත නොකරන්න."
                )
            ),
            EmergencyGuideItem(
                id = "choking",
                titleSinhala = "හුස්ම හිරවීම (Choking - Heimlich Maneuver)",
                titleEnglish = "Choking & Airway Obstruction",
                emergencyPhone = "1990",
                icon = Icons.Default.LocalHospital,
                themeColor = Color(0xFF0284C7),
                urgentNotice = "කතා කිරීමට හෝ හුස්ම ගැනීමට නොහැකිව බෙල්ල අල්ලා ගන්නේ නම් වහාම ක්‍රියාත්මක වන්න!",
                steps = listOf(
                    "1. රෝගියාට තදින් කැස්ස ඇති කිරීමට උනන්දු කරන්න.",
                    "2. පිටුපසින් සිට උරහිස් තල අතරට ඔබේ අත්ලේ විලුඹෙන් තදින් 5 වතාවක් පහර දෙන්න (Back blows).",
                    "3. හිරවූ දෙය ඉවත් නොවන්නේ නම්: රෝගියා පිටුපසින් සිට බඩවැල ඉහළින් (බුරියට ඉහළින්) මිට මොළවා අල්ලා අභ්‍යන්තරයට හා ඉහළට තදින් 5 වරක් තල්ලු කරන්න (Abdominal thrusts).",
                    "4. ස්වසන මාර්ගය පැහැදිලි වන තෙක් මෙම ක්‍රම මාරුවෙන් මාරුවට සිදු කරන්න.",
                    "5. රෝගියා සිහිසුන් වුවහොත් බිම නිදි කරවා CPR ආරම්භ කරන්න."
                ),
                donts = listOf(
                    "ඇසට නොපෙනෙන දේවල් ඇඟිල්ලෙන් අතපත ගා එළියට ගැනීමට උත්සාහ නොකරන්න (තවත් ඇතුළට යා හැක).",
                    "කුඩා ළදරුවන්ට වැඩිහිටියන් මෙන් උදරය තදින් එබීම නොකරන්න."
                )
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Quick 1-Tap Emergency Bar
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = BorderStroke(1.5.dp, Color(0xFFFCA5A5)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(EmergencyRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ක්ෂණික හදිසි ඇමතුම් (1-Tap Emergency Hotline)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = EmergencyRed
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EmergencyCallButton(
                            title = "1990 සුවසැරිය",
                            sub = "ගිලන්රථ",
                            number = "1990",
                            color = Color(0xFFDC2626),
                            modifier = Modifier.weight(1f),
                            context = context
                        )
                        EmergencyCallButton(
                            title = "119 පොලිසිය",
                            sub = "හදිසි සහන",
                            number = "119",
                            color = PoliceNavy,
                            modifier = Modifier.weight(1f),
                            context = context
                        )
                        EmergencyCallButton(
                            title = "110 ගිනි නිවීම්",
                            sub = "ආපදා",
                            number = "110",
                            color = Color(0xFFD97706),
                            modifier = Modifier.weight(1f),
                            context = context
                        )
                    }
                }
            }
        }

        // Metronome / Interactive CPR Helper
        item {
            CprInteractiveHelperCard()
        }

        // Guide List
        items(guides, key = { it.id }) { item ->
            val isExpanded = expandedId == item.id
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = if (isExpanded) 1.5.dp else 1.dp,
                    color = if (isExpanded) item.themeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(if (isExpanded) 4.dp else 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedId = if (isExpanded) null else item.id }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(item.themeColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.titleEnglish,
                                tint = item.themeColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.titleSinhala,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = item.titleEnglish,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            )
                        }
                        IconButton(
                            onClick = { expandedId = if (isExpanded) null else item.id },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ChevronRight,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(modifier = Modifier.padding(top = 14.dp)) {
                            // Urgent Notice Box
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = item.themeColor.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, item.themeColor.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Alert",
                                        tint = item.themeColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.urgentNotice,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = item.themeColor,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "පියවරෙන් පියවර උපදෙස් (Action Steps):",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            item.steps.forEach { step ->
                                Row(
                                    modifier = Modifier.padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Step",
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = step,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }

                            if (item.donts.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "නොකළ යුතු දේ (Do NOT do):",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFDC2626)
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                item.donts.forEach { dont ->
                                    Text(
                                        text = "• $dont",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            color = Color(0xFF991B1B)
                                        ),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.emergencyPhone}"))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = item.themeColor),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "වහාම අමතන්න (${item.emergencyPhone})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
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
fun EmergencyCallButton(
    title: String,
    sub: String,
    number: String,
    color: Color,
    modifier: Modifier = Modifier,
    context: Context
) {
    Surface(
        onClick = {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
            context.startActivity(intent)
        },
        shape = RoundedCornerShape(12.dp),
        color = color,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp
            )
            Text(
                text = sub,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun CprInteractiveHelperCard() {
    var isRunning by remember { mutableStateOf(false) }
    var beatCount by remember { mutableIntStateOf(0) }
    var pulseActive by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            // CPR rhythm: 110 beats per minute -> ~545ms per compression
            while (isRunning) {
                pulseActive = true
                delay(120)
                pulseActive = false
                beatCount = (beatCount % 30) + 1
                delay(425)
            }
        } else {
            beatCount = 0
            pulseActive = false
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (pulseActive) Color(0xFFEF4444) else Color(0xFF334155)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isRunning) "$beatCount" else "CPR",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CPR රිද්ම සහායක (100-120 bpm Metronome)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = if (isRunning) "පපුව ඔබන රිද්මය: $beatCount / 30" else "සම්බාහන වේගය පාලනයට මෙතන ඔබන්න",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
            IconButton(
                onClick = { isRunning = !isRunning },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isRunning) Color(0xFFDC2626) else Color(0xFF16A34A))
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Stop" else "Start",
                    tint = Color.White
                )
            }
        }
    }
}

package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PoliceGold
import com.example.ui.theme.PoliceNavy
import com.example.ui.tools.DailyCashCalculatorView
import com.example.ui.tools.EmergencyActionGuideView
import com.example.ui.tools.PersonalItemTrackerView
import com.example.ui.tools.PhoneSensorCheckerView
import com.example.ui.tools.VehicleDocumentReminderView
import com.example.util.AppLanguage

enum class SubToolType {
    NONE,
    EMERGENCY_GUIDE,
    VEHICLE_REMINDER,
    CASH_CALCULATOR,
    SENSOR_CHECKER,
    ITEM_TRACKER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreToolsScreen(
    selectedLanguage: AppLanguage,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTool by remember { mutableStateOf(SubToolType.NONE) }

    // Hardware back handler
    BackHandler {
        if (activeTool != SubToolType.NONE) {
            activeTool = SubToolType.NONE
        } else {
            onNavigateBack()
        }
    }

    AnimatedContent(
        targetState = activeTool,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "MoreToolsScreenTransition"
    ) { currentTool ->
        when (currentTool) {
            SubToolType.NONE -> {
                ToolsDashboardView(
                    selectedLanguage = selectedLanguage,
                    onNavigateBack = onNavigateBack,
                    onSelectTool = { activeTool = it },
                    modifier = modifier
                )
            }
            SubToolType.EMERGENCY_GUIDE -> {
                ToolContainerScaffold(
                    title = when (selectedLanguage) {
                        AppLanguage.SINHALA -> "හදිසි අවස්ථා මාර්ගෝපදේශය (Emergency Action Guide)"
                        AppLanguage.TAMIL -> "அவசர நடவடிக்கை வழிகாட்டி (Emergency Action Guide)"
                        AppLanguage.ENGLISH -> "Emergency Action Guide"
                    },
                    themeColor = Color(0xFFDC2626),
                    onBack = { activeTool = SubToolType.NONE }
                ) {
                    EmergencyActionGuideView(
                        selectedLanguage = selectedLanguage,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            SubToolType.VEHICLE_REMINDER -> {
                ToolContainerScaffold(
                    title = when (selectedLanguage) {
                        AppLanguage.SINHALA -> "වාහන ලේඛන මතක් කිරීම් (Vehicle Document Reminder)"
                        AppLanguage.TAMIL -> "வாகன ஆவண நினைவூட்டல் (Vehicle Document Reminder)"
                        AppLanguage.ENGLISH -> "Vehicle Document Reminder"
                    },
                    themeColor = Color(0xFF1D4ED8),
                    onBack = { activeTool = SubToolType.NONE }
                ) {
                    VehicleDocumentReminderView(
                        selectedLanguage = selectedLanguage,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            SubToolType.CASH_CALCULATOR -> {
                ToolContainerScaffold(
                    title = when (selectedLanguage) {
                        AppLanguage.SINHALA -> "දෛනික මුදල් ගණකය (Daily Cash Calculator)"
                        AppLanguage.TAMIL -> "தினசரி பணக் கணிப்பான் (Daily Cash Calculator)"
                        AppLanguage.ENGLISH -> "Daily Cash Calculator"
                    },
                    themeColor = Color(0xFF15803D),
                    onBack = { activeTool = SubToolType.NONE }
                ) {
                    DailyCashCalculatorView(
                        selectedLanguage = selectedLanguage,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            SubToolType.SENSOR_CHECKER -> {
                ToolContainerScaffold(
                    title = when (selectedLanguage) {
                        AppLanguage.SINHALA -> "දුරකථන සංවේදක පරීක්ෂාව (Phone Sensor Checker)"
                        AppLanguage.TAMIL -> "தொலைபேசி சென்சார் சோதனையாளர் (Phone Sensor Checker)"
                        AppLanguage.ENGLISH -> "Phone Sensor Checker"
                    },
                    themeColor = Color(0xFF7E22CE),
                    onBack = { activeTool = SubToolType.NONE }
                ) {
                    PhoneSensorCheckerView(
                        selectedLanguage = selectedLanguage,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            SubToolType.ITEM_TRACKER -> {
                ToolContainerScaffold(
                    title = when (selectedLanguage) {
                        AppLanguage.SINHALA -> "වැදගත් තොරතුරු සුරැකුම (Personal Item Tracker)"
                        AppLanguage.TAMIL -> "தனிப்பட்ட உருப்படி டிராக்கர் (Personal Item Tracker)"
                        AppLanguage.ENGLISH -> "Personal Item Tracker"
                    },
                    themeColor = Color(0xFFB45309),
                    onBack = { activeTool = SubToolType.NONE }
                ) {
                    PersonalItemTrackerView(
                        selectedLanguage = selectedLanguage,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolContainerScaffold(
    title: String,
    themeColor: Color,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("tool_scaffold_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = themeColor
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolsDashboardView(
    selectedLanguage: AppLanguage,
    onNavigateBack: () -> Unit,
    onSelectTool: (SubToolType) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = PoliceGold.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = PoliceGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = when (selectedLanguage) {
                                    AppLanguage.SINHALA -> "තවත් මෙවලම් (More Tools)"
                                    AppLanguage.TAMIL -> "கூடுதல் கருவிகள் (More Tools)"
                                    AppLanguage.ENGLISH -> "More Tools & Utilities"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = when (selectedLanguage) {
                                    AppLanguage.SINHALA -> "ප්‍රයෝජනවත් දෛනික මෙවලම් එකතුව"
                                    AppLanguage.TAMIL -> "பயனுள்ள தினசரி கருவிகள்"
                                    AppLanguage.ENGLISH -> "Useful Emergency & Daily Utilities"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("tools_dashboard_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PoliceNavy
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PoliceNavy.copy(alpha = 0.05f)
                    ),
                    border = BorderStroke(1.dp, PoliceNavy.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PoliceNavy,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = PoliceGold,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (selectedLanguage) {
                                    AppLanguage.SINHALA -> "විශේෂාංග හා අතිරේක මෙවලම්"
                                    AppLanguage.TAMIL -> "சிறப்பு கருவிகள் மற்றும் சேவைகள்"
                                    AppLanguage.ENGLISH -> "Smart Utilities for Sri Lanka"
                                },
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PoliceNavy
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when (selectedLanguage) {
                                    AppLanguage.SINHALA -> "ඔබට අවශ්‍ය මෙවලම තෝරා ක්ෂණිකව ප්‍රයෝජනයට ගන්න."
                                    AppLanguage.TAMIL -> "உங்களுக்கு தேவையான கருவியைத் தேர்ந்தெடுக்கவும்."
                                    AppLanguage.ENGLISH -> "Select a utility tool below to launch instantly."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            // 1. Emergency Action Guide
            item {
                ToolMenuCard(
                    title = "Emergency Action Guide",
                    sinhalaTitle = "හදිසි අවස්ථා මාර්ගෝපදේශය",
                    tagline = "හදිසි අවස්ථාවල ප්‍රයෝජනවත් wena lesata",
                    description = when (selectedLanguage) {
                        AppLanguage.SINHALA -> "CPR හෘද සම්බාහනය, සර්ප දෂ්ටන, ගිනි ගැනීම්, හදිසි අනතුරු සහ 1990/119 ක්ෂණික ඇමතුම් පියවර"
                        AppLanguage.TAMIL -> "CPR, பாம்புக்கடி, தீ விபத்துக்கள் மற்றும் 1990/119 அவசர அழைப்புகள்"
                        AppLanguage.ENGLISH -> "Life-saving CPR guides, snake bite, fire actions & emergency hotlines."
                    },
                    icon = Icons.Default.Favorite,
                    badgeText = "හදිසි අවස්ථා (Emergency)",
                    accentColor = Color(0xFFDC2626),
                    lightBgColor = Color(0xFFFEF2F2),
                    borderColor = Color(0xFFFECACA),
                    testTag = "tool_card_emergency_guide",
                    onClick = { onSelectTool(SubToolType.EMERGENCY_GUIDE) }
                )
            }

            // 2. Vehicle Document Reminder
            item {
                ToolMenuCard(
                    title = "Vehicle Document Reminder",
                    sinhalaTitle = "වාහන ලේඛන මතක් කිරීම්",
                    tagline = "රියදුරන්ට ප්‍රයෝජනවත් wena lesa",
                    description = when (selectedLanguage) {
                        AppLanguage.SINHALA -> "ආදායම් බලපත්‍ර, වාහන රක්ෂණ, රියදුරු බලපත්‍ර සහ සේවා වාර කල් ඉකුත්වීම් නිසි කලට මතක් කිරීම"
                        AppLanguage.TAMIL -> "வருமான வரி அனுமதி, காப்புறுதி, ஓட்டுநர் அனுமதி நினைவூட்டல்"
                        AppLanguage.ENGLISH -> "Expiry trackers for Revenue License, Insurance, Driving License & Service."
                    },
                    icon = Icons.Default.DirectionsCar,
                    badgeText = "රියදුරන් සඳහා (Drivers)",
                    accentColor = Color(0xFF1D4ED8),
                    lightBgColor = Color(0xFFEFF6FF),
                    borderColor = Color(0xFFBFDBFE),
                    testTag = "tool_card_vehicle_reminder",
                    onClick = { onSelectTool(SubToolType.VEHICLE_REMINDER) }
                )
            }

            // 3. Daily Cash Calculator
            item {
                ToolMenuCard(
                    title = "Daily Cash Calculator",
                    sinhalaTitle = "දෛනික මුදල් ගණකය",
                    tagline = "දෛනික මුදල් කළමනාකරණයට ප්‍රයෝජනවත් wena lesa",
                    description = when (selectedLanguage) {
                        AppLanguage.SINHALA -> "ශ්‍රී ලංකා මුදල් නෝට්ටු (රු. 5000 - රු. 20) සහ කාසි (රු. 10 - රු. 1) එකතුව පහසුවෙන් ගණනය කිරීම"
                        AppLanguage.TAMIL -> "இலங்கை ரூபாய் நோட்டுகள் மற்றும் நாணயங்களை எளிதாக கணக்கிட"
                        AppLanguage.ENGLISH -> "Cash counter for Sri Lankan banknotes (Rs. 5000 - 20) and coins."
                    },
                    icon = Icons.Default.Calculate,
                    badgeText = "මුදල් කළමනාකරණය (Cash)",
                    accentColor = Color(0xFF15803D),
                    lightBgColor = Color(0xFFF0FDF4),
                    borderColor = Color(0xFFBBF7D0),
                    testTag = "tool_card_cash_calculator",
                    onClick = { onSelectTool(SubToolType.CASH_CALCULATOR) }
                )
            }

            // 4. Phone Sensor Checker
            item {
                ToolMenuCard(
                    title = "Phone Sensor Checker",
                    sinhalaTitle = "දුරකථන සංවේදක පරීක්ෂාව",
                    tagline = "දුරකථනයේ Sensors පරීක්ෂා කිරීමට",
                    description = when (selectedLanguage) {
                        AppLanguage.SINHALA -> "දුරකථනයේ Accelerometer, Gyroscope, ආලෝක සංවේදකය, Proximity සංවේදකය සජීවීව පරීක්ෂා කිරීම"
                        AppLanguage.TAMIL -> "தொலைபேசி சென்சார்கள் (Accelerometer, Gyroscope, Light, Proximity) சோதனை"
                        AppLanguage.ENGLISH -> "Live testing for Accelerometer, Gyroscope, Compass, Light & Proximity."
                    },
                    icon = Icons.Default.Sensors,
                    badgeText = "Hardware Sensors",
                    accentColor = Color(0xFF7E22CE),
                    lightBgColor = Color(0xFFFAF5FF),
                    borderColor = Color(0xFFE9D5FF),
                    testTag = "tool_card_sensor_checker",
                    onClick = { onSelectTool(SubToolType.SENSOR_CHECKER) }
                )
            }

            // 5. Personal Item Tracker
            item {
                ToolMenuCard(
                    title = "Personal Item Tracker",
                    sinhalaTitle = "වැදගත් තොරතුරු සුරැකුම",
                    tagline = "වැදගත් තොරතුරු එකම තැනක සුරැකීමට",
                    description = when (selectedLanguage) {
                        AppLanguage.SINHALA -> "හැඳුනුම්පත් අංක, රියදුරු බලපත්‍ර, ගිණුම් අංක, පාස්පෝට් ආදී වැදගත් දත්ත ආරක්ෂිතව උපාංගයේ සුරැකීම"
                        AppLanguage.TAMIL -> "முக்கிய அடையாள ஆவணங்கள், வங்கி கணக்குகள், கடவுச்சீட்டு தரவுகளை சேமிக்க"
                        AppLanguage.ENGLISH -> "Secure local vault for IDs, Driving License, Passports, and Account numbers."
                    },
                    icon = Icons.Default.Lock,
                    badgeText = "ආරක්ෂිත සුරැකුම (Vault)",
                    accentColor = Color(0xFFB45309),
                    lightBgColor = Color(0xFFFFFBEB),
                    borderColor = Color(0xFFFDE68A),
                    testTag = "tool_card_item_tracker",
                    onClick = { onSelectTool(SubToolType.ITEM_TRACKER) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ToolMenuCard(
    title: String,
    sinhalaTitle: String,
    tagline: String,
    description: String,
    icon: ImageVector,
    badgeText: String,
    accentColor: Color,
    lightBgColor: Color,
    borderColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = lightBgColor,
                    border = BorderStroke(1.5.dp, borderColor),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = accentColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accentColor.copy(alpha = 0.12f),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    color = accentColor
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "$sinhalaTitle • $tagline",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = accentColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "විවෘත කරන්න (Open Tool)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

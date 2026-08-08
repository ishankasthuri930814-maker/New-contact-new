package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ContactCategory
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.example.ui.components.ContactCard
import com.example.ui.components.ContactDetailBottomSheet
import com.example.ui.components.EmergencyHeader
import com.example.ui.theme.PoliceGold
import com.example.ui.theme.PoliceNavy

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PoliceScreen(
    viewModel: PoliceViewModel,
    modifier: Modifier = Modifier,
    onOpenPhoneAuth: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showInfoDialog by remember { mutableStateOf(true) }
    var showAiSearchDialog by remember { mutableStateOf(false) }
    var aiSearchQuery by remember { mutableStateOf("") }

    // Handle user messages in Snackbar
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    val rotationAngle by animateFloatAsState(
        targetValue = if (uiState.isRefreshing) 360f else 0f,
        label = "RefreshRotation"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.navigationBars,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { BannerAdView() },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PoliceNavy,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PoliceGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalPolice,
                                contentDescription = null,
                                tint = PoliceNavy,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ALL Police Contact",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Synced: ${uiState.lastSyncTime}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenPhoneAuth,
                        modifier = Modifier.testTag("otp_verify_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone OTP Verification",
                            tint = PoliceGold
                        )
                    }

                    Surface(
                        onClick = {
                            aiSearchQuery = uiState.searchQuery
                            showAiSearchDialog = true
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = PoliceGold,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("top_ai_search_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "AI Google Search",
                                tint = PoliceNavy,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "✨ AI Search",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PoliceNavy,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier.testTag("info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "App Creator Info",
                            tint = PoliceGold
                        )
                    }
                    IconButton(
                        onClick = { viewModel.loadContacts(forceRefresh = true) },
                        modifier = Modifier.testTag("refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync Sheet Data",
                            modifier = Modifier.rotate(rotationAngle)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Field & Category Filter Bar Header Section
                Surface(
                    color = PoliceNavy,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        // Search Bar
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            placeholder = {
                                Text(
                                    text = "Search station, officer, phone or email...",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = PoliceNavy
                                )
                            },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear search",
                                            tint = PoliceNavy
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = PoliceGold,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = PoliceNavy,
                                unfocusedTextColor = PoliceNavy
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_text_field")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Category Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(ContactCategory.values()) { category ->
                                val isSelected = uiState.selectedCategory == category
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.onCategorySelect(category) },
                                    label = {
                                        Text(
                                            text = "${category.displayName} (${category.sinhalaName})",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = Color.White.copy(alpha = 0.15f),
                                        labelColor = Color.White,
                                        selectedContainerColor = PoliceGold,
                                        selectedLabelColor = PoliceNavy
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = Color.White.copy(alpha = 0.3f),
                                        selectedBorderColor = PoliceGold
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.testTag("filter_chip_${category.name}")
                                )
                            }
                        }
                    }
                }

                // Main Contacts List Content
                if (uiState.isLoading && uiState.contacts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = PoliceNavy)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading Police Directory...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("contacts_lazy_column"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Emergency Header Card (Only show if no search query or in All/Emergency category)
                        if (uiState.searchQuery.isEmpty() && (uiState.selectedCategory == ContactCategory.ALL || uiState.selectedCategory == ContactCategory.EMERGENCY)) {
                            item {
                                EmergencyHeader(
                                    onEmergencyCall = { number -> viewModel.makePhoneCall(context, number) },
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }

                        // Results count indicator
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Directory Results (${uiState.filteredContacts.size})",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )

                                if (uiState.searchQuery.isNotEmpty()) {
                                    Text(
                                        text = "Query: \"${uiState.searchQuery}\"",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Contact items
                        if (uiState.filteredContacts.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.SearchOff,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(52.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "මෙම ඇප් එකේ සටහන් වී නැත / Not Found in App",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "ඇප් එකේ නොමැති ඕනෑම අංකයක් හෝ නමක් ගූගල් සෙවුම හරහා සෘජුවම ලබා ගන්න",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = {
                                                aiSearchQuery = uiState.searchQuery
                                                showAiSearchDialog = true
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = PoliceNavy),
                                            modifier = Modifier.testTag("empty_state_ai_search_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = null,
                                                tint = PoliceGold,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (uiState.searchQuery.isNotEmpty()) "🔍 '${uiState.searchQuery}' ගූගල් හි සොයන්න" else "✨ AI Google Search",
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            items(
                                items = uiState.filteredContacts,
                                key = { it.id }
                            ) { contact ->
                                ContactCard(
                                    contact = contact,
                                    onCallClick = { phone -> viewModel.makePhoneCall(context, phone) },
                                    onEmailClick = { email, station -> viewModel.sendEmail(context, email, station) },
                                    onFavoriteToggle = { c -> viewModel.toggleFavorite(c) },
                                    onShareClick = { c -> viewModel.shareContact(context, c) },
                                    onCardClick = { c -> viewModel.openContactDetail(c) }
                                )
                            }
                        }
                    }
                }
            }

            // Contact Detail Modal Bottom Sheet
            uiState.selectedContactForDetail?.let { selectedContact ->
                ContactDetailBottomSheet(
                    contact = selectedContact,
                    sheetState = sheetState,
                    onDismiss = { viewModel.closeContactDetail() },
                    onCallClick = { phone -> viewModel.makePhoneCall(context, phone) },
                    onEmailClick = { email, station -> viewModel.sendEmail(context, email, station) },
                    onCopyClick = { text, label -> viewModel.copyToClipboard(context, text, label) },
                    onShareClick = { c -> viewModel.shareContact(context, c) },
                    onFavoriteToggle = { c -> viewModel.toggleFavorite(c) }
                )
            }

            // Creator Info / Welcome Dialog
            if (showInfoDialog) {
                Dialog(
                    onDismissRequest = { showInfoDialog = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 10.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Top Header Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(PoliceNavy)
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.align(Alignment.CenterStart)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(PoliceGold),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalPolice,
                                            contentDescription = null,
                                            tint = PoliceNavy,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "සාදරයෙන් පිළිගනිමු",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 16.sp
                                            )
                                        )
                                        Text(
                                            text = "Sri Lanka Services Directory",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = PoliceGold,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { showInfoDialog = false },
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .size(32.dp)
                                        .testTag("welcome_dialog_close_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Content Container
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "මෙම යෙදුමෙහි ඇතුළත් ප්‍රධාන සේවාවන් 07:",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A),
                                        fontSize = 13.sp
                                    )
                                )

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    WelcomeFeatureRow(
                                        number = "1",
                                        titleSi = "පොලිස් කොට්ඨාස, කලාප සහ ජ්‍යෙෂ්ඨ නිලධාරීන්",
                                        titleEn = "Police Stations & Senior Officers",
                                        badgeColor = Color(0xFF0D1B2A),
                                        icon = Icons.Default.LocalPolice
                                    )
                                    WelcomeFeatureRow(
                                        number = "2",
                                        titleSi = "රජයේ දෙපාර්තමේන්තු සහ සේවා",
                                        titleEn = "Govt Departments & Services",
                                        badgeColor = Color(0xFF1E3A8A),
                                        icon = Icons.Default.AccountBalance
                                    )
                                    WelcomeFeatureRow(
                                        number = "3",
                                        titleSi = "ගමන් බිමන් සහ ප්‍රවාහනය",
                                        titleEn = "Travel & Transport Services",
                                        badgeColor = Color(0xFF3730A3),
                                        icon = Icons.Default.DirectionsBus
                                    )
                                    WelcomeFeatureRow(
                                        number = "4",
                                        titleSi = "රෝහල් සහ හදිසි ප්‍රතිකාර ඒකක",
                                        titleEn = "Hospitals & Medical Services",
                                        badgeColor = Color(0xFF991B1B),
                                        icon = Icons.Default.LocalHospital
                                    )
                                    WelcomeFeatureRow(
                                        number = "5",
                                        titleSi = "කෙටි සංකේත සහ තොරතුරු සේවා",
                                        titleEn = "Short Codes & Helplines",
                                        badgeColor = Color(0xFF6B21A8),
                                        icon = Icons.Default.Dialpad
                                    )
                                    WelcomeFeatureRow(
                                        number = "6",
                                        titleSi = "හදිසි ඇමතුම් සේවා (119, 1990)",
                                        titleEn = "Emergency Hotlines & Rescue",
                                        badgeColor = Color(0xFFDC2626),
                                        icon = Icons.Default.PhoneInTalk
                                    )
                                    WelcomeFeatureRow(
                                        number = "7",
                                        titleSi = "ගිනි නිවීම් සේවා (110)",
                                        titleEn = "Fire Stations & Services",
                                        badgeColor = Color(0xFFEA580C),
                                        icon = Icons.Default.Fireplace
                                    )
                                }

                                Divider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(vertical = 2.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "නිර්මාණකරු: ",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF64748B),
                                                fontSize = 11.sp
                                            )
                                        )
                                        Text(
                                            text = "Ishan Maduranga",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E293B),
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                    Text(
                                        text = "v1.0.0",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF94A3B8),
                                            fontSize = 10.sp
                                        )
                                    )
                                }

                                Button(
                                    onClick = { showInfoDialog = false },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                        .testTag("welcome_dialog_accept_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PoliceNavy,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "තහවුරුයි / OK",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PoliceGold,
                                            fontSize = 14.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // AI Web Search Dialog
            if (showAiSearchDialog) {
                AiSearchDialog(
                    initialQuery = aiSearchQuery,
                    onDismiss = { showAiSearchDialog = false },
                    onSearch = { query ->
                        showAiSearchDialog = false
                        viewModel.performGoogleSearch(context, query)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiSearchDialog(
    initialQuery: String,
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit
) {
    var queryText by remember { mutableStateOf(initialQuery) }
    val quickSuggestions = listOf(
        "ගම්පහ පොලිසිය",
        "කුරුණෑගල පොලිසිය",
        "මාතර පොලිසිය",
        "Sri Lanka Police Hotline",
        "National Hospital Colombo",
        "Disaster Management Center"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 10.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PoliceNavy)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PoliceGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = PoliceNavy,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "✨ AI Google Search / ගූගල් සෙවුම",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            )
                            Text(
                                text = "Search any number or contact not in app",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "මෙම ඇප් එකේ ඇතුළත් නොවන ඕනෑම පොලිස් ස්ථානයක්, රාජ්‍ය ආයතනයක්, නිලධාරියෙකු හෝ අංකයක් මෙහි ඇතුළත් කර සෘජුවම ගූගල් (Google) හරහා සොයන්න:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = PoliceNavy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        placeholder = { 
                            Text(
                                "නම, ස්ථානය හෝ දුරකථන අංකය...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            ) 
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = PoliceNavy)
                        },
                        trailingIcon = {
                            if (queryText.isNotEmpty()) {
                                IconButton(onClick = { queryText = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = PoliceNavy)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("ai_search_dialog_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PoliceNavy,
                            unfocusedTextColor = PoliceNavy,
                            focusedContainerColor = Color(0xFFF8F9FA),
                            unfocusedContainerColor = Color(0xFFF8F9FA),
                            focusedBorderColor = PoliceNavy,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "ඉක්මන් මාතෘකා / Quick Shortcuts:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PoliceNavy
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        quickSuggestions.forEach { suggestion ->
                            Surface(
                                onClick = { queryText = suggestion },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { onSearch(queryText) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("ai_search_submit_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PoliceNavy)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = PoliceGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🔍 ගූගල් හි සොයන්න (Search Google)",
                            style = MaterialTheme.typography.titleSmall.copy(
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

@Composable
private fun WelcomeFeatureRow(
    number: String,
    titleSi: String,
    titleEn: String,
    badgeColor: Color,
    icon: ImageVector
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$number. $titleSi",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
                Text(
                    text = titleEn,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF475569),
                        fontSize = 9.5.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

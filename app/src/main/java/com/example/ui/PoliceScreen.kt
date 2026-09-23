package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.History
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.style.TextOverflow
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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
import com.example.data.model.PoliceContact
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.example.ui.components.ContactCard
import com.example.ui.components.ContactDetailBottomSheet
import com.example.ui.components.ContactQrDialog
import com.example.ui.components.AppUpdateDialog
import com.example.ui.components.AppNoticeDialog
import com.example.ui.components.PoliceDataBufferingView
import com.example.ui.components.InAppNotificationBanner
import com.example.ui.components.InAppMessageDialog
import com.example.service.InAppNotification
import com.example.service.InAppNotificationBus
import com.example.util.AppUpdateManager
import com.example.util.AppNoticeManager
import com.example.util.AppConfigManager
import com.example.util.UpdateStatus
import com.example.data.model.AppConfig
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.EmergencyHeader
import com.example.ui.theme.PoliceGold
import com.example.ui.theme.PoliceNavy

enum class MainScreenTab {
    DIRECTORY,
    CHAT,
    PROFILE
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PoliceScreen(
    viewModel: PoliceViewModel,
    modifier: Modifier = Modifier,
    currentUser: String? = null,
    onLogout: () -> Unit = {},
    onOpenPhoneAuth: () -> Unit = {},
    onOpenAdminPanel: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showInfoDialog by remember { mutableStateOf(true) }
    var showUserDialog by remember { mutableStateOf(false) }
    var showAiSearchDialog by remember { mutableStateOf(false) }
    var aiSearchQuery by remember { mutableStateOf("") }
    var contactForQrDialog by remember { mutableStateOf<PoliceContact?>(null) }
    var showManualUpdateDialog by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(MainScreenTab.DIRECTORY) }
    val chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.Factory(context)
    )
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.Factory(context)
    )
    val coroutineScope = rememberCoroutineScope()
    val updateStatus by AppUpdateManager.updateStatus.collectAsStateWithLifecycle()
    val currentNotice by AppNoticeManager.currentNotice.collectAsStateWithLifecycle()
    val appConfig by AppConfigManager.appConfig.collectAsStateWithLifecycle()

    // Real-time In-App Notification state
    var activeInAppBanner by remember { mutableStateOf<InAppNotification?>(null) }
    var activeInAppDialog by remember { mutableStateOf<InAppNotification?>(null) }

    LaunchedEffect(Unit) {
        InAppNotificationBus.notificationFlow.collect { notification ->
            activeInAppBanner = notification
        }
    }

    // Silently check for GitHub app updates, announcements and dynamic UI config in background on launch
    LaunchedEffect(Unit) {
        AppUpdateManager.checkForUpdates(context, isManualCheck = false)
        AppNoticeManager.checkForNotices(context, forceShow = false)
        AppConfigManager.loadConfig(context)
    }

    // Interstitial Ad when user was viewing contact details popup and presses Back
    BackHandler(enabled = uiState.selectedContactForDetail != null) {
        viewModel.closeContactDetail()
        activity?.let { act ->
            com.example.ads.AdMobManager.showInterstitialAd(act)
        }
    }

    // Interstitial Ad when user was searching a contact and presses Back
    BackHandler(enabled = uiState.searchQuery.isNotEmpty() && uiState.selectedContactForDetail == null) {
        viewModel.onSearchQueryChange("")
        keyboardController?.hide()
        activity?.let { act ->
            com.example.ads.AdMobManager.showInterstitialAd(act)
        }
    }

    // Interstitial Ad when user presses back to close the app from the main screen
    BackHandler(
        enabled = uiState.searchQuery.isEmpty() &&
                uiState.selectedContactForDetail == null &&
                !showAiSearchDialog &&
                !showUserDialog &&
                !showInfoDialog &&
                contactForQrDialog == null
    ) {
        if (activity != null && com.example.ads.AdMobManager.hasInterstitialAd()) {
            com.example.ads.AdMobManager.showInterstitialAd(activity, ignoreCooldown = true) {
                activity.finish()
            }
        } else {
            activity?.finish()
        }
    }

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

    val listState = rememberLazyListState()

    // Automatically scroll back to top whenever user searches or selects a new category
    LaunchedEffect(uiState.searchQuery, uiState.selectedCategory) {
        listState.scrollToItem(0)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.navigationBars,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Top Section: App Navigation Bar (Directory, Chat, Profile)
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        tonalElevation = 0.dp
                    ) {
                        NavigationBarItem(
                            selected = currentTab == MainScreenTab.DIRECTORY,
                            onClick = { currentTab = MainScreenTab.DIRECTORY },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Directory"
                                )
                            },
                            label = {
                                Text(
                                    text = com.example.util.AppStrings.navDirectory(uiState.selectedLanguage),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (currentTab == MainScreenTab.DIRECTORY) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_item_directory")
                        )

                        NavigationBarItem(
                            selected = currentTab == MainScreenTab.CHAT,
                            onClick = { currentTab = MainScreenTab.CHAT },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Forum,
                                    contentDescription = "Chat"
                                )
                            },
                            label = {
                                Text(
                                    text = com.example.util.AppStrings.navChat(uiState.selectedLanguage),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (currentTab == MainScreenTab.CHAT) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_item_chat")
                        )

                        NavigationBarItem(
                            selected = currentTab == MainScreenTab.PROFILE,
                            onClick = { currentTab = MainScreenTab.PROFILE },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile"
                                )
                            },
                            label = {
                                Text(
                                    text = com.example.util.AppStrings.navProfile(uiState.selectedLanguage),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (currentTab == MainScreenTab.PROFILE) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_item_profile")
                        )
                    }

                    Divider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        thickness = 0.5.dp
                    )

                    // Bottom-most Section: Banner Ad Space
                    BannerAdView()
                }
            }
        },
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
                                text = com.example.util.AppStrings.appTitle(uiState.selectedLanguage),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (uiState.isOfflineMode) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFB74D))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = com.example.util.AppStrings.offlineBanner(uiState.selectedLanguage),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFFFFD54F),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4CAF50))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Online | Synced: ${uiState.lastSyncTime}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    // Language Switcher Dropdown (සිංහල / English / தமிழ்)
                    Box {
                        Surface(
                            onClick = { showLanguageMenu = true },
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, PoliceGold.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .testTag("top_language_switcher_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${uiState.selectedLanguage.flag} ${uiState.selectedLanguage.displayName}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Switch Language",
                                    tint = PoliceGold,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showLanguageMenu,
                            onDismissRequest = { showLanguageMenu = false },
                            modifier = Modifier
                                .background(Color.White)
                                .testTag("language_dropdown_menu")
                        ) {
                            com.example.util.AppLanguage.values().forEach { lang ->
                                val isSelected = uiState.selectedLanguage == lang
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(text = lang.flag, fontSize = 16.sp)
                                            Column {
                                                Text(
                                                    text = lang.displayName,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) PoliceNavy else Color(0xFF1E293B)
                                                    )
                                                )
                                                Text(
                                                    text = lang.englishName,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Color.Gray,
                                                        fontSize = 10.sp
                                                    )
                                                )
                                            }
                                        }
                                    },
                                    trailingIcon = {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = PoliceGold,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    onClick = {
                                        showLanguageMenu = false
                                        if (uiState.selectedLanguage != lang) {
                                            viewModel.setLanguage(lang)
                                            Toast.makeText(
                                                context,
                                                com.example.util.AppStrings.languageSwitched(lang),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                )
                            }
                        }
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
                        onClick = {
                            viewModel.loadContacts(forceRefresh = true)
                            coroutineScope.launch {
                                AppConfigManager.loadConfig(context)
                            }
                        },
                        modifier = Modifier.testTag("refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync Sheet Data",
                            modifier = Modifier.rotate(rotationAngle)
                        )
                    }
                    IconButton(
                        onClick = onOpenAdminPanel,
                        modifier = Modifier.testTag("topbar_admin_panel_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Dashboard",
                            tint = PoliceGold
                        )
                    }
                    IconButton(
                        onClick = { currentTab = MainScreenTab.PROFILE },
                        modifier = Modifier.testTag("account_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "User Account",
                            tint = Color.White
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
            when (currentTab) {
                MainScreenTab.DIRECTORY -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // In-App Notification Banner if notification received in foreground
                        InAppNotificationBanner(
                            notification = activeInAppBanner,
                            onDismiss = { activeInAppBanner = null },
                            onClick = {
                                val n = activeInAppBanner
                                activeInAppBanner = null
                                if (n != null) {
                                    activeInAppDialog = n
                                }
                            }
                        )

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
                        // Search Bar & History Dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = uiState.searchQuery,
                                    onValueChange = { 
                                        viewModel.onSearchQueryChange(it)
                                    },
                                    placeholder = {
                                        Text(
                                            text = com.example.util.AppStrings.searchPlaceholder(uiState.selectedLanguage),
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
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(end = 4.dp)
                                        ) {
                                            if (uiState.searchQuery.isNotEmpty()) {
                                                IconButton(onClick = {
                                                    viewModel.onSearchQueryChange("")
                                                    keyboardController?.hide()
                                                    activity?.let { act ->
                                                        com.example.ads.AdMobManager.showInterstitialAd(act)
                                                    }
                                                }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Clear,
                                                        contentDescription = "Clear search",
                                                        tint = PoliceNavy
                                                    )
                                                }
                                            }
                                            if (uiState.searchHistory.isNotEmpty()) {
                                                IconButton(
                                                    onClick = {
                                                        viewModel.toggleSearchHistoryDropdown()
                                                    },
                                                    modifier = Modifier.testTag("search_history_dropdown_toggle")
                                                ) {
                                                    Icon(
                                                        imageVector = if (uiState.isSearchHistoryDropdownOpen) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                                        contentDescription = "Search History",
                                                        tint = if (uiState.isSearchHistoryDropdownOpen) PoliceGold else PoliceNavy
                                                    )
                                                }
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
                                    keyboardActions = KeyboardActions(
                                        onSearch = {
                                            viewModel.submitSearch(uiState.searchQuery)
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                        }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .onFocusChanged { focusState ->
                                            if (focusState.isFocused && uiState.searchHistory.isNotEmpty() && uiState.searchQuery.isEmpty()) {
                                                viewModel.setSearchHistoryDropdownOpen(true)
                                            }
                                        }
                                        .testTag("search_text_field")
                                )

                                // Search History Dropdown Card
                                AnimatedVisibility(
                                    visible = uiState.isSearchHistoryDropdownOpen && uiState.searchHistory.isNotEmpty(),
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                            .testTag("search_history_dropdown_card"),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                        ) {
                                            // Header
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.History,
                                                        contentDescription = null,
                                                        tint = PoliceNavy,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = com.example.util.AppStrings.recentSearches(uiState.selectedLanguage),
                                                        style = MaterialTheme.typography.labelMedium.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = PoliceNavy
                                                        )
                                                    )
                                                }
                                                TextButton(
                                                    onClick = { viewModel.clearSearchHistory() },
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.DeleteSweep,
                                                        contentDescription = null,
                                                        tint = Color.Gray,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = com.example.util.AppStrings.clearAll(uiState.selectedLanguage),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = Color.Gray,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    )
                                                }
                                            }

                                            Divider(
                                                color = Color(0xFFF1F5F9),
                                                thickness = 1.dp,
                                                modifier = Modifier.padding(vertical = 6.dp)
                                            )

                                            // History items
                                            val visibleHistory = if (uiState.searchQuery.isBlank()) {
                                                uiState.searchHistory.take(8)
                                            } else {
                                                uiState.searchHistory.filter { 
                                                    it.contains(uiState.searchQuery, ignoreCase = true) 
                                                }.take(8).ifEmpty { uiState.searchHistory.take(5) }
                                            }

                                            visibleHistory.forEach { historyItem ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .clickable {
                                                            viewModel.selectHistoryItem(historyItem)
                                                            keyboardController?.hide()
                                                            focusManager.clearFocus()
                                                        }
                                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        modifier = Modifier.weight(1f),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.History,
                                                            contentDescription = null,
                                                            tint = Color.Gray,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Text(
                                                            text = historyItem,
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                color = Color(0xFF1E293B),
                                                                fontWeight = FontWeight.Medium
                                                            ),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = { viewModel.removeHistoryItem(historyItem) },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Remove from history",
                                                            tint = Color.LightGray,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

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
                                            text = category.getLocalizedName(uiState.selectedLanguage),
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
                    PoliceDataBufferingView(
                        modifier = Modifier.fillMaxSize(),
                        statusText = "Google Sheets දත්ත පූරණය වෙමින් පවතී...",
                        subText = "දිවයින පුරා පොලිස් ස්ථාන හා හදිසි අංක යාවත්කාලීන කෙරේ"
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("contacts_lazy_column"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Emergency Header Card (Only show if no search query or in Police/All/Emergency category)
                        if (uiState.searchQuery.isEmpty() && (uiState.selectedCategory == ContactCategory.POLICE || uiState.selectedCategory == ContactCategory.ALL || uiState.selectedCategory == ContactCategory.EMERGENCY)) {
                            item {
                                EmergencyHeader(
                                    onEmergencyCall = { number -> viewModel.makePhoneCall(context, number) },
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    appConfig = appConfig,
                                    selectedLanguage = uiState.selectedLanguage
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
                                    onWhatsAppClick = { phone -> viewModel.openWhatsApp(context, phone) },
                                    onEmailClick = { email, station -> viewModel.sendEmail(context, email, station) },
                                    onFavoriteToggle = { c -> viewModel.toggleFavorite(c) },
                                    onShareClick = { c -> viewModel.shareContact(context, c) },
                                    onCardClick = { c -> viewModel.openContactDetail(c) },
                                    onNavigationClick = { c -> viewModel.startNavigation(context, c) },
                                    onQrCodeClick = { c -> contactForQrDialog = c },
                                    selectedLanguage = uiState.selectedLanguage,
                                    isCompactSearchMode = uiState.searchQuery.isNotBlank(),
                                    searchQuery = uiState.searchQuery
                                )
                            }
                        }
                    }
                }
            }
        }
        MainScreenTab.CHAT -> {
            ChatScreen(
                viewModel = chatViewModel,
                selectedLanguage = uiState.selectedLanguage,
                modifier = Modifier.fillMaxSize()
            )
        }
        MainScreenTab.PROFILE -> {
            ProfileScreen(
                viewModel = profileViewModel,
                currentUserEmail = currentUser,
                onLogout = onLogout,
                selectedLanguage = uiState.selectedLanguage,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

            // Contact Detail Modal Bottom Sheet
            uiState.selectedContactForDetail?.let { selectedContact ->
                ContactDetailBottomSheet(
                    contact = selectedContact,
                    sheetState = sheetState,
                    onDismiss = {
                        viewModel.closeContactDetail()
                        activity?.let { act ->
                            com.example.ads.AdMobManager.showInterstitialAd(act)
                        }
                    },
                    onCallClick = { phone -> viewModel.makePhoneCall(context, phone) },
                    onWhatsAppClick = { phone -> viewModel.openWhatsApp(context, phone) },
                    onEmailClick = { email, station -> viewModel.sendEmail(context, email, station) },
                    onCopyClick = { text, label -> viewModel.copyToClipboard(context, text, label) },
                    onShareClick = { c -> viewModel.shareContact(context, c) },
                    onFavoriteToggle = { c -> viewModel.toggleFavorite(c) },
                    onNavigationClick = { c -> viewModel.startNavigation(context, c) },
                    onQrCodeClick = { c -> contactForQrDialog = c },
                    selectedLanguage = uiState.selectedLanguage
                )
            }

            // QR Code Scan & Save Dialog
            val activeQrContact = contactForQrDialog
            if (activeQrContact != null) {
                ContactQrDialog(
                    contact = activeQrContact,
                    onDismiss = { contactForQrDialog = null }
                )
            }

            // GitHub In-App Update Dialog
            if (updateStatus is UpdateStatus.UpdateAvailable ||
                updateStatus is UpdateStatus.Downloading ||
                updateStatus is UpdateStatus.ReadyToInstall ||
                (updateStatus is UpdateStatus.Error && showManualUpdateDialog)
            ) {
                AppUpdateDialog(
                    status = updateStatus,
                    onDismiss = {
                        showManualUpdateDialog = false
                        AppUpdateManager.resetStatus()
                    }
                )
            }

            // GitHub Announcement / Notice Dialog
            val activeNotice = currentNotice
            if (activeNotice != null && updateStatus !is UpdateStatus.UpdateAvailable) {
                AppNoticeDialog(
                    notice = activeNotice,
                    onDismiss = {
                        AppNoticeManager.dismissNotice(context, activeNotice.id)
                    }
                )
            }

            // Firebase In-App Message Dialog
            val currentInAppDialog = activeInAppDialog
            if (currentInAppDialog != null) {
                InAppMessageDialog(
                    notification = currentInAppDialog,
                    onDismiss = { activeInAppDialog = null }
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
                                            text = "නිමැවුම: ",
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
                                        text = "v${AppUpdateManager.getCurrentVersionName()}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF94A3B8),
                                            fontSize = 10.sp
                                        )
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        showInfoDialog = false
                                        showManualUpdateDialog = true
                                        coroutineScope.launch {
                                            val res = AppUpdateManager.checkForUpdates(context, isManualCheck = true)
                                            if (res is UpdateStatus.NoUpdate) {
                                                snackbarHostState.showSnackbar("ඔබ දැනටමත් නවතම සංස්කරණය (v${AppUpdateManager.getCurrentVersionName()}) භාවිත කරයි!")
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                        .testTag("check_updates_info_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, PoliceNavy.copy(alpha = 0.5f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SystemUpdate,
                                        contentDescription = null,
                                        tint = PoliceNavy,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "නව Updates පරීක්ෂා කරන්න (Check Updates)",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PoliceNavy,
                                            fontSize = 11.sp
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

            // User Profile Dialog
            if (showUserDialog) {
                Dialog(onDismissRequest = { showUserDialog = false }) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(PoliceNavy),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = PoliceGold,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "පරිශීලක ගිණුම / User Account",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PoliceNavy,
                                    fontSize = 16.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = currentUser ?: "Authorized Police User",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFDCFCE7),
                                border = BorderStroke(1.dp, Color(0xFF86EFAC))
                            ) {
                                Text(
                                    text = "● Verified via Google Sheet",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF166534),
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Divider(color = Color(0xFFE2E8F0))

                            Spacer(modifier = Modifier.height(14.dp))

                            // Admin Panel Dashboard Access Button
                            OutlinedButton(
                                onClick = {
                                    showUserDialog = false
                                    onOpenAdminPanel()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("admin_panel_button"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.5.dp, PoliceNavy),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = PoliceNavy
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Admin Panel",
                                    tint = PoliceGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "පරිපාලක පුවරුව (Admin Dashboard)",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PoliceNavy,
                                        fontSize = 13.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    showUserDialog = false
                                    onLogout()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("logout_dialog_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDC2626),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ගිණුමෙන් ඉවත් වන්න (Sign Out)",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(
                                onClick = { showUserDialog = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("වසන්න / Close", color = Color(0xFF64748B))
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

private fun Context.findActivity(): Activity? {
    var cur = this
    while (cur is ContextWrapper) {
        if (cur is Activity) return cur
        cur = cur.baseContext
    }
    return null
}


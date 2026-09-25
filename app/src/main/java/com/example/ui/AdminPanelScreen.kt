package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ContactCategory
import com.example.data.model.PoliceContact
import com.example.ui.components.BulkImportDialog
import com.example.ui.components.ContactQrDialog
import com.example.ui.components.AdminUpdateSettingsCard
import com.example.ui.components.AdminAgoraSettingsCard
import com.example.ui.theme.PoliceGold
import com.example.ui.theme.PoliceNavy
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: PoliceViewModel,
    currentUser: String? = null,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Local Search & Category Filter
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ContactCategory.ALL) }

    // Dialog States
    var contactToEdit by remember { mutableStateOf<PoliceContact?>(null) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var contactToDelete by remember { mutableStateOf<PoliceContact?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showBulkImportDialog by remember { mutableStateOf(false) }
    var qrContactToShow by remember { mutableStateOf<PoliceContact?>(null) }

    // Operation Visual Loading States
    var isSaving by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var isBulkImporting by remember { mutableStateOf(false) }

    // Rotation animation for refresh icon
    val rotationAngle by animateFloatAsState(
        targetValue = if (uiState.isRefreshing) 360f else 0f,
        label = "adminRefreshRotation"
    )

    // Filtered Contacts for Admin Display
    val filteredList = remember(uiState.contacts, searchQuery, selectedCategory) {
        val q = searchQuery.trim().lowercase()
        uiState.contacts.filter { contact ->
            val matchesCategory = when (selectedCategory) {
                ContactCategory.ALL -> true
                ContactCategory.POLICE -> contact.category == ContactCategory.POLICE ||
                        contact.category == ContactCategory.DIVISIONS ||
                        contact.category == ContactCategory.RANGES
                ContactCategory.EMERGENCY -> contact.category == ContactCategory.EMERGENCY
                ContactCategory.FIRE_STATIONS -> contact.category == ContactCategory.FIRE_STATIONS || contact.stationOrDesignation.contains("Fire", ignoreCase = true)
                ContactCategory.HOSPITALS -> contact.category == ContactCategory.HOSPITALS
                ContactCategory.GOVT_SERVICES -> contact.category == ContactCategory.GOVT_SERVICES
                ContactCategory.TRAVEL -> contact.category == ContactCategory.TRAVEL
                ContactCategory.SHORT_CODES -> contact.category == ContactCategory.SHORT_CODES
                else -> contact.category == selectedCategory
            }

            val matchesQuery = if (q.isEmpty()) true else {
                contact.stationOrDesignation.lowercase().contains(q) ||
                        contact.officerName.lowercase().contains(q) ||
                        contact.rank.lowercase().contains(q) ||
                        contact.generalPhone.contains(q) ||
                        contact.mobilePhone.contains(q) ||
                        contact.locationAddress.lowercase().contains(q)
            }

            matchesCategory && matchesQuery
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "පරිපාලක පුවරුව",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PoliceGold
                            ) {
                                Text(
                                    text = "ADMIN",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = PoliceNavy,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                        Text(
                            text = "Admin: ${currentUser ?: "Authorized Officer"}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.loadContacts(forceRefresh = true) },
                        modifier = Modifier.testTag("admin_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync Directory Data",
                            tint = PoliceGold,
                            modifier = Modifier.rotate(rotationAngle)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PoliceNavy)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF1F5F9))
        ) {
            // Global top loading progress indicator during refresh or async operations
            if (uiState.isLoading || uiState.isRefreshing || isSaving || isDeleting) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = PoliceGold,
                    trackColor = PoliceNavy.copy(alpha = 0.2f)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // 1. Statistics Cards Overview
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AdminStatMiniCard(
                                title = "මුළු ගණන (Total)",
                                count = uiState.contacts.size.toString(),
                                iconColor = PoliceNavy,
                                modifier = Modifier.weight(1f)
                            )
                            AdminStatMiniCard(
                                title = "පොලිස් ස්ථාන",
                                count = uiState.contacts.count {
                                    it.category == ContactCategory.POLICE ||
                                            it.category == ContactCategory.DIVISIONS ||
                                            it.category == ContactCategory.RANGES
                                }.toString(),
                                iconColor = Color(0xFF1D4ED8),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AdminStatMiniCard(
                                title = "හදිසි ඇමතුම් (119/1990)",
                                count = uiState.contacts.count { it.category == ContactCategory.EMERGENCY }.toString(),
                                iconColor = Color(0xFFDC2626),
                                modifier = Modifier.weight(1f)
                            )
                            AdminStatMiniCard(
                                title = "ගිනි නිවීම් / රෝහල්",
                                count = uiState.contacts.count {
                                    it.category == ContactCategory.FIRE_STATIONS || it.category == ContactCategory.HOSPITALS
                                }.toString(),
                                iconColor = Color(0xFFEA580C),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 2. GitHub In-App Auto-Update Settings & Management Card
                item {
                    AdminUpdateSettingsCard(
                        onShowMessage = { msg ->
                            scope.launch {
                                snackbarHostState.showSnackbar(msg)
                            }
                        }
                    )
                }

                // 2.1 Agora Real-Time Voice & Chat Engine Settings Card
                item {
                    AdminAgoraSettingsCard(
                        onShowMessage = { msg ->
                            scope.launch {
                                snackbarHostState.showSnackbar(msg)
                            }
                        }
                    )
                }

                // 3. Action & Filter Bar Header
                item {
                    Surface(
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Action Buttons Row: Add Contact + Bulk Import
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        contactToEdit = null
                                        showAddEditDialog = true
                                    },
                                    modifier = Modifier
                                        .weight(1.1f)
                                        .height(46.dp)
                                        .testTag("admin_add_contact_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PoliceNavy,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = PoliceGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "+ නව තොරතුර (Add)",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                OutlinedButton(
                                    onClick = { showBulkImportDialog = true },
                                    modifier = Modifier
                                        .weight(1.05f)
                                        .height(46.dp)
                                        .testTag("admin_bulk_import_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.5.dp, PoliceNavy),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = PoliceNavy
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FileUpload,
                                        contentDescription = "Bulk Import",
                                        tint = PoliceNavy,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Bulk Import (CSV)",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = PoliceNavy
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Search Field
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_search_field"),
                                placeholder = { Text("ස්ථානය, නිලය, නම හෝ දුරකථන අංකය සොයන්න...", fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = Color(0xFF64748B)
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear",
                                                tint = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF0F172A),
                                    unfocusedTextColor = Color(0xFF0F172A),
                                    focusedBorderColor = PoliceNavy,
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC),
                                    focusedPlaceholderColor = Color(0xFF64748B),
                                    unfocusedPlaceholderColor = Color(0xFF94A3B8)
                                ),
                                textStyle = TextStyle(
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Category Filter Chips
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val categories = listOf(
                                    ContactCategory.ALL to "සියල්ල (All)",
                                    ContactCategory.POLICE to "පොලිස් (Police)",
                                    ContactCategory.EMERGENCY to "හදිසි (Emergency)",
                                    ContactCategory.FIRE_STATIONS to "ගිනි නිවීම් (Fire)",
                                    ContactCategory.HOSPITALS to "රෝහල් (Hospitals)",
                                    ContactCategory.GOVT_SERVICES to "රජයේ (Govt)",
                                    ContactCategory.TRAVEL to "ප්‍රවාහන (Travel)"
                                )
                                items(categories) { (cat, label) ->
                                    val isSelected = selectedCategory == cat
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedCategory = cat },
                                        label = {
                                            Text(
                                                text = label,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PoliceNavy,
                                            selectedLabelColor = PoliceGold,
                                            containerColor = Color(0xFFF1F5F9),
                                            labelColor = Color(0xFF334155)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Count indicator row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ලැයිස්තුව (${filteredList.size})",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155)
                            )
                        )
                        Text(
                            text = "Edit හෝ Delete තෝරන්න",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF64748B)
                            )
                        )
                    }
                }

                // 4. Contact Records
                if (filteredList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "ගැලපෙන තොරතුරු හමු නොවීය",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFF64748B),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                } else {
                    items(filteredList, key = { it.id }) { contact ->
                        AdminContactRowCard(
                            contact = contact,
                            onEdit = {
                                contactToEdit = contact
                                showAddEditDialog = true
                            },
                            onDelete = {
                                contactToDelete = contact
                                showDeleteConfirmDialog = true
                            },
                            onShowQr = {
                                qrContactToShow = contact
                            }
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    // ADD / EDIT CONTACT DIALOG (WITH VISUAL LOADING INDICATOR FOR SAVING)
    // =========================================================================
    if (showAddEditDialog) {
        AddEditContactDialog(
            initialContact = contactToEdit,
            isSaving = isSaving,
            onDismiss = {
                if (!isSaving) {
                    showAddEditDialog = false
                    contactToEdit = null
                }
            },
            onSave = { updatedContact ->
                isSaving = true
                scope.launch {
                    // Small delay to ensure visual loading state renders smoothly
                    delay(300)
                    viewModel.saveContact(
                        contact = updatedContact,
                        onSuccess = {
                            isSaving = false
                            showAddEditDialog = false
                            contactToEdit = null
                            scope.launch {
                                snackbarHostState.showSnackbar("✓ සම්බන්ධතාව සාර්ථකව සුරකින ලදී (Saved successfully)")
                            }
                        },
                        onError = { errMsg ->
                            isSaving = false
                            scope.launch {
                                snackbarHostState.showSnackbar("දෝෂයකි: $errMsg")
                            }
                        }
                    )
                }
            }
        )
    }

    // =========================================================================
    // DELETE CONFIRMATION DIALOG (WITH VISUAL LOADING INDICATOR FOR DELETING)
    // =========================================================================
    if (showDeleteConfirmDialog && contactToDelete != null) {
        val target = contactToDelete!!
        AlertDialog(
            onDismissRequest = {
                if (!isDeleting) {
                    showDeleteConfirmDialog = false
                    contactToDelete = null
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "සම්බන්ධතාව ඉවත් කරන්නද?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                )
            },
            text = {
                Column {
                    Text(
                        text = "\"${target.stationOrDesignation}\" තොරතුරු නාමාවලියෙන් ස්ථිරවම ඉවත් කිරීමට ඔබට අවශ්‍ය බව තහවුරු කරන්න.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF475569))
                    )
                    if (isDeleting) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFFDC2626),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "ඉවත් කරමින් පවතී... (Deleting)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleting = true
                        scope.launch {
                            delay(300)
                            viewModel.deleteContact(
                                contactId = target.id,
                                onSuccess = {
                                    isDeleting = false
                                    showDeleteConfirmDialog = false
                                    contactToDelete = null
                                    scope.launch {
                                        snackbarHostState.showSnackbar("🗑️ සම්බන්ධතාව සාර්ථකව ඉවත් කරන ලදී (Deleted)")
                                    }
                                },
                                onError = { err ->
                                    isDeleting = false
                                    scope.launch {
                                        snackbarHostState.showSnackbar("දෝෂයකි: $err")
                                    }
                                }
                            )
                        }
                    },
                    enabled = !isDeleting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("admin_confirm_delete_button")
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ඉවත් වෙමින්... (Deleting...)")
                    } else {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ඉවත් කරන්න (Delete)")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        contactToDelete = null
                    },
                    enabled = !isDeleting
                ) {
                    Text("අවලංගු කරන්න (Cancel)", color = Color(0xFF64748B))
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    // =========================================================================
    // BULK IMPORT CSV DIALOG
    // =========================================================================
    if (showBulkImportDialog) {
        BulkImportDialog(
            isImporting = isBulkImporting,
            onDismiss = {
                if (!isBulkImporting) {
                    showBulkImportDialog = false
                }
            },
            onImportContacts = { contactsToImport ->
                isBulkImporting = true
                viewModel.bulkImportContacts(
                    contacts = contactsToImport,
                    onSuccess = { count ->
                        isBulkImporting = false
                        showBulkImportDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar("✓ සම්බන්ධතා $count ක් සාර්ථකව ඇතුළත් කරන ලදී (Imported $count contacts)")
                        }
                    },
                    onError = { err ->
                        isBulkImporting = false
                        scope.launch {
                            snackbarHostState.showSnackbar("දෝෂයකි: $err")
                        }
                    }
                )
            }
        )
    }

    // =========================================================================
    // QR CODE DIALOG
    // =========================================================================
    val activeAdminQrContact = qrContactToShow
    if (activeAdminQrContact != null) {
        ContactQrDialog(
            contact = activeAdminQrContact,
            onDismiss = { qrContactToShow = null }
        )
    }
}

// -----------------------------------------------------------------------------
// Component: Admin Stat Mini Card
// -----------------------------------------------------------------------------
@Composable
fun AdminStatMiniCard(
    title: String,
    count: String,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF64748B),
                    fontSize = 11.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = iconColor,
                    fontSize = 20.sp
                )
            )
        }
    }
}

// -----------------------------------------------------------------------------
// Component: Admin Contact Row Card with Edit and Delete Action Buttons
// -----------------------------------------------------------------------------
@Composable
fun AdminContactRowCard(
    contact: PoliceContact,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShowQr: (PoliceContact) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contact.stationOrDesignation,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            fontSize = 14.sp
                        )
                    )

                    if (contact.officerName.isNotBlank() || contact.rank.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (contact.rank.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFE2E8F0)
                                ) {
                                    Text(
                                        text = contact.rank,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF334155),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            if (contact.officerName.isNotBlank()) {
                                Text(
                                    text = contact.officerName,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF475569),
                                        fontSize = 12.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Category Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFDBEAFE))
                ) {
                    Text(
                        text = contact.category.name.replace('_', ' '),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF1D4ED8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Phone info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (contact.generalPhone.isNotBlank()) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = contact.generalPhone,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A),
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                if (contact.mobilePhone.isNotBlank()) {
                    Text(
                        text = "Mob: ${contact.mobilePhone}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF16A34A),
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            if (contact.locationAddress.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = contact.locationAddress,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: QR, Edit & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onShowQr(contact) },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFD97706)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB45309)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = "QR Code",
                        tint = Color(0xFFB45309),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "QR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309),
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, PoliceNavy),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = PoliceNavy,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "සංස්කරණය (Edit)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PoliceNavy,
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFDC2626)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ඉවත් කරන්න",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Component: Add / Edit Contact Dialog (WITH VISUAL LOADING INDICATOR FOR SAVING)
// -----------------------------------------------------------------------------
@Composable
fun AddEditContactDialog(
    initialContact: PoliceContact?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (PoliceContact) -> Unit
) {
    val isEditing = initialContact != null

    var station by remember { mutableStateOf(initialContact?.stationOrDesignation ?: "") }
    var officerName by remember { mutableStateOf(initialContact?.officerName ?: "") }
    var rank by remember { mutableStateOf(initialContact?.rank ?: "") }
    var generalPhone by remember { mutableStateOf(initialContact?.generalPhone ?: "") }
    var mobilePhone by remember { mutableStateOf(initialContact?.mobilePhone ?: "") }
    var officePhone2 by remember { mutableStateOf(initialContact?.officePhone2 ?: "") }
    var pvtNumber by remember { mutableStateOf(initialContact?.pvtNumber ?: "") }
    var oicTraffic by remember { mutableStateOf(initialContact?.oicTraffic ?: "") }
    var oicCrime by remember { mutableStateOf(initialContact?.oicCrime ?: "") }
    var locationAddress by remember { mutableStateOf(initialContact?.locationAddress ?: "") }
    var coordinates by remember { mutableStateOf(initialContact?.locationCoordinates ?: "") }
    var email by remember { mutableStateOf(initialContact?.email ?: "") }
    var selectedCategory by remember { mutableStateOf(initialContact?.category ?: ContactCategory.POLICE) }

    val adminFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color(0xFF0F172A),
        unfocusedTextColor = Color(0xFF0F172A),
        disabledTextColor = Color(0xFF64748B),
        focusedLabelColor = PoliceNavy,
        unfocusedLabelColor = Color(0xFF334155),
        disabledLabelColor = Color(0xFF94A3B8),
        focusedBorderColor = PoliceNavy,
        unfocusedBorderColor = Color(0xFF94A3B8),
        cursorColor = PoliceNavy,
        focusedContainerColor = Color(0xFFF8FAFC),
        unfocusedContainerColor = Color(0xFFF8FAFC),
        disabledContainerColor = Color(0xFFF1F5F9),
        focusedPlaceholderColor = Color(0xFF64748B),
        unfocusedPlaceholderColor = Color(0xFF94A3B8)
    )

    val adminFieldTextStyle = TextStyle(
        color = Color(0xFF0F172A),
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 10.dp
        ) {
            CompositionLocalProvider(LocalContentColor provides Color(0xFF0F172A)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header
                    Surface(
                        color = PoliceNavy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.Add,
                                contentDescription = null,
                                tint = PoliceGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isEditing) "තොරතුරු සංස්කරණය (Edit Contact)" else "නව තොරතුරක් එක් කිරීම (Add Contact)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }

                    // Dynamic Saving Progress Banner inside Dialog
                    AnimatedVisibility(visible = isSaving) {
                        Surface(
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = PoliceNavy,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "දත්ත සුරකිමින් පවතී... (Saving to directory...)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PoliceNavy
                                    )
                                )
                            }
                        }
                    }

                    // Scrollable Form Fields
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = station,
                            onValueChange = { station = it },
                            label = { Text("ස්ථානය / කාර්යාලය (Station/Office) *", fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_field_station"),
                            enabled = !isSaving,
                            singleLine = true,
                            colors = adminFieldColors,
                            textStyle = adminFieldTextStyle
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = officerName,
                                onValueChange = { officerName = it },
                                label = { Text("ස්ථානාධිපති / නිලධාරී", fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier
                                    .weight(1.4f)
                                    .testTag("admin_field_officer"),
                                enabled = !isSaving,
                                singleLine = true,
                                colors = adminFieldColors,
                                textStyle = adminFieldTextStyle
                            )
                            OutlinedTextField(
                                value = rank,
                                onValueChange = { rank = it },
                                label = { Text("නිලය (Rank)", fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier
                                    .weight(0.8f)
                                    .testTag("admin_field_rank"),
                                enabled = !isSaving,
                                singleLine = true,
                                colors = adminFieldColors,
                                textStyle = adminFieldTextStyle
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = generalPhone,
                                onValueChange = { generalPhone = it },
                                label = { Text("ප්‍රධාන දුරකථන *", fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_field_general_phone"),
                                enabled = !isSaving,
                                singleLine = true,
                                colors = adminFieldColors,
                                textStyle = adminFieldTextStyle
                            )
                            OutlinedTextField(
                                value = mobilePhone,
                                onValueChange = { mobilePhone = it },
                                label = { Text("ජංගම දුරකථන", fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_field_mobile_phone"),
                                enabled = !isSaving,
                                singleLine = true,
                                colors = adminFieldColors,
                                textStyle = adminFieldTextStyle
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = officePhone2,
                                onValueChange = { officePhone2 = it },
                                label = { Text("කාර්යාල අංක 2", fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.weight(1f),
                                enabled = !isSaving,
                                singleLine = true,
                                colors = adminFieldColors,
                                textStyle = adminFieldTextStyle
                            )
                            OutlinedTextField(
                                value = pvtNumber,
                                onValueChange = { pvtNumber = it },
                                label = { Text("පෞද්ගලික (PVT)", fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.weight(1f),
                                enabled = !isSaving,
                                singleLine = true,
                                colors = adminFieldColors,
                                textStyle = adminFieldTextStyle
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = oicTraffic,
                                onValueChange = { oicTraffic = it },
                                label = { Text("ස්ථානාධිපති රථවාහන", fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.weight(1f),
                                enabled = !isSaving,
                                singleLine = true,
                                colors = adminFieldColors,
                                textStyle = adminFieldTextStyle
                            )
                            OutlinedTextField(
                                value = oicCrime,
                                onValueChange = { oicCrime = it },
                                label = { Text("ස්ථානාධිපති අපරාධ", fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.weight(1f),
                                enabled = !isSaving,
                                singleLine = true,
                                colors = adminFieldColors,
                                textStyle = adminFieldTextStyle
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = locationAddress,
                            onValueChange = { locationAddress = it },
                            label = { Text("ලිපිනය (Address)", fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSaving,
                            singleLine = true,
                            colors = adminFieldColors,
                            textStyle = adminFieldTextStyle
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = coordinates,
                            onValueChange = { coordinates = it },
                            label = { Text("GPS ඛණ්ඩාංක (Coordinates e.g. 6.9344, 79.8428)", fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSaving,
                            singleLine = true,
                            colors = adminFieldColors,
                            textStyle = adminFieldTextStyle
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("විද්‍යුත් තැපෑල (Email)", fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSaving,
                            singleLine = true,
                            colors = adminFieldColors,
                            textStyle = adminFieldTextStyle
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Category Selector Chips
                        Text(
                            text = "කාණ්ඩය (Category):",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PoliceNavy,
                                fontSize = 12.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val categoryOptions = listOf(
                                ContactCategory.POLICE to "පොලිස් (Police)",
                                ContactCategory.EMERGENCY to "හදිසි (Emergency)",
                                ContactCategory.FIRE_STATIONS to "ගිනි නිවීම් (Fire)",
                                ContactCategory.HOSPITALS to "රෝහල් (Hospitals)",
                                ContactCategory.GOVT_SERVICES to "රජයේ (Govt)",
                                ContactCategory.TRAVEL to "ප්‍රවාහන (Travel)",
                                ContactCategory.SHORT_CODES to "කෙටි අංක (Short Codes)"
                            )
                            items(categoryOptions) { (cat, label) ->
                                val isSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PoliceNavy,
                                        selectedLabelColor = PoliceGold,
                                        containerColor = Color(0xFFF1F5F9),
                                        labelColor = Color(0xFF334155)
                                    )
                                )
                            }
                        }
                    }

                    // Footer Buttons with Visual Loading Indicator on Save Button
                    Surface(
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = onDismiss,
                                enabled = !isSaving
                            ) {
                                Text("අවලංගු කරන්න (Cancel)", color = Color(0xFF64748B))
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Button(
                                onClick = {
                                    if (station.isNotBlank()) {
                                        val id = initialContact?.id ?: ("custom_" + System.currentTimeMillis())
                                        val contact = PoliceContact(
                                            id = id,
                                            stationOrDesignation = station.trim(),
                                            rank = rank.trim(),
                                            officerName = officerName.trim(),
                                            generalPhone = generalPhone.trim(),
                                            mobilePhone = mobilePhone.trim(),
                                            officePhone2 = officePhone2.trim(),
                                            pvtNumber = pvtNumber.trim(),
                                            email = email.trim(),
                                            category = selectedCategory,
                                            oicTraffic = oicTraffic.trim(),
                                            oicCrime = oicCrime.trim(),
                                            locationAddress = locationAddress.trim(),
                                            locationCoordinates = coordinates.trim(),
                                            isFavorite = initialContact?.isFavorite ?: false
                                        )
                                        onSave(contact)
                                    }
                                },
                                enabled = !isSaving && station.isNotBlank(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PoliceNavy,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.testTag("admin_dialog_save_button")
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("සුරකිමින් පවතී... (Saving)")
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = PoliceGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("සුරකින්න (Save Contact)", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

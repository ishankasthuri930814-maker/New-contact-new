package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ContactCategory
import com.example.data.model.PoliceContact
import com.example.data.repository.PoliceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PoliceUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isOfflineMode: Boolean = false,
    val contacts: List<PoliceContact> = emptyList(),
    val filteredContacts: List<PoliceContact> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: ContactCategory = ContactCategory.POLICE,
    val selectedContactForDetail: PoliceContact? = null,
    val lastSyncTime: String = "",
    val userMessage: String? = null,
    val searchHistory: List<String> = emptyList(),
    val isSearchHistoryDropdownOpen: Boolean = false,
    val selectedLanguage: com.example.util.AppLanguage = com.example.util.AppLanguage.SINHALA
)

class PoliceViewModel(private val repository: PoliceRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PoliceUiState())
    val uiState: StateFlow<PoliceUiState> = _uiState.asStateFlow()

    private var hasObservedInitialNetwork = false

    init {
        // 1. Immediately load local offline contacts (retrieved in previous session) so UI shows contacts with 0 delay
        loadCachedContactsImmediately()

        // 2. Observe network connectivity: when mobile data or Wi-Fi turns ON, auto-refresh live Google Sheet data!
        observeNetworkConnectivity()

        // 3. Load saved search history from local storage
        loadSearchHistory()

        // 4. Load saved language preference
        loadSavedLanguage()
    }

    private fun loadSavedLanguage() {
        val savedLang = repository.getSavedLanguage()
        _uiState.update { it.copy(selectedLanguage = savedLang) }
    }

    fun setLanguage(language: com.example.util.AppLanguage) {
        repository.saveLanguage(language)
        _uiState.update { it.copy(selectedLanguage = language) }
    }

    private fun loadSearchHistory() {
        val history = repository.getSearchHistory()
        _uiState.update { it.copy(searchHistory = history) }
    }

    private fun loadCachedContactsImmediately() {
        val cached = repository.getCachedContactsFast()
        val syncTime = repository.getLastSyncTimeString()
        val isOffline = !repository.networkMonitor.isOnline
        _uiState.update { state ->
            val filtered = filterContactsList(cached, state.searchQuery, state.selectedCategory)
            state.copy(
                isLoading = false,
                isOfflineMode = isOffline,
                contacts = cached,
                filteredContacts = filtered,
                lastSyncTime = syncTime,
                userMessage = if (isOffline && cached.isNotEmpty()) {
                    "Offline මාදිලිය: පෙර ලබාගත් Google Sheet දත්ත පෙන්වයි"
                } else null
            )
        }
    }

    private fun observeNetworkConnectivity() {
        viewModelScope.launch {
            repository.networkMonitor.isOnlineFlow.collect { isOnline ->
                val wasOffline = _uiState.value.isOfflineMode
                _uiState.update { it.copy(isOfflineMode = !isOnline) }

                if (isOnline) {
                    // Mobile data or Wi-Fi connected! Auto-refresh live data from Google Sheets!
                    if (!hasObservedInitialNetwork || wasOffline) {
                        hasObservedInitialNetwork = true
                        loadContacts(forceRefresh = true, isAutoRefresh = true)
                    }
                } else {
                    hasObservedInitialNetwork = true
                }
            }
        }
    }

    fun loadContacts(forceRefresh: Boolean = false, isAutoRefresh: Boolean = false) {
        viewModelScope.launch {
            val isOnline = repository.networkMonitor.isOnline
            if (forceRefresh) {
                _uiState.update { it.copy(isRefreshing = true) }
            } else if (_uiState.value.contacts.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            }

            val result = repository.getPoliceContacts(forceRefresh)
            val syncTime = repository.getLastSyncTimeString()

            result.onSuccess { contactsList ->
                _uiState.update { state ->
                    val filtered = filterContactsList(contactsList, state.searchQuery, state.selectedCategory)
                    val message = when {
                        isAutoRefresh -> "ජාලය සම්බන්ධ විය. Google Sheet දත්ත යාවත්කාලීන විය!"
                        forceRefresh -> "තොරතුරු සාර්ථකව යාවත්කාලීන විය (Synced)"
                        !isOnline -> "Offline මාදිලිය: පෙර ලබාගත් දත්ත පෙන්වයි"
                        else -> null
                    }
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isOfflineMode = !isOnline,
                        contacts = contactsList,
                        filteredContacts = filtered,
                        lastSyncTime = syncTime,
                        userMessage = message
                    )
                }
            }.onFailure { error ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isOfflineMode = !isOnline,
                        lastSyncTime = syncTime,
                        userMessage = "Offline මාදිලිය: පෙර ලබාගත් Google Sheet දත්ත පෙන්වයි"
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val filtered = filterContactsList(state.contacts, query, state.selectedCategory)
            state.copy(
                searchQuery = query,
                filteredContacts = filtered
            )
        }
    }

    fun submitSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isNotEmpty()) {
            val updatedHistory = repository.saveSearchQuery(trimmed)
            _uiState.update { state ->
                val filtered = filterContactsList(state.contacts, trimmed, state.selectedCategory)
                state.copy(
                    searchQuery = trimmed,
                    filteredContacts = filtered,
                    searchHistory = updatedHistory,
                    isSearchHistoryDropdownOpen = false
                )
            }
        } else {
            _uiState.update { it.copy(isSearchHistoryDropdownOpen = false) }
        }
    }

    fun selectHistoryItem(item: String) {
        submitSearch(item)
    }

    fun removeHistoryItem(item: String) {
        val updatedHistory = repository.removeSearchQuery(item)
        _uiState.update { state ->
            state.copy(
                searchHistory = updatedHistory,
                isSearchHistoryDropdownOpen = updatedHistory.isNotEmpty()
            )
        }
    }

    fun clearSearchHistory() {
        repository.clearSearchHistory()
        _uiState.update { state ->
            state.copy(
                searchHistory = emptyList(),
                isSearchHistoryDropdownOpen = false
            )
        }
    }

    fun toggleSearchHistoryDropdown() {
        _uiState.update { state ->
            state.copy(isSearchHistoryDropdownOpen = !state.isSearchHistoryDropdownOpen)
        }
    }

    fun setSearchHistoryDropdownOpen(isOpen: Boolean) {
        _uiState.update { state ->
            state.copy(isSearchHistoryDropdownOpen = isOpen)
        }
    }

    fun onCategorySelect(category: ContactCategory) {
        _uiState.update { state ->
            val filtered = filterContactsList(state.contacts, state.searchQuery, category)
            state.copy(
                selectedCategory = category,
                filteredContacts = filtered
            )
        }
    }

    fun toggleFavorite(contact: PoliceContact) {
        val newFavState = repository.toggleFavorite(contact.id)
        _uiState.update { state ->
            val updatedContacts = state.contacts.map {
                if (it.id == contact.id) it.copy(isFavorite = newFavState) else it
            }
            val filtered = filterContactsList(updatedContacts, state.searchQuery, state.selectedCategory)
            state.copy(
                contacts = updatedContacts,
                filteredContacts = filtered,
                selectedContactForDetail = state.selectedContactForDetail?.let {
                    if (it.id == contact.id) it.copy(isFavorite = newFavState) else it
                },
                userMessage = if (newFavState) "Added to Favorites" else "Removed from Favorites"
            )
        }
    }

    fun openContactDetail(contact: PoliceContact) {
        _uiState.update { it.copy(selectedContactForDetail = contact) }
    }

    fun closeContactDetail() {
        _uiState.update { it.copy(selectedContactForDetail = null) }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun saveContact(
        contact: PoliceContact,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.saveContact(contact)
            result.onSuccess {
                _uiState.update { state ->
                    val updated = state.contacts.toMutableList()
                    val idx = updated.indexOfFirst { it.id == contact.id }
                    if (idx >= 0) {
                        updated[idx] = contact
                    } else {
                        updated.add(0, contact)
                    }
                    val filtered = filterContactsList(updated, state.searchQuery, state.selectedCategory)
                    state.copy(
                        contacts = updated,
                        filteredContacts = filtered,
                        userMessage = "සම්බන්ධතාව සාර්ථකව සුරකින ලදී (Contact saved successfully)"
                    )
                }
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "Failed to save contact")
            }
        }
    }

    fun deleteContact(
        contactId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.deleteContact(contactId)
            result.onSuccess {
                _uiState.update { state ->
                    val updated = state.contacts.filterNot { it.id == contactId }
                    val filtered = filterContactsList(updated, state.searchQuery, state.selectedCategory)
                    state.copy(
                        contacts = updated,
                        filteredContacts = filtered,
                        userMessage = "සම්බන්ධතාව සාර්ථකව ඉවත් කරන ලදී (Contact deleted)"
                    )
                }
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "Failed to delete contact")
            }
        }
    }

    fun bulkImportContacts(
        contacts: List<PoliceContact>,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.saveContactsBulk(contacts)
            result.onSuccess { count ->
                _uiState.update { state ->
                    val current = repository.loadFromLocalCache()
                    val filtered = filterContactsList(current, state.searchQuery, state.selectedCategory)
                    state.copy(
                        contacts = current,
                        filteredContacts = filtered,
                        userMessage = "සම්බන්ධතා $count ක් සාර්ථකව ආනයනය කරන ලදී ($count contacts imported)"
                    )
                }
                onSuccess(count)
            }.onFailure { err ->
                onError(err.message ?: "Failed to import contacts")
            }
        }
    }

    private fun filterContactsList(
        contacts: List<PoliceContact>,
        query: String,
        category: ContactCategory
    ): List<PoliceContact> {
        val q = query.trim().lowercase()

        return contacts.filter { contact ->
            // Category filter
            val matchesCategory = if (q.isNotEmpty() && category == ContactCategory.POLICE) {
                // If user is actively searching with text, allow matching across all emergency & public safety contacts
                true
            } else when (category) {
                ContactCategory.POLICE -> {
                    contact.category == ContactCategory.POLICE ||
                            contact.category == ContactCategory.DIVISIONS ||
                            contact.category == ContactCategory.RANGES ||
                            contact.category == ContactCategory.SENIOR_OFFICERS ||
                            (contact.category != ContactCategory.HOSPITALS &&
                                    contact.category != ContactCategory.FIRE_STATIONS &&
                                    contact.category != ContactCategory.GOVT_SERVICES &&
                                    contact.category != ContactCategory.TRAVEL &&
                                    contact.category != ContactCategory.SHORT_CODES)
                }
                ContactCategory.ALL -> true
                ContactCategory.FAVORITES -> contact.isFavorite
                ContactCategory.EMERGENCY -> contact.category == ContactCategory.EMERGENCY
                ContactCategory.FIRE_STATIONS -> contact.category == ContactCategory.FIRE_STATIONS || contact.stationOrDesignation.contains("Fire", ignoreCase = true)
                ContactCategory.SHORT_CODES -> contact.category == ContactCategory.SHORT_CODES
                ContactCategory.HOSPITALS -> contact.category == ContactCategory.HOSPITALS
                ContactCategory.GOVT_SERVICES -> contact.category == ContactCategory.GOVT_SERVICES
                ContactCategory.TRAVEL -> contact.category == ContactCategory.TRAVEL
                ContactCategory.DIVISIONS -> contact.category == ContactCategory.DIVISIONS || contact.stationOrDesignation.contains("Division", ignoreCase = true)
                ContactCategory.RANGES -> contact.category == ContactCategory.RANGES || contact.stationOrDesignation.contains("Range", ignoreCase = true)
                ContactCategory.SENIOR_OFFICERS -> contact.category == ContactCategory.SENIOR_OFFICERS || contact.rank.contains("DIG", ignoreCase = true) || contact.rank.contains("IGP", ignoreCase = true) || contact.rank.contains("SSP", ignoreCase = true)
            }

            // Text search filter with Sinhala, Tamil, and English Multilingual Support
            val matchesQuery = if (q.isEmpty()) {
                true
            } else {
                com.example.util.MultiLanguageSearchHelper.matchesContact(contact, q)
            }

            matchesCategory && matchesQuery
        }
    }

    // Direct Action Helpers
    fun makePhoneCall(context: Context, phoneNumber: String) {
        val cleanNumber = phoneNumber.trim().replace(" ", "").replace("-", "")
        if (cleanNumber.isEmpty()) {
            Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$cleanNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open phone dialer", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWhatsApp(context: Context, phoneNumber: String) {
        val digitsOnly = phoneNumber.replace(Regex("[^0-9+]"), "")
        if (digitsOnly.isBlank()) {
            Toast.makeText(context, "වලංගු දුරකථන අංකයක් නොමැත (No valid phone number)", Toast.LENGTH_SHORT).show()
            return
        }

        // Format for Sri Lanka (+94)
        val formattedNumber = when {
            digitsOnly.startsWith("+94") -> digitsOnly.removePrefix("+")
            digitsOnly.startsWith("0") -> "94" + digitsOnly.substring(1)
            digitsOnly.startsWith("94") -> digitsOnly
            digitsOnly.length == 9 && digitsOnly.startsWith("7") -> "94$digitsOnly"
            else -> digitsOnly.removePrefix("+")
        }

        try {
            // Attempt to open WhatsApp directly with the phone number
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedNumber")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.whatsapp")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // If WhatsApp package check fails or WhatsApp Business is installed or browser fallback
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$formattedNumber")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "WhatsApp විවෘත කිරීමට නොහැකි විය (Unable to open WhatsApp)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun sendEmail(context: Context, emailAddress: String, stationName: String) {
        val cleanEmail = emailAddress.trim()
        if (cleanEmail.isEmpty()) {
            Toast.makeText(context, "No email address available", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$cleanEmail")
                putExtra(Intent.EXTRA_SUBJECT, "Inquiry to $stationName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open email client", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareContact(context: Context, contact: PoliceContact) {
        val shareText = buildString {
            append("👮 ශ්‍රී ලංකා පොලිස් තොරතුරු / Sri Lanka Police Contact\n")
            append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            append("🏛 ස්ථානය / ආයතනය: ${contact.stationOrDesignation}\n")
            if (contact.rank.isNotBlank()) append("🎖 නිලය / Rank: ${contact.rank}\n")
            if (contact.officerName.isNotBlank()) append("👤 නිලධාරී / Officer: ${contact.officerName}\n")
            if (contact.category.displayName.isNotBlank()) {
                append("📂 ප්‍රවර්ගය / Category: ${contact.category.sinhalaName} (${contact.category.displayName})\n")
            }
            append("────────────────────────────\n")

            // All Contact Numbers
            append("📞 දුරකථන අංක / Contact Numbers:\n")
            if (contact.generalPhone.isNotBlank()) append("  • ප්‍රධාන දුරකථන: ${contact.generalPhone}\n")
            if (contact.mobilePhone.isNotBlank()) append("  • ජංගම දුරකථන: ${contact.mobilePhone}\n")
            if (contact.officePhone2.isNotBlank()) append("  • කාර්යාලය 2: ${contact.officePhone2}\n")
            if (contact.officePhone3.isNotBlank()) append("  • කාර්යාලය 3: ${contact.officePhone3}\n")
            if (contact.pvtNumber.isNotBlank()) append("  • පෞද්ගලික (PVT): ${contact.pvtNumber}\n")
            if (contact.fax.isNotBlank()) append("  • ෆැක්ස් (Fax): ${contact.fax}\n")
            if (contact.email.isNotBlank()) append("  • ඊමේල් (Email): ${contact.email}\n")

            // Section officers if present (Traffic, Crime, Vice, Community Policing)
            val hasSectionOfficers = contact.oicTraffic.isNotBlank() || contact.oicCrime.isNotBlank() ||
                    contact.oicVice.isNotBlank() || contact.oicCommunityPolicing.isNotBlank()
            if (hasSectionOfficers) {
                append("────────────────────────────\n")
                append("📋 අංශභාර ස්ථානාධිපතිවරුන් (OIC Sections):\n")
                if (contact.oicTraffic.isNotBlank()) append("  • 🚦 රථවාහන අංශය: ${contact.oicTraffic}\n")
                if (contact.oicCrime.isNotBlank()) append("  • 🔍 අපරාධ අංශය: ${contact.oicCrime}\n")
                if (contact.oicVice.isNotBlank()) append("  • 🛡️ දූෂණ මර්ධන අංශය: ${contact.oicVice}\n")
                if (contact.oicCommunityPolicing.isNotBlank()) append("  • 🤝 ප්‍රජා පොලිස් අංශය: ${contact.oicCommunityPolicing}\n")
            }

            // Address & GPS Navigation
            if (contact.locationAddress.isNotBlank() || contact.locationCoordinates.isNotBlank()) {
                append("────────────────────────────\n")
                if (contact.locationAddress.isNotBlank()) append("🏢 ලිපිනය / Address: ${contact.locationAddress}\n")
                if (contact.locationCoordinates.isNotBlank()) {
                    val cleanCoords = contact.locationCoordinates.trim()
                    append("🧭 GPS: $cleanCoords\n")
                    append("🗺️ සිතියම / Google Maps: https://www.google.com/maps/search/?api=1&query=$cleanCoords\n")
                    append("🚗 Navigation: https://www.google.com/maps/dir/?api=1&destination=$cleanCoords\n")
                }
            }

            append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            append("📲 Shared via Sri Lanka Police Directory App v3.5\n")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "${contact.stationOrDesignation} - Sri Lanka Police Contact")
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "සම්පූර්ණ තොරතුරු Share කරන්න (Share Contact)")
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(shareIntent)
    }

    fun startNavigation(context: Context, contact: PoliceContact) {
        val coords = contact.locationCoordinates.trim()
        val stationName = contact.stationOrDesignation.trim()

        try {
            if (coords.isNotBlank() && coords.contains(",")) {
                val parts = coords.split(",")
                val lat = parts[0].trim()
                val lng = parts[1].trim()

                // 1. First attempt: Direct turn-by-turn Navigation in Google Maps
                val navUri = Uri.parse("google.navigation:q=$lat,$lng&mode=d")
                val navIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
                    setPackage("com.google.android.apps.maps")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                if (navIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(navIntent)
                    return
                }

                // 2. Second attempt: geo intent
                val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(stationName)})")
                val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                if (mapIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(mapIntent)
                    return
                }

                // 3. Fallback: Google Maps web direction link
                val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")
                val browserIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
            } else {
                // If coordinates missing, query location by station name
                val query = if (stationName.lowercase().contains("police")) {
                    "$stationName, Sri Lanka"
                } else {
                    "$stationName Police Station, Sri Lanka"
                }
                val navUri = Uri.parse("google.navigation:q=${Uri.encode(query)}&mode=d")
                val navIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
                    setPackage("com.google.android.apps.maps")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                if (navIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(navIntent)
                    return
                }

                val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${Uri.encode(query)}")
                val browserIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
            }
        } catch (e: Exception) {
            try {
                val fallbackDest = if (coords.isNotBlank() && coords.contains(",")) coords else Uri.encode("$stationName Police Station, Sri Lanka")
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$fallbackDest")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Google Maps විවෘත කිරීමට නොහැකි විය (Unable to open Maps)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun copyToClipboard(context: Context, text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun performGoogleSearch(context: Context, query: String) {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) {
            Toast.makeText(context, "කරුණාකර සොයන්න අවශ්‍ය නම හෝ අංකය ඇතුළත් කරන්න", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val searchQuery = if (cleanQuery.lowercase().contains("sri lanka") || cleanQuery.lowercase().contains("police")) {
                "$cleanQuery contact phone number"
            } else {
                "$cleanQuery Sri Lanka police contact phone number"
            }
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://www.google.com/search?q=${Uri.encode(searchQuery)}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "ගූගල් සෙවුම විවෘත කිරීමට නොහැකි විය", Toast.LENGTH_SHORT).show()
        }
    }

    class Factory(private val repository: PoliceRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PoliceViewModel(repository) as T
        }
    }
}

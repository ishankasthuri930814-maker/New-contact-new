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
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val contacts: List<PoliceContact> = emptyList(),
    val filteredContacts: List<PoliceContact> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: ContactCategory = ContactCategory.ALL,
    val selectedContactForDetail: PoliceContact? = null,
    val lastSyncTime: String = "",
    val userMessage: String? = null
)

class PoliceViewModel(private val repository: PoliceRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PoliceUiState())
    val uiState: StateFlow<PoliceUiState> = _uiState.asStateFlow()

    init {
        loadContacts(forceRefresh = true)
    }

    fun loadContacts(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (forceRefresh) {
                _uiState.update { it.copy(isRefreshing = true) }
            } else {
                _uiState.update { it.copy(isLoading = true) }
            }

            val result = repository.getPoliceContacts(forceRefresh)
            val syncTime = repository.getLastSyncTimeString()

            result.onSuccess { contactsList ->
                _uiState.update { state ->
                    val filtered = filterContactsList(contactsList, state.searchQuery, state.selectedCategory)
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        contacts = contactsList,
                        filteredContacts = filtered,
                        lastSyncTime = syncTime,
                        userMessage = if (forceRefresh) "Contact directory updated successfully" else null
                    )
                }
            }.onFailure { error ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        lastSyncTime = syncTime,
                        userMessage = "Could not connect to online sheet. Displaying cached data."
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

    private fun filterContactsList(
        contacts: List<PoliceContact>,
        query: String,
        category: ContactCategory
    ): List<PoliceContact> {
        val q = query.trim().lowercase()

        return contacts.filter { contact ->
            // Category filter
            val matchesCategory = when (category) {
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

            // Text search filter
            val matchesQuery = if (q.isEmpty()) {
                true
            } else {
                contact.stationOrDesignation.lowercase().contains(q) ||
                        contact.officerName.lowercase().contains(q) ||
                        contact.rank.lowercase().contains(q) ||
                        contact.generalPhone.lowercase().contains(q) ||
                        contact.mobilePhone.lowercase().contains(q) ||
                        contact.pvtNumber.lowercase().contains(q) ||
                        contact.officePhone2.lowercase().contains(q) ||
                        contact.officePhone3.lowercase().contains(q) ||
                        contact.fax.lowercase().contains(q) ||
                        contact.email.lowercase().contains(q) ||
                        contact.oicTraffic.lowercase().contains(q) ||
                        contact.oicCrime.lowercase().contains(q) ||
                        contact.oicVice.lowercase().contains(q) ||
                        contact.oicCommunityPolicing.lowercase().contains(q) ||
                        contact.locationAddress.lowercase().contains(q)
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
            append("👮 Sri Lanka Police Contact Details\n")
            append("📍 ${contact.stationOrDesignation}\n")
            if (contact.rank.isNotBlank()) append("🎖 Rank: ${contact.rank}\n")
            if (contact.officerName.isNotBlank()) append("👤 Officer: ${contact.officerName}\n")
            if (contact.generalPhone.isNotBlank()) append("📞 Telephone: ${contact.generalPhone}\n")
            if (contact.mobilePhone.isNotBlank()) append("📱 Mobile: ${contact.mobilePhone}\n")
            if (contact.pvtNumber.isNotBlank()) append("🔒 PVT: ${contact.pvtNumber}\n")
            if (contact.email.isNotBlank()) append("✉️ Email: ${contact.email}\n")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Police Contact")
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(shareIntent)
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

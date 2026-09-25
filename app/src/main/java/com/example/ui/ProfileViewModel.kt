package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserProfile
import com.example.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val editDisplayName: String = "",
    val editPhoneNumber: String = "",
    val editDistrict: String = "Colombo",
    val editEmergencyNote: String = "",
    val editProfilePhotoUrl: String = "",
    val selectedAvatarIndex: Int = 0,
    val isPhonePublic: Boolean = false,
    val allowDirectMessages: Boolean = true,
    val allowDirectCalls: Boolean = true,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val statusMessage: String? = null
)

class ProfileViewModel(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userProfileRepository.currentProfile.collect { prof ->
                _uiState.update {
                    it.copy(
                        profile = prof,
                        editDisplayName = if (it.editDisplayName.isBlank()) prof.displayName else it.editDisplayName,
                        editPhoneNumber = if (it.editPhoneNumber.isBlank()) prof.phoneNumber else it.editPhoneNumber,
                        editDistrict = if (it.editDistrict.isBlank()) prof.district else it.editDistrict,
                        editEmergencyNote = if (it.editEmergencyNote.isBlank()) prof.emergencyNote else it.editEmergencyNote,
                        editProfilePhotoUrl = if (it.editProfilePhotoUrl.isBlank()) prof.profilePhotoUrl else it.editProfilePhotoUrl,
                        selectedAvatarIndex = prof.avatarIndex,
                        isPhonePublic = prof.isPhonePublic,
                        allowDirectMessages = prof.allowDirectMessages,
                        allowDirectCalls = prof.allowDirectCalls
                    )
                }
            }
        }
    }

    fun syncUserEmail(email: String, fallbackName: String? = null, userId: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val loaded = userProfileRepository.getOrCreateProfileForEmail(email, fallbackName, userId)
            _uiState.update {
                it.copy(
                    profile = loaded,
                    editDisplayName = loaded.displayName,
                    editPhoneNumber = loaded.phoneNumber,
                    editDistrict = loaded.district,
                    editEmergencyNote = loaded.emergencyNote,
                    editProfilePhotoUrl = loaded.profilePhotoUrl,
                    selectedAvatarIndex = loaded.avatarIndex,
                    isPhonePublic = loaded.isPhonePublic,
                    allowDirectMessages = loaded.allowDirectMessages,
                    allowDirectCalls = loaded.allowDirectCalls,
                    isLoading = false
                )
            }
        }
    }

    fun onNameChange(name: String) = _uiState.update { it.copy(editDisplayName = name) }
    fun onPhoneChange(phone: String) = _uiState.update { it.copy(editPhoneNumber = phone) }
    fun onDistrictChange(district: String) = _uiState.update { it.copy(editDistrict = district) }
    fun onEmergencyNoteChange(note: String) = _uiState.update { it.copy(editEmergencyNote = note) }
    fun onAvatarSelect(index: Int) = _uiState.update { it.copy(selectedAvatarIndex = index) }
    fun onPhotoSelected(base64: String) {
        _uiState.update { it.copy(editProfilePhotoUrl = base64) }
        saveProfile()
    }
    fun onRemovePhoto() {
        _uiState.update { it.copy(editProfilePhotoUrl = "") }
        saveProfile()
    }
    fun onPhonePublicToggle(value: Boolean) = _uiState.update { it.copy(isPhonePublic = value) }
    fun onDirectMessagesToggle(value: Boolean) = _uiState.update { it.copy(allowDirectMessages = value) }
    fun onDirectCallsToggle(value: Boolean) = _uiState.update { it.copy(allowDirectCalls = value) }
    fun clearStatusMessage() = _uiState.update { it.copy(statusMessage = null) }

    fun saveProfile(onSuccess: (String) -> Unit = {}) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val success = userProfileRepository.updateProfile(
                displayName = state.editDisplayName,
                phoneNumber = state.editPhoneNumber,
                district = state.editDistrict,
                emergencyNote = state.editEmergencyNote,
                avatarIndex = state.selectedAvatarIndex,
                isPhonePublic = state.isPhonePublic,
                allowDirectMessages = state.allowDirectMessages,
                allowDirectCalls = state.allowDirectCalls,
                profilePhotoUrl = state.editProfilePhotoUrl
            )
            _uiState.update {
                it.copy(
                    isSaving = false,
                    statusMessage = if (success) "Profile එක සාර්ථකව සුරකින ලදී!" else "සුරැකීම අසාර්ථක විය"
                )
            }
            if (success) {
                onSuccess("Profile එක සාර්ථකව සුරකින ලදී!")
            }
        }
    }

    class Factory(
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repo = UserProfileRepository(context.applicationContext)
            return ProfileViewModel(repo) as T
        }
    }
}

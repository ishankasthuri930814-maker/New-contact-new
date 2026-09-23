package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CallSession
import com.example.data.model.ChatMessage
import com.example.data.model.DirectChatMessage
import com.example.data.model.UserProfile
import com.example.data.repository.CallRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ChatMode {
    COMMUNITY_ROOM, // Public Community channels
    REGISTERED_USERS, // List of registered users for 1-on-1 private messaging
    DIRECT_CHAT_ROOM // Active 1-on-1 private chat with a specific user
}

data class ChatUiState(
    val chatMode: ChatMode = ChatMode.COMMUNITY_ROOM,
    // Community
    val messages: List<ChatMessage> = emptyList(),
    val selectedChannel: String = "general",
    // 1-on-1 Direct Private
    val registeredUsers: List<UserProfile> = emptyList(),
    val searchQuery: String = "",
    val activeDirectUser: UserProfile? = null,
    val directMessagesMap: Map<String, List<DirectChatMessage>> = emptyMap(),
    // Input
    val inputText: String = "",
    val isEmergencyFlag: Boolean = false,
    val isSending: Boolean = false,
    val currentUserProfile: UserProfile = UserProfile(),
    // In-App Voice Calling
    val activeCall: CallSession? = null,
    val incomingCall: CallSession? = null,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = true
) {
    val activeDirectMessages: List<DirectChatMessage>
        get() {
            val other = activeDirectUser ?: return emptyList()
            val myKey = currentUserProfile.email.ifBlank { currentUserProfile.userId }
            val otherKey = other.email.ifBlank { other.userId }
            val convId = DirectChatMessage.createConversationId(myKey, otherKey)
            return directMessagesMap[convId] ?: emptyList()
        }
}

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val userProfileRepository: UserProfileRepository,
    private val callRepository: CallRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            chatRepository.messages.collect { msgList ->
                _uiState.update { it.copy(messages = msgList) }
            }
        }

        viewModelScope.launch {
            userProfileRepository.currentProfile.collect { profile ->
                _uiState.update { it.copy(currentUserProfile = profile) }
                if (profile.email.isNotBlank() || profile.userId.isNotBlank()) {
                    callRepository.startListeningForIncomingCalls(profile)
                    chatRepository.startListeningForIncomingDirectMessages(profile)
                }
            }
        }

        viewModelScope.launch {
            userProfileRepository.allRegisteredUsers.collect { users ->
                _uiState.update { it.copy(registeredUsers = users) }
            }
        }

        viewModelScope.launch {
            chatRepository.directMessages.collect { directMap ->
                _uiState.update { it.copy(directMessagesMap = directMap) }
            }
        }

        viewModelScope.launch {
            callRepository.activeCall.collect { call ->
                _uiState.update { it.copy(activeCall = call) }
            }
        }

        viewModelScope.launch {
            callRepository.incomingCall.collect { incoming ->
                _uiState.update { it.copy(incomingCall = incoming) }
            }
        }

        viewModelScope.launch {
            callRepository.isMuted.collect { muted ->
                _uiState.update { it.copy(isMuted = muted) }
            }
        }

        viewModelScope.launch {
            callRepository.isSpeakerOn.collect { speaker ->
                _uiState.update { it.copy(isSpeakerOn = speaker) }
            }
        }
    }

    fun setChatMode(mode: ChatMode) {
        _uiState.update { it.copy(chatMode = mode) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onChannelChange(channelId: String) {
        chatRepository.setChannel(channelId)
        _uiState.update { it.copy(selectedChannel = channelId) }
    }

    fun openDirectChatWith(otherUser: UserProfile) {
        val myKey = _uiState.value.currentUserProfile.email.ifBlank { _uiState.value.currentUserProfile.userId }
        val otherKey = otherUser.email.ifBlank { otherUser.userId }
        val convId = DirectChatMessage.createConversationId(myKey, otherKey)
        chatRepository.listenToDirectConversation(convId)

        _uiState.update {
            it.copy(
                activeDirectUser = otherUser,
                chatMode = ChatMode.DIRECT_CHAT_ROOM,
                inputText = ""
            )
        }
    }

    fun closeDirectChat() {
        _uiState.update {
            it.copy(
                activeDirectUser = null,
                chatMode = ChatMode.REGISTERED_USERS,
                inputText = ""
            )
        }
    }

    fun onInputTextChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun toggleEmergencyFlag() {
        _uiState.update { it.copy(isEmergencyFlag = !it.isEmergencyFlag) }
    }

    fun sendQuickPrompt(promptText: String) {
        _uiState.update { it.copy(inputText = promptText) }
        sendMessage()
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return

        val state = _uiState.value
        val profile = state.currentUserProfile

        if (state.chatMode == ChatMode.DIRECT_CHAT_ROOM) {
            val other = state.activeDirectUser ?: return
            val otherKey = other.email.ifBlank { other.userId }
            viewModelScope.launch {
                _uiState.update { it.copy(isSending = true, inputText = "") }
                chatRepository.sendDirectMessage(
                    sender = profile,
                    receiverUserId = otherKey,
                    text = text
                )
                _uiState.update { it.copy(isSending = false) }
            }
        } else {
            // Community message
            val channel = state.selectedChannel
            val isEmergency = state.isEmergencyFlag
            viewModelScope.launch {
                _uiState.update { it.copy(isSending = true, inputText = "", isEmergencyFlag = false) }
                chatRepository.sendMessage(
                    userProfile = profile,
                    text = text,
                    channelId = channel,
                    isEmergency = isEmergency
                )
                _uiState.update { it.copy(isSending = false) }
            }
        }
    }

    // --- In-App Voice Calling Actions ---

    fun startInAppCallWith(otherUser: UserProfile) {
        viewModelScope.launch {
            callRepository.initiateCall(_uiState.value.currentUserProfile, otherUser)
        }
    }

    fun acceptIncomingCall(callId: String) {
        viewModelScope.launch {
            callRepository.acceptCall(callId)
        }
    }

    fun declineIncomingCall(callId: String) {
        viewModelScope.launch {
            callRepository.declineCall(callId)
        }
    }

    fun endActiveCall(callId: String) {
        viewModelScope.launch {
            callRepository.endCall(callId)
        }
    }

    fun toggleMute() {
        callRepository.toggleMute()
    }

    fun toggleSpeaker() {
        callRepository.toggleSpeaker()
    }

    fun dismissCallDialog() {
        callRepository.dismissCallDialog()
    }

    override fun onCleared() {
        super.onCleared()
        chatRepository.onCleared()
        callRepository.onCleared()
    }

    class Factory(
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val userProfileRepo = UserProfileRepository(context.applicationContext)
            val chatRepo = ChatRepository(context.applicationContext)
            val callRepo = CallRepository(context.applicationContext)
            return ChatViewModel(chatRepo, userProfileRepo, callRepo) as T
        }
    }
}

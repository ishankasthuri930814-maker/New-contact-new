package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChatMessage
import com.example.data.model.DirectChatMessage
import com.example.data.model.UserProfile
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
    val currentUserProfile: UserProfile = UserProfile()
) {
    val activeDirectMessages: List<DirectChatMessage>
        get() {
            val other = activeDirectUser ?: return emptyList()
            val myId = currentUserProfile.userId.ifBlank { currentUserProfile.email }
            val otherId = other.userId.ifBlank { other.email }
            val convId = DirectChatMessage.createConversationId(myId, otherId)
            return directMessagesMap[convId] ?: emptyList()
        }
}

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val userProfileRepository: UserProfileRepository
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
        val myId = _uiState.value.currentUserProfile.userId.ifBlank { _uiState.value.currentUserProfile.email }
        val otherId = otherUser.userId.ifBlank { otherUser.email }
        val convId = DirectChatMessage.createConversationId(myId, otherId)
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
            val otherId = other.userId.ifBlank { other.email }
            viewModelScope.launch {
                _uiState.update { it.copy(isSending = true, inputText = "") }
                chatRepository.sendDirectMessage(
                    sender = profile,
                    receiverUserId = otherId,
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

    override fun onCleared() {
        super.onCleared()
        chatRepository.onCleared()
    }

    class Factory(
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val userProfileRepo = UserProfileRepository(context.applicationContext)
            val chatRepo = ChatRepository(context.applicationContext)
            return ChatViewModel(chatRepo, userProfileRepo) as T
        }
    }
}

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
    val chatMode: ChatMode = ChatMode.REGISTERED_USERS,
    val previousChatMode: ChatMode = ChatMode.COMMUNITY_ROOM,
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
    val isSpeakerOn: Boolean = true,
    val isAudioConnected: Boolean = false,
    val micAmplitude: Float = 0f,
    val speakerAmplitude: Float = 0f
) {
    val activeDirectMessages: List<DirectChatMessage>
        get() {
            val other = activeDirectUser ?: return emptyList()

            val myKeys = listOfNotNull(
                currentUserProfile.email.takeIf { it.isNotBlank() },
                currentUserProfile.userId.takeIf { it.isNotBlank() },
                currentUserProfile.phoneNumber.takeIf { it.isNotBlank() },
                currentUserProfile.email.takeIf { it.isNotBlank() }?.let { DirectChatMessage.normalizeUserKey(it) },
                currentUserProfile.userId.takeIf { it.isNotBlank() }?.let { DirectChatMessage.normalizeUserKey(it) },
                currentUserProfile.phoneNumber.takeIf { it.isNotBlank() }?.let { DirectChatMessage.normalizeUserKey(it) }
            ).map { it.lowercase().trim() }.distinct()

            val otherKeys = listOfNotNull(
                other.email.takeIf { it.isNotBlank() },
                other.userId.takeIf { it.isNotBlank() },
                other.phoneNumber.takeIf { it.isNotBlank() },
                other.email.takeIf { it.isNotBlank() }?.let { DirectChatMessage.normalizeUserKey(it) },
                other.userId.takeIf { it.isNotBlank() }?.let { DirectChatMessage.normalizeUserKey(it) },
                other.phoneNumber.takeIf { it.isNotBlank() }?.let { DirectChatMessage.normalizeUserKey(it) }
            ).map { it.lowercase().trim() }.distinct()

            val myKey = currentUserProfile.email.ifBlank { currentUserProfile.userId.ifBlank { currentUserProfile.phoneNumber } }
            val otherKey = other.email.ifBlank { other.userId.ifBlank { other.phoneNumber } }
            val convId = DirectChatMessage.createConversationId(myKey, otherKey)

            val directList = directMessagesMap[convId] ?: emptyList()

            // Cross-match from all incoming conversations in case conversationId normalization varied
            val crossList = directMessagesMap.values.flatten().filter { msg ->
                val msgParticipants = msg.participants.map { it.lowercase().trim() }
                val hasMe = myKeys.any { it in msgParticipants || it == msg.senderId.lowercase().trim() || it == msg.receiverId.lowercase().trim() }
                val hasOther = otherKeys.any { it in msgParticipants || it == msg.senderId.lowercase().trim() || it == msg.receiverId.lowercase().trim() }
                hasMe && hasOther
            }

            return (directList + crossList).distinctBy { it.id }.sortedBy { it.timestamp }
        }
}

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val userProfileRepository: UserProfileRepository,
    private val callRepository: CallRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    private val loggedCallIds = mutableSetOf<String>()

    init {
        val initialCached = userProfileRepository.loadCachedProfile()
        _uiState.update { it.copy(currentUserProfile = initialCached) }
        if (initialCached.email.isNotBlank() || initialCached.userId.isNotBlank()) {
            callRepository.startListeningForIncomingCalls(initialCached)
            chatRepository.startListeningForIncomingDirectMessages(initialCached)
        }

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
                if (call != null && (call.status == CallSession.STATUS_ENDED ||
                            call.status == CallSession.STATUS_DECLINED ||
                            call.status == CallSession.STATUS_MISSED)) {
                    handleCallFinished(call)
                }
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

        viewModelScope.launch {
            callRepository.isAudioConnected.collect { connected ->
                _uiState.update { it.copy(isAudioConnected = connected) }
            }
        }

        viewModelScope.launch {
            callRepository.micAmplitude.collect { amp ->
                _uiState.update { it.copy(micAmplitude = amp) }
            }
        }

        viewModelScope.launch {
            callRepository.speakerAmplitude.collect { amp ->
                _uiState.update { it.copy(speakerAmplitude = amp) }
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

    fun openDirectChatWith(otherUser: UserProfile, fromMode: ChatMode = _uiState.value.chatMode) {
        val myKey = _uiState.value.currentUserProfile.email.ifBlank { _uiState.value.currentUserProfile.userId }
        val otherKey = otherUser.email.ifBlank { otherUser.userId }
        val convId = DirectChatMessage.createConversationId(myKey, otherKey)
        chatRepository.listenToDirectConversation(convId)

        _uiState.update {
            it.copy(
                activeDirectUser = otherUser,
                previousChatMode = fromMode,
                chatMode = ChatMode.DIRECT_CHAT_ROOM,
                inputText = ""
            )
        }
    }

    fun openDirectChatWithUser(
        targetUserId: String,
        targetDisplayName: String,
        targetEmail: String,
        targetAvatarIndex: Int
    ) {
        val existing = _uiState.value.registeredUsers.firstOrNull {
            (it.email.isNotBlank() && it.email.equals(targetEmail, ignoreCase = true)) ||
            (it.userId.isNotBlank() && it.userId == targetUserId)
        }

        val targetProfile = existing ?: UserProfile(
            userId = targetUserId.ifBlank { targetEmail },
            email = targetEmail,
            displayName = targetDisplayName.takeIf { it.isNotBlank() && !it.equals("Citizen", ignoreCase = true) }
                ?: targetEmail.substringBefore("@").replaceFirstChar { it.uppercase() }.ifBlank { "User" },
            avatarIndex = targetAvatarIndex
        )

        openDirectChatWith(targetProfile, fromMode = _uiState.value.chatMode)
    }

    fun closeDirectChat() {
        val backTo = _uiState.value.previousChatMode
        _uiState.update {
            it.copy(
                activeDirectUser = null,
                chatMode = backTo,
                inputText = ""
            )
        }
    }

    fun deleteCommunityMessage(messageId: String) {
        viewModelScope.launch {
            chatRepository.deleteCommunityMessage(messageId)
        }
    }

    fun deleteDirectMessage(messageId: String) {
        val active = _uiState.value.activeDirectUser ?: return
        val myKey = _uiState.value.currentUserProfile.email.ifBlank { _uiState.value.currentUserProfile.userId }
        val otherKey = active.email.ifBlank { active.userId }
        val convId = DirectChatMessage.createConversationId(myKey, otherKey)
        viewModelScope.launch {
            chatRepository.deleteDirectMessage(messageId, convId)
        }
    }

    fun deleteSelectedDirectMessages(messageIds: Set<String>) {
        if (messageIds.isEmpty()) return
        val active = _uiState.value.activeDirectUser ?: return
        val myKey = _uiState.value.currentUserProfile.email.ifBlank { _uiState.value.currentUserProfile.userId }
        val otherKey = active.email.ifBlank { active.userId }
        val convId = DirectChatMessage.createConversationId(myKey, otherKey)
        viewModelScope.launch {
            chatRepository.deleteMultipleDirectMessages(messageIds, convId)
        }
    }

    fun deleteSelectedCommunityMessages(messageIds: Set<String>) {
        if (messageIds.isEmpty()) return
        viewModelScope.launch {
            chatRepository.deleteMultipleCommunityMessages(messageIds)
        }
    }

    fun clearCurrentDirectConversation() {
        val active = _uiState.value.activeDirectUser ?: return
        val myKey = _uiState.value.currentUserProfile.email.ifBlank { _uiState.value.currentUserProfile.userId }
        val otherKey = active.email.ifBlank { active.userId }
        val convId = DirectChatMessage.createConversationId(myKey, otherKey)
        viewModelScope.launch {
            chatRepository.clearDirectConversation(convId)
        }
    }

    private fun handleCallFinished(call: CallSession) {
        if (call.callId.isBlank() || loggedCallIds.contains(call.callId)) return
        loggedCallIds.add(call.callId)

        val durationSeconds = if (call.connectedAt > 0L) {
            val end = if (call.endedAt > call.connectedAt) call.endedAt else System.currentTimeMillis()
            ((end - call.connectedAt) / 1000).toInt().coerceAtLeast(1)
        } else {
            0
        }

        val status = if (call.connectedAt > 0L) {
            "CONNECTED"
        } else if (call.status == CallSession.STATUS_DECLINED) {
            "DECLINED"
        } else {
            "MISSED"
        }

        viewModelScope.launch {
            chatRepository.logCallMessage(
                callId = call.callId,
                callerKey = call.callerId,
                callerName = call.callerName,
                callerAvatarIndex = call.callerAvatarIndex,
                receiverKey = call.receiverId,
                callStatus = status,
                durationSeconds = durationSeconds,
                timestamp = if (call.endedAt > 0L) call.endedAt else System.currentTimeMillis()
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
            viewModelScope.launch {
                _uiState.update { it.copy(isSending = true, inputText = "") }
                chatRepository.sendDirectMessage(
                    sender = profile,
                    receiver = other,
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
        val resolved = _uiState.value.registeredUsers.firstOrNull {
            (it.email.isNotBlank() && it.email.equals(otherUser.email, ignoreCase = true)) ||
            (it.userId.isNotBlank() && it.userId == otherUser.userId) ||
            (it.displayName.isNotBlank() && it.displayName.equals(otherUser.displayName, ignoreCase = true))
        } ?: otherUser

        viewModelScope.launch {
            callRepository.initiateCall(_uiState.value.currentUserProfile, resolved)
        }
    }

    fun acceptIncomingCall(callId: String) {
        viewModelScope.launch {
            callRepository.acceptCall(callId)
        }
    }

    fun declineIncomingCall(callId: String) {
        val currentIncoming = _uiState.value.incomingCall
        if (currentIncoming != null && currentIncoming.callId == callId) {
            handleCallFinished(currentIncoming.copy(status = CallSession.STATUS_DECLINED, endedAt = System.currentTimeMillis()))
        }
        viewModelScope.launch {
            callRepository.declineCall(callId)
        }
    }

    fun endActiveCall(callId: String) {
        val currentActive = _uiState.value.activeCall
        if (currentActive != null && currentActive.callId == callId) {
            val finalStatus = if (currentActive.connectedAt == 0L) CallSession.STATUS_MISSED else CallSession.STATUS_ENDED
            handleCallFinished(currentActive.copy(status = finalStatus, endedAt = System.currentTimeMillis()))
        }
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

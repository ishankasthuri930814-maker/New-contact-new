package com.example.service

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class InAppNotification(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val body: String,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val data: Map<String, String> = emptyMap()
)

object InAppNotificationBus {
    private val _notificationFlow = MutableSharedFlow<InAppNotification>(
        replay = 1,
        extraBufferCapacity = 10
    )
    val notificationFlow: SharedFlow<InAppNotification> = _notificationFlow.asSharedFlow()

    fun postNotification(notification: InAppNotification) {
        _notificationFlow.tryEmit(notification)
    }

    fun clear() {
        _notificationFlow.resetReplayCache()
    }
}

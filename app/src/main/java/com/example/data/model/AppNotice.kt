package com.example.data.model

data class AppNotice(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "info", // "info", "warning", "important"
    val date: String = "",
    val active: Boolean = true,
    val dismissible: Boolean = true
)

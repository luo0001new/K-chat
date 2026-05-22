package com.example.kchat.data

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val isAnimating: Boolean = false,
    val isRetracted: Boolean = false
)

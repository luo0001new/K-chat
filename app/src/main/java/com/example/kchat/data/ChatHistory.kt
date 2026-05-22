package com.example.kchat.data

import kotlinx.serialization.Serializable

@Serializable
data class ChatHistory(
    val friendId: String,
    val messages: List<Message> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

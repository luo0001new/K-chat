package com.example.kchat.data

import kotlinx.serialization.Serializable

@Serializable
data class AIFriend(
    val id: String,
    val name: String,
    val avatarUri: String? = null,
    val personality: String = "",
    val apiConfig: ApiConfig,
    val createdAt: Long = System.currentTimeMillis(),
    val lastMessage: String? = null,
    val lastMessageTime: Long? = null,
    val isPinned: Boolean = false,
    val memoryPackage: String? = null,
    val enableMomentPost: Boolean = true,
    val enableProactiveChat: Boolean = true
)

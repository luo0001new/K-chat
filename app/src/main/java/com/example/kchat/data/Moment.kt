package com.example.kchat.data

import kotlinx.serialization.Serializable

@Serializable
data class Moment(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String? = null,
    val content: String,
    val imageUrls: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val likes: List<Like> = emptyList(),
    val comments: List<Comment> = emptyList()
)

@Serializable
data class Like(
    val userId: String,
    val userName: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class Comment(
    val id: String,
    val userId: String,
    val userName: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

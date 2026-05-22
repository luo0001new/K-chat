package com.example.kchat.data

import kotlinx.serialization.Serializable

@Serializable
data class ApiConfig(
    val provider: String = "OpenAI Compatible",
    val baseUrl: String = "https://aihubmix.com/v1",
    val apiKey: String = "",
    val modelId: String = "gpt-4o-mini"
)

package com.example.kchat.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class CharacterShareData(
    val name: String,
    val personality: String = "",
    val provider: String = "OpenAI Compatible",
    val baseUrl: String = "https://aihubmix.com/v1",
    val modelId: String = "gpt-4o-mini",
    val memoryPackage: String? = null
) {
    fun toJson(): String {
        return json.encodeToString(this)
    }

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = false
        }

        fun fromJson(jsonStr: String): CharacterShareData? {
            return try {
                json.decodeFromString<CharacterShareData>(jsonStr)
            } catch (e: Exception) {
                null
            }
        }

        fun fromAIFriend(friend: AIFriend): CharacterShareData {
            return CharacterShareData(
                name = friend.name,
                personality = friend.personality,
                provider = friend.apiConfig.provider,
                baseUrl = friend.apiConfig.baseUrl,
                modelId = friend.apiConfig.modelId,
                memoryPackage = friend.memoryPackage
            )
        }
    }
}

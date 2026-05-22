package com.example.kchat.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SettingsRepository private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "kchat_settings",
        Context.MODE_PRIVATE
    )

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    private val _apiConfig = MutableStateFlow(loadApiConfig())
    val apiConfig: StateFlow<ApiConfig> = _apiConfig.asStateFlow()

    private val _aiName = MutableStateFlow(loadAiName())
    val aiName: StateFlow<String> = _aiName.asStateFlow()

    private val _aiAvatarUri = MutableStateFlow(loadAiAvatarUri())
    val aiAvatarUri: StateFlow<String?> = _aiAvatarUri.asStateFlow()

    private val _userAvatarUri = MutableStateFlow(loadUserAvatarUri())
    val userAvatarUri: StateFlow<String?> = _userAvatarUri.asStateFlow()

    private val _aiFriends = MutableStateFlow(loadAIFriends())
    val aiFriends: StateFlow<List<AIFriend>> = _aiFriends.asStateFlow()

    private val _chatHistories = MutableStateFlow(loadAllChatHistories())
    val chatHistories: StateFlow<Map<String, ChatHistory>> = _chatHistories.asStateFlow()

    private val _moments = MutableStateFlow(loadMoments())
    val moments: StateFlow<List<Moment>> = _moments.asStateFlow()

    private val _momentCheckIns = MutableStateFlow(loadMomentCheckIns())
    val momentCheckIns: StateFlow<Map<String, MomentCheckIn>> = _momentCheckIns.asStateFlow()

    private fun loadApiConfig(): ApiConfig {
        return ApiConfig(
            provider = prefs.getString(KEY_PROVIDER, "OpenAI Compatible") ?: "OpenAI Compatible",
            baseUrl = prefs.getString(KEY_BASE_URL, "https://aihubmix.com/v1") ?: "https://aihubmix.com/v1",
            apiKey = prefs.getString(KEY_API_KEY, "") ?: "",
            modelId = prefs.getString(KEY_MODEL_ID, "gpt-4o-mini") ?: "gpt-4o-mini"
        )
    }

    private fun loadAiName(): String {
        return prefs.getString(KEY_AI_NAME, "AI助手") ?: "AI助手"
    }

    private fun loadAiAvatarUri(): String? {
        return prefs.getString(KEY_AI_AVATAR_URI, null)
    }

    private fun loadUserAvatarUri(): String? {
        return prefs.getString(KEY_USER_AVATAR_URI, null)
    }

    private fun loadAIFriends(): List<AIFriend> {
        val jsonStr = prefs.getString(KEY_AI_FRIENDS, null)
        return if (jsonStr != null) {
            try {
                json.decodeFromString<List<AIFriend>>(jsonStr)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    private fun loadAllChatHistories(): Map<String, ChatHistory> {
        val jsonStr = prefs.getString(KEY_CHAT_HISTORIES, null)
        return if (jsonStr != null) {
            try {
                json.decodeFromString<Map<String, ChatHistory>>(jsonStr)
            } catch (e: Exception) {
                emptyMap()
            }
        } else {
            emptyMap()
        }
    }

    private fun loadMoments(): List<Moment> {
        val jsonStr = prefs.getString(KEY_MOMENTS, null)
        return if (jsonStr != null) {
            try {
                json.decodeFromString<List<Moment>>(jsonStr)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    private fun loadMomentCheckIns(): Map<String, MomentCheckIn> {
        val jsonStr = prefs.getString(KEY_MOMENT_CHECK_INS, null)
        return if (jsonStr != null) {
            try {
                json.decodeFromString<Map<String, MomentCheckIn>>(jsonStr)
            } catch (e: Exception) {
                emptyMap()
            }
        } else {
            emptyMap()
        }
    }

    fun saveChatHistory(friendId: String, messages: List<Message>) {
        val currentHistories = _chatHistories.value.toMutableMap()
        currentHistories[friendId] = ChatHistory(
            friendId = friendId,
            messages = messages,
            lastUpdated = System.currentTimeMillis()
        )
        saveAllChatHistories(currentHistories)
    }

    fun getChatHistory(friendId: String): ChatHistory? {
        return _chatHistories.value[friendId]
    }

    fun clearChatHistory(friendId: String) {
        val currentHistories = _chatHistories.value.toMutableMap()
        currentHistories.remove(friendId)
        saveAllChatHistories(currentHistories)
    }

    private fun saveAllChatHistories(histories: Map<String, ChatHistory>) {
        val jsonStr = json.encodeToString(histories)
        prefs.edit().putString(KEY_CHAT_HISTORIES, jsonStr).apply()
        _chatHistories.value = histories
    }

    fun saveApiConfig(config: ApiConfig) {
        prefs.edit().apply {
            putString(KEY_PROVIDER, config.provider)
            putString(KEY_BASE_URL, config.baseUrl)
            putString(KEY_API_KEY, config.apiKey)
            putString(KEY_MODEL_ID, config.modelId)
            apply()
        }
        _apiConfig.value = config
    }

    fun saveAiName(name: String) {
        prefs.edit().putString(KEY_AI_NAME, name).apply()
        _aiName.value = name
    }

    fun saveAiAvatarUri(uri: String?) {
        prefs.edit().putString(KEY_AI_AVATAR_URI, uri).apply()
        _aiAvatarUri.value = uri
    }

    fun saveUserAvatarUri(uri: String?) {
        prefs.edit().putString(KEY_USER_AVATAR_URI, uri).apply()
        _userAvatarUri.value = uri
    }

    fun addAIFriend(friend: AIFriend) {
        val currentList = _aiFriends.value.toMutableList()
        currentList.add(0, friend)
        saveAIFriends(currentList)
    }

    fun updateAIFriend(friend: AIFriend) {
        val currentList = _aiFriends.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == friend.id }
        if (index != -1) {
            currentList[index] = friend
            saveAIFriends(currentList)
        }
    }

    fun deleteAIFriend(friendId: String) {
        val currentList = _aiFriends.value.filterNot { it.id == friendId }
        saveAIFriends(currentList)
        clearChatHistory(friendId)
    }

    fun updateFriendLastMessage(friendId: String, message: String, timestamp: Long) {
        val currentList = _aiFriends.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == friendId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(
                lastMessage = message,
                lastMessageTime = timestamp
            )
            saveAIFriends(currentList)
        }
    }

    fun togglePinFriend(friendId: String) {
        val currentList = _aiFriends.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == friendId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(
                isPinned = !currentList[index].isPinned
            )
            saveAIFriends(currentList)
        }
    }

    private fun saveAIFriends(list: List<AIFriend>) {
        val jsonStr = json.encodeToString(list)
        prefs.edit().putString(KEY_AI_FRIENDS, jsonStr).apply()
        _aiFriends.value = list
    }

    fun addMoment(moment: Moment) {
        val currentList = _moments.value.toMutableList()
        currentList.add(0, moment)
        saveMoments(currentList)
    }

    fun toggleLike(momentId: String, userId: String, userName: String) {
        val currentList = _moments.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == momentId }
        if (index != -1) {
            val moment = currentList[index]
            val existingLike = moment.likes.find { it.userId == userId }
            val newLikes = if (existingLike != null) {
                moment.likes.filterNot { it.userId == userId }
            } else {
                moment.likes + Like(userId = userId, userName = userName)
            }
            currentList[index] = moment.copy(likes = newLikes)
            saveMoments(currentList)
        }
    }

    fun addComment(momentId: String, comment: Comment) {
        val currentList = _moments.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == momentId }
        if (index != -1) {
            val moment = currentList[index]
            currentList[index] = moment.copy(comments = moment.comments + comment)
            saveMoments(currentList)
        }
    }

    fun deleteComment(momentId: String, commentId: String) {
        val currentList = _moments.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == momentId }
        if (index != -1) {
            val moment = currentList[index]
            currentList[index] = moment.copy(comments = moment.comments.filterNot { it.id == commentId })
            saveMoments(currentList)
        }
    }

    fun clearAllMoments() {
        saveMoments(emptyList())
    }

    private fun saveMoments(list: List<Moment>) {
        val jsonStr = json.encodeToString(list)
        prefs.edit().putString(KEY_MOMENTS, jsonStr).apply()
        _moments.value = list
    }

    fun getMomentCheckIn(friendId: String): MomentCheckIn? {
        return _momentCheckIns.value[friendId]
    }

    fun updateMomentCheckIn(friendId: String, checkIn: MomentCheckIn) {
        val currentCheckIns = _momentCheckIns.value.toMutableMap()
        currentCheckIns[friendId] = checkIn
        saveMomentCheckIns(currentCheckIns)
    }

    fun recordMomentPosted(friendId: String, momentTimestamp: Long) {
        val today = getTodayDateString()
        val currentCheckIn = _momentCheckIns.value[friendId]

        if (currentCheckIn == null || currentCheckIn.date != today) {
            val newCheckIn = MomentCheckIn(
                friendId = friendId,
                date = today,
                postedCount = 1,
                lastPostedTime = momentTimestamp
            )
            updateMomentCheckIn(friendId, newCheckIn)
        } else {
            val updatedCheckIn = currentCheckIn.copy(
                postedCount = currentCheckIn.postedCount + 1,
                lastPostedTime = momentTimestamp
            )
            updateMomentCheckIn(friendId, updatedCheckIn)
        }
    }

    private fun saveMomentCheckIns(checkIns: Map<String, MomentCheckIn>) {
        val jsonStr = json.encodeToString(checkIns)
        prefs.edit().putString(KEY_MOMENT_CHECK_INS, jsonStr).apply()
        _momentCheckIns.value = checkIns
    }

    private fun getTodayDateString(): String {
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return formatter.format(java.util.Date())
    }

    companion object {
        private const val KEY_PROVIDER = "provider"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_MODEL_ID = "model_id"
        private const val KEY_AI_NAME = "ai_name"
        private const val KEY_AI_AVATAR_URI = "ai_avatar_uri"
        private const val KEY_USER_AVATAR_URI = "user_avatar_uri"
        private const val KEY_AI_FRIENDS = "ai_friends"
        private const val KEY_CHAT_HISTORIES = "chat_histories"
        private const val KEY_MOMENTS = "moments"
        private const val KEY_MOMENT_CHECK_INS = "moment_check_ins"

        @Volatile
        private var INSTANCE: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

@Serializable
data class MomentCheckIn(
    val friendId: String,
    val date: String,
    val postedCount: Int = 0,
    val lastPostedTime: Long = 0
)

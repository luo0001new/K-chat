package com.example.kchat.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kchat.data.ApiConfig
import com.example.kchat.data.Message
import com.example.kchat.data.SettingsRepository
import com.example.kchat.data.network.ApiService
import com.example.kchat.data.network.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository.getInstance(application)
    private val apiService = ApiService()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentFriendId: String? = null

    fun loadChatHistory(friendId: String) {
        currentFriendId = friendId
        val chatHistory = repository.getChatHistory(friendId)
        _messages.value = chatHistory?.messages ?: emptyList()
    }

    fun addMessage(message: Message) {
        _messages.value = _messages.value + message
        saveCurrentChatHistory()
    }

    fun sendMessage(
        text: String,
        apiConfig: ApiConfig,
        personality: String,
        aiName: String,
        onMessageReceived: (String) -> Unit
    ) {
        if (text.isBlank()) return

        _isTyping.value = true

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val conversationMessages = mutableListOf<ChatMessage>()

            val recentMoments = repository.moments.value
                .filter { it.authorId == currentFriendId }
                .take(3)
                .map { it.content }

            val currentTime = java.text.SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date())

            val systemContent = buildString {
                if (personality.isNotBlank()) {
                    append("$personality\n\n")
                }
                append("当前时间：$currentTime\n\n")
                if (recentMoments.isNotEmpty()) {
                    append("你最近发过的朋友圈内容：\n")
                    recentMoments.forEachIndexed { index, content ->
                        append("${index + 1}. $content\n")
                    }
                    append("\n在与用户聊天时，如果话题相关，可以提及你朋友圈的内容。")
                }
            }

            conversationMessages.add(
                ChatMessage(
                    role = "system",
                    content = systemContent
                )
            )

            conversationMessages.add(
                ChatMessage(
                    role = "assistant",
                    content = "你好！我是$aiName，很高兴和你聊天！"
                )
            )

            conversationMessages.addAll(
                _messages.value
                    .filter { !it.isRetracted }
                    .map { msg ->
                        ChatMessage(
                            role = if (msg.isFromUser) "user" else "assistant",
                            content = msg.text
                        )
                    }
            )

            if (apiConfig.apiKey.isBlank()) {
                val aiMessage = Message(
                    id = UUID.randomUUID().toString(),
                    text = "请先在设置中配置API Key",
                    isFromUser = false,
                    timestamp = System.currentTimeMillis()
                )
                _messages.value = _messages.value + aiMessage
                saveCurrentChatHistory()
                _isLoading.value = false
                _isTyping.value = false
                return@launch
            }

            val result = apiService.sendMessage(
                apiConfig = apiConfig,
                messages = conversationMessages
            )

            result.fold(
                onSuccess = { response ->
                    val aiText = response.ifBlank { "抱歉，我没有收到有效的回复" }
                    val aiMessage = Message(
                        id = UUID.randomUUID().toString(),
                        text = aiText,
                        isFromUser = false,
                        timestamp = System.currentTimeMillis()
                    )
                    _messages.value = _messages.value + aiMessage
                    saveCurrentChatHistory()
                    onMessageReceived(aiText)
                },
                onFailure = { e ->
                    _error.value = e.message
                    val aiText = "发送消息失败: ${e.message}"
                    val aiMessage = Message(
                        id = UUID.randomUUID().toString(),
                        text = aiText,
                        isFromUser = false,
                        timestamp = System.currentTimeMillis()
                    )
                    _messages.value = _messages.value + aiMessage
                    saveCurrentChatHistory()
                    onMessageReceived(aiText)
                }
            )

            _isTyping.value = false
            _isLoading.value = false
        }
    }

    private fun saveCurrentChatHistory() {
        currentFriendId?.let { friendId ->
            repository.saveChatHistory(friendId, _messages.value)
        }
    }

    fun retractMessage(messageId: String) {
        val messageIndex = _messages.value.indexOfFirst { it.id == messageId }
        if (messageIndex != -1) {
            _messages.value = _messages.value.mapIndexed { index, message ->
                if (index == messageIndex) message.copy(isRetracted = true)
                else message
            }
            saveCurrentChatHistory()
        }
    }

    fun resetCharacter(friendId: String) {
        _messages.value = emptyList()
        repository.clearChatHistory(friendId)
    }
}

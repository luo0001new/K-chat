package com.example.kchat.data.network

import com.example.kchat.data.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateMomentContent(
        apiConfig: ApiConfig,
        aiName: String,
        personality: String,
        recentMoments: List<String> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val recentContext = if (recentMoments.isNotEmpty()) {
                "你最近发过的朋友圈：\n" + recentMoments.joinToString("\n") { "- $it" } + "\n\n请发布一条新的朋友圈，内容要与之前的有所不同，可以是新的生活感悟、日常趣事、或者对之前话题的延续。"
            } else {
                ""
            }

            val prompt = """你是一个活跃在社交平台上喜欢分享生活的用户，你的网名叫${aiName}。${if (personality.isNotEmpty()) "你的性格是：${personality}。" else ""}
${recentContext}
请发布一条微信朋友圈，风格要自然真实，像真实用户发的一样。
要求：
- 30-50字左右
- 可以是日常生活、美食、旅游、心情、工作感悟等
- 语言要自然，像真实用户在发朋友圈
- 可以配上emoji增加趣味
- 不要有引号等符号装饰
- 只输出朋友圈文本内容，不要其他说明"""

            val jsonMessages = JSONArray()
            jsonMessages.put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            })

            val jsonBody = JSONObject().apply {
                put("model", apiConfig.modelId)
                put("messages", jsonMessages)
                put("stream", false)
            }

            val requestBody = jsonBody.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("${apiConfig.baseUrl}/chat/completions")
                .addHeader("Authorization", "Bearer ${apiConfig.apiKey}")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val maxRetries = 2
            var lastException: Exception? = null

            for (attempt in 0..maxRetries) {
                if (attempt > 0) {
                    delay(attempt * 1000L)
                }

                try {
                    val response = client.newCall(request).execute()

                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string() ?: "Unknown error"
                        lastException = Exception("API Error: ${response.code} - $errorBody")
                        if (response.code in 400..499) break
                        continue
                    } else {
                        val responseBody = response.body?.string() ?: ""
                        val content = parseResponse(responseBody)
                        return@withContext Result.success(content.trim())
                    }
                } catch (e: Exception) {
                    lastException = e
                    continue
                }
            }

            Result.failure(lastException ?: Exception("Unknown error"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateMomentReply(
        apiConfig: ApiConfig,
        aiName: String,
        personality: String,
        momentContent: String,
        commentContent: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """你叫${aiName}，${if (personality.isNotEmpty()) "性格是：${personality}。" else ""}你刚在微信朋友圈发了一条动态：
"${momentContent}"

现在有朋友在你的朋友圈下评论："${commentContent}"

请以朋友圈回复的风格回复这条评论，要求：
- 10-30字左右
- 语言自然，像真实用户在回复
- 可以用口语化的表达
- 回复内容要符合你发的朋友圈主题
- 不要有引号等符号
- 只输出回复内容，不要其他说明"""

            val jsonMessages = JSONArray()
            jsonMessages.put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            })

            val jsonBody = JSONObject().apply {
                put("model", apiConfig.modelId)
                put("messages", jsonMessages)
                put("stream", false)
            }

            val requestBody = jsonBody.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("${apiConfig.baseUrl}/chat/completions")
                .addHeader("Authorization", "Bearer ${apiConfig.apiKey}")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val maxRetries = 2
            var lastException: Exception? = null

            for (attempt in 0..maxRetries) {
                if (attempt > 0) {
                    delay(attempt * 1000L)
                }

                try {
                    val response = client.newCall(request).execute()

                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string() ?: "Unknown error"
                        lastException = Exception("API Error: ${response.code} - $errorBody")
                        if (response.code in 400..499) break
                        continue
                    } else {
                        val responseBody = response.body?.string() ?: ""
                        val content = parseResponse(responseBody)
                        return@withContext Result.success(content.trim())
                    }
                } catch (e: Exception) {
                    lastException = e
                    continue
                }
            }

            Result.failure(lastException ?: Exception("Unknown error"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateChatMessage(
        apiConfig: ApiConfig,
        aiName: String,
        systemPrompt: String,
        userPrompt: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val jsonMessages = JSONArray()
            jsonMessages.put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            })
            jsonMessages.put(JSONObject().apply {
                put("role", "user")
                put("content", userPrompt)
            })

            val jsonBody = JSONObject().apply {
                put("model", apiConfig.modelId)
                put("messages", jsonMessages)
                put("stream", false)
            }

            val requestBody = jsonBody.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("${apiConfig.baseUrl}/chat/completions")
                .addHeader("Authorization", "Bearer ${apiConfig.apiKey}")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val maxRetries = 2
            var lastException: Exception? = null

            for (attempt in 0..maxRetries) {
                if (attempt > 0) {
                    delay(attempt * 1000L)
                }

                try {
                    val response = client.newCall(request).execute()

                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string() ?: "Unknown error"
                        lastException = Exception("API Error: ${response.code} - $errorBody")
                        if (response.code in 400..499) break
                        continue
                    } else {
                        val responseBody = response.body?.string() ?: ""
                        val content = parseResponse(responseBody)
                        return@withContext Result.success(content.trim())
                    }
                } catch (e: Exception) {
                    lastException = e
                    continue
                }
            }

            Result.failure(lastException ?: Exception("Unknown error"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(
        apiConfig: ApiConfig,
        messages: List<ChatMessage>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val jsonMessages = JSONArray()
            messages.forEach { msg ->
                jsonMessages.put(JSONObject().apply {
                    put("role", if (msg.role == "user") "user" else "assistant")
                    put("content", msg.content)
                })
            }

            val jsonBody = JSONObject().apply {
                put("model", apiConfig.modelId)
                put("messages", jsonMessages)
                put("stream", false)
            }

            val requestBody = jsonBody.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("${apiConfig.baseUrl}/chat/completions")
                .addHeader("Authorization", "Bearer ${apiConfig.apiKey}")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val maxRetries = 2
            var lastException: Exception? = null

            for (attempt in 0..maxRetries) {
                if (attempt > 0) {
                    delay(attempt * 1000L)
                }

                try {
                    val response = client.newCall(request).execute()

                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string() ?: "Unknown error"
                        lastException = Exception("API Error: ${response.code} - $errorBody")
                        if (response.code in 400..499) break
                        continue
                    } else {
                        val responseBody = response.body?.string() ?: ""
                        val content = parseResponse(responseBody)
                        return@withContext Result.success(content)
                    }
                } catch (e: Exception) {
                    lastException = e
                    continue
                }
            }

            Result.failure(lastException ?: Exception("Unknown error"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseResponse(response: String): String {
        return try {
            val json = JSONObject(response)
            val choices = json.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val message = choices.getJSONObject(0).optJSONObject("message")
                message?.optString("content") ?: ""
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }
}

data class ChatMessage(
    val role: String,
    val content: String
)

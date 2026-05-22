package com.example.kchat.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.kchat.MainActivity
import com.example.kchat.data.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ProactiveChatSchedulerService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var schedulerJob: Job? = null

    private lateinit var repository: SettingsRepository
    private lateinit var apiService: ApiService
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        repository = SettingsRepository.getInstance(this)
        apiService = ApiService()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        startScheduler()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        schedulerJob?.cancel()
        serviceScope.cancel()
    }

    private fun startScheduler() {
        schedulerJob?.cancel()
        schedulerJob = serviceScope.launch {
            while (true) {
                val friends = repository.aiFriends.value

                if (friends.isNotEmpty()) {
                    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    val today = getTodayDateString()

                    friends.forEach { friend ->
                        if (!friend.enableProactiveChat) {
                            return@forEach
                        }

                        val lastChatDate = prefs.getString("${friend.id}_last_chat_date", null)
                        val todayChatCount = prefs.getInt("${friend.id}_today_chat_count_$today", 0)

                        val shouldGreet = when {
                            lastChatDate != today -> true
                            todayChatCount == 0 -> true
                            todayChatCount < 3 && isTimeForNextMessage(lastChatDate, todayChatCount) -> true
                            else -> false
                        }

                        if (shouldGreet && todayChatCount < 3) {
                            val isFirstMessage = todayChatCount == 0
                            val greetingType = determineGreetingType(currentHour, isFirstMessage)

                            if (greetingType != null) {
                                val personality = friend.personality.ifEmpty { "友善热情" }
                                val recentMoments = repository.moments.value
                                    .filter { it.authorId == friend.id }
                                    .take(3)
                                    .map { it.content }

                                val systemPrompt = buildString {
                                    append("$personality\n\n")
                                    append("当前时间：${getCurrentTimeString()}\n\n")
                                    if (recentMoments.isNotEmpty()) {
                                        append("你最近发过的朋友圈内容：\n")
                                        recentMoments.forEachIndexed { index, content ->
                                            append("${index + 1}. $content\n")
                                        }
                                        append("\n在与用户聊天时，如果话题相关，可以提及你朋友圈的内容。")
                                    }
                                }

                                val greetingPrompt = when (greetingType) {
                                    GreetingType.MORNING_GREETING -> {
                                        "作为${friend.name}，根据你的性格（$personality），生成一句简单的早上问候语。10-25字左右，像朋友间的问候。不要有引号等符号，只输出问候语内容。"
                                    }
                                    GreetingType.EVENING_WARM -> {
                                        "作为${friend.name}，根据你的性格（$personality），生成一句关心用户不要熬夜的温馨话语。10-25字左右，像朋友间的关心。不要有引号等符号，只输出内容。"
                                    }
                                    GreetingType.ASK_ABOUT_DAY -> {
                                        "作为${friend.name}，根据你的性格（$personality），询问用户今天在干嘛、分享的事情或经历。10-30字左右，像朋友间的闲聊。不要有引号等符号，只输出内容。"
                                    }
                                }

                                val result = apiService.generateChatMessage(
                                    apiConfig = friend.apiConfig,
                                    aiName = friend.name,
                                    systemPrompt = systemPrompt,
                                    userPrompt = greetingPrompt
                                )

                                result.onSuccess { message ->
                                    if (message.isNotBlank()) {
                                        sendChatNotification(friend.id, friend.name, message.trim())
                                        saveChatMessage(friend.id, message.trim(), "ai_schedule")
                                    }
                                }

                                delay(2000)
                            }
                        }
                    }
                }

                delay(TimeUnit.MINUTES.toMillis(15))
            }
        }
    }

    private val prefs by lazy {
        getSharedPreferences("proactive_chat_service", Context.MODE_PRIVATE)
    }

    private fun saveChatMessage(friendId: String, message: String, type: String) {
        val today = getTodayDateString()
        val chatHistory = repository.getChatHistory(friendId)
        val currentMessages = chatHistory?.messages?.toMutableList() ?: mutableListOf()

        currentMessages.add(
            Message(
                id = System.currentTimeMillis().toString(),
                text = message,
                isFromUser = false,
                timestamp = System.currentTimeMillis()
            )
        )

        repository.saveChatHistory(friendId, currentMessages)
        repository.updateFriendLastMessage(friendId, message, System.currentTimeMillis())

        prefs.edit()
            .putString("${friendId}_last_chat_date", today)
            .putString("last_chat_time", System.currentTimeMillis().toString())
            .putInt("${friendId}_today_chat_count_$today", prefs.getInt("${friendId}_today_chat_count_$today", 0) + 1)
            .apply()
    }

    private fun sendChatNotification(friendId: String, friendName: String, message: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(friendName)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    friendId.hashCode(),
                    Intent(this, MainActivity::class.java).apply {
                        putExtra("friendId", friendId)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

        val notificationId = (friendId.hashCode() + System.currentTimeMillis()).toInt()
        notificationManager.notify(notificationId, notification)
    }

    private fun determineGreetingType(currentHour: Int, isFirstMessage: Boolean): GreetingType? {
        return when {
            isFirstMessage && currentHour in 6..10 -> GreetingType.MORNING_GREETING
            isFirstMessage && currentHour in 18..23 -> GreetingType.EVENING_WARM
            isFirstMessage -> GreetingType.MORNING_GREETING
            currentHour in 6..9 -> GreetingType.MORNING_GREETING
            currentHour in 18..23 -> GreetingType.EVENING_WARM
            currentHour in 10..17 -> GreetingType.ASK_ABOUT_DAY
            else -> null
        }
    }

    private fun isTimeForNextMessage(lastChatDate: String?, todayChatCount: Int): Boolean {
        if (lastChatDate != getTodayDateString()) return true

        val lastChatTimeStr = prefs.getString("last_chat_time", null) ?: return true
        val lastChatTime = lastChatTimeStr.toLongOrNull() ?: return true

        val hoursSinceLastChat = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - lastChatTime)
        val minInterval = when (todayChatCount) {
            0 -> 0L
            1 -> 3L
            2 -> 4L
            else -> 24L
        }

        return hoursSinceLastChat >= minInterval
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannels(listOf(
                NotificationChannel(
                    CHANNEL_ID,
                    "AI朋友圈服务",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "保持AI朋友圈自动发送服务运行"
                    setShowBadge(false)
                },
                NotificationChannel(
                    CHAT_CHANNEL_ID,
                    "AI助手消息",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "AI助手主动发送的消息通知"
                    enableVibration(true)
                }
            ))
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHAT_CHANNEL_ID)
        .setContentTitle("AI助手服务运行中")
        .setContentText("AI好友正在自动聊天")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()

    private fun getTodayDateString(): String {
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return formatter.format(java.util.Date())
    }

    private fun getCurrentTimeString(): String {
        val formatter = java.text.SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", java.util.Locale.getDefault())
        return formatter.format(java.util.Date())
    }

    private enum class GreetingType {
        MORNING_GREETING,
        EVENING_WARM,
        ASK_ABOUT_DAY
    }

    companion object {
        private const val CHANNEL_ID = "proactive_chat_scheduler_channel"
        private const val CHAT_CHANNEL_ID = "ai_chat_messages"
        private const val NOTIFICATION_ID = 1002

        fun start(context: Context) {
            val intent = Intent(context, ProactiveChatSchedulerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ProactiveChatSchedulerService::class.java))
        }
    }
}

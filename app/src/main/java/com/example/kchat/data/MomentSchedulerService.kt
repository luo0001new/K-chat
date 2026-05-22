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
import java.util.concurrent.TimeUnit

class MomentSchedulerService : Service() {

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
                val moments = repository.moments.value

                if (friends.isNotEmpty()) {
                    friends.forEach { friend ->
                        if (!friend.enableMomentPost) {
                            return@forEach
                        }

                        val checkIn = repository.getMomentCheckIn(friend.id)
                        val today = getTodayDateString()

                        if (checkIn == null || checkIn.date != today) {
                            val missingDays = if (checkIn == null) {
                                1
                            } else {
                                val daysDiff = calculateDaysDifference(checkIn.date, today)
                                daysDiff.coerceAtMost(3)
                            }

                            for (day in 0 until missingDays) {
                                val targetDate = if (day == 0) {
                                    today
                                } else {
                                    getDateStringDaysAgo(missingDays - day)
                                }

                                val momentsToPost = 2 + (0..1).random()
                                for (i in 0 until momentsToPost) {
                                    val fakeTimestamp = generateRandomTimestampForDate(targetDate)
                                    val friendMoments = repository.moments.value
                                        .filter { it.authorId == friend.id }
                                        .take(3)
                                        .map { it.content }

                                    val result = apiService.generateMomentContent(
                                        apiConfig = friend.apiConfig,
                                        aiName = friend.name,
                                        personality = friend.personality,
                                        recentMoments = friendMoments
                                    )
                                    result.onSuccess { content ->
                                        val moment = Moment(
                                            id = System.currentTimeMillis().toString() + "_${friend.id}_${i}",
                                            authorId = friend.id,
                                            authorName = friend.name,
                                            authorAvatar = friend.avatarUri,
                                            content = content,
                                            createdAt = fakeTimestamp
                                        )
                                        repository.addMoment(moment)
                                        repository.recordMomentPosted(friend.id, fakeTimestamp)
                                    }
                                    delay(500)
                                }
                            }
                        }

                        val todayMoments = repository.moments.value
                            .filter { it.authorId == friend.id && getDateStringFromTimestamp(it.createdAt) == today }

                        if (todayMoments.size < 2) {
                            val momentsNeeded = 2 - todayMoments.size
                            repeat(momentsNeeded) {
                                val friendMoments = repository.moments.value
                                    .filter { it.authorId == friend.id }
                                    .take(3)
                                    .map { it.content }

                                val result = apiService.generateMomentContent(
                                    apiConfig = friend.apiConfig,
                                    aiName = friend.name,
                                    personality = friend.personality,
                                    recentMoments = friendMoments
                                )
                                result.onSuccess { content ->
                                    val moment = Moment(
                                        id = System.currentTimeMillis().toString(),
                                        authorId = friend.id,
                                        authorName = friend.name,
                                        authorAvatar = friend.avatarUri,
                                        content = content,
                                        createdAt = System.currentTimeMillis()
                                    )
                                    repository.addMoment(moment)
                                    repository.recordMomentPosted(friend.id, System.currentTimeMillis())
                                }
                            }
                        }
                    }

                    moments.forEach { moment ->
                        if (moment.comments.isNotEmpty() && (0..2).random() == 0) {
                            val lastComment = moment.comments.last()
                            if (lastComment.userId != moment.authorId && !lastComment.userId.startsWith("ai_")) {
                                val author = friends.find { it.id == moment.authorId }
                                author?.let { friend ->
                                    launch {
                                        val replyResult = apiService.generateMomentReply(
                                            apiConfig = friend.apiConfig,
                                            aiName = friend.name,
                                            personality = friend.personality,
                                            momentContent = moment.content,
                                            commentContent = lastComment.content
                                        )
                                        replyResult.onSuccess { replyContent ->
                                            repository.addComment(
                                                moment.id,
                                                Comment(
                                                    id = System.currentTimeMillis().toString(),
                                                    userId = friend.id,
                                                    userName = friend.name,
                                                    content = replyContent,
                                                    createdAt = System.currentTimeMillis()
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if ((0..3).random() == 0) {
                            val otherFriend = friends.find { it.id != moment.authorId }
                            if (otherFriend != null) {
                                repository.toggleLike(moment.id, otherFriend.id, otherFriend.name)
                            }
                        }
                    }
                }

                delay(TimeUnit.MINUTES.toMillis(30))
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AI朋友圈服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "保持AI朋友圈自动发送服务运行"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("AI朋友圈服务运行中")
        .setContentText("AI好友正在自动发朋友圈")
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

    private fun getDateStringDaysAgo(days: Int): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -days)
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return formatter.format(calendar.time)
    }

    private fun getDateStringFromTimestamp(timestamp: Long): String {
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return formatter.format(java.util.Date(timestamp))
    }

    private fun calculateDaysDifference(date1: String, date2: String): Int {
        return try {
            val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val d1 = formatter.parse(date1)
            val d2 = formatter.parse(date2)
            if (d1 != null && d2 != null) {
                TimeUnit.DAYS.convert(d2.time - d1.time, TimeUnit.MILLISECONDS).toInt()
            } else {
                1
            }
        } catch (e: Exception) {
            1
        }
    }

    private fun generateRandomTimestampForDate(dateString: String): Long {
        return try {
            val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val date = formatter.parse(dateString) ?: return System.currentTimeMillis()

            val calendar = java.util.Calendar.getInstance()
            calendar.time = date

            val hourRanges = listOf(
                8 to 10,
                11 to 13,
                14 to 16,
                17 to 19,
                20 to 22
            )
            val (startHour, endHour) = hourRanges.random()
            val hour = startHour + (0..(endHour - startHour)).random()
            val minute = (0..59).random()
            val second = (0..59).random()

            calendar.set(java.util.Calendar.HOUR_OF_DAY, hour)
            calendar.set(java.util.Calendar.MINUTE, minute)
            calendar.set(java.util.Calendar.SECOND, second)

            calendar.timeInMillis
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    companion object {
        private const val CHANNEL_ID = "moment_scheduler_channel"
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, MomentSchedulerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, MomentSchedulerService::class.java))
        }
    }
}

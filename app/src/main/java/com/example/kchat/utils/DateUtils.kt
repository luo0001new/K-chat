package com.example.kchat.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    fun formatMessageTime(timestamp: Long): String {
        val messageDate = Date(timestamp)
        val calendar = Calendar.getInstance()
        val todayCalendar = Calendar.getInstance()
        val yesterdayCalendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }

        calendar.time = messageDate

        return when {
            isSameDay(calendar, todayCalendar) -> {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                timeFormat.format(messageDate)
            }
            isSameDay(calendar, yesterdayCalendar) -> {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                "昨天 ${timeFormat.format(messageDate)}"
            }
            calendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) -> {
                val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
                dateFormat.format(messageDate)
            }
            else -> {
                val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                dateFormat.format(messageDate)
            }
        }
    }

    fun formatMessageTimestamp(timestamp: Long): String {
        val messageDate = Date(timestamp)
        val calendar = Calendar.getInstance()
        calendar.time = messageDate

        val todayCalendar = Calendar.getInstance()
        val yesterdayCalendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }

        return when {
            isSameDay(calendar, todayCalendar) -> {
                val timeFormat = SimpleDateFormat("今天 HH:mm", Locale.getDefault())
                timeFormat.format(messageDate)
            }
            isSameDay(calendar, yesterdayCalendar) -> {
                val timeFormat = SimpleDateFormat("昨天 HH:mm", Locale.getDefault())
                timeFormat.format(messageDate)
            }
            else -> {
                val dateFormat = SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault())
                dateFormat.format(messageDate)
            }
        }
    }

    fun formatRelativeTime(timestamp: Long, currentTime: Long = System.currentTimeMillis()): String {
        val diff = currentTime - timestamp

        val oneMinute = 60 * 1000L
        val oneHour = 60 * oneMinute
        val oneDay = 24 * oneHour

        return when {
            diff < oneMinute -> "刚刚"
            diff < oneHour -> "${diff / oneMinute}分钟前"
            diff < oneDay -> "${diff / oneHour}小时前"
            diff < 7 * oneDay -> "${diff / oneDay}天前"
            else -> {
                val formatter = SimpleDateFormat("MM/dd", Locale.getDefault())
                formatter.format(Date(timestamp))
            }
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { time = Date(timestamp1) }
        val cal2 = Calendar.getInstance().apply { time = Date(timestamp2) }
        return isSameDay(cal1, cal2)
    }
}

package com.example.kchat.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kchat.utils.DateUtils

/**
 * 时间分隔组件
 * 用于在消息列表中按时间分组显示
 *
 * @param timestamp 时间戳（毫秒）
 * @param modifier 修饰符
 */
@Composable
fun TimeDivider(
    timestamp: Long,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = DateUtils.formatMessageTime(timestamp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Gray.copy(alpha = 0.8f)
        )
    }
}

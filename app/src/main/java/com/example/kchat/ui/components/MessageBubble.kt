package com.example.kchat.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kchat.data.Message
import com.example.kchat.ui.theme.iOSBlue
import com.example.kchat.ui.theme.iOSGray
import com.example.kchat.ui.theme.iOSGrayDark
import com.example.kchat.utils.DateUtils
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    isNewMessage: Boolean = false,
    avatarUri: String? = null,
    onAvatarClick: (() -> Unit)? = null,
    onRetract: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    enableAnimation: Boolean = true
) {
    if (message.isRetracted) {
        return
    }

    val isFromUser = message.isFromUser
    val backgroundColor = if (isFromUser) iOSBlue else iOSGray
    val textColor = if (isFromUser) Color.White else Color.Black

    val bubbleShape = remember(isFromUser) {
        if (isFromUser) {
            RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
        } else {
            RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
        }
    }

    var displayedText by remember { mutableStateOf("") }
    var bubbleVisible by remember { mutableStateOf(false) }

    val gradientColors = remember(isFromUser) {
        if (isFromUser) listOf(iOSBlue, Color(0xFF5856D6))
        else listOf(Color(0xFFE9E9EB), Color(0xFFDCDCE0))
    }

    val bubbleAlpha by animateColorAsState(
        targetValue = if (bubbleVisible) Color.Unspecified else Color.Unspecified,
        animationSpec = tween(300),
        label = "bubbleAlpha"
    )

    val animationKey = remember(message.id) { message.id + "_" + isNewMessage }
    LaunchedEffect(animationKey) {
        if (!isFromUser && isNewMessage && enableAnimation) {
            displayedText = ""
            bubbleVisible = true
            message.text.forEachIndexed { index, _ ->
                delay(20)
                displayedText = message.text.take(index + 1)
            }
        } else {
            displayedText = message.text
            bubbleVisible = true
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        horizontalArrangement = if (isFromUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isFromUser) {
            Avatar(
                isAI = true,
                avatarUri = avatarUri,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .then(
                        if (onAvatarClick != null) {
                            Modifier.clickable(onClick = onAvatarClick)
                        } else {
                            Modifier
                        }
                    )
            )
        }

        Column(
            horizontalAlignment = if (isFromUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .shadow(
                        elevation = if (isFromUser) 3.dp else 1.dp,
                        shape = bubbleShape
                    )
                    .clip(bubbleShape)
                    .background(
                        brush = Brush.verticalGradient(gradientColors),
                        shape = bubbleShape
                    )
                    .padding(horizontal = 14.dp, vertical = 9.dp)
                    .then(
                        if (isFromUser && onRetract != null) {
                            Modifier.combinedClickable(
                                onClick = {},
                                onLongClick = onRetract
                            )
                        } else {
                            Modifier
                        }
                    )
            ) {
                Text(
                    text = displayedText,
                    fontSize = 16.sp,
                    color = textColor,
                    lineHeight = 22.sp,
                    letterSpacing = 0.2.sp
                )
            }

            Row(
                modifier = Modifier.padding(top = 2.dp, end = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isFromUser) {
                    Text(
                        text = "Read",
                        fontSize = 11.sp,
                        color = iOSGrayDark,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                Text(
                    text = formatTime(message.timestamp),
                    fontSize = 11.sp,
                    color = iOSGrayDark
                )
            }
        }

        if (isFromUser) {
            Avatar(
                isAI = false,
                avatarUri = avatarUri,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .then(
                        if (onAvatarClick != null) {
                            Modifier.clickable(onClick = onAvatarClick)
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}

private fun formatTime(timestamp: Long): String {
    return DateUtils.formatMessageTime(timestamp)
}

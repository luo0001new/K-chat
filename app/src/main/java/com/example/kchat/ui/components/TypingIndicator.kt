package com.example.kchat.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun TypingIndicator(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    Row(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .padding(start = 44.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFE5E5EA))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Dot(index = 0)
                Dot(index = 1)
                Dot(index = 2)
            }
        }
    }
}

@Composable
private fun Dot(index: Int) {
    val offset = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(index * 150L)
        offset.animateTo(
            targetValue = -6f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 600),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Box(
        modifier = Modifier
            .size(8.dp)
            .offset(y = offset.value.dp)
            .clip(CircleShape)
            .background(Color.Gray.copy(alpha = 0.6f))
    )
}

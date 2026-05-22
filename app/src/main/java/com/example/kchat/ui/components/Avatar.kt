package com.example.kchat.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kchat.ui.theme.iOSBlue

@Composable
fun Avatar(
    isAI: Boolean,
    avatarUri: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isAI) Color(0xFF8E8E93) else iOSBlue
    val text = if (isAI) "AI" else "我"

    val clickModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    if (avatarUri != null) {
        AsyncImage(
            model = Uri.parse(avatarUri),
            contentDescription = null,
            modifier = clickModifier
                .size(36.dp)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = clickModifier
                .size(36.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

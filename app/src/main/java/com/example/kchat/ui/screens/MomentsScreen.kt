package com.example.kchat.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import coil.compose.AsyncImage
import com.example.kchat.data.AIFriend
import com.example.kchat.data.Comment
import com.example.kchat.data.Moment
import com.example.kchat.data.SettingsRepository
import com.example.kchat.data.network.ApiService
import com.example.kchat.ui.components.Avatar
import com.example.kchat.ui.theme.iOSBackground
import com.example.kchat.ui.theme.iOSBlue
import com.example.kchat.ui.theme.iOSCardBackground
import com.example.kchat.ui.theme.iOSGray
import com.example.kchat.ui.theme.iOSGrayDark
import com.example.kchat.ui.theme.iOSLikeRed
import com.example.kchat.ui.theme.iOSThinSeparator
import com.example.kchat.utils.DateUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun MomentsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = SettingsRepository.getInstance(context)
    val friends by repository.aiFriends.collectAsState()
    val moments by repository.moments.collectAsState()
    val scope = rememberCoroutineScope()
    val apiService = remember { ApiService() }

    var isGenerating by remember { mutableStateOf(false) }
    var generatingFriendId by remember { mutableStateOf<String?>(null) }
    var isCardExpanded by remember { mutableStateOf(false) }
    var tick by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            tick = System.currentTimeMillis()
            delay(60000)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (friends.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = iOSCardBackground)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isCardExpanded = !isCardExpanded }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "让AI好友发朋友圈",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Icon(
                            imageVector = if (isCardExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isCardExpanded) "收起" else "展开",
                            tint = iOSGrayDark
                        )
                    }

                    AnimatedVisibility(visible = isCardExpanded) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    friends.take(5).forEach { friend ->
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .background(iOSGray.copy(alpha = 0.3f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (generatingFriendId == friend.id) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(24.dp),
                                                    strokeWidth = 2.dp,
                                                    color = iOSBlue
                                                )
                                            } else {
                                                IconButton(
                                                    onClick = {
                                                        if (!isGenerating) {
                                                            isGenerating = true
                                                            generatingFriendId = friend.id
                                                            scope.launch {
                                                                val friendMoments = moments.filter { it.authorId == friend.id }
                                                                val recentMoments = friendMoments.take(3).map { it.content }
                                                                val result = apiService.generateMomentContent(
                                                                    apiConfig = friend.apiConfig,
                                                                    aiName = friend.name,
                                                                    personality = friend.personality.ifEmpty { "友善热情" },
                                                                    recentMoments = recentMoments
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
                                                            result.onFailure {
                                                                Toast.makeText(context, "朋友圈生成失败", Toast.LENGTH_SHORT).show()
                                                            }
                                                            isGenerating = false
                                                            generatingFriendId = null
                                                            }
                                                        }
                                                    },
                                                    enabled = !isGenerating,
                                                    modifier = Modifier.size(56.dp)
                                                ) {
                                                    if (friend.avatarUri != null) {
                                                        AsyncImage(
                                                            model = android.net.Uri.parse(friend.avatarUri),
                                                            contentDescription = null,
                                                            modifier = Modifier
                                                                .size(48.dp)
                                                                .clip(CircleShape)
                                                        )
                                                    } else {
                                                        Avatar(
                                                            isAI = true,
                                                            avatarUri = null,
                                                            modifier = Modifier.size(48.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        repository.clearAllMoments()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "清除",
                                        tint = Color.Red,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "清除朋友圈",
                                        color = Color.Red,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (friends.isEmpty()) {
            EmptyMomentsState()
        } else if (moments.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "还没有朋友圈",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "点击上方AI好友的头像让他们发朋友圈",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "AI也会在1-24小时内自动发朋友圈",
                        fontSize = 12.sp,
                        color = Color.Gray.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            val sortedMoments = remember(moments) {
                moments.sortedByDescending { it.createdAt }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(iOSBackground)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
            ) {
                items(sortedMoments, key = { it.id }) { moment ->
                    MomentItem(
                        moment = moment,
                        currentUserId = "current_user",
                        currentUserName = "我",
                        tick = tick,
                        friends = friends,
                        onLike = { userId, userName ->
                            repository.toggleLike(moment.id, userId, userName)
                        },
                        onComment = { comment ->
                            repository.addComment(moment.id, comment)
                            scope.launch {
                                delay(1000)
                                val friend = friends.find { it.id == moment.authorId }
                                if (friend != null) {
                                    val replyResult = apiService.generateMomentReply(
                                        apiConfig = friend.apiConfig,
                                        aiName = friend.name,
                                        personality = friend.personality,
                                        momentContent = moment.content,
                                        commentContent = comment.content
                                    )
                                    replyResult.onSuccess { replyContent ->
                                        val replyComment = Comment(
                                            id = UUID.randomUUID().toString(),
                                            userId = friend.id,
                                            userName = friend.name,
                                            content = replyContent,
                                            createdAt = System.currentTimeMillis()
                                        )
                                        repository.addComment(moment.id, replyComment)
                                    }
                                }
                            }
                        },
                        onDeleteComment = { commentId ->
                            repository.deleteComment(moment.id, commentId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyMomentsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(iOSBlue.copy(alpha = 0.12f), iOSBlue.copy(alpha = 0.05f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = iOSBlue,
                    modifier = Modifier.size(40.dp)
                )
            }

            Text(
                text = "朋友圈",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "添加AI好友后，让他们发朋友圈哦",
                fontSize = 15.sp,
                color = iOSGrayDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Text(
                text = "AI会在1-24小时内自动发朋友圈",
                fontSize = 12.sp,
                color = Color.Gray.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MomentItem(
    moment: Moment,
    currentUserId: String,
    currentUserName: String,
    tick: Long,
    friends: List<AIFriend>,
    onLike: (String, String) -> Unit,
    onComment: (Comment) -> Unit,
    onDeleteComment: (String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    var showComments by remember { mutableStateOf(true) }
    var likeAnimTrigger by remember { mutableStateOf(0) }
    val likeAnimatable = remember { Animatable(1f) }

    LaunchedEffect(likeAnimTrigger) {
        if (likeAnimTrigger > 0) {
            likeAnimatable.snapTo(1f)
            likeAnimatable.animateTo(1.3f, animationSpec = tween(200))
            likeAnimatable.animateTo(1.0f, animationSpec = tween(200))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color.White, Color(0xFFFAFAFC))
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (moment.authorAvatar != null) {
                AsyncImage(
                    model = android.net.Uri.parse(moment.authorAvatar),
                    contentDescription = null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                )
            } else {
                Avatar(
                    isAI = true,
                    avatarUri = null,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = moment.authorName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = moment.content,
                    fontSize = 15.sp,
                    color = Color.Black,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = formatMomentTime(moment.createdAt, tick),
                    fontSize = 13.sp,
                    color = iOSGrayDark
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    onLike(currentUserId, currentUserName)
                    likeAnimTrigger++
                }
            ) {
                val isLiked = moment.likes.any { it.userId == currentUserId }
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "点赞",
                    tint = if (isLiked) iOSLikeRed else iOSGrayDark,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer(
                            scaleX = likeAnimatable.value,
                            scaleY = likeAnimatable.value
                        )
                )
                if (moment.likes.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${moment.likes.size}",
                        fontSize = 13.sp,
                        color = iOSGrayDark
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    showComments = !showComments
                }
            ) {
                Text(
                    text = if (showComments) "收起评论" else "评论(${moment.comments.size})",
                    fontSize = 13.sp,
                    color = iOSGrayDark
                )
            }
        }

        if (moment.comments.isNotEmpty() && showComments) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iOSBackground)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                moment.comments.forEachIndexed { index, comment ->
                    CommentItem(
                        comment = comment,
                        currentUserId = currentUserId,
                        onDelete = { onDeleteComment(comment.id) }
                    )
                    if (index < moment.comments.lastIndex) {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(iOSThinSeparator)
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = commentText,
                onValueChange = { commentText = it },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(iOSBackground)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                cursorBrush = SolidColor(iOSBlue),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box {
                        if (commentText.isEmpty()) {
                            Text(
                                text = "写评论...",
                                fontSize = 14.sp,
                                color = iOSGrayDark
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (commentText.isNotBlank()) {
                        val comment = Comment(
                            id = UUID.randomUUID().toString(),
                            userId = currentUserId,
                            userName = currentUserName,
                            content = commentText.trim()
                        )
                        onComment(comment)
                        commentText = ""
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "发送",
                    tint = iOSBlue
                )
            }
        }
    }
}

@Composable
private fun CommentItem(
    comment: Comment,
    currentUserId: String,
    onDelete: () -> Unit
) {
    val isOwnComment = comment.userId == currentUserId

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = comment.userName,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = iOSBlue
        )

        if (isOwnComment) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "(我)",
                fontSize = 13.sp,
                color = iOSGrayDark
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = comment.content,
            fontSize = 13.sp,
            color = Color.Black,
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f)
        )

        if (isOwnComment) {
            Text(
                text = "删除",
                fontSize = 12.sp,
                color = Color.Red,
                modifier = Modifier
                    .clickable(onClick = onDelete)
                    .padding(start = 8.dp)
            )
        }
    }
}

private fun formatMomentTime(timestamp: Long, currentTime: Long = System.currentTimeMillis()): String {
    return DateUtils.formatRelativeTime(timestamp, currentTime)
}

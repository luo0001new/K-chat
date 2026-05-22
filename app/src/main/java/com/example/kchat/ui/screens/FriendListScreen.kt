package com.example.kchat.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kchat.data.AIFriend
import com.example.kchat.data.CharacterShareData
import com.example.kchat.data.SettingsRepository
import com.example.kchat.ui.components.Avatar
import com.example.kchat.ui.theme.iOSBackground
import com.example.kchat.ui.theme.iOSBlue
import com.example.kchat.ui.theme.iOSCardBackground
import com.example.kchat.ui.theme.iOSGray
import com.example.kchat.ui.theme.iOSGrayDark
import com.example.kchat.ui.theme.iOSInputBackground
import com.example.kchat.ui.theme.iOSLikeRed
import com.example.kchat.ui.theme.iOSPlaceholder
import com.example.kchat.ui.theme.SeparatorTint
import com.example.kchat.utils.DateUtils

enum class MainTab {
    CHAT, MOMENTS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendListScreen(
    onAddFriendClick: () -> Unit,
    onFriendClick: (AIFriend) -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = SettingsRepository.getInstance(context)
    val friends by repository.aiFriends.collectAsState()
    var selectedTab by remember { mutableStateOf(MainTab.CHAT) }
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredFriends = remember(searchQuery, friends) {
        if (searchQuery.isBlank()) friends
        else friends.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        if (!isSearchVisible) {
                            Text(
                                text = when (selectedTab) {
                                    MainTab.CHAT -> "K chat"
                                    MainTab.MOMENTS -> "朋友圈"
                                },
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onAboutClick) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "关于",
                                tint = iOSBlue
                            )
                        }
                        IconButton(onClick = {
                            isSearchVisible = !isSearchVisible
                            if (!isSearchVisible) searchQuery = ""
                        }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "搜索",
                                tint = if (isSearchVisible) iOSGrayDark else iOSBlue
                            )
                        }
                        IconButton(onClick = onAddFriendClick) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "添加好友",
                                tint = iOSBlue
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.White
                    )
                )
                if (isSearchVisible) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = {
                            Text("搜索好友...", fontSize = 15.sp, color = iOSPlaceholder)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = iOSGrayDark,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "清除",
                                        tint = iOSGrayDark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = iOSInputBackground,
                            unfocusedContainerColor = iOSInputBackground,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = iOSBlue
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { isSearchVisible = false })
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFE5E5E5))
                )
                TabNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        },
        containerColor = iOSBackground
    ) { paddingValues ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "tabContent"
        ) { tab ->
            when (tab) {
                MainTab.CHAT -> {
                    if (filteredFriends.isEmpty()) {
                        EmptyState(onAddClick = onAddFriendClick, modifier = Modifier.padding(paddingValues))
                    } else {
                        FriendList(
                        friends = filteredFriends,
                        onFriendClick = onFriendClick,
                        onPinToggle = { friend -> repository.togglePinFriend(friend.id) },
                        onShareClick = { friend ->
                            val shareData = CharacterShareData.fromAIFriend(friend)
                            val jsonStr = shareData.toJson()
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, jsonStr)
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "推荐AI好友: ${friend.name}")
                            }
                            context.startActivity(
                                android.content.Intent.createChooser(intent, "分享人物")
                            )
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                    }
                }
                MainTab.MOMENTS -> {
                    MomentsScreen(modifier = Modifier.padding(paddingValues))
                }
            }
        }
    }
}

@Composable
private fun TabNavigationBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        TabItem(
            text = "聊天",
            isSelected = selectedTab == MainTab.CHAT,
            onClick = { onTabSelected(MainTab.CHAT) }
        )
        Spacer(modifier = Modifier.width(24.dp))
        TabItem(
            text = "朋友圈",
            isSelected = selectedTab == MainTab.MOMENTS,
            onClick = { onTabSelected(MainTab.MOMENTS) }
        )
    }
}

@Composable
private fun TabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgAnim by animateColorAsState(
        targetValue = if (isSelected) Color(0x1A007AFF) else Color.Transparent,
        animationSpec = tween(250),
        label = "tabBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) iOSBlue else iOSGrayDark,
        animationSpec = tween(250),
        label = "tabText"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(bgAnim)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun EmptyState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(iOSBlue.copy(alpha = 0.1f), iOSBlue.copy(alpha = 0.05f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = iOSBlue,
                    modifier = Modifier.size(52.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "还没有AI好友",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "点击下方按钮添加你的第一个AI好友",
                    fontSize = 15.sp,
                    color = iOSGrayDark,
                    textAlign = TextAlign.Center
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.horizontalGradient(listOf(iOSBlue, Color(0xFF5856D6)))
                    )
                    .clickable(onClick = onAddClick)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "添加AI好友",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendList(
    friends: List<AIFriend>,
    onFriendClick: (AIFriend) -> Unit,
    onPinToggle: (AIFriend) -> Unit,
    onShareClick: (AIFriend) -> Unit,
    modifier: Modifier = Modifier
) {
    val pinnedFriends = friends.filter { it.isPinned }
    val defaultFriends = friends.filterNot { it.isPinned }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        if (pinnedFriends.isNotEmpty()) {
            item {
                FriendCard(
                    friends = pinnedFriends,
                    onFriendClick = onFriendClick,
                    onPinToggle = onPinToggle,
                    onShareClick = onShareClick,
                    cardTitle = "置顶"
                )
            }
        }

        if (defaultFriends.isNotEmpty()) {
            item {
                FriendCard(
                    friends = defaultFriends,
                    onFriendClick = onFriendClick,
                    onPinToggle = onPinToggle,
                    onShareClick = onShareClick,
                    cardTitle = "全部"
                )
            }
        }
    }
}

@Composable
private fun FriendCard(
    friends: List<AIFriend>,
    onFriendClick: (AIFriend) -> Unit,
    onPinToggle: (AIFriend) -> Unit,
    onShareClick: (AIFriend) -> Unit,
    cardTitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color.White, Color(0xFFFCFCFD))
                )
            )
            .padding(16.dp)
    ) {
        Text(
            text = cardTitle,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = iOSGrayDark,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        friends.forEachIndexed { index, friend ->
            FriendItem(
                friend = friend,
                onClick = { onFriendClick(friend) },
                onPinToggle = { onPinToggle(friend) },
                onShareClick = { onShareClick(friend) }
            )
            if (index < friends.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .padding(start = 58.dp)
                        .background(SeparatorTint)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FriendItem(
    friend: AIFriend,
    onClick: () -> Unit,
    onPinToggle: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showMenu = true }
                )
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (friend.avatarUri != null) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                ) {
                    AsyncImage(
                        model = android.net.Uri.parse(friend.avatarUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Avatar(
                    isAI = true,
                    avatarUri = null,
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(3.dp, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = friend.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (friend.lastMessageTime != null) {
                        Text(
                            text = formatTime(friend.lastMessageTime),
                            fontSize = 12.sp,
                            color = iOSGrayDark
                        )
                    }
                }

                if (friend.lastMessage != null) {
                    Text(
                        text = friend.lastMessage,
                        fontSize = 14.sp,
                        color = iOSGrayDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(if (friend.isPinned) "取消置顶" else "置顶") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = iOSGrayDark
                    )
                },
                onClick = {
                    showMenu = false
                    onPinToggle()
                }
            )
            DropdownMenuItem(
                text = { Text("分享") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = iOSBlue
                    )
                },
                onClick = {
                    showMenu = false
                    onShareClick()
                }
            )
        }
    }
}

private fun formatTime(timestamp: Long): String {
    return DateUtils.formatRelativeTime(timestamp)
}

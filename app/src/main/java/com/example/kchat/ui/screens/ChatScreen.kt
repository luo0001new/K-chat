package com.example.kchat.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.core.content.ContextCompat
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.kchat.data.AIFriend
import com.example.kchat.data.Message
import com.example.kchat.data.SettingsRepository
import com.example.kchat.ui.components.Avatar
import com.example.kchat.ui.components.MessageBubble
import com.example.kchat.ui.components.MessageInput
import com.example.kchat.ui.components.NameEditDialog
import com.example.kchat.ui.components.TimeDivider
import com.example.kchat.ui.components.TypingIndicator
import com.example.kchat.utils.DateUtils
import com.example.kchat.ui.theme.iOSBackground
import com.example.kchat.ui.theme.iOSBlue
import com.example.kchat.ui.viewmodel.ChatViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    friend: AIFriend,
    onNavigateBack: () -> Unit,
    onNavigateToModelSettings: () -> Unit,
    resetTrigger: Int = 0,
    viewModel: ChatViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { SettingsRepository.getInstance(context) }
    
    val messages by viewModel.messages.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    val userAvatarUri by repository.userAvatarUri.collectAsState()
    val listState = rememberLazyListState()
    
    var showNameEditDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var currentAvatarType by remember { mutableStateOf(AvatarType.AI) }
    var isInitialLoad by remember { mutableStateOf(true) }
    
    val lastMessageId = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(friend.id, resetTrigger) {
        isInitialLoad = true
        viewModel.loadChatHistory(friend.id)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            val newLastMessageId = messages.last().id
            if (lastMessageId.value != newLastMessageId) {
                lastMessageId.value = newLastMessageId
                if (!isInitialLoad) {
                    listState.animateScrollToItem(messages.size - 1)
                } else {
                    listState.scrollToItem(messages.size - 1)
                    isInitialLoad = false
                }
            }
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            try {
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(it, flag)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            when (currentAvatarType) {
                AvatarType.AI -> {
                    val updatedFriend = friend.copy(avatarUri = it.toString())
                    repository.updateAIFriend(updatedFriend)
                }
                AvatarType.USER -> repository.saveUserAvatarUri(it.toString())
            }
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            pickImageLauncher.launch("image/*")
        } else {
            showPermissionDialog = true
        }
    }

    fun pickAvatar(type: AvatarType) {
        currentAvatarType = type
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            pickImageLauncher.launch("image/*")
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        val userMessage = Message(
            id = UUID.randomUUID().toString(),
            text = text.trim(),
            isFromUser = true,
            timestamp = System.currentTimeMillis()
        )
        
        viewModel.addMessage(userMessage)
        repository.updateFriendLastMessage(friend.id, text.trim(), userMessage.timestamp)
        
        viewModel.sendMessage(
            text = text,
            apiConfig = friend.apiConfig,
            personality = friend.personality,
            aiName = friend.name,
            onMessageReceived = { aiText ->
                repository.updateFriendLastMessage(friend.id, aiText, System.currentTimeMillis())
            }
        )
    }

    if (showPermissionDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("需要存储权限") },
            text = { Text("请在设置中开启存储权限以选择头像") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showPermissionDialog = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = android.net.Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text("去设置")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showPermissionDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showNameEditDialog) {
        NameEditDialog(
            currentName = friend.name,
            onConfirm = { newName ->
                val updatedFriend = friend.copy(name = newName)
                repository.updateAIFriend(updatedFriend)
                showNameEditDialog = false
            },
            onDismiss = { showNameEditDialog = false }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable { pickAvatar(AvatarType.AI) }
                            ) {
                                if (friend.avatarUri != null) {
                                    AsyncImage(
                                        model = android.net.Uri.parse(friend.avatarUri),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFF8E8E93)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "AI",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = friend.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.clickable {
                                    showNameEditDialog = true
                                }
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回",
                                tint = iOSBlue
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "通话",
                                tint = iOSBlue
                            )
                        }
                        IconButton(onClick = onNavigateToModelSettings) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "更多",
                                tint = iOSBlue
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.White
                    )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFE5E5E5))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.04f), Color.Transparent)
                            )
                        )
                )
            }
        },
        containerColor = iOSBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(iOSBackground)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages.size, key = { index -> messages[index].id }) { index ->
                    val message = messages[index]

                    if (index == 0 || !DateUtils.isSameDay(messages[index - 1].timestamp, message.timestamp)) {
                        TimeDivider(timestamp = message.timestamp)
                    }

                    val isNewMessage = message.id == messages.lastOrNull()?.id
                    val avatarUri = if (message.isFromUser) userAvatarUri else friend.avatarUri
                    MessageBubble(
                        message = message,
                        isNewMessage = isNewMessage,
                        avatarUri = avatarUri,
                        onAvatarClick = {
                            if (message.isFromUser) {
                                pickAvatar(AvatarType.USER)
                            } else {
                                pickAvatar(AvatarType.AI)
                            }
                        },
                        onRetract = if (message.isFromUser) {
                            {
                                viewModel.retractMessage(message.id)
                            }
                        } else {
                            null
                        },
                        enableAnimation = !isInitialLoad
                    )
                }

                item {
                    TypingIndicator(visible = isTyping)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(iOSBackground)
            ) {
                MessageInput(
                    onSendMessage = ::sendMessage
                )
            }
        }
    }
}

private enum class AvatarType {
    AI, USER
}

package com.example.kchat.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kchat.data.AIFriend
import com.example.kchat.data.CharacterShareData
import com.example.kchat.data.SettingsRepository
import com.example.kchat.data.network.ApiService
import com.example.kchat.ui.theme.iOSBackground
import com.example.kchat.ui.theme.iOSBlue
import com.example.kchat.ui.theme.iOSCardBackground
import com.example.kchat.ui.theme.iOSGray
import com.example.kchat.ui.theme.iOSGrayDark
import com.example.kchat.ui.theme.iOSOrange
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSettingsScreen(
    friend: AIFriend,
    onNavigateBack: () -> Unit,
    onDeleteFriend: () -> Unit,
    onResetCharacter: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { SettingsRepository.getInstance(context) }
    val apiService = remember { ApiService() }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(friend.name) }
    var avatarUri by remember { mutableStateOf(friend.avatarUri) }
    var provider by remember { mutableStateOf(friend.apiConfig.provider) }
    var baseUrl by remember { mutableStateOf(friend.apiConfig.baseUrl) }
    var apiKey by remember { mutableStateOf(friend.apiConfig.apiKey) }
    var modelId by remember { mutableStateOf(friend.apiConfig.modelId) }
    var personality by remember { mutableStateOf(friend.personality) }
    var memoryPackage by remember { mutableStateOf(friend.memoryPackage) }
    var isGeneratingMemory by remember { mutableStateOf(false) }
    var isMemoryExpanded by remember { mutableStateOf(false) }
    var enableMomentPost by remember { mutableStateOf(friend.enableMomentPost) }
    var enableProactiveChat by remember { mutableStateOf(friend.enableProactiveChat) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val chatHistory by repository.chatHistories.collectAsState()
    val friendMessages = remember(chatHistory, friend.id) {
        chatHistory[friend.id]?.messages ?: emptyList()
    }

    if (showResetConfirmDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("重启人物") },
            text = { Text("将清空所有聊天记录，AI将忘记之前的所有对话内容。\n\n此操作不可撤销，确认继续？") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showResetConfirmDialog = false
                    repository.clearChatHistory(friend.id)
                    onResetCharacter?.invoke()
                    onNavigateBack()
                }) {
                    Text("确认重启", color = Color.Red)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "模型配置",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回",
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
            }
        },
        containerColor = iOSBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .systemBarsPadding()
                .background(iOSBackground)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .border(1.5.dp, Color.White, CircleShape)
                        .background(iOSGray.copy(alpha = 0.3f))
                ) {
                    if (avatarUri != null) {
                        AsyncImage(
                            model = android.net.Uri.parse(avatarUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "AI",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }

            SectionTitle("基本信息")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = iOSCardBackground)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("AI好友名称") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        singleLine = true
                    )
                }
            }

            SectionTitle("API配置")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = provider,
                        onValueChange = { provider = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Provider") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Base URL") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("API Key") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = modelId,
                        onValueChange = { modelId = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Model ID") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        singleLine = true
                    )
                }
            }

            SectionTitle("性格设定")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                            value = personality,
                        onValueChange = { personality = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        placeholder = {
                            Text(
                                "描述这个AI的性格特点，比如：\n" +
                                        "- 温柔体贴\n" +
                                        "- 幽默风趣\n" +
                                        "- 知识渊博\n" +
                                        "- 善于倾听"
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        maxLines = 8
                    )
                }
            }

            SectionTitle("主动设置")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "自动发朋友圈",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            Text(
                                text = "开启后AI会主动发朋友圈",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = enableMomentPost,
                            onCheckedChange = { enableMomentPost = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = iOSBlue,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFE5E5E5)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "主动聊天",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            Text(
                                text = "开启后AI会主动发消息",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = enableProactiveChat,
                            onCheckedChange = { enableProactiveChat = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = iOSBlue,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFE5E5E5)
                            )
                        )
                    }
                }
            }

            SectionTitle("记忆包")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isMemoryExpanded = !isMemoryExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "记忆包",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            Text(
                                text = if (memoryPackage != null) "已生成，点击查看" else "包含性格设定和聊天上下文记忆",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        if (memoryPackage != null) {
                            val arrowRotation by animateFloatAsState(
                                targetValue = if (isMemoryExpanded) 180f else 0f,
                                animationSpec = tween(durationMillis = 300),
                                label = "arrowRotation"
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isMemoryExpanded) "收起" else "展开",
                                tint = iOSGrayDark,
                                modifier = Modifier.rotate(arrowRotation)
                            )
                        }
                    }

                    AnimatedVisibility(visible = isMemoryExpanded && memoryPackage != null) {
                        Text(
                            text = memoryPackage ?: "",
                            fontSize = 14.sp,
                            color = Color.Black,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (apiKey.isBlank()) {
                                    return@Button
                                }
                                isGeneratingMemory = true
                                scope.launch {
                                    val chatContext = friendMessages.takeLast(20)
                                        .joinToString("\n") { msg ->
                                            if (msg.isFromUser) "[用户]: ${msg.text}" else "[${name}]: ${msg.text}"
                                        }

                                    val memoryPrompt = """你叫${name}，${if (personality.isNotEmpty()) "性格是：${personality}。" else ""}

根据以下聊天记录，生成一个详细的记忆文本，内容包括你的性格特点和对用户的了解记忆：

${if (chatContext.isNotEmpty()) "聊天记录：\n$chatContext" else "暂无聊天记录"}

要求：
- 生成一段详细的记忆文本，100-200字左右
- 内容包括：你的性格特点、对用户兴趣爱好/习惯/经历的观察和记忆
- 语言自然，像真实的记忆
- 只输出记忆文本内容，不要其他说明"""

                                    val result = apiService.generateChatMessage(
                                        apiConfig = friend.apiConfig.copy(
                                            provider = provider,
                                            baseUrl = baseUrl,
                                            apiKey = apiKey,
                                            modelId = modelId
                                        ),
                                        aiName = name,
                                        systemPrompt = "你是一个记忆生成助手。",
                                        userPrompt = memoryPrompt
                                    )

                                    result.onSuccess { generatedMemory ->
                                        memoryPackage = generatedMemory.trim()
                                        val updatedFriend = friend.copy(
                                            name = name,
                                            avatarUri = avatarUri,
                                            personality = personality,
                                            apiConfig = friend.apiConfig.copy(
                                                provider = provider,
                                                baseUrl = baseUrl,
                                                apiKey = apiKey,
                                                modelId = modelId
                                            ),
                                            memoryPackage = memoryPackage,
                                            enableMomentPost = enableMomentPost,
                                            enableProactiveChat = enableProactiveChat
                                        )
                                        repository.updateAIFriend(updatedFriend)
                                    }
                                    isGeneratingMemory = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isGeneratingMemory && apiKey.isNotBlank()
                        ) {
                            if (isGeneratingMemory) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Text("保存记忆", fontSize = 14.sp)
                            }
                        }

                        if (memoryPackage != null) {
                            OutlinedButton(
                                onClick = {
                                    personality = memoryPackage!!
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("加载记忆", fontSize = 14.sp)
                            }
                        }
                    }

                    if (memoryPackage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "已有记忆包，再次保存将覆盖",
                            fontSize = 12.sp,
                            color = iOSBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val updatedFriend = friend.copy(
                        name = name,
                        avatarUri = avatarUri,
                        personality = personality,
                        apiConfig = friend.apiConfig.copy(
                            provider = provider,
                            baseUrl = baseUrl,
                            apiKey = apiKey,
                            modelId = modelId
                        ),
                        memoryPackage = memoryPackage,
                        enableMomentPost = enableMomentPost,
                        enableProactiveChat = enableProactiveChat
                    )
                    repository.updateAIFriend(updatedFriend)
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(8.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = iOSBlue
                )
            ) {
                Text(
                    "保存修改",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (onResetCharacter != null) {
                OutlinedButton(
                    onClick = { showResetConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = iOSOrange
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        "重启人物",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedButton(
                onClick = {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = iOSBlue
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    "分享此人物",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onDeleteFriend,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Red
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    "删除此好友",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = iOSGrayDark,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

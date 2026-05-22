package com.example.kchat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.kchat.data.AIFriend
import com.example.kchat.data.SettingsRepository
import com.example.kchat.ui.screens.AboutScreen
import com.example.kchat.ui.screens.AddAIFriendScreen
import com.example.kchat.ui.screens.ChatScreen
import com.example.kchat.ui.screens.FriendListScreen
import com.example.kchat.ui.screens.ModelSettingsScreen
import com.example.kchat.ui.theme.KChatTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()
        setContent {
            KChatTheme {
                KChatApp()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

private enum class Screen {
    FriendList,
    AddFriend,
    Chat,
    ModelSettings,
    About
}

private const val SWIPE_THRESHOLD = 0.3f

private data class ScreenState(
    val screen: Screen,
    val friend: AIFriend? = null
)

@Composable
fun KChatApp() {
    val context = LocalContext.current
    val repository = remember { SettingsRepository.getInstance(context) }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.FriendList) }
    var selectedFriend by remember { mutableStateOf<AIFriend?>(null) }

    val screenStack = remember { mutableStateOf(listOf<ScreenState>()) }
    var resetTrigger by remember { mutableStateOf(0) }

    var dragOffset by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var dragStartX by remember { mutableStateOf(0f) }

    fun navigateTo(screen: Screen) {
        if (currentScreen != screen) {
            screenStack.value = screenStack.value + ScreenState(currentScreen, selectedFriend)
            currentScreen = screen
            dragOffset = 0f
        }
    }

    fun navigateBack() {
        if (screenStack.value.isNotEmpty()) {
            val previousState = screenStack.value.last()
            screenStack.value = screenStack.value.dropLast(1)
            currentScreen = previousState.screen
            selectedFriend = previousState.friend
            dragOffset = 0f
        }
    }

    BackHandler(enabled = screenStack.value.isNotEmpty()) {
        navigateBack()
    }

    val canSwipeBack = screenStack.value.isNotEmpty()
    val configuration = LocalConfiguration.current
    val edgeWidthPx = with(density) { 24.dp.toPx() }
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val thresholdPx = screenWidthPx * SWIPE_THRESHOLD

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .then(
                if (canSwipeBack) {
                    Modifier.pointerInput(currentScreen) {
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                dragStartX = offset.x
                                if (offset.x < edgeWidthPx) {
                                    isDragging = true
                                }
                            },
                            onDragEnd = {
                                if (isDragging) {
                                    if (dragOffset > thresholdPx) {
                                        navigateBack()
                                    } else {
                                        scope.launch {
                                            animate(
                                                initialValue = dragOffset,
                                                targetValue = 0f,
                                                animationSpec = tween(200)
                                            ) { value, _ -> dragOffset = value }
                                        }
                                    }
                                    isDragging = false
                                }
                            },
                            onDragCancel = {
                                if (isDragging) {
                                    scope.launch {
                                        animate(
                                            initialValue = dragOffset,
                                            targetValue = 0f,
                                            animationSpec = tween(200)
                                        ) { value, _ -> dragOffset = value }
                                    }
                                    isDragging = false
                                }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                if (isDragging) {
                                    dragOffset = (dragOffset + dragAmount).coerceIn(0f, screenWidthPx)
                                }
                            }
                        )
                    }
                } else {
                    Modifier
                }
            )
    ) {
        if (canSwipeBack && (isDragging || dragOffset > 1f)) {
            val prevState = screenStack.value.last()
            val prevScreen = prevState.screen
            val prevFriend = prevState.friend
            val needsFriend = prevScreen == Screen.Chat || prevScreen == Screen.ModelSettings
            if (!needsFriend || prevFriend != null) {
                val currentFriend = prevFriend
                when (prevScreen) {
                    Screen.FriendList -> FriendListScreen(
                        onAddFriendClick = { navigateTo(Screen.AddFriend) },
                        onFriendClick = { friend ->
                            selectedFriend = friend
                            navigateTo(Screen.Chat)
                        },
                        onAboutClick = { navigateTo(Screen.About) }
                    )
                    Screen.AddFriend -> AddAIFriendScreen(
                        onNavigateBack = { navigateBack() },
                        onAddFriend = { friendId, name, avatarUri, apiConfig, personality ->
                            val friend = AIFriend(
                                id = friendId, name = name, avatarUri = avatarUri,
                                personality = personality, apiConfig = apiConfig
                            )
                            repository.addAIFriend(friend)
                            selectedFriend = friend
                            navigateTo(Screen.Chat)
                        }
                    )
                    Screen.Chat -> if (currentFriend != null) {
                        ChatScreen(
                            friend = currentFriend,
                            onNavigateBack = { navigateBack() },
                            onNavigateToModelSettings = { navigateTo(Screen.ModelSettings) },
                            resetTrigger = resetTrigger
                        )
                    }
                    Screen.ModelSettings -> if (currentFriend != null) {
                        ModelSettingsScreen(
                            friend = currentFriend,
                            onNavigateBack = {
                                val updated = repository.aiFriends.value.find { it.id == currentFriend.id }
                                if (updated != null) selectedFriend = updated
                                navigateBack()
                            },
                            onDeleteFriend = {
                                repository.deleteAIFriend(currentFriend.id)
                                navigateBack()
                                selectedFriend = null
                            },
                            onResetCharacter = { resetTrigger++ }
                        )
                    }
                    Screen.About -> AboutScreen(
                        onNavigateBack = { navigateBack() }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(dragOffset.roundToInt(), 0) }
                .fillMaxSize()
        ) {
            when (currentScreen) {
                Screen.FriendList -> FriendListScreen(
                    onAddFriendClick = { navigateTo(Screen.AddFriend) },
                    onFriendClick = { friend ->
                        selectedFriend = friend
                        navigateTo(Screen.Chat)
                    },
                    onAboutClick = { navigateTo(Screen.About) }
                )
                Screen.AddFriend -> AddAIFriendScreen(
                    onNavigateBack = { navigateBack() },
                    onAddFriend = { friendId, name, avatarUri, apiConfig, personality ->
                        val friend = AIFriend(
                            id = friendId, name = name, avatarUri = avatarUri,
                            personality = personality, apiConfig = apiConfig
                        )
                        repository.addAIFriend(friend)
                        selectedFriend = friend
                        navigateTo(Screen.Chat)
                    }
                )
                Screen.Chat -> selectedFriend?.let { friend ->
                    ChatScreen(
                        friend = friend,
                        onNavigateBack = { navigateBack() },
                        onNavigateToModelSettings = { navigateTo(Screen.ModelSettings) },
                        resetTrigger = resetTrigger
                    )
                }
                Screen.ModelSettings -> selectedFriend?.let { friend ->
                    ModelSettingsScreen(
                        friend = friend,
                        onNavigateBack = {
                            val updatedFriend = repository.aiFriends.value.find { it.id == friend.id }
                            if (updatedFriend != null) selectedFriend = updatedFriend
                            navigateBack()
                        },
                        onDeleteFriend = {
                            repository.deleteAIFriend(friend.id)
                            navigateBack()
                            selectedFriend = null
                        },
                        onResetCharacter = { resetTrigger++ }
                    )
                }
                Screen.About -> AboutScreen(
                    onNavigateBack = { navigateBack() }
                )
            }
        }
    }
}

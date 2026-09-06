package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AiMode
import com.example.data.model.ChatMessage
import com.example.data.model.MessageRole
import com.example.ui.components.ChatHistoryDrawer
import com.example.ui.components.MarkdownTextView
import com.example.ui.components.PromptSuggestions
import com.example.ui.theme.Indigo50
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Indigo700
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val sessions by viewModel.sessions.collectAsState()
    val activeSessionId by viewModel.activeSessionId.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val activeMode by viewModel.activeMode.collectAsState()

    val activeSession = remember(sessions, activeSessionId) {
        sessions.find { it.id == activeSessionId } ?: sessions.first()
    }

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new messages
    LaunchedEffect(activeSession.messages.size, isGenerating) {
        if (activeSession.messages.isNotEmpty()) {
            listState.animateScrollToItem(activeSession.messages.size - 1)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChatHistoryDrawer(
                sessions = sessions,
                activeSessionId = activeSessionId,
                selectedMode = activeMode,
                onSelectSession = { viewModel.selectSession(it) },
                onNewChat = { viewModel.createNewChat() },
                onDeleteSession = { viewModel.deleteSession(it) },
                onSelectMode = { viewModel.setMode(it) },
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            containerColor = Color.White,
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Adish's chatboot",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900,
                                        letterSpacing = (-0.3).sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Indigo50
                                    ) {
                                        Text(
                                            text = "All-in-One",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Indigo700,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "A powerfull AI chatboot created byAdish Yadav",
                                    fontSize = 11.sp,
                                    color = Slate500,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Open Chat Menu",
                                    tint = Slate700
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.createNewChat() }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "New Chat",
                                    tint = Slate700
                                )
                            }
                            if (activeSession.messages.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearCurrentChat() }) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteSweep,
                                        contentDescription = "Clear Chat",
                                        tint = Slate400
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.White
                        )
                    )
                    HorizontalDivider(color = Slate100, thickness = 1.dp)
                }
            },
            bottomBar = {
                ChatInputBar(
                    text = inputText,
                    onTextChanged = { inputText = it },
                    isGenerating = isGenerating,
                    activeMode = activeMode,
                    onSelectMode = { viewModel.setMode(it) },
                    onSend = {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    },
                    onStop = { viewModel.stopGenerating() },
                    onInsertCodeSnippet = {
                        inputText = if (inputText.isBlank()) "```\n\n```" else "$inputText\n```\n\n```"
                    },
                    onQuickPrompt = { promptText ->
                        viewModel.sendMessage(promptText)
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
            ) {
                if (activeSession.messages.isEmpty()) {
                    // Empty State: Clean Minimalist Prompt suggestions & hero
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            PromptSuggestions(
                                onSelectPrompt = { prompt, mode ->
                                    viewModel.sendMessage(prompt, mode)
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                } else {
                    // Message List in Clean Minimalism style
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(activeSession.messages, key = { it.id }) { message ->
                            val isLatestAssistant = message == activeSession.messages.lastOrNull { it.role == MessageRole.ASSISTANT }
                            ChatMessageItem(
                                message = message,
                                isLatest = isLatestAssistant,
                                onRetry = { viewModel.retryLastMessage() }
                            )
                        }

                        if (isGenerating) {
                            item {
                                ThinkingIndicator(mode = activeMode)
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    isLatest: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isUser = message.role == MessageRole.USER
    val timeString = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        // Avatar circle (Clean Minimalism style)
        if (isUser) {
            Surface(
                shape = CircleShape,
                color = Slate200,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "U",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700
                    )
                }
            }
        } else {
            Surface(
                shape = CircleShape,
                color = Indigo600,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Message Column
        Column(modifier = Modifier.weight(1f)) {
            // Header row: Name + timestamp + token badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isUser) "You" else "Adish's chatboot",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = timeString,
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }

                if (!isUser && message.tokenCount != null) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Slate100
                    ) {
                        Text(
                            text = "${message.tokenCount} tokens",
                            fontSize = 10.sp,
                            color = Slate500,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Body text / markdown content
            MarkdownTextView(
                content = message.content,
                isUser = false
            )

            // Bottom Actions for assistant message
            if (!isUser) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("AI Response", message.content)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied response to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy message",
                            tint = Slate400,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    if (isLatest) {
                        IconButton(
                            onClick = onRetry,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Regenerate",
                                tint = Slate400,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThinkingIndicator(
    mode: AiMode,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = Indigo600,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = "Adish's chatboot is thinking...",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Slate800
            )
            Text(
                text = "A powerfull AI chatboot created byAdish Yadav",
                fontSize = 11.sp,
                color = Slate400
            )
        }
    }
}

@Composable
fun ChatInputBar(
    text: String,
    onTextChanged: (String) -> Unit,
    isGenerating: Boolean,
    activeMode: AiMode,
    onSelectMode: (AiMode) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onInsertCodeSnippet: () -> Unit,
    onQuickPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            HorizontalDivider(color = Slate100, thickness = 1.dp, modifier = Modifier.padding(bottom = 8.dp))

            // Contextual quick chips (matching Design HTML pills: "Explain this code", "Add docstrings", "Optimize for speed")
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val quickActions = listOf(
                    "Explain this code",
                    "Add docstrings",
                    "Optimize for speed",
                    "Troubleshoot error",
                    "Convert to TypeScript"
                )

                quickActions.forEach { action ->
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(1.dp, Slate200),
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onQuickPrompt(action) }
                    ) {
                        Text(
                            text = action,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate600,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Input field & Send button (matching Design HTML textarea with border-2 border-slate-200 rounded-2xl)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Code block shortcut
                IconButton(
                    onClick = onInsertCodeSnippet,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Insert Code Block",
                        tint = Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Text Input
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    placeholder = {
                        Text(
                            text = "Ask anything or paste code...",
                            fontSize = 14.sp,
                            color = Slate400
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Default
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Indigo600,
                        unfocusedBorderColor = Slate200,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send or Stop Button (Clean Minimalism style: rounded-xl, indigo-600 bg, white icon)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isGenerating) {
                        Color(0xFFEF4444)
                    } else if (text.isNotBlank()) {
                        Indigo600
                    } else {
                        Slate200
                    },
                    shadowElevation = if (text.isNotBlank() || isGenerating) 2.dp else 0.dp,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = isGenerating || text.isNotBlank()) {
                            if (isGenerating) {
                                onStop()
                            } else {
                                onSend()
                            }
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isGenerating) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop generation",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send message",
                                tint = if (text.isNotBlank()) Color.White else Slate400,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Disclaimer text (matching Design HTML: OmniAI can make mistakes. Check important info.)
            Text(
                text = "OmniAI can make mistakes. Check important info.",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Slate400,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            )
        }
    }
}


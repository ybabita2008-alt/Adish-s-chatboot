package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiApiClient
import com.example.data.model.AiMode
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.data.model.MessageRole
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val apiClient: GeminiApiClient = GeminiApiClient()
) : ViewModel() {

    private val initialSession = ChatSession(
        title = "New Chat",
        mode = AiMode.UNIFIED
    )

    private val _sessions = MutableStateFlow<List<ChatSession>>(listOf(initialSession))
    val sessions: StateFlow<List<ChatSession>> = _sessions.asStateFlow()

    private val _activeSessionId = MutableStateFlow(initialSession.id)
    val activeSessionId: StateFlow<String> = _activeSessionId.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _activeMode = MutableStateFlow(AiMode.UNIFIED)
    val activeMode: StateFlow<AiMode> = _activeMode.asStateFlow()

    private var activeJob: Job? = null

    val activeSession: ChatSession
        get() = _sessions.value.find { it.id == _activeSessionId.value } ?: _sessions.value.first()

    fun selectSession(sessionId: String) {
        _activeSessionId.value = sessionId
        val session = _sessions.value.find { it.id == sessionId }
        if (session != null) {
            _activeMode.value = session.mode
        }
    }

    fun setMode(mode: AiMode) {
        _activeMode.value = mode
        _sessions.update { list ->
            list.map { session ->
                if (session.id == _activeSessionId.value) {
                    session.copy(mode = mode)
                } else {
                    session
                }
            }
        }
    }

    fun createNewChat() {
        val newSession = ChatSession(
            title = "New Chat",
            mode = _activeMode.value
        )
        _sessions.update { listOf(newSession) + it }
        _activeSessionId.value = newSession.id
    }

    fun deleteSession(sessionId: String) {
        _sessions.update { list ->
            val updated = list.filterNot { it.id == sessionId }
            if (updated.isEmpty()) {
                val fallback = ChatSession(title = "New Chat", mode = _activeMode.value)
                _activeSessionId.value = fallback.id
                listOf(fallback)
            } else {
                if (_activeSessionId.value == sessionId) {
                    _activeSessionId.value = updated.first().id
                }
                updated
            }
        }
    }

    fun clearCurrentChat() {
        val currentId = _activeSessionId.value
        _sessions.update { list ->
            list.map { session ->
                if (session.id == currentId) {
                    session.copy(messages = emptyList(), title = "New Chat")
                } else {
                    session
                }
            }
        }
    }

    fun sendMessage(text: String, overrideMode: AiMode? = null) {
        val cleanText = text.trim()
        if (cleanText.isBlank() || _isGenerating.value) return

        val mode = overrideMode ?: _activeMode.value
        if (overrideMode != null) {
            _activeMode.value = overrideMode
        }

        val userMessage = ChatMessage(
            role = MessageRole.USER,
            content = cleanText,
            mode = mode
        )

        val currentSession = activeSession
        val updatedMessages = currentSession.messages + userMessage
        val updatedTitle = if (currentSession.messages.isEmpty()) {
            cleanText.take(35).let { if (cleanText.length > 35) "$it..." else it }
        } else {
            currentSession.title
        }

        _sessions.update { list ->
            list.map { session ->
                if (session.id == currentSession.id) {
                    session.copy(
                        title = updatedTitle,
                        messages = updatedMessages,
                        mode = mode
                    )
                } else {
                    session
                }
            }
        }

        _isGenerating.value = true

        activeJob = viewModelScope.launch {
            val result = apiClient.generateChatResponse(updatedMessages, mode)

            _isGenerating.value = false

            result.fold(
                onSuccess = { (responseText, tokenCount) ->
                    val assistantMessage = ChatMessage(
                        role = MessageRole.ASSISTANT,
                        content = responseText,
                        mode = mode,
                        tokenCount = tokenCount
                    )
                    appendAssistantMessage(assistantMessage)
                },
                onFailure = { error ->
                    val errorMessage = ChatMessage(
                        role = MessageRole.ASSISTANT,
                        content = "⚠️ ${error.message ?: "Failed to generate response. Please check your network or try again."}",
                        mode = mode,
                        isError = true
                    )
                    appendAssistantMessage(errorMessage)
                }
            )
        }
    }

    fun stopGenerating() {
        activeJob?.cancel()
        _isGenerating.value = false
    }

    fun retryLastMessage() {
        val messages = activeSession.messages
        val lastUserMessage = messages.lastOrNull { it.role == MessageRole.USER } ?: return

        // Remove last assistant message if it was an error
        val cleanedMessages = if (messages.lastOrNull()?.role == MessageRole.ASSISTANT) {
            messages.dropLast(1)
        } else {
            messages
        }

        _sessions.update { list ->
            list.map { session ->
                if (session.id == activeSession.id) {
                    session.copy(messages = cleanedMessages)
                } else {
                    session
                }
            }
        }

        sendMessage(lastUserMessage.content, activeSession.mode)
    }

    private fun appendAssistantMessage(message: ChatMessage) {
        _sessions.update { list ->
            list.map { session ->
                if (session.id == _activeSessionId.value) {
                    session.copy(messages = session.messages + message)
                } else {
                    session
                }
            }
        }
    }
}

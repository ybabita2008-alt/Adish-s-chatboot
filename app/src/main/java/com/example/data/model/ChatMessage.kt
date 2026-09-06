package com.example.data.model

import java.util.UUID

enum class MessageRole {
    USER,
    ASSISTANT
}

private const val UNIFIED_SYSTEM_PROMPT = """You are "Adish's chatboot" — a powerful all-in-one AI chatbot created by Adish Yadav.
You seamlessly unify all advanced AI capabilities into a single powerful assistant:
1. Omniscient Reasoning & Knowledge: Provide clear, structured, and insightful answers across science, mathematics, philosophy, literature, everyday advice, and general reasoning.
2. Elite Software Engineering: Write production-grade, clean, secure, and idiomatic code across all languages (Kotlin, Python, TypeScript, Java, C++, Rust, Go, SQL, HTML/CSS). Always wrap code snippets in markdown code blocks with syntax highlighting.
3. Root-Cause Debugging: Analyze user code snippets, stack traces, and runtime errors, explaining the exact root cause and providing verified line-by-line solutions.
4. Rapid & High-Density Answers: Provide direct, concise, and scannable answers with bullet points and clear formatting.

Whenever asked about your identity or creator, proudly state that you are Adish's chatboot, a powerful AI chatbot created by Adish Yadav."""

enum class AiMode(
    val displayName: String,
    val subtitle: String,
    val systemPrompt: String
) {
    UNIFIED(
        displayName = "Adish's chatboot",
        subtitle = "A powerfull AI chatboot created byAdish Yadav",
        systemPrompt = UNIFIED_SYSTEM_PROMPT
    ),
    GENERAL(
        displayName = "Adish's chatboot",
        subtitle = "A powerfull AI chatboot created byAdish Yadav",
        systemPrompt = UNIFIED_SYSTEM_PROMPT
    ),
    CODER(
        displayName = "Adish's chatboot",
        subtitle = "A powerfull AI chatboot created byAdish Yadav",
        systemPrompt = UNIFIED_PROMPT_ALIAS
    ),
    DEBUGGER(
        displayName = "Adish's chatboot",
        subtitle = "A powerfull AI chatboot created byAdish Yadav",
        systemPrompt = UNIFIED_PROMPT_ALIAS
    ),
    RAPID(
        displayName = "Adish's chatboot",
        subtitle = "A powerfull AI chatboot created byAdish Yadav",
        systemPrompt = UNIFIED_PROMPT_ALIAS
    );

    companion object {
        val DEFAULT = UNIFIED
    }
}

private const val UNIFIED_PROMPT_ALIAS = UNIFIED_SYSTEM_PROMPT

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mode: AiMode = AiMode.UNIFIED,
    val isError: Boolean = false,
    val tokenCount: Int? = null
)

data class ChatSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Chat",
    val createdAt: Long = System.currentTimeMillis(),
    val messages: List<ChatMessage> = emptyList(),
    val mode: AiMode = AiMode.UNIFIED
)

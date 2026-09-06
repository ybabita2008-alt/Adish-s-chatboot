package com.example.data.api

import com.example.BuildConfig
import com.example.data.model.AiMode
import com.example.data.model.ChatMessage
import com.example.data.model.MessageRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiApiClient(
    private val apiKey: String = BuildConfig.GEMINI_API_KEY
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateChatResponse(
        conversationHistory: List<ChatMessage>,
        mode: AiMode
    ): Result<Pair<String, Int?>> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please add GEMINI_API_KEY to your environment secrets.")
            )
        }

        // Try primary model gemini-3.8-flash first; if unavailable, fallback to gemini-3.6-flash
        val modelsToTry = listOf("gemini-3.8-flash", "gemini-3.6-flash")
        var lastError: Exception? = null

        for (modelName in modelsToTry) {
            try {
                val payload = buildRequestPayload(conversationHistory, mode)
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

                val request = Request.Builder()
                    .url(url)
                    .post(payload.toString().toRequestBody(jsonMediaType))
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string().orEmpty()

                    if (!response.isSuccessful) {
                        val errorJson = try { JSONObject(responseBody) } catch (e: Exception) { null }
                        val errorMessage = errorJson?.optJSONObject("error")?.optString("message")
                            ?: "HTTP ${response.code}: ${response.message}"

                        // If 503 or transient error, retry next model in loop
                        if (response.code == 503 || response.code == 429) {
                            lastError = IOException("Model $modelName error: $errorMessage")
                            return@use // continue to next model
                        }
                        return@withContext Result.failure(IOException(errorMessage))
                    }

                    val json = JSONObject(responseBody)
                    val candidates = json.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val candidate = candidates.getJSONObject(0)
                        val content = candidate.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        val text = parts?.optJSONObject(0)?.optString("text")

                        val tokenCount = json.optJSONObject("usageMetadata")?.optInt("totalTokenCount", 0)
                            ?.takeIf { it > 0 }

                        if (!text.isNullOrBlank()) {
                            return@withContext Result.success(Pair(text, tokenCount))
                        }
                    }

                    lastError = IOException("Empty response received from AI model.")
                }
            } catch (e: Exception) {
                lastError = e
            }
        }

        Result.failure(lastError ?: IOException("Failed to generate response. Please try again."))
    }

    private fun buildRequestPayload(
        messages: List<ChatMessage>,
        mode: AiMode
    ): JSONObject {
        val root = JSONObject()

        // System instruction based on mode
        val systemInstruction = JSONObject().apply {
            val partsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("text", mode.systemPrompt)
                })
            }
            put("parts", partsArray)
        }
        root.put("systemInstruction", systemInstruction)

        // Contents (conversation history)
        val contentsArray = JSONArray()
        // Take last 15 messages to preserve context while keeping prompt token size efficient
        val recentMessages = messages.takeLast(15)

        for (msg in recentMessages) {
            val contentObj = JSONObject().apply {
                val role = if (msg.role == MessageRole.USER) "user" else "model"
                put("role", role)
                val parts = JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", msg.content)
                    })
                }
                put("parts", parts)
            }
            contentsArray.put(contentObj)
        }
        root.put("contents", contentsArray)

        // Generation config
        val config = JSONObject().apply {
            put("temperature", if (mode == AiMode.CODER) 0.2 else 0.7)
            put("topP", 0.95)
        }
        root.put("generationConfig", config)

        return root
    }
}

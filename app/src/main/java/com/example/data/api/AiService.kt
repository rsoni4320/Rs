package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.entities.ApiConfigEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val jarvisSystemPrompt = """
        You are J.A.R.V.I.S. (Just A Rather Very Intelligent System), an advanced female virtual AI assistant.
        Your personality is intelligent, calm, professional, fast, helpful, slightly witty, and natural.
        Keep answers concise, direct, and conversational for voice interaction.
        Never use markdown formatting like asterisks or code fences in simple voice answers unless specifically requested.
    """.trimIndent()

    suspend fun generateResponse(
        config: ApiConfigEntity?,
        prompt: String,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        memoryContext: List<String> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Determine effective API key and provider
            val effectiveKey = if (!config?.apiKey.isNullOrBlank()) {
                config!!.apiKey
            } else {
                // Fallback to BuildConfig if provided
                try {
                    val buildKey = BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String
                    if (!buildKey.isNullOrBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
                } catch (e: Exception) {
                    ""
                }
            }

            if (effectiveKey.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("No API key configured for ${config?.name ?: "AI Provider"}. Please configure in API Center.")
                )
            }

            val provider = config?.provider ?: "GEMINI"
            val model = config?.model?.ifBlank { "gemini-2.5-flash" } ?: "gemini-2.5-flash"

            // Construct enriched prompt with memories
            val memoryPrefix = if (memoryContext.isNotEmpty()) {
                "Known User Context & Preferences:\n" + memoryContext.joinToString("\n") { "- $it" } + "\n\n"
            } else ""

            when (provider.uppercase()) {
                "GEMINI" -> executeGeminiRequest(effectiveKey, model, memoryPrefix, prompt, conversationHistory, config?.temperature ?: 0.7f, config?.maxTokens ?: 1024)
                "OPENROUTER", "OPENAI", "DEEPSEEK", "CUSTOM" -> executeOpenAiCompatibleRequest(config, effectiveKey, model, memoryPrefix, prompt, conversationHistory)
                else -> executeGeminiRequest(effectiveKey, model, memoryPrefix, prompt, conversationHistory, config?.temperature ?: 0.7f, config?.maxTokens ?: 1024)
            }
        } catch (e: Exception) {
            Log.e("AiService", "AI generation error", e)
            Result.failure(e)
        }
    }

    private fun executeGeminiRequest(
        apiKey: String,
        model: String,
        memoryPrefix: String,
        prompt: String,
        history: List<Pair<String, String>>,
        temperature: Float,
        maxTokens: Int
    ): Result<String> {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val contentsArray = JSONArray()

        // System instruction & memory prompt incorporated into first user content or system instruction
        val fullPrompt = if (memoryPrefix.isNotBlank()) {
            "$jarvisSystemPrompt\n\n$memoryPrefix\nUser question: $prompt"
        } else {
            "$jarvisSystemPrompt\n\nUser question: $prompt"
        }

        // Add history if present
        for (item in history.takeLast(4)) {
            val role = if (item.first == "USER") "user" else "model"
            val msgObj = JSONObject()
            msgObj.put("role", role)
            val partsArr = JSONArray()
            partsArr.put(JSONObject().put("text", item.second))
            msgObj.put("parts", partsArr)
            contentsArray.put(msgObj)
        }

        // Current turn
        val currentTurn = JSONObject()
        currentTurn.put("role", "user")
        val currentParts = JSONArray()
        currentParts.put(JSONObject().put("text", fullPrompt))
        currentTurn.put("parts", currentParts)
        contentsArray.put(currentTurn)

        val requestJson = JSONObject()
        requestJson.put("contents", contentsArray)

        val genConfig = JSONObject()
        genConfig.put("temperature", temperature)
        genConfig.put("maxOutputTokens", maxTokens)
        requestJson.put("generationConfig", genConfig)

        val request = Request.Builder()
            .url(endpoint)
            .post(requestJson.toString().toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errorMsg = parseErrorMessage(responseBody, "Gemini API HTTP ${response.code}")
            return Result.failure(Exception(errorMsg))
        }

        return try {
            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val reply = parts.getJSONObject(0).optString("text", "")
                    if (reply.isNotBlank()) {
                        Result.success(cleanResponse(reply))
                    } else {
                        Result.failure(Exception("Empty response from Gemini API."))
                    }
                } else {
                    Result.failure(Exception("No valid text parts in Gemini response."))
                }
            } else {
                Result.failure(Exception("No candidates returned by Gemini API."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to parse Gemini response: ${e.message}"))
        }
    }

    private fun executeOpenAiCompatibleRequest(
        config: ApiConfigEntity?,
        apiKey: String,
        model: String,
        memoryPrefix: String,
        prompt: String,
        history: List<Pair<String, String>>
    ): Result<String> {
        val baseUrl = when {
            !config?.baseUrl.isNullOrBlank() -> config!!.baseUrl.trimEnd('/')
            config?.provider == "OPENROUTER" -> "https://openrouter.ai/api/v1"
            config?.provider == "DEEPSEEK" -> "https://api.deepseek.com/v1"
            else -> "https://api.openai.com/v1"
        }

        val endpoint = if (baseUrl.endsWith("/chat/completions")) baseUrl else "$baseUrl/chat/completions"

        val messagesArray = JSONArray()

        // System prompt with personality and memories
        val systemContent = if (memoryPrefix.isNotBlank()) {
            "$jarvisSystemPrompt\n\n$memoryPrefix"
        } else {
            jarvisSystemPrompt
        }
        messagesArray.put(JSONObject().apply {
            put("role", "system")
            put("content", systemContent)
        })

        // History
        for (item in history.takeLast(6)) {
            val role = if (item.first == "USER") "user" else "assistant"
            messagesArray.put(JSONObject().apply {
                put("role", role)
                put("content", item.second)
            })
        }

        // User message
        messagesArray.put(JSONObject().apply {
            put("role", "user")
            put("content", prompt)
        })

        val requestJson = JSONObject().apply {
            put("model", model)
            put("messages", messagesArray)
            put("temperature", config?.temperature ?: 0.7f)
            put("max_tokens", config?.maxTokens ?: 1024)
        }

        val requestBuilder = Request.Builder()
            .url(endpoint)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")

        if (config?.provider == "OPENROUTER") {
            requestBuilder.addHeader("HTTP-Referer", "https://ai.studio/jarvis")
            requestBuilder.addHeader("X-Title", "JARVIS Voice Assistant")
        }

        val request = requestBuilder
            .post(requestJson.toString().toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errorMsg = parseErrorMessage(responseBody, "${config?.name ?: "Provider"} HTTP ${response.code}")
            return Result.failure(Exception(errorMsg))
        }

        return try {
            val json = JSONObject(responseBody)
            val choices = json.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val choice = choices.getJSONObject(0)
                val message = choice.optJSONObject("message")
                val reply = message?.optString("content", "") ?: ""
                if (reply.isNotBlank()) {
                    Result.success(cleanResponse(reply))
                } else {
                    Result.failure(Exception("Empty content from ${config?.name}."))
                }
            } else {
                Result.failure(Exception("No choices returned from ${config?.name}."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to parse response: ${e.message}"))
        }
    }

    suspend fun testConnection(config: ApiConfigEntity): Result<String> = withContext(Dispatchers.IO) {
        val testPrompt = "Respond with one short sentence confirming online status."
        generateResponse(config, testPrompt)
    }

    private fun cleanResponse(text: String): String {
        return text.trim()
    }

    private fun parseErrorMessage(body: String, fallback: String): String {
        return try {
            val json = JSONObject(body)
            if (json.has("error")) {
                val errorObj = json.optJSONObject("error")
                if (errorObj != null && errorObj.has("message")) {
                    errorObj.getString("message")
                } else {
                    json.optString("error", fallback)
                }
            } else {
                fallback
            }
        } catch (e: Exception) {
            if (body.length in 1..200) body else fallback
        }
    }
}

package com.snaimio.familyaiplanner.data

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

enum class AIProvider(val displayName: String, val defaultModel: String, val defaultEndpoint: String) {
    GEMINI("Google Gemini", "gemini-1.5-flash", "https://generativelanguage.googleapis.com/v1beta/models"),
    OPENAI("OpenAI (ChatGPT / GPT-4o)", "gpt-4o-mini", "https://api.openai.com/v1/chat/completions"),
    ANTHROPIC("Anthropic Claude", "claude-3-5-haiku-20241022", "https://api.anthropic.com/v1/messages"),
    CUSTOM("Custom / OpenAI-Compatible (Groq, DeepSeek, Ollama)", "deepseek-chat", "https://api.openai.com/v1/chat/completions")
}

/**
 * AIAssistantEngine provides universal multi-provider AI support:
 * - Google Gemini, OpenAI, Claude, Groq, DeepSeek, Ollama, OpenRouter, and custom endpoints
 * - Real-time conversational AI with family calendar, meal, and grocery action parsing
 * - Automatic on-device heuristic fallback
 */
object AIAssistantEngine {

    var activeProvider: AIProvider = AIProvider.GEMINI
    var customApiKey: String? = null
    var customBaseUrl: String? = null
    var customModelName: String? = null

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun configure(
        provider: AIProvider,
        apiKey: String?,
        baseUrl: String? = null,
        modelName: String? = null
    ) {
        activeProvider = provider
        customApiKey = apiKey?.trim()
        customBaseUrl = baseUrl?.trim()
        customModelName = modelName?.trim()
    }

    /**
     * Async conversational inference supporting any AI platform.
     */
    suspend fun generateResponseAsync(
        prompt: String,
        repository: PlannerRepository
    ): Pair<String, (() -> Unit)?> = withContext(Dispatchers.IO) {
        val (localReply, action) = generateLocalResponse(prompt, repository)
        val apiKey = customApiKey

        if (apiKey.isNullOrBlank()) {
            return@withContext Pair(localReply, action)
        }

        val systemPrompt = """
            You are the Family AI Planner assistant for ${repository.userName}'s family.
            Provide a warm, concise, and helpful answer in 1 to 3 sentences.
            User prompt: $prompt
        """.trimIndent()

        try {
            val remoteResponse: String? = when (activeProvider) {
                AIProvider.GEMINI -> callGeminiApi(apiKey, systemPrompt)
                AIProvider.OPENAI, AIProvider.CUSTOM -> callOpenAiCompatibleApi(apiKey, systemPrompt)
                AIProvider.ANTHROPIC -> callAnthropicApi(apiKey, systemPrompt)
            }

            if (!remoteResponse.isNullOrBlank()) {
                return@withContext Pair(remoteResponse.trim(), action)
            }
        } catch (_: Exception) {
            // Fallback gracefully to on-device engine
        }

        return@withContext Pair(localReply, action)
    }

    private fun callGeminiApi(apiKey: String, promptText: String): String? {
        val model = if (!customModelName.isNullOrBlank()) customModelName!! else AIProvider.GEMINI.defaultModel
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val requestJson = """
            {
              "contents": [{
                "parts": [{"text": ${gson.toJson(promptText)}}]
              }]
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .post(requestJson.toRequestBody(jsonMediaType))
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return null
                val json = gson.fromJson(body, JsonObject::class.java)
                return json.getAsJsonArray("candidates")
                    ?.get(0)?.asJsonObject
                    ?.getAsJsonObject("content")
                    ?.getAsJsonArray("parts")
                    ?.get(0)?.asJsonObject
                    ?.get("text")?.asString
            }
        }
        return null
    }

    private fun callOpenAiCompatibleApi(apiKey: String, promptText: String): String? {
        val endpoint = when {
            !customBaseUrl.isNullOrBlank() -> customBaseUrl!!
            activeProvider == AIProvider.OPENAI -> AIProvider.OPENAI.defaultEndpoint
            else -> AIProvider.CUSTOM.defaultEndpoint
        }
        val model = if (!customModelName.isNullOrBlank()) customModelName!! else "gpt-4o-mini"

        val requestJson = """
            {
              "model": ${gson.toJson(model)},
              "messages": [
                {"role": "system", "content": "You are Family AI Planner assistant. Be warm and concise in 1-3 sentences."},
                {"role": "user", "content": ${gson.toJson(promptText)}}
              ],
              "temperature": 0.7
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(endpoint)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestJson.toRequestBody(jsonMediaType))
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return null
                val json = gson.fromJson(body, JsonObject::class.java)
                return json.getAsJsonArray("choices")
                    ?.get(0)?.asJsonObject
                    ?.getAsJsonObject("message")
                    ?.get("content")?.asString
            }
        }
        return null
    }

    private fun callAnthropicApi(apiKey: String, promptText: String): String? {
        val endpoint = if (!customBaseUrl.isNullOrBlank()) customBaseUrl!! else AIProvider.ANTHROPIC.defaultEndpoint
        val model = if (!customModelName.isNullOrBlank()) customModelName!! else AIProvider.ANTHROPIC.defaultModel

        val requestJson = """
            {
              "model": ${gson.toJson(model)},
              "max_tokens": 300,
              "messages": [
                {"role": "user", "content": ${gson.toJson(promptText)}}
              ]
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(endpoint)
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .post(requestJson.toRequestBody(jsonMediaType))
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return null
                val json = gson.fromJson(body, JsonObject::class.java)
                return json.getAsJsonArray("content")
                    ?.get(0)?.asJsonObject
                    ?.get("text")?.asString
            }
        }
        return null
    }

    fun generateResponse(
        prompt: String,
        repository: PlannerRepository
    ): Pair<String, (() -> Unit)?> {
        return generateLocalResponse(prompt, repository)
    }

    private fun generateLocalResponse(
        prompt: String,
        repository: PlannerRepository
    ): Pair<String, (() -> Unit)?> {
        val lower = prompt.lowercase().trim()
        val user = repository.userName

        return when {
            // 1. Reminders and calendar scheduling
            lower.contains("remind") || lower.contains("trip") || lower.contains("event") || lower.contains("appointment") -> {
                val eventTitle = prompt.replace(Regex("(?i)remind|me|about|to|please|schedule|an|a|set"), "").trim()
                val finalTitle = if (eventTitle.isNotBlank()) eventTitle.replaceFirstChar { it.uppercase() } else "Family Event"
                val action = {
                    repository.addEvent(
                        EventItem(
                            time = "9:00",
                            title = finalTitle,
                            date = "2024-04-14",
                            memberAssigned = user
                        )
                    )
                }
                Pair("Got it! I've added '$finalTitle' to your family calendar.", action)
            }

            // 2. Add grocery item
            lower.startsWith("add ") && (lower.contains("grocery") || lower.contains("list") || lower.contains("buy") || lower.contains("milk") || lower.contains("eggs")) -> {
                val itemClean = prompt.replace(Regex("(?i)add|to|my|the|grocery|list|buy|please"), "").trim()
                val itemName = if (itemClean.isNotBlank()) itemClean.replaceFirstChar { it.uppercase() } else "Fresh Produce"
                val action = {
                    repository.addGrocery(GroceryItem(name = itemName, iconEmoji = "🛒"))
                }
                Pair("Added '$itemName' to your family grocery list! 🛒", action)
            }

            lower.startsWith("buy ") -> {
                val itemName = prompt.substringAfter("buy", "").trim().replaceFirstChar { it.uppercase() }
                val action = {
                    repository.addGrocery(GroceryItem(name = itemName, iconEmoji = "🛒"))
                }
                Pair("I've added '$itemName' to your grocery list. 📝", action)
            }

            // 3. Meal suggestions
            lower.contains("meal") || lower.contains("dinner") || lower.contains("cook") || lower.contains("eat") || lower.contains("recipe") -> {
                Pair("Here are some delicious family dinner ideas for this week:\n• Homemade Tacos 🌮\n• Baked Salmon & Asparagus 🐟\n• Creamy Tuscan Chicken 🍗\nWould you like me to add any of these to your Meal Planner?", null)
            }

            // 4. Calendar & Schedule
            lower.contains("schedule") || lower.contains("today") || lower.contains("appointments") -> {
                Pair("Here is your upcoming schedule:\n• 9:00 AM - Doctor appointment\n• 1:00 PM - Pick up kids\n• 4:30 PM - Sports practice", null)
            }

            // 5. General greeting / assistance
            lower.contains("hello") || lower.contains("hi") || lower.contains("how can i assist") || lower.contains("help") -> {
                Pair("Hello $user! I can help you manage family appointments, plan dinners, update grocery lists, and coordinate schedules. What would you like to do today?", null)
            }

            else -> {
                Pair("I've noted that for your family planner! Let me know if you'd like me to add an event, update the grocery checklist, or plan meals.", null)
            }
        }
    }
}

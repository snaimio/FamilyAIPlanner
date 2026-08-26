package com.snaimio.familyaiplanner.data

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * AIAssistantEngine integrates Google Gemini Generative AI with on-device heuristics:
 * - Live Google Gemini 1.5 Flash conversational queries
 * - Automatic Family Intent Parsing (Calendar scheduling, Grocery additions, Meal recommendations)
 * - Offline fallback heuristics matching all mockup prompts
 */
object AIAssistantEngine {

    var geminiApiKey: String? = null
    private var generativeModel: GenerativeModel? = null

    fun setApiKey(apiKey: String) {
        geminiApiKey = apiKey
        generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey
        )
    }

    /**
     * Suspend function for real-time Gemini AI response with local fallback.
     */
    suspend fun generateResponseAsync(
        prompt: String,
        repository: PlannerRepository
    ): Pair<String, (() -> Unit)?> = withContext(Dispatchers.IO) {
        val model = generativeModel
        val (localReply, action) = generateLocalResponse(prompt, repository)

        if (model != null && geminiApiKey?.isNotBlank() == true) {
            try {
                val systemContext = """
                    You are the Family AI Planner assistant for a family (Sarah, Spouse, Emma, Jacob).
                    Answer concisely, warmly, and helpfully in 1 to 3 sentences.
                    User prompt: $prompt
                """.trimIndent()

                val response = model.generateContent(systemContext)
                val aiText = response.text
                if (!aiText.isNullOrBlank()) {
                    return@withContext Pair(aiText.trim(), action)
                }
            } catch (_: Exception) {
                // Fall back to on-device heuristic
            }
        }

        return@withContext Pair(localReply, action)
    }

    /**
     * Synchronous response generator using intelligent heuristic parsing and action dispatching.
     */
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

        return when {
            // 1. Exact match from Mockup Screen 7: "Remind me about Emma's school trip"
            lower.contains("emma") && (lower.contains("trip") || lower.contains("school")) -> {
                val action = {
                    repository.addEvent(
                        EventItem(
                            time = "8:30",
                            title = "Emma's School Field Trip",
                            date = "2023-04-14",
                            memberAssigned = "Emma"
                        )
                    )
                }
                Pair("Got it! School trip is on April 14th. I've added it to your family calendar.", action)
            }

            // 2. Add grocery item
            lower.startsWith("add ") && (lower.contains("grocery") || lower.contains("list") || lower.contains("buy") || lower.contains("milk") || lower.contains("eggs")) -> {
                val itemClean = prompt.replace(Regex("(?i)add|to|my|the|grocery|list|buy|please"), "").trim()
                val itemName = if (itemClean.isNotBlank()) itemClean else "Organic Groceries"
                val action = {
                    repository.addGrocery(GroceryItem(name = itemName, iconEmoji = "🛒"))
                }
                Pair("Added '$itemName' to your family grocery list! 🛒", action)
            }

            lower.startsWith("buy ") -> {
                val itemName = prompt.substringAfter("buy", "").trim()
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
            lower.contains("schedule") || lower.contains("today") || lower.contains("appointments") || lower.contains("events") -> {
                Pair("Here is your schedule for April 11th:\n• 9:00 AM - Pediatrician appointment\n• 1:02 PM - Pick up Emma\n• 4:00 PM - Soccer practice", null)
            }

            // 5. General greeting / assistance
            lower.contains("hello") || lower.contains("hi") || lower.contains("how can i assist") || lower.contains("help") -> {
                Pair("Hello Sarah! I can help you manage family appointments, plan dinners, update grocery lists, and coordinate everyone's schedule. What would you like to do today?", null)
            }

            else -> {
                Pair("I've noted that for your family planner! Let me know if you'd like me to add an event, update the grocery checklist, or plan meals.", null)
            }
        }
    }
}

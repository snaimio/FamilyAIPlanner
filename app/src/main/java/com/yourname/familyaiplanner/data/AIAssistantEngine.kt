package com.yourname.familyaiplanner.data

/**
 * AIAssistantEngine parses conversational prompts and performs automatic family actions:
 * - Adding calendar events
 * - Adding grocery items
 * - Suggesting meals
 * - Answering family schedule queries
 */
object AIAssistantEngine {

    fun generateResponse(
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
            lower.startsWith("add ") && (lower.contains("grocery") || lower.contains("list") || lower.contains("buy")) -> {
                val itemClean = prompt.replace(Regex("(?i)add|to|my|the|grocery|list|buy|please"), "").trim()
                val itemName = if (itemClean.isNotBlank()) itemClean else "Fresh Produce"
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
            lower.contains("meal") || lower.contains("dinner") || lower.contains("cook") || lower.contains("eat") -> {
                Pair("Here are some quick family dinner ideas for this week:\n• Homemade Tacos 🌮\n• Baked Salmon & Asparagus 🐟\n• Creamy Tuscan Chicken 🍗\nWould you like me to add any of these to your Meal Planner?", null)
            }

            // 4. Calendar & Schedule
            lower.contains("schedule") || lower.contains("today") || lower.contains("appointments") -> {
                Pair("Here is your schedule for April 11th:\n• 9:00 AM - Pediatrician appointment\n• 1:02 PM - Pick up Emma\n• 4:00 PM - Soccer practice", null)
            }

            // 5. General greeting / assistance
            lower.contains("hello") || lower.contains("hi") || lower.contains("how can i assist") -> {
                Pair("Hello Sarah! I can help you manage family appointments, plan dinners, update grocery lists, and coordinate everyone's schedule. What would you like to do today?", null)
            }

            else -> {
                Pair("I've noted that for your family planner! Let me know if you'd like me to add an event, update the grocery checklist, or plan meals.", null)
            }
        }
    }
}

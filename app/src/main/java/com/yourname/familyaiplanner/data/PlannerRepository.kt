package com.yourname.familyaiplanner.data

import android.content.Context
import android.content.SharedPreferences

/**
 * PlannerRepository provides in-memory and persistent state for all Family AI Planner modules.
 * Pre-populated with the exact sample data from the design mockup.
 */
class PlannerRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("family_planner_prefs", Context.MODE_PRIVATE)

    // In-memory data lists
    private val events = mutableListOf<EventItem>()
    private val meals = mutableListOf<MealItem>()
    private val suggestions = mutableListOf<MealItem>()
    private val groceries = mutableListOf<GroceryItem>()
    private val chatMessages = mutableListOf<ChatMessage>()
    private val familyMembers = mutableListOf<FamilyMember>()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        // 1. Calendar Events (Matching Screen 3)
        events.add(EventItem(1, "9:00", "Pediatrician appointment", "2023-04-11", "Sarah"))
        events.add(EventItem(2, "1:02", "Pick up Emma", "2023-04-11", "Sarah"))
        events.add(EventItem(3, "4:00", "Soccer practice", "2023-04-11", "Jacob"))

        // 2. Meal Planner & Suggestions (Matching Screen 4)
        meals.add(MealItem(1, "Spaghetti", "May / Mon"))
        meals.add(MealItem(2, "Chicken Stir Fry", "Wes / Wed"))
        meals.add(MealItem(3, "Vegetable Soup", "Wo / Fri"))

        suggestions.add(MealItem(4, "Tacos", isSuggestion = true))
        suggestions.add(MealItem(5, "Meatloaf", isSuggestion = true))
        suggestions.add(MealItem(6, "Grilled Cheese", isSuggestion = true))

        // 3. Grocery List (Matching Screen 5)
        groceries.add(GroceryItem(1, "Bread", "🍞", isChecked = false))
        groceries.add(GroceryItem(2, "Apples", "🍎", isChecked = false))
        groceries.add(GroceryItem(3, "Chicken", "🍗", isChecked = false))
        groceries.add(GroceryItem(4, "Pasta", "🍝", isChecked = false))
        groceries.add(GroceryItem(5, "Carrots", "🥕", isChecked = false))

        // 4. Initial AI Chat Conversation (Matching Screens 6 & 7)
        chatMessages.add(
            ChatMessage(
                id = 1,
                text = "Remind me about Emma's school trip.",
                isFromUser = true,
                timestamp = "10:14 AM"
            )
        )
        chatMessages.add(
            ChatMessage(
                id = 2,
                text = "Got it! School trip is on April 14th.",
                isFromUser = false,
                timestamp = "10:14 AM"
            )
        )

        // 5. Family Members (Matching Screen 8)
        familyMembers.add(FamilyMember(1, "Spouse", "Spouse", "👤"))
        familyMembers.add(FamilyMember(2, "Emma", "Daughter", "👧"))
        familyMembers.add(FamilyMember(3, "Jacob", "Son", "👦"))
        familyMembers.add(FamilyMember(4, "Grandparent", "Grandmother", "👵"))
    }

    // Event operations
    fun getEvents(): List<EventItem> = events.toList()
    fun addEvent(event: EventItem) {
        events.add(event)
    }

    // Meal operations
    fun getMeals(): List<MealItem> = meals.toList()
    fun getSuggestions(): List<MealItem> = suggestions.toList()
    fun addMeal(meal: MealItem) {
        meals.add(meal)
    }

    // Grocery operations
    fun getGroceries(): List<GroceryItem> = groceries.toList()
    fun addGrocery(item: GroceryItem) {
        groceries.add(0, item)
    }
    fun toggleGrocery(id: Long) {
        val item = groceries.find { it.id == id }
        item?.let { it.isChecked = !it.isChecked }
    }

    // Chat operations
    fun getChatMessages(): List<ChatMessage> = chatMessages.toList()
    fun addChatMessage(message: ChatMessage) {
        chatMessages.add(message)
    }

    // Member operations
    fun getFamilyMembers(): List<FamilyMember> = familyMembers.toList()
    fun addFamilyMember(member: FamilyMember) {
        familyMembers.add(member)
    }
}

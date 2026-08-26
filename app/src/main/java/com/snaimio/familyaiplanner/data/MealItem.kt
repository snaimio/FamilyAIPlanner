package com.snaimio.familyaiplanner.data

/**
 * Data model representing a meal plan or AI meal suggestion.
 */
data class MealItem(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val dayOfWeek: String? = null, // e.g. "May", "Wes", "Wo" or "M", "W"
    val isSuggestion: Boolean = false,
    val category: String = "Dinner"
)

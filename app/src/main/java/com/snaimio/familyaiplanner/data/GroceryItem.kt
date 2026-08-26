package com.snaimio.familyaiplanner.data

/**
 * Data model representing a family grocery checklist item.
 */
data class GroceryItem(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val iconEmoji: String = "🍞",
    var isChecked: Boolean = false,
    val category: String = "Pantry"
)

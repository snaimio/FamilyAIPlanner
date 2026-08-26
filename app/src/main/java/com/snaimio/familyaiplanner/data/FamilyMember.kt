package com.snaimio.familyaiplanner.data

/**
 * Data model for registered family members in the planner.
 */
data class FamilyMember(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val role: String, // e.g. "Spouse", "Emma", "Jacob", "Grandparent"
    val avatarEmoji: String = "👤"
)

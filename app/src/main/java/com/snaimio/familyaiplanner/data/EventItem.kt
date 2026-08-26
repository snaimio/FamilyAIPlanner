package com.snaimio.familyaiplanner.data

/**
 * Data model representing a scheduled family calendar event.
 */
data class EventItem(
    val id: Long = System.currentTimeMillis(),
    val time: String,
    val title: String,
    val date: String = "2023-04-11",
    val memberAssigned: String? = null
)

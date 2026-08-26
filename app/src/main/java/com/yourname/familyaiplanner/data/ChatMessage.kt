package com.yourname.familyaiplanner.data

/**
 * Data model for a conversation turn in the AI Assistant chat.
 */
data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val isFromUser: Boolean,
    val timestamp: String = "Just now"
)

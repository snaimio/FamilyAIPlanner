package com.snaimio.familyaiplanner.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.snaimio.familyaiplanner.R
import com.snaimio.familyaiplanner.data.ChatMessage

class ChatAdapter(
    private var messages: List<ChatMessage>
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userContainer: View = itemView.findViewById(R.id.userBubbleContainer)
        val userText: TextView = itemView.findViewById(R.id.userMessageText)
        val aiContainer: View = itemView.findViewById(R.id.aiBubbleContainer)
        val aiText: TextView = itemView.findViewById(R.id.aiMessageText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        if (message.isFromUser) {
            holder.userContainer.visibility = View.VISIBLE
            holder.userText.text = message.text
            holder.aiContainer.visibility = View.GONE
        } else {
            holder.userContainer.visibility = View.GONE
            holder.aiContainer.visibility = View.VISIBLE
            holder.aiText.text = message.text
        }
    }

    override fun getItemCount(): Int = messages.size

    fun updateData(newMessages: List<ChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }
}

package com.snaimio.familyaiplanner.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.snaimio.familyaiplanner.MainActivity
import com.snaimio.familyaiplanner.R
import com.snaimio.familyaiplanner.data.AIAssistantEngine
import com.snaimio.familyaiplanner.data.ChatMessage
import com.snaimio.familyaiplanner.ui.adapters.ChatAdapter

/**
 * AIAssistantFragment presents Screens 6 & 7 of the design mockup.
 */
class AIAssistantFragment : Fragment() {

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var inputMessage: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_ai_assistant, container, false)
        val mainActivity = activity as? MainActivity
        val repository = mainActivity?.repository ?: return root

        val btnBack = root.findViewById<ImageView>(R.id.btnChatBack)
        chatRecyclerView = root.findViewById(R.id.chatRecyclerView)
        inputMessage = root.findViewById(R.id.inputChatMessage)
        val btnSend = root.findViewById<ImageButton>(R.id.btnSendMessage)

        val chipEmma = root.findViewById<TextView>(R.id.chipEmmaTrip)
        val chipAdd = root.findViewById<TextView>(R.id.chipAddItem)
        val chipAssist = root.findViewById<TextView>(R.id.chipHowAssist)

        btnBack.setOnClickListener {
            mainActivity.selectBottomNavTab(R.id.nav_home)
        }

        chatAdapter = ChatAdapter(repository.getChatMessages())
        val layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
        chatRecyclerView.layoutManager = layoutManager
        chatRecyclerView.adapter = chatAdapter

        btnSend.setOnClickListener {
            val text = inputMessage.text.toString().trim()
            if (text.isNotBlank()) {
                sendMessage(text, repository)
                inputMessage.text.clear()
            }
        }

        chipEmma.setOnClickListener {
            sendMessage("Remind me about Emma's school trip", repository)
        }

        chipAdd.setOnClickListener {
            sendMessage("Add organic milk and eggs to grocery list", repository)
        }

        chipAssist.setOnClickListener {
            sendMessage("How can I assist you today?", repository)
        }

        return root
    }

    private fun sendMessage(text: String, repository: com.snaimio.familyaiplanner.data.PlannerRepository) {
        val userMsg = ChatMessage(text = text, isFromUser = true)
        repository.addChatMessage(userMsg)
        chatAdapter.updateData(repository.getChatMessages())
        chatRecyclerView.smoothScrollToPosition(repository.getChatMessages().size - 1)

        val (replyText, action) = AIAssistantEngine.generateResponse(text, repository)
        action?.invoke()

        view?.postDelayed({
            val aiMsg = ChatMessage(text = replyText, isFromUser = false)
            repository.addChatMessage(aiMsg)
            chatAdapter.updateData(repository.getChatMessages())
            chatRecyclerView.smoothScrollToPosition(repository.getChatMessages().size - 1)
        }, 350)
    }
}

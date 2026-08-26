package com.yourname.familyaiplanner.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yourname.familyaiplanner.R
import com.yourname.familyaiplanner.data.EventItem

class EventAdapter(
    private var events: List<EventItem>,
    private val onItemClick: ((EventItem) -> Unit)? = null
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeText: TextView = itemView.findViewById(R.id.itemEventTime)
        val titleText: TextView = itemView.findViewById(R.id.itemEventTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.timeText.text = event.time
        holder.titleText.text = event.title
        holder.itemView.setOnClickListener { onItemClick?.invoke(event) }
    }

    override fun getItemCount(): Int = events.size

    fun updateData(newEvents: List<EventItem>) {
        events = newEvents
        notifyDataSetChanged()
    }
}

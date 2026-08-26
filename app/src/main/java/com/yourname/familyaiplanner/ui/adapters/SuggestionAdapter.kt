package com.yourname.familyaiplanner.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yourname.familyaiplanner.R
import com.yourname.familyaiplanner.data.MealItem

class SuggestionAdapter(
    private var suggestions: List<MealItem>,
    private val onAddClick: ((MealItem) -> Unit)? = null
) : RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder>() {

    class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.itemSuggestionName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_suggestion, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val item = suggestions[position]
        holder.nameText.text = item.name
        holder.itemView.setOnClickListener { onAddClick?.invoke(item) }
    }

    override fun getItemCount(): Int = suggestions.size

    fun updateData(newSuggestions: List<MealItem>) {
        suggestions = newSuggestions
        notifyDataSetChanged()
    }
}

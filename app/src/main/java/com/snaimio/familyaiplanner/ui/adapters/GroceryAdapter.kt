package com.snaimio.familyaiplanner.ui.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.snaimio.familyaiplanner.R
import com.snaimio.familyaiplanner.data.GroceryItem

class GroceryAdapter(
    private var groceries: List<GroceryItem>,
    private val onToggleCheck: ((GroceryItem) -> Unit)? = null
) : RecyclerView.Adapter<GroceryAdapter.GroceryViewHolder>() {

    class GroceryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: MaterialCheckBox = itemView.findViewById(R.id.itemGroceryCheckBox)
        val iconText: TextView = itemView.findViewById(R.id.itemGroceryIcon)
        val nameText: TextView = itemView.findViewById(R.id.itemGroceryName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroceryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_grocery, parent, false)
        return GroceryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroceryViewHolder, position: Int) {
        val item = groceries[position]
        holder.nameText.text = item.name
        holder.iconText.text = item.iconEmoji

        // Prevent check triggering during bind
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = item.isChecked

        updateStrikeThrough(holder.nameText, item.isChecked)

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            item.isChecked = isChecked
            updateStrikeThrough(holder.nameText, isChecked)
            onToggleCheck?.invoke(item)
        }

        holder.itemView.setOnClickListener {
            holder.checkBox.isChecked = !holder.checkBox.isChecked
        }
    }

    private fun updateStrikeThrough(textView: TextView, isChecked: Boolean) {
        if (isChecked) {
            textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            textView.alpha = 0.5f
        } else {
            textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            textView.alpha = 1.0f
        }
    }

    override fun getItemCount(): Int = groceries.size

    fun updateData(newGroceries: List<GroceryItem>) {
        groceries = newGroceries
        notifyDataSetChanged()
    }
}

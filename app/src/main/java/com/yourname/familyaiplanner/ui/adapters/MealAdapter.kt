package com.yourname.familyaiplanner.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yourname.familyaiplanner.R
import com.yourname.familyaiplanner.data.MealItem

class MealAdapter(
    private var meals: List<MealItem>,
    private val onItemClick: ((MealItem) -> Unit)? = null
) : RecyclerView.Adapter<MealAdapter.MealViewHolder>() {

    class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.itemMealName)
        val dayBadge: TextView = itemView.findViewById(R.id.itemMealDayBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_meal, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]
        holder.nameText.text = meal.name
        holder.dayBadge.text = meal.dayOfWeek ?: ""
        holder.dayBadge.visibility = if (meal.dayOfWeek.isNullOrBlank()) View.GONE else View.VISIBLE
        holder.itemView.setOnClickListener { onItemClick?.invoke(meal) }
    }

    override fun getItemCount(): Int = meals.size

    fun updateData(newMeals: List<MealItem>) {
        meals = newMeals
        notifyDataSetChanged()
    }
}

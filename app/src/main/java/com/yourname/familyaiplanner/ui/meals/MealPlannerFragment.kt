package com.yourname.familyaiplanner.ui.meals

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yourname.familyaiplanner.MainActivity
import com.yourname.familyaiplanner.R
import com.yourname.familyaiplanner.data.MealItem
import com.yourname.familyaiplanner.ui.adapters.MealAdapter
import com.yourname.familyaiplanner.ui.adapters.SuggestionAdapter

/**
 * MealPlannerFragment presents Screen 4 of the design mockup.
 */
class MealPlannerFragment : Fragment() {

    private lateinit var mealAdapter: MealAdapter
    private lateinit var suggestionAdapter: SuggestionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_meal_planner, container, false)
        val mainActivity = activity as? MainActivity
        val repository = mainActivity?.repository ?: return root

        val btnBack = root.findViewById<ImageView>(R.id.btnMealBack)
        val mealsRecycler = root.findViewById<RecyclerView>(R.id.mealsRecyclerView)
        val suggestionsRecycler = root.findViewById<RecyclerView>(R.id.suggestionsRecyclerView)
        val btnGenerate = root.findViewById<Button>(R.id.btnGenerateRecipes)

        btnBack.setOnClickListener {
            mainActivity.selectBottomNavTab(R.id.nav_home)
        }

        mealAdapter = MealAdapter(repository.getMeals())
        mealsRecycler.layoutManager = LinearLayoutManager(context)
        mealsRecycler.adapter = mealAdapter

        suggestionAdapter = SuggestionAdapter(repository.getSuggestions()) { suggestion ->
            repository.addMeal(MealItem(name = suggestion.name, dayOfWeek = "Next"))
            mealAdapter.updateData(repository.getMeals())
            Toast.makeText(context, "Added '${suggestion.name}' to dinner schedule! 🍴", Toast.LENGTH_SHORT).show()
        }
        suggestionsRecycler.layoutManager = LinearLayoutManager(context)
        suggestionsRecycler.adapter = suggestionAdapter

        btnGenerate.setOnClickListener {
            mainActivity.selectBottomNavTab(R.id.nav_chat)
        }

        return root
    }
}

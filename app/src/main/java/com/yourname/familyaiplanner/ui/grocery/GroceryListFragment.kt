package com.yourname.familyaiplanner.ui.grocery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yourname.familyaiplanner.MainActivity
import com.yourname.familyaiplanner.R
import com.yourname.familyaiplanner.data.GroceryItem
import com.yourname.familyaiplanner.ui.adapters.GroceryAdapter

/**
 * GroceryListFragment presents Screen 5 of the design mockup.
 */
class GroceryListFragment : Fragment() {

    private lateinit var groceryAdapter: GroceryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_grocery_list, container, false)
        val mainActivity = activity as? MainActivity
        val repository = mainActivity?.repository ?: return root

        val btnBack = root.findViewById<ImageView>(R.id.btnGroceryBack)
        val recyclerView = root.findViewById<RecyclerView>(R.id.groceryRecyclerView)
        val inputItem = root.findViewById<EditText>(R.id.inputGroceryItem)
        val btnAdd = root.findViewById<ImageButton>(R.id.btnAddGrocery)

        btnBack.setOnClickListener {
            mainActivity.selectBottomNavTab(R.id.nav_home)
        }

        groceryAdapter = GroceryAdapter(repository.getGroceries()) { item ->
            repository.toggleGrocery(item.id)
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = groceryAdapter

        btnAdd.setOnClickListener {
            val text = inputItem.text.toString().trim()
            if (text.isNotBlank()) {
                val newItem = GroceryItem(name = text, iconEmoji = "🛒")
                repository.addGrocery(newItem)
                groceryAdapter.updateData(repository.getGroceries())
                inputItem.text.clear()
            }
        }

        return root
    }
}

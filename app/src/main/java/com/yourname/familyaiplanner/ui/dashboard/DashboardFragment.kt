package com.yourname.familyaiplanner.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.yourname.familyaiplanner.MainActivity
import com.yourname.familyaiplanner.R
import com.yourname.familyaiplanner.ui.grocery.GroceryListFragment
import com.yourname.familyaiplanner.ui.settings.SettingsFragment

/**
 * DashboardFragment presents Screen 2 of the design mockup with interactive hamburger drawer.
 */
class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val mainActivity = activity as? MainActivity

        // Dynamic Greeting with user's name
        val greetingText = root.findViewById<TextView>(R.id.greetingText)
        val userName = mainActivity?.activeUserName ?: "Sarah"
        greetingText.text = "Good morning, $userName."

        // Functional Hamburger Button opening side Navigation Drawer
        root.findViewById<ImageView>(R.id.btnMenu).setOnClickListener {
            mainActivity?.openDrawer()
        }

        root.findViewById<ImageView>(R.id.btnProfile).setOnClickListener {
            mainActivity?.navigateTo(SettingsFragment())
        }

        root.findViewById<LinearLayout>(R.id.tileCalendar).setOnClickListener {
            mainActivity?.selectBottomNavTab(R.id.nav_calendar)
        }

        root.findViewById<LinearLayout>(R.id.tileTasks).setOnClickListener {
            mainActivity?.selectBottomNavTab(R.id.nav_calendar)
        }

        root.findViewById<LinearLayout>(R.id.tileMeals).setOnClickListener {
            mainActivity?.selectBottomNavTab(R.id.nav_meals)
        }

        root.findViewById<LinearLayout>(R.id.tileGrocery).setOnClickListener {
            mainActivity?.navigateTo(GroceryListFragment())
        }

        root.findViewById<LinearLayout>(R.id.bannerAIAssistant).setOnClickListener {
            mainActivity?.selectBottomNavTab(R.id.nav_chat)
        }

        return root
    }
}

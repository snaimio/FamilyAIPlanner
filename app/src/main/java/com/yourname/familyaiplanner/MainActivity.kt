package com.yourname.familyaiplanner

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yourname.familyaiplanner.data.PlannerRepository
import com.yourname.familyaiplanner.ui.calendar.CalendarFragment
import com.yourname.familyaiplanner.ui.chat.AIAssistantFragment
import com.yourname.familyaiplanner.ui.dashboard.DashboardFragment
import com.yourname.familyaiplanner.ui.grocery.GroceryListFragment
import com.yourname.familyaiplanner.ui.meals.MealPlannerFragment
import com.yourname.familyaiplanner.ui.settings.SettingsFragment

/**
 * MainActivity acts as the host for Family AI Planner navigation.
 */
class MainActivity : AppCompatActivity() {

    lateinit var repository: PlannerRepository
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        repository = PlannerRepository(this)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        if (savedInstanceState == null) {
            navigateTo(DashboardFragment())
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    navigateTo(DashboardFragment())
                    true
                }
                R.id.nav_calendar -> {
                    navigateTo(CalendarFragment())
                    true
                }
                R.id.nav_meals -> {
                    navigateTo(MealPlannerFragment())
                    true
                }
                R.id.nav_chat -> {
                    navigateTo(AIAssistantFragment())
                    true
                }
                R.id.nav_settings -> {
                    navigateTo(SettingsFragment())
                    true
                }
                else -> false
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainRoot)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    fun navigateTo(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun selectBottomNavTab(menuItemId: Int) {
        bottomNavigation.selectedItemId = menuItemId
    }
}

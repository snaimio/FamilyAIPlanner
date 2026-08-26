package com.yourname.familyaiplanner

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.yourname.familyaiplanner.data.PlannerRepository
import com.yourname.familyaiplanner.ui.calendar.CalendarFragment
import com.yourname.familyaiplanner.ui.chat.AIAssistantFragment
import com.yourname.familyaiplanner.ui.dashboard.DashboardFragment
import com.yourname.familyaiplanner.ui.grocery.GroceryListFragment
import com.yourname.familyaiplanner.ui.meals.MealPlannerFragment
import com.yourname.familyaiplanner.ui.settings.SettingsFragment

/**
 * MainActivity acts as the host for Family AI Planner navigation,
 * managing the DrawerLayout (hamburger menu) and BottomNavigationView.
 */
class MainActivity : AppCompatActivity() {

    lateinit var repository: PlannerRepository
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var bottomNavigation: BottomNavigationView

    var activeUserName: String = "Sarah"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        repository = PlannerRepository(this)
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        activeUserName = intent.getStringExtra("USER_NAME")
            ?: FirebaseAuth.getInstance().currentUser?.displayName
            ?: "Sarah"

        setupDrawerHeader()
        setupDrawerNavigation()
        setupBottomNavigation()

        if (savedInstanceState == null) {
            navigateTo(DashboardFragment())
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainRoot)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    private fun setupDrawerHeader() {
        val headerView = navigationView.getHeaderView(0)
        val nameText = headerView.findViewById<TextView>(R.id.drawerUserName)
        val emailText = headerView.findViewById<TextView>(R.id.drawerUserEmail)

        val currentUser = FirebaseAuth.getInstance().currentUser
        nameText.text = currentUser?.displayName ?: activeUserName
        emailText.text = currentUser?.email ?: "${activeUserName.lowercase()}.family@gmail.com"
    }

    private fun setupDrawerNavigation() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            drawerLayout.closeDrawer(GravityCompat.START)
            when (menuItem.itemId) {
                R.id.drawer_home -> {
                    selectBottomNavTab(R.id.nav_home)
                    true
                }
                R.id.drawer_calendar -> {
                    selectBottomNavTab(R.id.nav_calendar)
                    true
                }
                R.id.drawer_meals -> {
                    selectBottomNavTab(R.id.nav_meals)
                    true
                }
                R.id.drawer_grocery -> {
                    navigateTo(GroceryListFragment())
                    true
                }
                R.id.drawer_chat -> {
                    selectBottomNavTab(R.id.nav_chat)
                    true
                }
                R.id.drawer_members -> {
                    navigateTo(SettingsFragment())
                    true
                }
                R.id.drawer_settings -> {
                    navigateTo(SettingsFragment())
                    true
                }
                R.id.drawer_signout -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, WelcomeActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupBottomNavigation() {
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
                else -> false
            }
        }
    }

    fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
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

package com.snaimio.familyaiplanner

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.snaimio.familyaiplanner.data.PlannerRepository
import com.snaimio.familyaiplanner.ui.calendar.CalendarFragment
import com.snaimio.familyaiplanner.ui.chat.AIAssistantFragment
import com.snaimio.familyaiplanner.ui.dashboard.DashboardFragment
import com.snaimio.familyaiplanner.ui.grocery.GroceryListFragment
import com.snaimio.familyaiplanner.ui.meals.MealPlannerFragment
import com.snaimio.familyaiplanner.ui.settings.SettingsFragment

/**
 * MainActivity acts as the host for Family AI Planner navigation,
 * managing the DrawerLayout (hamburger menu) and BottomNavigationView
 * with real authenticated user profile data from Google / Firebase.
 */
@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    lateinit var repository: PlannerRepository
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var bottomNavigation: BottomNavigationView

    var activeUserName: String = "Account Owner"
    var activeUserEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        val lastGoogleAccount = GoogleSignIn.getLastSignedInAccount(this)
        val passedName = intent.getStringExtra("USER_NAME")
        val passedEmail = intent.getStringExtra("USER_EMAIL")

        activeUserName = when {
            !currentUser?.displayName.isNullOrBlank() -> currentUser!!.displayName!!
            !lastGoogleAccount?.displayName.isNullOrBlank() -> lastGoogleAccount!!.displayName!!
            !passedName.isNullOrBlank() -> passedName
            !currentUser?.email.isNullOrBlank() -> currentUser!!.email!!.substringBefore("@").replaceFirstChar { it.uppercase() }
            !lastGoogleAccount?.email.isNullOrBlank() -> lastGoogleAccount!!.email!!.substringBefore("@").replaceFirstChar { it.uppercase() }
            !passedEmail.isNullOrBlank() -> passedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
            else -> "Family Admin"
        }

        activeUserEmail = when {
            !currentUser?.email.isNullOrBlank() -> currentUser!!.email!!
            !lastGoogleAccount?.email.isNullOrBlank() -> lastGoogleAccount!!.email!!
            !passedEmail.isNullOrBlank() -> passedEmail
            else -> "Signed in account"
        }

        repository = PlannerRepository(this, activeUserName)
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Load saved universal AI settings
        val aiPrefs = getSharedPreferences("family_settings_prefs", android.content.Context.MODE_PRIVATE)
        val providerIndex = aiPrefs.getInt("ai_provider_index", 0)
        val provider = com.snaimio.familyaiplanner.data.AIProvider.values().getOrElse(providerIndex) { com.snaimio.familyaiplanner.data.AIProvider.GEMINI }
        val apiKey = aiPrefs.getString("ai_api_key", null)
        val baseUrl = aiPrefs.getString("ai_base_url", null)
        val modelName = aiPrefs.getString("ai_model_name", null)
        com.snaimio.familyaiplanner.data.AIAssistantEngine.configure(provider, apiKey, baseUrl, modelName)

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

        nameText.text = activeUserName
        emailText.text = activeUserEmail
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
                    navigateTo(com.snaimio.familyaiplanner.ui.members.FamilyMembersFragment())
                    true
                }
                R.id.drawer_settings -> {
                    navigateTo(SettingsFragment())
                    true
                }
                R.id.drawer_signout -> {
                    FirebaseAuth.getInstance().signOut()
                    try {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                        GoogleSignIn.getClient(this, gso).signOut()
                    } catch (_: Exception) {}
                    val prefs = getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
                    prefs.edit().clear().apply()
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

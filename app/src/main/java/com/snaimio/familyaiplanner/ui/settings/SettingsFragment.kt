package com.snaimio.familyaiplanner.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.snaimio.familyaiplanner.MainActivity
import com.snaimio.familyaiplanner.R
import com.snaimio.familyaiplanner.WelcomeActivity
import com.snaimio.familyaiplanner.data.AIAssistantEngine

/**
 * SettingsFragment provides dedicated App & Account Settings and Preferences.
 */
@Suppress("DEPRECATION")
class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        val mainActivity = activity as? MainActivity
        val prefs = requireContext().getSharedPreferences("family_settings_prefs", Context.MODE_PRIVATE)

        val btnBack = root.findViewById<ImageView>(R.id.btnSettingsBack)
        val textName = root.findViewById<TextView>(R.id.settingsUserName)
        val textEmail = root.findViewById<TextView>(R.id.settingsUserEmail)
        val btnSignOut = root.findViewById<Button>(R.id.btnSettingsSignOut)

        val inputApiKey = root.findViewById<EditText>(R.id.inputGeminiApiKey)
        val btnSaveApiKey = root.findViewById<Button>(R.id.btnSaveApiKey)

        val switchMorning = root.findViewById<SwitchMaterial>(R.id.switchMorningBriefing)
        val switchGrocery = root.findViewById<SwitchMaterial>(R.id.switchGroceryAlerts)
        val switchAppointment = root.findViewById<SwitchMaterial>(R.id.switchAppointmentReminders)

        btnBack.setOnClickListener {
            mainActivity?.selectBottomNavTab(R.id.nav_home)
        }

        // 1. User Profile
        textName.text = mainActivity?.activeUserName ?: "Account Owner"
        textEmail.text = mainActivity?.activeUserEmail ?: "Signed in"

        btnSignOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            try {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                GoogleSignIn.getClient(requireActivity(), gso).signOut()
            } catch (_: Exception) {}

            val authPrefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            authPrefs.edit().clear().apply()

            startActivity(Intent(requireActivity(), WelcomeActivity::class.java))
            requireActivity().finish()
        }

        // 2. Load saved switches
        switchMorning.isChecked = prefs.getBoolean("notif_morning", true)
        switchGrocery.isChecked = prefs.getBoolean("notif_grocery", true)
        switchAppointment.isChecked = prefs.getBoolean("notif_appointment", true)

        switchMorning.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notif_morning", isChecked).apply()
        }
        switchGrocery.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notif_grocery", isChecked).apply()
        }
        switchAppointment.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notif_appointment", isChecked).apply()
        }

        // 3. Gemini API Key
        val savedApiKey = prefs.getString("gemini_api_key", "")
        if (!savedApiKey.isNullOrBlank()) {
            inputApiKey.setText(savedApiKey)
            AIAssistantEngine.setApiKey(savedApiKey)
        }

        btnSaveApiKey.setOnClickListener {
            val key = inputApiKey.text.toString().trim()
            if (key.isNotBlank()) {
                prefs.edit().putString("gemini_api_key", key).apply()
                AIAssistantEngine.setApiKey(key)
                Toast.makeText(context, "Gemini AI API Key saved! ✨", Toast.LENGTH_SHORT).show()
            } else {
                prefs.edit().remove("gemini_api_key").apply()
                AIAssistantEngine.geminiApiKey = null
                Toast.makeText(context, "API Key cleared (using on-device engine).", Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }
}

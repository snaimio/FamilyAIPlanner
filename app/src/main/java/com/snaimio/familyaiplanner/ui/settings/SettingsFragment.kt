package com.snaimio.familyaiplanner.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
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
import com.snaimio.familyaiplanner.data.AIProvider

/**
 * SettingsFragment provides universal AI platform configuration (Gemini, OpenAI, Claude, Groq, DeepSeek, Ollama),
 * dietary meal goals, smart notifications, and account management.
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

        val spinnerProvider = root.findViewById<Spinner>(R.id.spinnerAiProvider)
        val inputApiKey = root.findViewById<EditText>(R.id.inputUniversalApiKey)
        val inputBaseUrl = root.findViewById<EditText>(R.id.inputCustomBaseUrl)
        val inputModelName = root.findViewById<EditText>(R.id.inputCustomModelName)
        val btnSaveAi = root.findViewById<Button>(R.id.btnSaveUniversalAi)

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

        // 2. Setup Universal AI Provider Spinner
        val providers = AIProvider.values()
        val providerNames = providers.map { it.displayName }
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, providerNames)
        spinnerProvider.adapter = spinnerAdapter

        val savedProviderIndex = prefs.getInt("ai_provider_index", 0)
        spinnerProvider.setSelection(savedProviderIndex)

        inputApiKey.setText(prefs.getString("ai_api_key", ""))
        inputBaseUrl.setText(prefs.getString("ai_base_url", ""))
        inputModelName.setText(prefs.getString("ai_model_name", ""))

        spinnerProvider.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = providers[position]
                if (inputBaseUrl.text.isNullOrBlank()) {
                    inputBaseUrl.hint = selected.defaultEndpoint
                }
                if (inputModelName.text.isNullOrBlank()) {
                    inputModelName.hint = selected.defaultModel
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnSaveAi.setOnClickListener {
            val position = spinnerProvider.selectedItemPosition
            val selectedProvider = providers.getOrElse(position) { AIProvider.GEMINI }
            val key = inputApiKey.text.toString().trim()
            val baseUrl = inputBaseUrl.text.toString().trim()
            val modelName = inputModelName.text.toString().trim()

            prefs.edit()
                .putInt("ai_provider_index", position)
                .putString("ai_api_key", key)
                .putString("ai_base_url", baseUrl)
                .putString("ai_model_name", modelName)
                .apply()

            AIAssistantEngine.configure(
                provider = selectedProvider,
                apiKey = if (key.isNotBlank()) key else null,
                baseUrl = if (baseUrl.isNotBlank()) baseUrl else null,
                modelName = if (modelName.isNotBlank()) modelName else null
            )

            val providerName = selectedProvider.displayName
            if (key.isNotBlank()) {
                Toast.makeText(context, "$providerName API configured & active! ✨", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "API Key cleared (using on-device engine).", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. Notification switches
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

        return root
    }
}

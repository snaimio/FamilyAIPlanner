package com.snaimio.familyaiplanner.ui.settings

import android.app.AlertDialog
import android.content.Context
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
import com.google.android.material.chip.Chip
import com.google.android.material.switchmaterial.SwitchMaterial
import com.snaimio.familyaiplanner.MainActivity
import com.snaimio.familyaiplanner.R
import com.snaimio.familyaiplanner.data.AIAssistantEngine
import com.snaimio.familyaiplanner.data.FamilyMember
import com.snaimio.familyaiplanner.ui.adapters.MemberAdapter

/**
 * SettingsFragment provides full Family Settings & Preferences:
 * - Household Family Member Profiles
 * - Dietary & Meal Planning Preferences
 * - Smart Notification Toggles
 * - Custom Google Gemini AI API Configuration
 */
class SettingsFragment : Fragment() {

    private lateinit var memberAdapter: MemberAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        val mainActivity = activity as? MainActivity
        val repository = mainActivity?.repository ?: return root
        val prefs = requireContext().getSharedPreferences("family_settings_prefs", Context.MODE_PRIVATE)

        val btnBack = root.findViewById<ImageView>(R.id.btnSettingsBack)
        val recyclerView = root.findViewById<RecyclerView>(R.id.membersRecyclerView)
        val btnAddMember = root.findViewById<Button>(R.id.btnAddMember)

        val inputApiKey = root.findViewById<EditText>(R.id.inputGeminiApiKey)
        val btnSaveApiKey = root.findViewById<Button>(R.id.btnSaveApiKey)

        val switchMorning = root.findViewById<SwitchMaterial>(R.id.switchMorningBriefing)
        val switchGrocery = root.findViewById<SwitchMaterial>(R.id.switchGroceryAlerts)
        val switchAppointment = root.findViewById<SwitchMaterial>(R.id.switchAppointmentReminders)

        btnBack.setOnClickListener {
            mainActivity.selectBottomNavTab(R.id.nav_home)
        }

        // 1. Members List
        memberAdapter = MemberAdapter(repository.getFamilyMembers())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = memberAdapter

        btnAddMember.setOnClickListener {
            showAddMemberDialog(repository)
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

    private fun showAddMemberDialog(repository: com.snaimio.familyaiplanner.data.PlannerRepository) {
        val context = context ?: return
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Add Family Member")

        val layout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val nameInput = EditText(context).apply { hint = "Name (e.g. Grandma, Oliver)" }
        val roleInput = EditText(context).apply { hint = "Role (e.g. Spouse, Son, Daughter, Parent)" }

        layout.addView(nameInput)
        layout.addView(roleInput)
        builder.setView(layout)

        builder.setPositiveButton("Add") { _, _ ->
            val name = nameInput.text.toString().trim()
            val role = roleInput.text.toString().trim()
            if (name.isNotBlank()) {
                val newMember = FamilyMember(
                    id = System.currentTimeMillis(),
                    name = name,
                    role = if (role.isNotBlank()) role else "Family Member",
                    avatarEmoji = "👤"
                )
                repository.addFamilyMember(newMember)
                memberAdapter.updateData(repository.getFamilyMembers())
                Toast.makeText(context, "Added $name to household! 🏡", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}

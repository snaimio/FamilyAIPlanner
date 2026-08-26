package com.snaimio.familyaiplanner.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.snaimio.familyaiplanner.MainActivity
import com.snaimio.familyaiplanner.R
import com.snaimio.familyaiplanner.data.FamilyMember
import com.snaimio.familyaiplanner.ui.adapters.MemberAdapter

/**
 * SettingsFragment presents Screen 8 of the design mockup.
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

        val btnBack = root.findViewById<ImageView>(R.id.btnSettingsBack)
        val recyclerView = root.findViewById<RecyclerView>(R.id.membersRecyclerView)
        val btnAddMember = root.findViewById<Button>(R.id.btnAddMember)

        btnBack.setOnClickListener {
            mainActivity.selectBottomNavTab(R.id.nav_home)
        }

        memberAdapter = MemberAdapter(repository.getFamilyMembers())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = memberAdapter

        btnAddMember.setOnClickListener {
            showAddMemberDialog(repository)
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
        val roleInput = EditText(context).apply { hint = "Role (e.g. Son, Daughter, Grandfather)" }

        layout.addView(nameInput)
        layout.addView(roleInput)
        builder.setView(layout)

        builder.setPositiveButton("Add") { _, _ ->
            val name = nameInput.text.toString().trim()
            val role = roleInput.text.toString().trim()
            if (name.isNotBlank()) {
                val newMember = FamilyMember(
                    name = name,
                    role = if (role.isNotBlank()) role else "Family Member",
                    avatarEmoji = "👤"
                )
                repository.addFamilyMember(newMember)
                memberAdapter.updateData(repository.getFamilyMembers())
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}

package com.yourname.familyaiplanner.ui.calendar

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yourname.familyaiplanner.MainActivity
import com.yourname.familyaiplanner.R
import com.yourname.familyaiplanner.data.EventItem
import com.yourname.familyaiplanner.ui.adapters.EventAdapter
import com.yourname.familyaiplanner.ui.dashboard.DashboardFragment
import com.yourname.familyaiplanner.ui.views.CalendarMonthView

/**
 * CalendarFragment presents Screen 3 of the design mockup.
 */
class CalendarFragment : Fragment() {

    private lateinit var eventAdapter: EventAdapter
    private lateinit var selectedDateHeader: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_calendar, container, false)
        val mainActivity = activity as? MainActivity
        val repository = mainActivity?.repository ?: return root

        selectedDateHeader = root.findViewById(R.id.selectedDateHeader)
        val monthView = root.findViewById<CalendarMonthView>(R.id.monthCalendarView)
        val recyclerView = root.findViewById<RecyclerView>(R.id.eventsRecyclerView)
        val fabAdd = root.findViewById<FloatingActionButton>(R.id.fabAddEvent)
        val btnBack = root.findViewById<ImageView>(R.id.btnCalendarBack)

        btnBack.setOnClickListener {
            mainActivity.selectBottomNavTab(R.id.nav_home)
        }

        eventAdapter = EventAdapter(repository.getEvents())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = eventAdapter

        monthView.onDateSelected = { day ->
            selectedDateHeader.text = "April $day"
        }

        fabAdd.setOnClickListener {
            showAddEventDialog(repository)
        }

        return root
    }

    private fun showAddEventDialog(repository: com.yourname.familyaiplanner.data.PlannerRepository) {
        val context = context ?: return
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Add Family Event")

        val layout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val timeInput = EditText(context).apply { hint = "Time (e.g. 5:30 PM)" }
        val titleInput = EditText(context).apply { hint = "Event title (e.g. Piano Lesson)" }

        layout.addView(timeInput)
        layout.addView(titleInput)
        builder.setView(layout)

        builder.setPositiveButton("Add") { _, _ ->
            val time = timeInput.text.toString().trim()
            val title = titleInput.text.toString().trim()
            if (title.isNotBlank()) {
                val newEvent = EventItem(
                    time = if (time.isNotBlank()) time else "All day",
                    title = title
                )
                repository.addEvent(newEvent)
                eventAdapter.updateData(repository.getEvents())
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}

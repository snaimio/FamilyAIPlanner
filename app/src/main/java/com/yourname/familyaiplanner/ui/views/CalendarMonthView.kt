package com.yourname.familyaiplanner.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * CalendarMonthView is a custom Canvas view matching Screen 3 of the design mockup:
 * - "April 2023" Month Title & Prev/Next Arrows
 * - Day-of-week headers (Su, Mo, Tu, We, Th, Fr, Sa)
 * - Numbered date grid with active day (11) highlighted in a solid Teal circle.
 */
class CalendarMonthView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var selectedDay: Int = 11
        set(value) {
            field = value
            invalidate()
        }

    var onDateSelected: ((Int) -> Unit)? = null

    private val dayHeaders = arrayOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
    // Grid configuration for April 2023 (April 1st started on Saturday)
    private val totalDaysInMonth = 30
    private val startDayOffset = 6 // Saturday

    private val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A2F31")
        textSize = 34f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val dayNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#5C7C7F")
        textSize = 24f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A2F31")
        textSize = 26f
        textAlign = Paint.Align.CENTER
    }

    private val activeDatePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 26f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val circleHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3D7B80")
        style = Paint.Style.FILL
    }

    private val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A2F31")
        textSize = 36f
        textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val cx = w / 2f

        // 1. Draw Month Header
        canvas.drawText("April 2023", cx, 44f, headerPaint)
        canvas.drawText("‹", 48f, 44f, arrowPaint)
        canvas.drawText("›", w - 48f, 44f, arrowPaint)

        // 2. Draw Day Headers (Su, Mo, Tu, We, Th, Fr, Sa)
        val colWidth = w / 7f
        val startY = 96f
        for (i in 0 until 7) {
            val colCenterX = i * colWidth + colWidth / 2f
            canvas.drawText(dayHeaders[i], colCenterX, startY, dayNamePaint)
        }

        // 3. Draw Calendar Number Grid
        val rowHeight = 52f
        var currentDay = 1

        for (row in 0..5) {
            for (col in 0..6) {
                val dayIndex = row * 7 + col
                if (dayIndex >= startDayOffset && currentDay <= totalDaysInMonth) {
                    val x = col * colWidth + colWidth / 2f
                    val y = startY + (row + 1) * rowHeight

                    if (currentDay == selectedDay) {
                        // Draw solid Teal circle
                        canvas.drawCircle(x, y - 9f, 22f, circleHighlightPaint)
                        canvas.drawText(currentDay.toString(), x, y, activeDatePaint)
                    } else {
                        canvas.drawText(currentDay.toString(), x, y, datePaint)
                    }
                    currentDay++
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val colWidth = width.toFloat() / 7f
            val rowHeight = 52f
            val startY = 96f

            val touchX = event.x
            val touchY = event.y

            val col = (touchX / colWidth).toInt().coerceIn(0, 6)
            val row = ((touchY - startY) / rowHeight).toInt() - 1

            if (row in 0..5) {
                val dayIndex = row * 7 + col
                val clickedDay = dayIndex - startDayOffset + 1
                if (clickedDay in 1..totalDaysInMonth) {
                    selectedDay = clickedDay
                    onDateSelected?.invoke(clickedDay)
                    return true
                }
            }
        }
        return true
    }
}

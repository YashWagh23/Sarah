package com.sarah.app.domain.engine

import com.sarah.app.domain.model.AgendaItem
import com.sarah.app.domain.model.CollegeSchedule
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.Task
import java.util.Locale
import kotlin.math.min

class AgendaPlanner {

    fun generateAgenda(
        tasks: List<Task>,
        currentMinutes: Int,
        sleepMinutes: Int,
        energyLevel: EnergyLevel,
        schedule: CollegeSchedule
    ): List<AgendaItem> {
        val agenda = mutableListOf<AgendaItem>()
        var clock = currentMinutes

        // If it's before or during dinner time (e.g., around 18:30 - 20:00), insert a rest / dinner slot
        val dinnerTime = 19 * 60 // 7:00 PM
        if (clock <= dinnerTime && (clock + schedule.dinnerBufferMinutes) < sleepMinutes) {
            val dinnerStart = clock
            val dinnerEnd = min(dinnerStart + schedule.dinnerBufferMinutes, sleepMinutes)
            agenda.add(
                AgendaItem(
                    startTimeFormatted = formatTime(dinnerStart),
                    endTimeFormatted = formatTime(dinnerEnd),
                    title = "Rest / Dinner Buffer",
                    subtitle = "Unwind and recharge nutrition",
                    isBreak = true,
                    durationMinutes = dinnerEnd - dinnerStart
                )
            )
            clock = dinnerEnd
        }

        // Determine session and break lengths based on EnergyLevel
        val breakDuration = when (energyLevel) {
            EnergyLevel.EXHAUSTED -> 20
            EnergyLevel.LOW -> 15
            EnergyLevel.NORMAL -> 15
            EnergyLevel.HIGH -> 10
        }

        for (task in tasks) {
            if (clock >= sleepMinutes) break

            var remainingTaskMinutes = task.estimatedMinutes
            val maxChunk = when (energyLevel) {
                EnergyLevel.EXHAUSTED -> 25
                EnergyLevel.LOW -> 30
                EnergyLevel.NORMAL -> 45
                EnergyLevel.HIGH -> 60
            }

            while (remainingTaskMinutes > 0 && clock < sleepMinutes) {
                val chunk = min(remainingTaskMinutes, min(maxChunk, sleepMinutes - clock))
                if (chunk <= 0) break

                val slotStart = clock
                val slotEnd = clock + chunk
                agenda.add(
                    AgendaItem(
                        startTimeFormatted = formatTime(slotStart),
                        endTimeFormatted = formatTime(slotEnd),
                        title = task.title,
                        subtitle = task.subjectName ?: task.type.displayName,
                        isBreak = false,
                        durationMinutes = chunk,
                        taskId = task.id
                    )
                )
                clock = slotEnd
                remainingTaskMinutes -= chunk

                // Add a break if there is still remaining task or more tasks and time before sleep
                if (clock + breakDuration < sleepMinutes && (remainingTaskMinutes > 0 || tasks.indexOf(task) < tasks.lastIndex)) {
                    val breakStart = clock
                    val breakEnd = min(clock + breakDuration, sleepMinutes)
                    agenda.add(
                        AgendaItem(
                            startTimeFormatted = formatTime(breakStart),
                            endTimeFormatted = formatTime(breakEnd),
                            title = "Restorative Break",
                            subtitle = if (energyLevel == EnergyLevel.EXHAUSTED) "Hydrate & rest eyes" else "Step away & stretch",
                            isBreak = true,
                            durationMinutes = breakEnd - breakStart
                        )
                    )
                    clock = breakEnd
                }
            }
        }

        // Add bedtime slot if time allows
        if (clock < sleepMinutes && agenda.isNotEmpty()) {
            agenda.add(
                AgendaItem(
                    startTimeFormatted = formatTime(clock),
                    endTimeFormatted = formatTime(sleepMinutes),
                    title = "Wind Down & Sleep Prep",
                    subtitle = "Relaxation & prepare for tomorrow",
                    isBreak = true,
                    durationMinutes = sleepMinutes - clock
                )
            )
        }

        return agenda
    }

    private fun formatTime(minutesFromMidnight: Int): String {
        val totalMins = minutesFromMidnight % (24 * 60)
        val hour24 = totalMins / 60
        val mins = totalMins % 60
        val hour12 = when (val h = hour24 % 12) {
            0 -> 12
            else -> h
        }
        val amPm = if (hour24 < 12) "AM" else "PM"
        return String.format(Locale.US, "%d:%02d %s", hour12, mins, amPm)
    }
}

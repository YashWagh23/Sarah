package com.sarah.app.domain.engine

import com.sarah.app.domain.model.AgendaItem
import com.sarah.app.domain.model.CollegeSchedule
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.Task
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
        val (sessionDuration, breakDuration) = when (energyLevel) {
            EnergyLevel.HIGH -> Pair(50, 10)
            EnergyLevel.NORMAL -> Pair(40, 10)
            EnergyLevel.LOW -> Pair(25, 10)
            EnergyLevel.EXHAUSTED -> Pair(20, 15)
        }

        for (task in tasks) {
            var taskRemaining = task.remainingMinutes
            if (taskRemaining <= 0) taskRemaining = task.estimatedMinutes

            while (taskRemaining > 0 && clock < sleepMinutes) {
                val availableChunk = min(sessionDuration, taskRemaining)
                val blockEnd = min(clock + availableChunk, sleepMinutes)
                val actualDuration = blockEnd - clock

                if (actualDuration <= 0) break

                agenda.add(
                    AgendaItem(
                        startTimeFormatted = formatTime(clock),
                        endTimeFormatted = formatTime(blockEnd),
                        title = task.title,
                        subtitle = "${task.subjectName ?: task.type.displayName} • Focus block",
                        isBreak = false,
                        durationMinutes = actualDuration
                    )
                )

                taskRemaining -= actualDuration
                clock = blockEnd

                // Add a break if there is still study time before sleep and more work
                if (clock + breakDuration < sleepMinutes && (taskRemaining > 0 || tasks.indexOf(task) < tasks.size - 1)) {
                    val breakEnd = min(clock + breakDuration, sleepMinutes)
                    agenda.add(
                        AgendaItem(
                            startTimeFormatted = formatTime(clock),
                            endTimeFormatted = formatTime(breakEnd),
                            title = "Pacing Break",
                            subtitle = "Rest eyes and reset attention",
                            isBreak = true,
                            durationMinutes = breakEnd - breakStartOrCurrent(clock, breakEnd)
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

    private fun breakStartOrCurrent(clock: Int, breakEnd: Int): Int {
        return clock
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
        val minsStr = if (mins < 10) "0$mins" else "$mins"
        return "$hour12:$minsStr $amPm"
    }
}

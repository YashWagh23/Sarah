package com.sarah.app.data.local

import com.sarah.app.domain.model.*

object IosModelSerializers {

    // --- Task ---
    fun serializeTask(t: Task): String {
        return buildString {
            append("{")
            append("\"id\":${t.id},")
            append("\"title\":\"${IosJsonHelper.escapeJson(t.title)}\",")
            append("\"subjectId\":${t.subjectId ?: "null"},")
            append("\"subjectName\":${t.subjectName?.let { "\"${IosJsonHelper.escapeJson(it)}\"" } ?: "null"},")
            append("\"type\":\"${t.type.name}\",")
            append("\"description\":\"${IosJsonHelper.escapeJson(t.description)}\",")
            append("\"deadlineEpochMs\":${t.deadlineEpochMs},")
            append("\"estimatedMinutes\":${t.estimatedMinutes},")
            append("\"priority\":\"${t.priority.name}\",")
            append("\"difficulty\":\"${t.difficulty.name}\",")
            append("\"energyRequirement\":\"${t.energyRequirement.name}\",")
            append("\"status\":\"${t.status.name}\",")
            append("\"completionPercentage\":${t.completionPercentage},")
            append("\"completedMinutes\":${t.completedMinutes},")
            append("\"createdAtEpochMs\":${t.createdAtEpochMs},")
            append("\"completedAtEpochMs\":${t.completedAtEpochMs ?: "null"}")
            append("}")
        }
    }

    fun deserializeTask(json: String): Task {
        val map = IosJsonHelper.parseObject(json)
        return Task(
            id = map["id"]?.toLongOrNull() ?: 0L,
            title = map["title"] ?: "",
            subjectId = map["subjectId"]?.toLongOrNull(),
            subjectName = map["subjectName"],
            type = map["type"]?.let { runCatching { TaskType.valueOf(it) }.getOrNull() } ?: TaskType.ASSIGNMENT,
            description = map["description"] ?: "",
            deadlineEpochMs = map["deadlineEpochMs"]?.toLongOrNull() ?: 0L,
            estimatedMinutes = map["estimatedMinutes"]?.toIntOrNull() ?: 45,
            priority = map["priority"]?.let { runCatching { TaskPriority.valueOf(it) }.getOrNull() } ?: TaskPriority.MEDIUM,
            difficulty = map["difficulty"]?.let { runCatching { Difficulty.valueOf(it) }.getOrNull() } ?: Difficulty.MEDIUM,
            energyRequirement = map["energyRequirement"]?.let { runCatching { EnergyRequirement.valueOf(it) }.getOrNull() } ?: EnergyRequirement.MEDIUM,
            status = map["status"]?.let { runCatching { TaskStatus.valueOf(it) }.getOrNull() } ?: TaskStatus.PENDING,
            completionPercentage = map["completionPercentage"]?.toIntOrNull() ?: 0,
            completedMinutes = map["completedMinutes"]?.toIntOrNull() ?: 0,
            createdAtEpochMs = map["createdAtEpochMs"]?.toLongOrNull() ?: 0L,
            completedAtEpochMs = map["completedAtEpochMs"]?.toLongOrNull()
        )
    }

    // --- Subject ---
    fun serializeSubject(s: Subject): String {
        return buildString {
            append("{")
            append("\"id\":${s.id},")
            append("\"name\":\"${IosJsonHelper.escapeJson(s.name)}\",")
            append("\"code\":\"${IosJsonHelper.escapeJson(s.code)}\",")
            append("\"professorName\":\"${IosJsonHelper.escapeJson(s.professorName)}\",")
            append("\"colorHex\":\"${IosJsonHelper.escapeJson(s.colorHex)}\",")
            append("\"weeklyHours\":${s.weeklyHours},")
            append("\"targetAttendancePercentage\":${s.targetAttendancePercentage},")
            append("\"currentAttendancePercentage\":${s.currentAttendancePercentage},")
            append("\"isActive\":${s.isActive}")
            append("}")
        }
    }

    fun deserializeSubject(json: String): Subject {
        val map = IosJsonHelper.parseObject(json)
        return Subject(
            id = map["id"]?.toLongOrNull() ?: 0L,
            name = map["name"] ?: "",
            code = map["code"] ?: "",
            professorName = map["professorName"] ?: "",
            colorHex = map["colorHex"] ?: "#7C4DFF",
            weeklyHours = map["weeklyHours"]?.toIntOrNull() ?: 4,
            targetAttendancePercentage = map["targetAttendancePercentage"]?.toIntOrNull() ?: 75,
            currentAttendancePercentage = map["currentAttendancePercentage"]?.toIntOrNull() ?: 100,
            isActive = map["isActive"]?.toBooleanStrictOrNull() ?: true
        )
    }

    // --- CollegeSchedule ---
    fun serializeSchedule(s: CollegeSchedule): String {
        return buildString {
            append("{")
            append("\"id\":${s.id},")
            append("\"wakeTimeMinutes\":${s.wakeTimeMinutes},")
            append("\"sleepTimeMinutes\":${s.sleepTimeMinutes},")
            append("\"collegeStartTimeMinutes\":${s.collegeStartTimeMinutes},")
            append("\"collegeEndTimeMinutes\":${s.collegeEndTimeMinutes},")
            append("\"commuteMinutes\":${s.commuteMinutes},")
            append("\"dinnerBufferMinutes\":${s.dinnerBufferMinutes},")
            append("\"breakDurationMinutes\":${s.breakDurationMinutes},")
            append("\"preferredSessionLengthMinutes\":${s.preferredSessionLengthMinutes}")
            append("}")
        }
    }

    fun deserializeSchedule(json: String): CollegeSchedule {
        val map = IosJsonHelper.parseObject(json)
        return CollegeSchedule(
            id = map["id"]?.toLongOrNull() ?: 1L,
            wakeTimeMinutes = map["wakeTimeMinutes"]?.toIntOrNull() ?: (7 * 60),
            sleepTimeMinutes = map["sleepTimeMinutes"]?.toIntOrNull() ?: (23 * 60 + 30),
            collegeStartTimeMinutes = map["collegeStartTimeMinutes"]?.toIntOrNull() ?: (9 * 60),
            collegeEndTimeMinutes = map["collegeEndTimeMinutes"]?.toIntOrNull() ?: (16 * 60 + 30),
            commuteMinutes = map["commuteMinutes"]?.toIntOrNull() ?: 45,
            dinnerBufferMinutes = map["dinnerBufferMinutes"]?.toIntOrNull() ?: 45,
            breakDurationMinutes = map["breakDurationMinutes"]?.toIntOrNull() ?: 15,
            preferredSessionLengthMinutes = map["preferredSessionLengthMinutes"]?.toIntOrNull() ?: 45
        )
    }

    // --- UserProfile ---
    fun serializeUserProfile(p: UserProfile): String {
        return buildString {
            append("{")
            append("\"id\":${p.id},")
            append("\"name\":\"${IosJsonHelper.escapeJson(p.name)}\",")
            append("\"collegeName\":\"${IosJsonHelper.escapeJson(p.collegeName)}\",")
            append("\"department\":\"${IosJsonHelper.escapeJson(p.department)}\",")
            append("\"semesterYear\":\"${IosJsonHelper.escapeJson(p.semesterYear)}\",")
            append("\"isOnboardingCompleted\":${p.isOnboardingCompleted},")
            append("\"defaultEnergyLevel\":\"${p.defaultEnergyLevel.name}\"")
            append("}")
        }
    }

    fun deserializeUserProfile(json: String): UserProfile {
        val map = IosJsonHelper.parseObject(json)
        return UserProfile(
            id = map["id"]?.toLongOrNull() ?: 1L,
            name = map["name"] ?: "Student",
            collegeName = map["collegeName"] ?: "",
            department = map["department"] ?: "",
            semesterYear = map["semesterYear"] ?: "",
            isOnboardingCompleted = map["isOnboardingCompleted"]?.toBooleanStrictOrNull() ?: false,
            defaultEnergyLevel = map["defaultEnergyLevel"]?.let { runCatching { EnergyLevel.valueOf(it) }.getOrNull() } ?: EnergyLevel.NORMAL
        )
    }

    // --- Reminder ---
    fun serializeReminder(r: Reminder): String {
        return buildString {
            append("{")
            append("\"id\":${r.id},")
            append("\"taskId\":${r.taskId ?: "null"},")
            append("\"taskTitle\":${r.taskTitle?.let { "\"${IosJsonHelper.escapeJson(it)}\"" } ?: "null"},")
            append("\"title\":\"${IosJsonHelper.escapeJson(r.title)}\",")
            append("\"message\":\"${IosJsonHelper.escapeJson(r.message)}\",")
            append("\"reminderTimeEpochMs\":${r.reminderTimeEpochMs},")
            append("\"type\":\"${r.type.name}\",")
            append("\"enabled\":${r.enabled},")
            append("\"createdAtEpochMs\":${r.createdAtEpochMs},")
            append("\"dismissedAtEpochMs\":${r.dismissedAtEpochMs ?: "null"},")
            append("\"snoozedUntilEpochMs\":${r.snoozedUntilEpochMs ?: "null"}")
            append("}")
        }
    }

    fun deserializeReminder(json: String): Reminder {
        val map = IosJsonHelper.parseObject(json)
        return Reminder(
            id = map["id"]?.toLongOrNull() ?: 0L,
            taskId = map["taskId"]?.toLongOrNull(),
            taskTitle = map["taskTitle"],
            title = map["title"] ?: "",
            message = map["message"] ?: "",
            reminderTimeEpochMs = map["reminderTimeEpochMs"]?.toLongOrNull() ?: 0L,
            type = map["type"]?.let { runCatching { ReminderType.valueOf(it) }.getOrNull() } ?: ReminderType.TASK_REMINDER,
            enabled = map["enabled"]?.toBooleanStrictOrNull() ?: true,
            createdAtEpochMs = map["createdAtEpochMs"]?.toLongOrNull() ?: 0L,
            dismissedAtEpochMs = map["dismissedAtEpochMs"]?.toLongOrNull(),
            snoozedUntilEpochMs = map["snoozedUntilEpochMs"]?.toLongOrNull()
        )
    }

    // --- AcademicNote ---
    fun serializeAcademicNote(n: AcademicNote): String {
        return buildString {
            append("{")
            append("\"id\":${n.id},")
            append("\"subjectId\":${n.subjectId ?: "null"},")
            append("\"subjectName\":${n.subjectName?.let { "\"${IosJsonHelper.escapeJson(it)}\"" } ?: "null"},")
            append("\"title\":\"${IosJsonHelper.escapeJson(n.title)}\",")
            append("\"content\":\"${IosJsonHelper.escapeJson(n.content)}\",")
            append("\"isPinned\":${n.isPinned},")
            append("\"createdEpochMs\":${n.createdEpochMs},")
            append("\"updatedEpochMs\":${n.updatedEpochMs}")
            append("}")
        }
    }

    fun deserializeAcademicNote(json: String): AcademicNote {
        val map = IosJsonHelper.parseObject(json)
        return AcademicNote(
            id = map["id"]?.toLongOrNull() ?: 0L,
            subjectId = map["subjectId"]?.toLongOrNull(),
            subjectName = map["subjectName"],
            title = map["title"] ?: "",
            content = map["content"] ?: "",
            isPinned = map["isPinned"]?.toBooleanStrictOrNull() ?: false,
            createdEpochMs = map["createdEpochMs"]?.toLongOrNull() ?: 0L,
            updatedEpochMs = map["updatedEpochMs"]?.toLongOrNull() ?: 0L
        )
    }

    // --- TemporaryInterruption ---
    fun serializeInterruption(i: TemporaryInterruption): String {
        return buildString {
            append("{")
            append("\"id\":${i.id},")
            append("\"title\":\"${IosJsonHelper.escapeJson(i.title)}\",")
            append("\"startMinutes\":${i.startMinutes},")
            append("\"endMinutes\":${i.endMinutes},")
            append("\"dateEpochDay\":${i.dateEpochDay}")
            append("}")
        }
    }

    fun deserializeInterruption(json: String): TemporaryInterruption {
        val map = IosJsonHelper.parseObject(json)
        return TemporaryInterruption(
            id = map["id"]?.toLongOrNull() ?: 0L,
            title = map["title"] ?: "",
            startMinutes = map["startMinutes"]?.toIntOrNull() ?: 0,
            endMinutes = map["endMinutes"]?.toIntOrNull() ?: 0,
            dateEpochDay = map["dateEpochDay"]?.toLongOrNull() ?: 0L
        )
    }

    // --- PlanItem ---
    fun serializePlanItem(item: PlanItem): String {
        return buildString {
            append("{")
            append("\"id\":${item.id},")
            append("\"dailyPlanId\":${item.dailyPlanId},")
            append("\"taskId\":${item.taskId ?: "null"},")
            append("\"taskTitle\":\"${IosJsonHelper.escapeJson(item.taskTitle)}\",")
            append("\"subjectName\":${item.subjectName?.let { "\"${IosJsonHelper.escapeJson(it)}\"" } ?: "null"},")
            append("\"type\":\"${item.type.name}\",")
            append("\"status\":\"${item.status.name}\",")
            append("\"startTimeMinutes\":${item.startTimeMinutes},")
            append("\"endTimeMinutes\":${item.endTimeMinutes},")
            append("\"durationMinutes\":${item.durationMinutes},")
            append("\"orderIndex\":${item.orderIndex},")
            append("\"reason\":\"${IosJsonHelper.escapeJson(item.reason)}\",")
            append("\"isBreak\":${item.isBreak}")
            append("}")
        }
    }

    fun deserializePlanItem(json: String): PlanItem {
        val map = IosJsonHelper.parseObject(json)
        return PlanItem(
            id = map["id"]?.toLongOrNull() ?: 0L,
            dailyPlanId = map["dailyPlanId"]?.toLongOrNull() ?: 0L,
            taskId = map["taskId"]?.toLongOrNull(),
            taskTitle = map["taskTitle"] ?: "",
            subjectName = map["subjectName"],
            type = map["type"]?.let { runCatching { PlanItemType.valueOf(it) }.getOrNull() } ?: PlanItemType.TASK,
            status = map["status"]?.let { runCatching { PlanItemStatus.valueOf(it) }.getOrNull() } ?: PlanItemStatus.PLANNED,
            startTimeMinutes = map["startTimeMinutes"]?.toIntOrNull() ?: 0,
            endTimeMinutes = map["endTimeMinutes"]?.toIntOrNull() ?: 0,
            durationMinutes = map["durationMinutes"]?.toIntOrNull() ?: 0,
            orderIndex = map["orderIndex"]?.toIntOrNull() ?: 0,
            reason = map["reason"] ?: "",
            isBreak = map["isBreak"]?.toBooleanStrictOrNull() ?: false
        )
    }

    // --- DailyPlan ---
    fun serializeDailyPlan(p: DailyPlan): String {
        return buildString {
            append("{")
            append("\"id\":${p.id},")
            append("\"dateEpochDay\":${p.dateEpochDay},")
            append("\"generatedAtEpochMs\":${p.generatedAtEpochMs},")
            append("\"updatedAtEpochMs\":${p.updatedAtEpochMs},")
            append("\"availableMinutes\":${p.availableMinutes},")
            append("\"realisticCapacityMinutes\":${p.realisticCapacityMinutes},")
            append("\"requiredMinutes\":${p.requiredMinutes},")
            append("\"feasibilityStatus\":\"${p.feasibilityStatus.name}\",")
            append("\"currentEnergy\":\"${p.currentEnergy.name}\",")
            append("\"items\":[${p.items.joinToString(",") { serializePlanItem(it) }}]")
            append("}")
        }
    }

    fun deserializeDailyPlan(json: String): DailyPlan {
        val map = IosJsonHelper.parseObject(json)
        val itemsJson = map["items"]
        val items = if (itemsJson != null) {
            IosJsonHelper.splitArray(itemsJson).map { deserializePlanItem(it) }
        } else {
            emptyList()
        }
        return DailyPlan(
            id = map["id"]?.toLongOrNull() ?: 0L,
            dateEpochDay = map["dateEpochDay"]?.toLongOrNull() ?: 0L,
            generatedAtEpochMs = map["generatedAtEpochMs"]?.toLongOrNull() ?: 0L,
            updatedAtEpochMs = map["updatedAtEpochMs"]?.toLongOrNull() ?: 0L,
            availableMinutes = map["availableMinutes"]?.toIntOrNull() ?: 0,
            realisticCapacityMinutes = map["realisticCapacityMinutes"]?.toIntOrNull() ?: 0,
            requiredMinutes = map["requiredMinutes"]?.toIntOrNull() ?: 0,
            feasibilityStatus = map["feasibilityStatus"]?.let { runCatching { FeasibilityStatus.valueOf(it) }.getOrNull() } ?: FeasibilityStatus.MANAGEABLE,
            currentEnergy = map["currentEnergy"]?.let { runCatching { EnergyLevel.valueOf(it) }.getOrNull() } ?: EnergyLevel.NORMAL,
            items = items
        )
    }
}

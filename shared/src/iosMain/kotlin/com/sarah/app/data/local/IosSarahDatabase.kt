package com.sarah.app.data.local

import com.sarah.app.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.*

class IosSarahDatabase private constructor() {

    companion object {
        private var INSTANCE: IosSarahDatabase? = null

        fun getInstance(): IosSarahDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: IosSarahDatabase().also { INSTANCE = it }
            }
        }
    }

    private val documentsDirectory: String by lazy {
        val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
        paths.firstOrNull() as? String ?: ""
    }

    // --- StateFlows ---
    val tasksFlow = MutableStateFlow<List<Task>>(emptyList())
    val subjectsFlow = MutableStateFlow<List<Subject>>(emptyList())
    val scheduleFlow = MutableStateFlow<CollegeSchedule?>(null)
    val userProfileFlow = MutableStateFlow<UserProfile?>(null)
    val remindersFlow = MutableStateFlow<List<Reminder>>(emptyList())
    val academicNotesFlow = MutableStateFlow<List<AcademicNote>>(emptyList())
    val dailyPlansFlow = MutableStateFlow<Map<Long, DailyPlan>>(emptyMap())
    val interruptionsFlow = MutableStateFlow<Map<Long, List<TemporaryInterruption>>>(emptyMap())

    init {
        loadOrSeedData()
    }

    private fun writeString(fileName: String, content: String) {
        if (documentsDirectory.isEmpty()) return
        val filePath = "$documentsDirectory/$fileName"
        val nsStr = NSString.create(string = content)
        val data = nsStr.dataUsingEncoding(NSUTF8StringEncoding)
        data?.writeToFile(filePath, atomically = true)
    }

    private fun readString(fileName: String): String? {
        if (documentsDirectory.isEmpty()) return null
        val filePath = "$documentsDirectory/$fileName"
        val data = NSData.dataWithContentsOfFile(filePath) ?: return null
        return NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
    }

    private fun loadOrSeedData() {
        // 1. Subjects
        val subjectsStr = readString("sarah_subjects.json")
        if (subjectsStr != null) {
            val list = IosJsonHelper.splitArray(subjectsStr).map { IosModelSerializers.deserializeSubject(it) }
            subjectsFlow.value = list
        } else {
            // Seed initial subjects
            val initialSubjects = listOf(
                Subject(id = 1, name = "Operating Systems", code = "CS301", colorHex = "#6366F1", targetAttendancePercentage = 85, currentAttendancePercentage = 82),
                Subject(id = 2, name = "Computer Networks", code = "CS302", colorHex = "#0EA5E9", targetAttendancePercentage = 80, currentAttendancePercentage = 80),
                Subject(id = 3, name = "Database Management Systems", code = "CS303", colorHex = "#10B981", targetAttendancePercentage = 75, currentAttendancePercentage = 83),
                Subject(id = 4, name = "Theory of Computation", code = "CS304", colorHex = "#F59E0B", targetAttendancePercentage = 75, currentAttendancePercentage = 78)
            )
            subjectsFlow.value = initialSubjects
            saveSubjects()
        }

        // 2. Schedule
        val scheduleStr = readString("sarah_schedule.json")
        if (scheduleStr != null) {
            scheduleFlow.value = IosModelSerializers.deserializeSchedule(scheduleStr)
        } else {
            val initialSchedule = CollegeSchedule(
                id = 1,
                wakeTimeMinutes = 7 * 60,
                sleepTimeMinutes = 23 * 60 + 30,
                collegeStartTimeMinutes = 9 * 60,
                collegeEndTimeMinutes = 16 * 60 + 30,
                commuteMinutes = 45,
                dinnerBufferMinutes = 45,
                breakDurationMinutes = 15,
                preferredSessionLengthMinutes = 45
            )
            scheduleFlow.value = initialSchedule
            saveSchedule()
        }

        // 3. UserProfile
        val profileStr = readString("sarah_user_profile.json")
        if (profileStr != null) {
            userProfileFlow.value = IosModelSerializers.deserializeUserProfile(profileStr)
        } else {
            val initialProfile = UserProfile(
                id = 1,
                name = "Student",
                collegeName = "College of Engineering",
                department = "Computer Science",
                semesterYear = "3rd Year",
                isOnboardingCompleted = false,
                defaultEnergyLevel = EnergyLevel.NORMAL
            )
            userProfileFlow.value = initialProfile
            saveUserProfile()
        }

        // 4. Tasks
        val tasksStr = readString("sarah_tasks.json")
        if (tasksStr != null) {
            tasksFlow.value = IosJsonHelper.splitArray(tasksStr).map { IosModelSerializers.deserializeTask(it) }
        }

        // 5. Reminders
        val remindersStr = readString("sarah_reminders.json")
        if (remindersStr != null) {
            remindersFlow.value = IosJsonHelper.splitArray(remindersStr).map { IosModelSerializers.deserializeReminder(it) }
        }

        // 6. Academic Notes
        val notesStr = readString("sarah_notes.json")
        if (notesStr != null) {
            academicNotesFlow.value = IosJsonHelper.splitArray(notesStr).map { IosModelSerializers.deserializeAcademicNote(it) }
        }

        // 7. Daily Plans
        val plansStr = readString("sarah_daily_plans.json")
        if (plansStr != null) {
            val plansList = IosJsonHelper.splitArray(plansStr).map { IosModelSerializers.deserializeDailyPlan(it) }
            dailyPlansFlow.value = plansList.associateBy { it.dateEpochDay }
        }

        // 8. Interruptions
        val interruptionsStr = readString("sarah_interruptions.json")
        if (interruptionsStr != null) {
            val interruptionsList = IosJsonHelper.splitArray(interruptionsStr).map { IosModelSerializers.deserializeInterruption(it) }
            interruptionsFlow.value = interruptionsList.groupBy { it.dateEpochDay }
        }
    }

    // --- Save Helpers ---
    fun saveTasks() {
        val json = "[${tasksFlow.value.joinToString(",") { IosModelSerializers.serializeTask(it) }}]"
        writeString("sarah_tasks.json", json)
    }

    fun saveSubjects() {
        val json = "[${subjectsFlow.value.joinToString(",") { IosModelSerializers.serializeSubject(it) }}]"
        writeString("sarah_subjects.json", json)
    }

    fun saveSchedule() {
        scheduleFlow.value?.let {
            writeString("sarah_schedule.json", IosModelSerializers.serializeSchedule(it))
        }
    }

    fun saveUserProfile() {
        userProfileFlow.value?.let {
            writeString("sarah_user_profile.json", IosModelSerializers.serializeUserProfile(it))
        }
    }

    fun saveReminders() {
        val json = "[${remindersFlow.value.joinToString(",") { IosModelSerializers.serializeReminder(it) }}]"
        writeString("sarah_reminders.json", json)
    }

    fun saveAcademicNotes() {
        val json = "[${academicNotesFlow.value.joinToString(",") { IosModelSerializers.serializeAcademicNote(it) }}]"
        writeString("sarah_notes.json", json)
    }

    fun saveDailyPlans() {
        val json = "[${dailyPlansFlow.value.values.joinToString(",") { IosModelSerializers.serializeDailyPlan(it) }}]"
        writeString("sarah_daily_plans.json", json)
    }

    fun saveInterruptions() {
        val all = interruptionsFlow.value.values.flatten()
        val json = "[${all.joinToString(",") { IosModelSerializers.serializeInterruption(it) }}]"
        writeString("sarah_interruptions.json", json)
    }
}

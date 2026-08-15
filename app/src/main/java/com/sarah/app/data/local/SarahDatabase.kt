package com.sarah.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sarah.app.data.local.dao.ScheduleDao
import com.sarah.app.data.local.dao.SubjectDao
import com.sarah.app.data.local.dao.TaskDao
import com.sarah.app.data.local.dao.UserProfileDao
import com.sarah.app.data.local.entity.ScheduleEntity
import com.sarah.app.data.local.entity.SubjectEntity
import com.sarah.app.data.local.entity.TaskEntity
import com.sarah.app.data.local.entity.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TaskEntity::class,
        SubjectEntity::class,
        ScheduleEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SarahDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun subjectDao(): SubjectDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: SarahDatabase? = null

        fun getInstance(context: Context): SarahDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SarahDatabase::class.java,
                    "sarah_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default schedule and initial demo subjects on first DB creation
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getInstance(context)
                                database.scheduleDao().saveSchedule(
                                    ScheduleEntity(
                                        id = 1,
                                        wakeTimeMinutes = 7 * 60, // 7:00 AM
                                        sleepTimeMinutes = 23 * 60 + 30, // 11:30 PM
                                        collegeStartTimeMinutes = 9 * 60, // 9:00 AM
                                        collegeEndTimeMinutes = 16 * 60 + 30, // 4:30 PM
                                        commuteMinutes = 45,
                                        dinnerBufferMinutes = 45,
                                        breakDurationMinutes = 15,
                                        preferredSessionLengthMinutes = 45
                                    )
                                )
                                database.userProfileDao().saveUserProfile(
                                    UserProfileEntity(
                                        id = 1,
                                        name = "Student",
                                        collegeName = "College of Engineering",
                                        department = "Computer Science",
                                        semesterYear = "3rd Year",
                                        isOnboardingCompleted = false,
                                        defaultEnergyLevel = "NORMAL"
                                    )
                                )
                                // Add initial subjects
                                val s1 = database.subjectDao().insertSubject(
                                    SubjectEntity(
                                        id = 0,
                                        name = "Java & OOP",
                                        code = "CS301",
                                        professorName = "Prof. Sharma",
                                        colorHex = "#7C4DFF",
                                        weeklyHours = 4,
                                        targetAttendancePercentage = 75,
                                        currentAttendancePercentage = 88,
                                        isActive = true
                                    )
                                )
                                val s2 = database.subjectDao().insertSubject(
                                    SubjectEntity(
                                        id = 0,
                                        name = "Database Management",
                                        code = "CS302",
                                        professorName = "Dr. Rao",
                                        colorHex = "#3B82F6",
                                        weeklyHours = 4,
                                        targetAttendancePercentage = 75,
                                        currentAttendancePercentage = 78,
                                        isActive = true
                                    )
                                )
                                val s3 = database.subjectDao().insertSubject(
                                    SubjectEntity(
                                        id = 0,
                                        name = "Operating Systems",
                                        code = "CS303",
                                        professorName = "Prof. Gupta",
                                        colorHex = "#10B981",
                                        weeklyHours = 3,
                                        targetAttendancePercentage = 75,
                                        currentAttendancePercentage = 92,
                                        isActive = true
                                    )
                                )

                                // Add sample college tasks
                                val now = System.currentTimeMillis()
                                val tomorrow = now + (24 * 60 * 60 * 1000)
                                val inTwoDays = now + (48 * 60 * 60 * 1000)

                                database.taskDao().insertTask(
                                    TaskEntity(
                                        id = 0,
                                        title = "Java Practical (Programs 1-5)",
                                        subjectId = s1,
                                        subjectName = "Java & OOP",
                                        type = "PRACTICAL",
                                        description = "Complete inheritance and interface programs for lab manual",
                                        deadlineEpochMs = tomorrow,
                                        estimatedMinutes = 50,
                                        priority = "CRITICAL",
                                        difficulty = "MEDIUM",
                                        energyRequirement = "HIGH",
                                        status = "PENDING",
                                        completionPercentage = 0,
                                        createdAtEpochMs = now,
                                        completedAtEpochMs = null
                                    )
                                )
                                database.taskDao().insertTask(
                                    TaskEntity(
                                        id = 0,
                                        title = "DBMS Normalization Assignment",
                                        subjectId = s2,
                                        subjectName = "Database Management",
                                        type = "ASSIGNMENT",
                                        description = "Solve 2NF and 3NF decomposition questions from Unit 2",
                                        deadlineEpochMs = tomorrow,
                                        estimatedMinutes = 45,
                                        priority = "HIGH",
                                        difficulty = "MEDIUM",
                                        energyRequirement = "MEDIUM",
                                        status = "PENDING",
                                        completionPercentage = 0,
                                        createdAtEpochMs = now,
                                        completedAtEpochMs = null
                                    )
                                )
                                database.taskDao().insertTask(
                                    TaskEntity(
                                        id = 0,
                                        title = "Revise OS Process Scheduling",
                                        subjectId = s3,
                                        subjectName = "Operating Systems",
                                        type = "REVISION",
                                        description = "Review Round Robin and SJF algorithms before upcoming quiz",
                                        deadlineEpochMs = inTwoDays,
                                        estimatedMinutes = 30,
                                        priority = "MEDIUM",
                                        difficulty = "EASY",
                                        energyRequirement = "LOW",
                                        status = "PENDING",
                                        completionPercentage = 0,
                                        createdAtEpochMs = now,
                                        completedAtEpochMs = null
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

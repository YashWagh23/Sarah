package com.sarah.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sarah.app.data.local.dao.DailyPlanDao
import com.sarah.app.data.local.dao.ScheduleDao
import com.sarah.app.data.local.dao.SubjectDao
import com.sarah.app.data.local.dao.TaskDao
import com.sarah.app.data.local.dao.TemporaryInterruptionDao
import com.sarah.app.data.local.dao.UserProfileDao
import com.sarah.app.data.local.entity.DailyPlanEntity
import com.sarah.app.data.local.entity.PlanItemEntity
import com.sarah.app.data.local.entity.ScheduleEntity
import com.sarah.app.data.local.entity.SubjectEntity
import com.sarah.app.data.local.entity.TaskEntity
import com.sarah.app.data.local.entity.TemporaryInterruptionEntity
import com.sarah.app.data.local.dao.AcademicNoteDao
import com.sarah.app.data.local.dao.ReminderDao
import com.sarah.app.data.local.entity.AcademicNoteEntity
import com.sarah.app.data.local.entity.ReminderEntity
import com.sarah.app.data.local.entity.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TaskEntity::class,
        SubjectEntity::class,
        ScheduleEntity::class,
        UserProfileEntity::class,
        DailyPlanEntity::class,
        PlanItemEntity::class,
        TemporaryInterruptionEntity::class,
        ReminderEntity::class,
        AcademicNoteEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class SarahDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun subjectDao(): SubjectDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun dailyPlanDao(): DailyPlanDao
    abstract fun temporaryInterruptionDao(): TemporaryInterruptionDao
    abstract fun reminderDao(): ReminderDao
    abstract fun academicNoteDao(): AcademicNoteDao

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
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Initialize default schedule and profile deterministically on the active SQLite transaction
                            try {
                                db.execSQL(
                                    """
                                    INSERT OR IGNORE INTO schedule (
                                        id, wakeTimeMinutes, sleepTimeMinutes, collegeStartTimeMinutes, 
                                        collegeEndTimeMinutes, commuteMinutes, dinnerBufferMinutes, 
                                        breakDurationMinutes, preferredSessionLengthMinutes
                                    ) VALUES (1, 420, 1410, 540, 990, 45, 45, 15, 45)
                                    """.trimIndent()
                                )
                                db.execSQL(
                                    """
                                    INSERT OR IGNORE INTO user_profile (
                                        id, name, collegeName, department, semesterYear, 
                                        isOnboardingCompleted, defaultEnergyLevel
                                    ) VALUES (1, 'Student', 'College of Engineering', 'Computer Science', '3rd Year', 0, 'NORMAL')
                                    """.trimIndent()
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
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

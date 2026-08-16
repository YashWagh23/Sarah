package com.sarah.app

import android.app.Application
import com.sarah.app.data.local.SarahDatabase
import com.sarah.app.data.preferences.SarahPreferencesManager
import com.sarah.app.data.repository.DailyPlanRepositoryImpl
import com.sarah.app.data.repository.ScheduleRepositoryImpl
import com.sarah.app.data.repository.SubjectRepositoryImpl
import com.sarah.app.data.repository.TaskRepositoryImpl
import com.sarah.app.data.repository.UserRepositoryImpl
import com.sarah.app.domain.engine.AdaptivePlanner
import com.sarah.app.domain.engine.DeadlineReminderEngine
import com.sarah.app.domain.engine.DocumentTextExtractor
import com.sarah.app.domain.engine.FeasibilityEngine
import com.sarah.app.domain.engine.NaturalLanguageTaskParser
import com.sarah.app.domain.engine.NextActionEngine
import com.sarah.app.domain.engine.TaskPriorityScorer
import com.sarah.app.domain.repository.DailyPlanRepository
import com.sarah.app.domain.repository.ScheduleRepository
import com.sarah.app.domain.repository.SubjectRepository
import com.sarah.app.domain.repository.TaskRepository
import com.sarah.app.domain.repository.UserRepository

import com.sarah.app.data.repository.AcademicNoteRepositoryImpl
import com.sarah.app.data.repository.ReminderRepositoryImpl
import com.sarah.app.domain.engine.ReminderScheduler
import com.sarah.app.domain.repository.AcademicNoteRepository
import com.sarah.app.domain.repository.ReminderRepository
import com.sarah.app.notification.NotificationHelper
import com.sarah.app.notification.SarahNotificationScheduler

class SarahApp : Application() {

    lateinit var database: SarahDatabase private set
    lateinit var preferencesManager: SarahPreferencesManager private set

    lateinit var taskRepository: TaskRepository private set
    lateinit var subjectRepository: SubjectRepository private set
    lateinit var scheduleRepository: ScheduleRepository private set
    lateinit var userRepository: UserRepository private set
    lateinit var dailyPlanRepository: DailyPlanRepository private set
    lateinit var reminderRepository: ReminderRepository private set
    lateinit var reminderScheduler: ReminderScheduler private set
    lateinit var academicNoteRepository: AcademicNoteRepository private set

    val taskPriorityScorer: TaskPriorityScorer by lazy { TaskPriorityScorer() }
    val adaptivePlanner: AdaptivePlanner by lazy { AdaptivePlanner(taskPriorityScorer) }
    val nextActionEngine: NextActionEngine by lazy { NextActionEngine() }
    val feasibilityEngine: FeasibilityEngine by lazy { FeasibilityEngine() }
    val naturalLanguageTaskParser: NaturalLanguageTaskParser by lazy { NaturalLanguageTaskParser() }
    val documentTextExtractor: DocumentTextExtractor by lazy { DocumentTextExtractor() }
    val deadlineReminderEngine: DeadlineReminderEngine by lazy { DeadlineReminderEngine() }

    override fun onCreate() {
        super.onCreate()
        database = SarahDatabase.getInstance(this)
        preferencesManager = SarahPreferencesManager(this)

        taskRepository = TaskRepositoryImpl(database.taskDao())
        subjectRepository = SubjectRepositoryImpl(database.subjectDao())
        scheduleRepository = ScheduleRepositoryImpl(database.scheduleDao())
        userRepository = UserRepositoryImpl(database.userProfileDao())
        dailyPlanRepository = DailyPlanRepositoryImpl(database.dailyPlanDao(), database.temporaryInterruptionDao())
        reminderRepository = ReminderRepositoryImpl(database.reminderDao())
        reminderScheduler = SarahNotificationScheduler(this)
        academicNoteRepository = AcademicNoteRepositoryImpl(database.academicNoteDao(), database.subjectDao())

        NotificationHelper.createNotificationChannel(this)
    }
}

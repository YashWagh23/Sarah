package com.sarah.app

import android.app.Application
import com.sarah.app.data.local.SarahDatabase
import com.sarah.app.data.preferences.SarahPreferencesManager
import com.sarah.app.data.repository.AcademicNoteRepositoryImpl
import com.sarah.app.data.repository.DailyPlanRepositoryImpl
import com.sarah.app.data.repository.ReminderRepositoryImpl
import com.sarah.app.data.repository.ScheduleRepositoryImpl
import com.sarah.app.data.repository.SubjectRepositoryImpl
import com.sarah.app.data.repository.TaskRepositoryImpl
import com.sarah.app.data.repository.UserRepositoryImpl
import com.sarah.app.di.SarahAppContainer
import com.sarah.app.domain.engine.AdaptivePlanner
import com.sarah.app.domain.engine.DeadlineReminderEngine
import com.sarah.app.domain.engine.DocumentTextExtractor
import com.sarah.app.domain.engine.FeasibilityEngine
import com.sarah.app.domain.engine.NaturalLanguageTaskParser
import com.sarah.app.domain.engine.NextActionEngine
import com.sarah.app.domain.engine.ReminderScheduler
import com.sarah.app.domain.engine.TaskPriorityScorer
import com.sarah.app.domain.preferences.SarahPreferences
import com.sarah.app.domain.repository.AcademicNoteRepository
import com.sarah.app.domain.repository.DailyPlanRepository
import com.sarah.app.domain.repository.ReminderRepository
import com.sarah.app.domain.repository.ScheduleRepository
import com.sarah.app.domain.repository.SubjectRepository
import com.sarah.app.domain.repository.TaskRepository
import com.sarah.app.domain.repository.UserRepository
import com.sarah.app.notification.NotificationHelper
import com.sarah.app.notification.SarahNotificationScheduler

class SarahApp : Application(), SarahAppContainer {

    lateinit var database: SarahDatabase private set
    lateinit var preferencesManager: SarahPreferencesManager private set

    override lateinit var taskRepository: TaskRepository private set
    override lateinit var subjectRepository: SubjectRepository private set
    override lateinit var scheduleRepository: ScheduleRepository private set
    override lateinit var userRepository: UserRepository private set
    override lateinit var dailyPlanRepository: DailyPlanRepository private set
    override lateinit var reminderRepository: ReminderRepository private set
    override lateinit var reminderScheduler: ReminderScheduler private set
    override lateinit var academicNoteRepository: AcademicNoteRepository private set

    override val preferences: SarahPreferences
        get() = preferencesManager

    override val taskPriorityScorer: TaskPriorityScorer by lazy { TaskPriorityScorer() }
    override val adaptivePlanner: AdaptivePlanner by lazy { AdaptivePlanner(taskPriorityScorer) }
    override val nextActionEngine: NextActionEngine by lazy { NextActionEngine() }
    override val feasibilityEngine: FeasibilityEngine by lazy { FeasibilityEngine() }
    override val naturalLanguageTaskParser: NaturalLanguageTaskParser by lazy { NaturalLanguageTaskParser() }
    override val documentTextExtractor: DocumentTextExtractor by lazy { DocumentTextExtractor() }
    override val deadlineReminderEngine: DeadlineReminderEngine by lazy { DeadlineReminderEngine() }

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

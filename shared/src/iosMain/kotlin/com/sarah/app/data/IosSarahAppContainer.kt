package com.sarah.app.data

import com.sarah.app.data.local.IosSarahDatabase
import com.sarah.app.data.repository.*
import com.sarah.app.di.DefaultSarahAppContainer
import com.sarah.app.engine.IosDocumentTextExtractor
import com.sarah.app.notification.IosReminderScheduler
import com.sarah.app.preferences.IosSarahPreferences

class IosSarahAppContainer(
    val database: IosSarahDatabase = IosSarahDatabase.getInstance()
) : DefaultSarahAppContainer(
    taskRepository = IosTaskRepository(database),
    subjectRepository = IosSubjectRepository(database),
    scheduleRepository = IosScheduleRepository(database),
    userRepository = IosUserRepository(database),
    dailyPlanRepository = IosDailyPlanRepository(database),
    reminderRepository = IosReminderRepository(database),
    academicNoteRepository = IosAcademicNoteRepository(database),
    preferences = IosSarahPreferences(),
    reminderScheduler = IosReminderScheduler(),
    documentTextExtractor = IosDocumentTextExtractor()
)

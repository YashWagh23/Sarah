package com.sarah.app.di

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

interface SarahAppContainer {
    val taskRepository: TaskRepository
    val subjectRepository: SubjectRepository
    val scheduleRepository: ScheduleRepository
    val userRepository: UserRepository
    val dailyPlanRepository: DailyPlanRepository
    val reminderRepository: ReminderRepository
    val academicNoteRepository: AcademicNoteRepository

    val preferences: SarahPreferences
    val reminderScheduler: ReminderScheduler
    val documentTextExtractor: DocumentTextExtractor

    val taskPriorityScorer: TaskPriorityScorer
    val adaptivePlanner: AdaptivePlanner
    val nextActionEngine: NextActionEngine
    val feasibilityEngine: FeasibilityEngine
    val naturalLanguageTaskParser: NaturalLanguageTaskParser
    val deadlineReminderEngine: DeadlineReminderEngine
}

open class DefaultSarahAppContainer(
    override val taskRepository: TaskRepository,
    override val subjectRepository: SubjectRepository,
    override val scheduleRepository: ScheduleRepository,
    override val userRepository: UserRepository,
    override val dailyPlanRepository: DailyPlanRepository,
    override val reminderRepository: ReminderRepository,
    override val academicNoteRepository: AcademicNoteRepository,
    override val preferences: SarahPreferences,
    override val reminderScheduler: ReminderScheduler,
    override val documentTextExtractor: DocumentTextExtractor,
    override val taskPriorityScorer: TaskPriorityScorer = TaskPriorityScorer(),
    override val adaptivePlanner: AdaptivePlanner = AdaptivePlanner(taskPriorityScorer),
    override val nextActionEngine: NextActionEngine = NextActionEngine(),
    override val feasibilityEngine: FeasibilityEngine = FeasibilityEngine(),
    override val naturalLanguageTaskParser: NaturalLanguageTaskParser = NaturalLanguageTaskParser(),
    override val deadlineReminderEngine: DeadlineReminderEngine = DeadlineReminderEngine()
) : SarahAppContainer

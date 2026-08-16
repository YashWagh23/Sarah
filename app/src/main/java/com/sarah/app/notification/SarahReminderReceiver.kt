package com.sarah.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sarah.app.SarahApp
import com.sarah.app.domain.model.ReminderType
import com.sarah.app.domain.model.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SarahReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as? SarahApp ?: return

        when (intent.action) {
            NotificationHelper.ACTION_SHOW_REMINDER -> {
                val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)
                if (reminderId == -1L) return

                val reminderTypeName = intent.getStringExtra(NotificationHelper.EXTRA_REMINDER_TYPE)
                val reminderType = runCatching {
                    ReminderType.valueOf(reminderTypeName ?: "")
                }.getOrDefault(ReminderType.TASK_REMINDER)

                // Check settings
                if (reminderType == ReminderType.DEADLINE_REMINDER && !app.preferencesManager.isDeadlineRemindersEnabled) {
                    return
                }
                if (reminderType == ReminderType.CUSTOM_REMINDER && !app.preferencesManager.isCustomRemindersEnabled) {
                    return
                }

                CoroutineScope(Dispatchers.IO).launch {
                    val reminderEntity = app.database.reminderDao().getReminderById(reminderId)
                    if (reminderEntity != null && reminderEntity.enabled && reminderEntity.dismissedAtEpochMs == null) {
                        // Check if linked task is completed or deleted
                        if (reminderEntity.taskId != null) {
                            val task = app.database.taskDao().getTaskById(reminderEntity.taskId)
                            if (task == null || task.status == TaskStatus.COMPLETED.name) {
                                // Task is completed or removed -> dismiss and cancel
                                app.database.reminderDao().dismissReminder(reminderId, System.currentTimeMillis())
                                return@launch
                            }
                        }
                        NotificationHelper.showReminderNotification(context, reminderEntity.toDomain())
                    }
                }
            }

            NotificationHelper.ACTION_TASK_DONE -> {
                val taskId = intent.getLongExtra(NotificationHelper.EXTRA_TASK_ID, -1L)
                val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)

                NotificationHelper.cancelNotification(context, reminderId.toInt())

                if (taskId != -1L) {
                    CoroutineScope(Dispatchers.IO).launch {
                        app.taskRepository.updateTaskStatus(taskId, TaskStatus.COMPLETED)
                        app.reminderRepository.deleteRemindersByTaskId(taskId)
                        app.reminderScheduler.cancelTaskReminders(taskId)
                    }
                } else if (reminderId != -1L) {
                    CoroutineScope(Dispatchers.IO).launch {
                        app.reminderRepository.dismissReminder(reminderId)
                    }
                }
            }

            NotificationHelper.ACTION_REMINDER_SNOOZE_10M -> {
                val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)
                if (reminderId == -1L) return

                NotificationHelper.cancelNotification(context, reminderId.toInt())

                CoroutineScope(Dispatchers.IO).launch {
                    app.reminderRepository.snoozeReminder(reminderId, 10)
                    val updated = app.reminderRepository.getReminderById(reminderId)
                    if (updated != null) {
                        app.reminderScheduler.scheduleReminder(updated)
                    }
                }
            }

            NotificationHelper.ACTION_REMINDER_SNOOZE_30M -> {
                val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)
                if (reminderId == -1L) return

                NotificationHelper.cancelNotification(context, reminderId.toInt())

                CoroutineScope(Dispatchers.IO).launch {
                    app.reminderRepository.snoozeReminder(reminderId, 30)
                    val updated = app.reminderRepository.getReminderById(reminderId)
                    if (updated != null) {
                        app.reminderScheduler.scheduleReminder(updated)
                    }
                }
            }
        }
    }
}

package com.sarah.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.sarah.app.SarahApp
import com.sarah.app.domain.engine.ReminderScheduler
import com.sarah.app.domain.model.Reminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SarahNotificationScheduler(
    private val context: Context
) : ReminderScheduler {

    private val alarmManager: AlarmManager? = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    override fun scheduleReminder(reminder: Reminder) {
        if (alarmManager == null) return
        val now = System.currentTimeMillis()
        if (reminder.reminderTimeEpochMs <= now || !reminder.enabled || reminder.dismissedAtEpochMs != null) {
            return
        }

        val intent = Intent(context, SarahReminderReceiver::class.java).apply {
            action = NotificationHelper.ACTION_SHOW_REMINDER
            putExtra(NotificationHelper.EXTRA_REMINDER_ID, reminder.id)
            putExtra(NotificationHelper.EXTRA_TASK_ID, reminder.taskId)
            putExtra(NotificationHelper.EXTRA_REMINDER_TITLE, reminder.title)
            putExtra(NotificationHelper.EXTRA_REMINDER_MESSAGE, reminder.message)
            putExtra(NotificationHelper.EXTRA_REMINDER_TYPE, reminder.type.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminder.reminderTimeEpochMs,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminder.reminderTimeEpochMs,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.reminderTimeEpochMs,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminder.reminderTimeEpochMs,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // Fallback for strict background alarm policies
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                reminder.reminderTimeEpochMs,
                pendingIntent
            )
        }
    }

    override fun cancelReminder(reminderId: Long) {
        if (alarmManager == null) return
        val intent = Intent(context, SarahReminderReceiver::class.java).apply {
            action = NotificationHelper.ACTION_SHOW_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
        NotificationHelper.cancelNotification(context, reminderId.toInt())
    }

    override fun cancelTaskReminders(taskId: Long) {
        val app = context.applicationContext as? SarahApp ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val reminders = app.database.reminderDao().getRemindersForTaskSync(taskId)
            reminders.forEach { reminder ->
                cancelReminder(reminder.id)
            }
        }
    }

    override fun rescheduleReminder(reminderId: Long, newTimeEpochMs: Long) {
        cancelReminder(reminderId)
        val app = context.applicationContext as? SarahApp ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val entity = app.database.reminderDao().getReminderById(reminderId)
            if (entity != null) {
                val updated = entity.copy(reminderTimeEpochMs = newTimeEpochMs, enabled = true, dismissedAtEpochMs = null)
                app.database.reminderDao().updateReminder(updated)
                scheduleReminder(updated.toDomain())
            }
        }
    }

    override fun rescheduleAllActiveReminders() {
        val app = context.applicationContext as? SarahApp ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val now = System.currentTimeMillis()
            val pendingEntities = app.database.reminderDao().getUpcomingPendingRemindersSync(now)
            pendingEntities.forEach { entity ->
                scheduleReminder(entity.toDomain())
            }
        }
    }
}

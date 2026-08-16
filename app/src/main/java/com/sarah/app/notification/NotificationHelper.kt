package com.sarah.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sarah.app.MainActivity
import com.sarah.app.R
import com.sarah.app.domain.model.Reminder
import com.sarah.app.domain.model.ReminderType

object NotificationHelper {

    const val CHANNEL_ID = "sarah_academic_reminders"
    const val CHANNEL_NAME = "Academic Reminders & Deadlines"
    const val CHANNEL_DESCRIPTION = "Punctual alerts for college assignments, exams, practicals, and academic tasks"

    const val ACTION_SHOW_REMINDER = "com.sarah.app.ACTION_SHOW_REMINDER"
    const val ACTION_TASK_DONE = "com.sarah.app.ACTION_TASK_DONE"
    const val ACTION_REMINDER_SNOOZE_10M = "com.sarah.app.ACTION_REMINDER_SNOOZE_10M"
    const val ACTION_REMINDER_SNOOZE_30M = "com.sarah.app.ACTION_REMINDER_SNOOZE_30M"

    const val EXTRA_REMINDER_ID = "extra_reminder_id"
    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_REMINDER_TITLE = "extra_reminder_title"
    const val EXTRA_REMINDER_MESSAGE = "extra_reminder_message"
    const val EXTRA_REMINDER_TYPE = "extra_reminder_type"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showReminderNotification(context: Context, reminder: Reminder) {
        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) {
            return
        }

        // Tap content intent -> Opens Sarah MainActivity
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            reminder.taskId?.let { putExtra(EXTRA_TASK_ID, it) }
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            reminder.id.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val headerSubText = when (reminder.type) {
            ReminderType.DEADLINE_REMINDER -> "Upcoming Deadline"
            ReminderType.TASK_REMINDER -> "Study Reminder"
            ReminderType.CUSTOM_REMINDER -> "Sarah Reminder"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(reminder.title)
            .setContentText(reminder.message)
            .setSubText(headerSubText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(reminder.message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        // Action 1: "Done" if linked to a task
        if (reminder.taskId != null) {
            val doneIntent = Intent(context, SarahReminderReceiver::class.java).apply {
                action = ACTION_TASK_DONE
                putExtra(EXTRA_REMINDER_ID, reminder.id)
                putExtra(EXTRA_TASK_ID, reminder.taskId)
            }
            val donePendingIntent = PendingIntent.getBroadcast(
                context,
                (reminder.id * 10 + 1).toInt(),
                doneIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.checkbox_on_background,
                "Done",
                donePendingIntent
            )
        }

        // Action 2: "Snooze 10m"
        val snoozeIntent = Intent(context, SarahReminderReceiver::class.java).apply {
            action = ACTION_REMINDER_SNOOZE_10M
            putExtra(EXTRA_REMINDER_ID, reminder.id)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (reminder.id * 10 + 2).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(
            android.R.drawable.ic_popup_sync,
            "Snooze 10m",
            snoozePendingIntent
        )

        try {
            notificationManager.notify(reminder.id.toInt(), builder.build())
        } catch (_: SecurityException) {
            // Notification permission revoked
        }
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(notificationId)
    }
}

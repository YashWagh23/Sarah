package com.sarah.app.notification

import com.sarah.app.domain.engine.ReminderScheduler
import com.sarah.app.domain.model.Reminder
import com.sarah.app.domain.util.currentTimeEpochMs
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

/**
 * iOS implementation of ReminderScheduler using UserNotifications framework.
 *
 * Privacy-first compliance:
 * - Only requests Alert, Sound, and Badge permissions.
 * - Manages exact on-device local notifications without any remote analytics or background tracking.
 */
class IosReminderScheduler : ReminderScheduler {

    private val center = UNUserNotificationCenter.currentNotificationCenter()

    init {
        requestNotificationPermission()
    }

    fun requestNotificationPermission(onComplete: ((Boolean) -> Unit)? = null) {
        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        center.requestAuthorizationWithOptions(options) { granted, error ->
            onComplete?.invoke(granted && error == null)
        }
    }

    override fun scheduleReminder(reminder: Reminder) {
        val now = currentTimeEpochMs()
        if (reminder.reminderTimeEpochMs <= now || !reminder.enabled || reminder.dismissedAtEpochMs != null) {
            return
        }

        val secondsUntil = (reminder.reminderTimeEpochMs - now) / 1000.0
        if (secondsUntil <= 0.0) return

        val content = UNMutableNotificationContent().apply {
            setTitle(reminder.title)
            setBody(reminder.message.ifBlank { "Reminder from Sarah" })
            setSound(UNNotificationSound.defaultSound())
            val userInfoMap = mutableMapOf<Any?, Any>()
            userInfoMap["reminderId"] = reminder.id.toString()
            reminder.taskId?.let { userInfoMap["taskId"] = it.toString() }
            setUserInfo(userInfoMap)
        }

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            timeInterval = secondsUntil,
            repeats = false
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "sarah_reminder_${reminder.id}",
            content = content,
            trigger = trigger
        )

        center.addNotificationRequest(request) { error ->
            // Notification registered with iOS system scheduler
        }
    }

    override fun cancelReminder(reminderId: Long) {
        val identifier = "sarah_reminder_$reminderId"
        center.removePendingNotificationRequestsWithIdentifiers(listOf(identifier))
        center.removeDeliveredNotificationsWithIdentifiers(listOf(identifier))
    }

    override fun cancelTaskReminders(taskId: Long) {
        center.getPendingNotificationRequestsWithCompletionHandler { requests ->
            val toRemove = mutableListOf<String>()
            requests?.forEach { req ->
                val request = req as? UNNotificationRequest ?: return@forEach
                val linkedTaskId = request.content.userInfo["taskId"] as? String
                if (linkedTaskId == taskId.toString()) {
                    toRemove.add(request.identifier)
                }
            }
            if (toRemove.isNotEmpty()) {
                center.removePendingNotificationRequestsWithIdentifiers(toRemove)
            }
        }
    }

    override fun rescheduleReminder(reminderId: Long, newTimeEpochMs: Long) {
        cancelReminder(reminderId)
    }

    override fun rescheduleAllActiveReminders() {
        // Will be called by repositories or container when reminders are updated
    }
}

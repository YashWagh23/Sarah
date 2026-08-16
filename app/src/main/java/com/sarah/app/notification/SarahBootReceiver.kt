package com.sarah.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sarah.app.SarahApp

class SarahBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val app = context.applicationContext as? SarahApp ?: return
            app.reminderScheduler.rescheduleAllActiveReminders()
        }
    }
}

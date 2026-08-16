package com.sarah.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sarah.app.domain.model.Reminder
import com.sarah.app.domain.model.ReminderType
import com.sarah.app.domain.util.formatReminderTime
import com.sarah.app.ui.theme.SarahError
import com.sarah.app.ui.theme.SarahOnSurface
import com.sarah.app.ui.theme.SarahOnSurfaceVariant
import com.sarah.app.ui.theme.SarahOutline
import com.sarah.app.ui.theme.SarahPrimary
import com.sarah.app.ui.theme.SarahPrimaryFixedDim
import com.sarah.app.ui.theme.SarahSecondary
import com.sarah.app.ui.theme.SarahSurfaceContainerHigh
import com.sarah.app.ui.theme.SarahSurfaceContainerLowest
import com.sarah.app.ui.theme.SarahTertiary
import com.sarah.app.ui.theme.SarahTertiaryFixedDim

/**
 * Compact mini reminder card — designed for a 2-column grid on the Today screen.
 * Matches the reference design: white card, top-right decorative circle, icon, title, time.
 */
@Composable
fun ReminderCard(
    reminder      : Reminder,
    onSnoozeClick : (Reminder) -> Unit,
    onDismissClick: (Reminder) -> Unit,
    onDeleteClick : (Reminder) -> Unit,
    modifier      : Modifier = Modifier
) {
    val formattedTime = formatReminderTime(reminder.reminderTimeEpochMs)

    // Icon and accent color by reminder type
    val (icon, iconTint, decorTint) = when (reminder.type) {
        ReminderType.DEADLINE_REMINDER -> Triple(Icons.Outlined.Warning,       SarahError,    SarahError.copy(alpha = 0.15f))
        ReminderType.TASK_REMINDER     -> Triple(Icons.Outlined.Schedule,      SarahPrimary,  SarahPrimaryFixedDim.copy(alpha = 0.2f))
        ReminderType.CUSTOM_REMINDER   -> Triple(Icons.Outlined.Notifications, SarahTertiary, SarahTertiaryFixedDim.copy(alpha = 0.2f))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SarahSurfaceContainerLowest)
            .border(1.dp, Color.Black.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
    ) {
        // Decorative quarter-circle in top-right (reference design accent)
        Box(
            modifier = Modifier
                .size(64.dp)
                .offset(x = 16.dp, y = (-16).dp)
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .background(decorTint)
        )

        Column(
            modifier            = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Icon
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = iconTint,
                modifier           = Modifier.size(22.dp)
            )

            // Reminder title
            Text(
                text      = reminder.title,
                style     = MaterialTheme.typography.titleSmall,
                color     = SarahOnSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines  = 2,
                overflow  = TextOverflow.Ellipsis
            )

            // Time
            Text(
                text  = if (reminder.isSnoozed) "$formattedTime · Snoozed" else formattedTime,
                style = MaterialTheme.typography.labelSmall,
                color = SarahOnSurfaceVariant
            )

            // Action row: Snooze | Done/Dismiss
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Snooze
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SarahSurfaceContainerHigh)
                        .clickable { onSnoozeClick(reminder) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Snooze,
                        contentDescription = "Snooze",
                        tint               = SarahSecondary,
                        modifier           = Modifier.size(14.dp)
                    )
                }
                // Dismiss / Done
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SarahPrimary.copy(alpha = 0.08f))
                        .clickable { onDismissClick(reminder) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.CheckCircle,
                        contentDescription = if (reminder.taskId != null) "Done" else "Dismiss",
                        tint               = SarahPrimary,
                        modifier           = Modifier.size(14.dp)
                    )
                }
                // Delete
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SarahSurfaceContainerHigh)
                        .clickable { onDeleteClick(reminder) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint               = SarahOutline,
                        modifier           = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

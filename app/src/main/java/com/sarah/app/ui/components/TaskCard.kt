package com.sarah.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Timer
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskPriority
import com.sarah.app.domain.model.TaskStatus
import com.sarah.app.ui.theme.SarahError
import com.sarah.app.ui.theme.SarahOnSurface
import com.sarah.app.ui.theme.SarahOnSurfaceVariant
import com.sarah.app.ui.theme.SarahOutline
import com.sarah.app.ui.theme.SarahOutlineVariant
import com.sarah.app.ui.theme.SarahPrimary
import com.sarah.app.ui.theme.SarahSecondary
import com.sarah.app.ui.theme.SarahSurfaceContainer
import com.sarah.app.ui.theme.SarahSurfaceContainerLowest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Reference-style task row:
 * [checkbox] [title] [subject chip · date chip · time chip]
 *
 * Active: white background, outlined checkbox
 * Completed: slightly muted, filled primary checkbox, strikethrough title
 */
@Composable
fun TaskCard(
    task          : Task,
    onStatusToggle: (Task) -> Unit,
    onClick       : () -> Unit = {},
    modifier      : Modifier = Modifier
) {
    val isCompleted = task.status == TaskStatus.COMPLETED
    val zone        = ZoneId.systemDefault()

    val dueDateFormatted = task.deadlineEpochMs?.let { ms ->
        val dueDate = Instant.ofEpochMilli(ms).atZone(zone).toLocalDate()
        val today   = LocalDate.now(zone)
        when {
            dueDate == today             -> "Today"
            dueDate == today.plusDays(1) -> "Tomorrow"
            dueDate.isBefore(today)      -> "Overdue"
            else                         -> dueDate.format(DateTimeFormatter.ofPattern("MMM d"))
        }
    }
    val isOverdue = task.deadlineEpochMs?.let {
        Instant.ofEpochMilli(it).atZone(zone).toLocalDate().isBefore(LocalDate.now(zone))
    } == true

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isCompleted) SarahSurfaceContainerLowest.copy(alpha = 0.7f) else SarahSurfaceContainerLowest)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── Circular checkbox ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .then(
                    if (isCompleted) Modifier.background(SarahPrimary)
                    else Modifier
                        .background(Color.Transparent)
                        .border(2.dp, SarahOutline, CircleShape)
                )
                .clickable { onStatusToggle(task) },
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector        = Icons.Outlined.Check,
                    contentDescription = "Completed",
                    tint               = Color.White,
                    modifier           = Modifier.size(14.dp)
                )
            }
        }

        // ── Task content ─────────────────────────────────────────────────────
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // Title
            Text(
                text            = task.title,
                style           = MaterialTheme.typography.bodyLarge,
                color           = if (isCompleted) SarahSecondary else SarahOnSurface,
                fontWeight      = FontWeight.Normal,
                textDecoration  = if (isCompleted) TextDecoration.LineThrough else null,
                maxLines        = 2
            )

            // Metadata chips row
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Subject chip
                task.subjectName?.let { subject ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SarahSurfaceContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text  = subject,
                            style = MaterialTheme.typography.labelSmall,
                            color = SarahOnSurfaceVariant
                        )
                    }
                }

                // Due date chip
                dueDateFormatted?.let { date ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Icon(
                            imageVector        = if (isOverdue) Icons.Outlined.Warning else Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint               = if (isOverdue) SarahError else SarahSecondary,
                            modifier           = Modifier.size(12.dp)
                        )
                        Text(
                            text  = date,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOverdue) SarahError else SarahSecondary
                        )
                    }
                }

                // Duration chip
                if (task.estimatedMinutes > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Icon(
                            imageVector        = Icons.Outlined.Timer,
                            contentDescription = null,
                            tint               = if (task.subjectName != null) SarahPrimary else SarahSecondary,
                            modifier           = Modifier.size(12.dp)
                        )
                        Text(
                            text  = "${task.estimatedMinutes} min",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (task.subjectName != null) SarahPrimary else SarahSecondary
                        )
                    }
                }
            }
        }
    }
}

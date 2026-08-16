package com.sarah.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarah.app.domain.model.Difficulty
import com.sarah.app.domain.model.EnergyRequirement
import com.sarah.app.domain.model.ExtractedTaskDraft
import com.sarah.app.domain.model.Subject
import com.sarah.app.domain.model.TaskPriority
import com.sarah.app.domain.model.TaskType
import com.sarah.app.ui.theme.SarahError
import com.sarah.app.ui.theme.SarahPrimaryFixedDim
import com.sarah.app.ui.theme.SarahOutlineVariant
import com.sarah.app.ui.theme.SarahSurfaceContainerLowest
import com.sarah.app.ui.theme.SarahSurfaceContainer
import com.sarah.app.ui.theme.SarahPrimary
import com.sarah.app.ui.theme.SarahPrimary
import com.sarah.app.ui.theme.SarahSecondary
import com.sarah.app.ui.theme.SarahOnSurface
import com.sarah.app.ui.theme.SarahOnSurfaceVariant
import com.sarah.app.ui.theme.SarahTertiary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ExtractedTaskReviewCard(
    draft: ExtractedTaskDraft,
    availableSubjects: List<Subject>,
    onUpdateDraft: (
        title: String?,
        subjectId: Long?,
        type: TaskType?,
        description: String?,
        deadlineEpochMs: Long?,
        estimatedMinutes: Int?,
        priority: TaskPriority?,
        difficulty: Difficulty?,
        energyRequirement: EnergyRequirement?
    ) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SarahSurfaceContainerLowest)
            .border(1.dp, SarahPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        // Header with AI extracted pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(SarahPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = SarahPrimaryFixedDim,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI PARSED REVIEW",
                    style = MaterialTheme.typography.labelMedium,
                    color = SarahPrimaryFixedDim,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SarahSurfaceContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = draft.sourceType.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = SarahOnSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Title Input
        OutlinedTextField(
            value = draft.title,
            onValueChange = { onUpdateDraft(it, null, null, null, null, null, null, null, null) },
            label = { Text("Task Title") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SarahPrimary,
                unfocusedBorderColor = SarahOutlineVariant,
                focusedTextColor = SarahOnSurface,
                unfocusedTextColor = SarahOnSurface
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subject Selector Chips
        Text(
            text = "SUBJECT",
            style = MaterialTheme.typography.labelSmall,
            color = SarahSecondary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableSubjects.forEach { subject ->
                val isSelected = subject.id == draft.subjectId
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) SarahPrimary else SarahSurfaceContainer)
                        .border(1.dp, if (isSelected) SarahPrimary else SarahOutlineVariant, RoundedCornerShape(8.dp))
                        .clickable { onUpdateDraft(null, subject.id, null, null, null, null, null, null, null) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) SarahOnSurface else SarahOnSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Task Type Chips
        Text(
            text = "TASK TYPE",
            style = MaterialTheme.typography.labelSmall,
            color = SarahSecondary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskType.values().forEach { type ->
                val isSelected = type == draft.type
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) SarahPrimary else SarahSurfaceContainer)
                        .border(1.dp, if (isSelected) SarahPrimary else SarahOutlineVariant, RoundedCornerShape(8.dp))
                        .clickable { onUpdateDraft(null, null, type, null, null, null, null, null, null) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = type.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) SarahOnSurface else SarahOnSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Priority Chips
        Text(
            text = "PRIORITY",
            style = MaterialTheme.typography.labelSmall,
            color = SarahSecondary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskPriority.values().forEach { priority ->
                val isSelected = priority == draft.priority
                val color = when (priority) {
                    TaskPriority.CRITICAL -> SarahError
                    TaskPriority.HIGH -> SarahTertiary
                    TaskPriority.MEDIUM -> SarahPrimaryFixedDim
                    TaskPriority.LOW -> SarahSecondary
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) color.copy(alpha = 0.25f) else SarahSurfaceContainer)
                        .border(1.dp, if (isSelected) color else SarahOutlineVariant, RoundedCornerShape(8.dp))
                        .clickable { onUpdateDraft(null, null, null, null, null, null, priority, null, null) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = priority.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) SarahOnSurface else SarahOnSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Estimated Duration Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "ESTIMATED DURATION",
                style = MaterialTheme.typography.labelSmall,
                color = SarahSecondary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${draft.estimatedMinutes} min",
                style = MaterialTheme.typography.labelMedium,
                color = SarahPrimaryFixedDim,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = draft.estimatedMinutes.toFloat(),
            onValueChange = { onUpdateDraft(null, null, null, null, null, it.roundToInt(), null, null, null) },
            valueRange = 15f..180f,
            steps = 10,
            colors = SliderDefaults.colors(
                thumbColor = SarahPrimary,
                activeTrackColor = SarahPrimary,
                inactiveTrackColor = SarahSurfaceContainer
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Formatted Deadline Preview
        val formattedDate = DateTimeFormatter.ofPattern("EEE, MMM d • h:mm a", Locale.US)
            .format(Instant.ofEpochMilli(draft.deadlineEpochMs).atZone(ZoneId.systemDefault()))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SarahSurfaceContainer)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = SarahPrimaryFixedDim,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Deadline:",
                    style = MaterialTheme.typography.labelSmall,
                    color = SarahSecondary
                )
            }
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelSmall,
                color = SarahOnSurface,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Action Button
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SarahPrimary,
                contentColor = SarahOnSurface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Add to Academic Plan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

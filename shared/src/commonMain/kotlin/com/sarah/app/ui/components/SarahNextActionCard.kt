package com.sarah.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.PriorityHigh
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarah.app.domain.model.NextAction
import com.sarah.app.domain.model.NextActionType
import com.sarah.app.ui.theme.SarahError
import com.sarah.app.ui.theme.SarahErrorContainer
import com.sarah.app.ui.theme.SarahOnErrorContainer
import com.sarah.app.ui.theme.SarahOnPrimary
import com.sarah.app.ui.theme.SarahOnSurface
import com.sarah.app.ui.theme.SarahOnSurfaceVariant
import com.sarah.app.ui.theme.SarahPrimary
import com.sarah.app.ui.theme.SarahPrimaryFixed
import com.sarah.app.ui.theme.SarahSecondary
import com.sarah.app.ui.theme.SarahSurfaceContainerHigh
import com.sarah.app.ui.theme.SarahTertiary

@Composable
fun SarahNextActionCard(
    nextAction          : NextAction,
    onPrimaryActionClick: (NextAction) -> Unit,
    onMarkCompletedClick: (Long) -> Unit,
    modifier            : Modifier = Modifier
) {
    val (accentColor, icon, buttonLabel) = when (nextAction.actionType) {
        NextActionType.START_TASK -> Triple(
            SarahPrimary,
            Icons.Outlined.PlayArrow,
            if (nextAction.durationMinutes > 0) "Start (${nextAction.durationMinutes}m)" else "Start"
        )
        NextActionType.CONTINUE_TASK -> Triple(
            SarahPrimary,
            Icons.Outlined.AutoAwesome,
            if (nextAction.durationMinutes > 0) "Continue (${nextAction.durationMinutes}m)" else "Continue"
        )
        NextActionType.TAKE_BREAK -> Triple(
            SarahTertiary,
            Icons.Outlined.Coffee,
            if (nextAction.durationMinutes > 0) "${nextAction.durationMinutes}m Break" else "Take Break"
        )
        NextActionType.MEAL -> Triple(
            SarahTertiary,
            Icons.Outlined.Fastfood,
            "Enjoy Meal"
        )
        NextActionType.REST -> Triple(
            SarahSecondary,
            Icons.Outlined.SelfImprovement,
            "Rest & Recover"
        )
        NextActionType.STOP_FOR_TONIGHT -> Triple(
            SarahSecondary,
            Icons.Outlined.Bedtime,
            if (nextAction.urgencyBadge == "COMPLETE") "Wrap Up Tonight" else "Rest for Tomorrow"
        )
        NextActionType.RECOVER_FROM_DELAY -> Triple(
            SarahError,
            Icons.Outlined.Refresh,
            "Recalibrate Plan"
        )
    }

    // Glass card — white 70% + gradient overlay
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation      = 4.dp,
                shape          = RoundedCornerShape(24.dp),
                ambientColor   = Color.Black.copy(alpha = 0.04f),
                spotColor      = Color.Black.copy(alpha = 0.06f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.78f))
            .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
    ) {
        // Atmospheric gradient overlay (primaryFixed 30% to transparent)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            SarahPrimaryFixed.copy(alpha = 0.30f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier          = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Label row: "SARAH'S NEXT MOVE" ──────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Outlined.Navigation,
                    contentDescription = null,
                    tint               = SarahPrimary,
                    modifier           = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text          = "SARAH'S NEXT MOVE",
                    style         = MaterialTheme.typography.labelSmall,
                    color         = SarahPrimary,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // ── Task title ────────────────────────────────────────────────
            Text(
                text      = nextAction.title,
                style     = MaterialTheme.typography.headlineMedium,
                color     = SarahOnSurface,
                fontWeight = FontWeight.Bold,
                maxLines  = 2,
                overflow  = TextOverflow.Ellipsis
            )

            // ── Metadata chips row ────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (nextAction.durationMinutes > 0) {
                    NextMoveChip(
                        icon  = Icons.Outlined.Timer,
                        label = "${nextAction.durationMinutes} min"
                    )
                }
                if (nextAction.subtitle.isNotBlank()) {
                    NextMoveChip(
                        icon  = Icons.Outlined.Event,
                        label = nextAction.subtitle
                    )
                }
                if (nextAction.urgencyBadge.isNotBlank() && nextAction.urgencyBadge != "COMPLETE") {
                    // Priority chip (error-colored)
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SarahErrorContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.PriorityHigh,
                                contentDescription = null,
                                tint               = SarahOnErrorContainer,
                                modifier           = Modifier.size(12.dp)
                            )
                            Text(
                                text       = nextAction.urgencyBadge,
                                style      = MaterialTheme.typography.labelSmall,
                                color      = SarahOnErrorContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // ── Reason callout ─────────────────────────────────────────────
            if (nextAction.reason.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.4f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text  = "💡 ${nextAction.reason}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SarahOnSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Start button ──────────────────────────────────────────────
            Button(
                onClick  = { onPrimaryActionClick(nextAction) },
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = SarahPrimary,
                    contentColor   = SarahOnPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text       = buttonLabel,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector        = Icons.Filled.NavigateNext,
                    contentDescription = null,
                    modifier           = Modifier.size(20.dp)
                )
            }

            // Secondary: mark-done button
            val actionTaskId = nextAction.taskId
            if (actionTaskId != null &&
                (nextAction.actionType == NextActionType.START_TASK ||
                 nextAction.actionType == NextActionType.CONTINUE_TASK)) {
                OutlinedButton(
                    onClick = { onMarkCompletedClick(actionTaskId) },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = SarahSecondary),
                    border   = androidx.compose.foundation.BorderStroke(1.dp, SarahSecondary.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Check,
                        contentDescription = "Mark Complete",
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text  = "Mark as done",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun NextMoveChip(icon: ImageVector, label: String) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(SarahSurfaceContainerHigh)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = SarahOnSurfaceVariant,
                modifier           = Modifier.size(12.dp)
            )
            Text(
                text       = label,
                style      = MaterialTheme.typography.labelSmall,
                color      = SarahOnSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

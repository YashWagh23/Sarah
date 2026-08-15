package com.sarah.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarah.app.domain.model.NextAction
import com.sarah.app.domain.model.NextActionType
import com.sarah.app.ui.theme.CoralRed
import com.sarah.app.ui.theme.CyanAccent
import com.sarah.app.ui.theme.DarkBorder
import com.sarah.app.ui.theme.DarkSurface
import com.sarah.app.ui.theme.DarkSurfaceVariant
import com.sarah.app.ui.theme.ElectricIndigo
import com.sarah.app.ui.theme.MintEmerald
import com.sarah.app.ui.theme.TextMuted
import com.sarah.app.ui.theme.TextPrimary
import com.sarah.app.ui.theme.TextSecondary
import com.sarah.app.ui.theme.WarmAmber

@Composable
fun SarahNextActionCard(
    nextAction: NextAction,
    onPrimaryActionClick: (NextAction) -> Unit,
    onMarkCompletedClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val (accentColor, icon, buttonLabel) = when (nextAction.actionType) {
        NextActionType.START_TASK -> Triple(
            ElectricIndigo,
            Icons.Rounded.PlayArrow,
            if (nextAction.durationMinutes > 0) "Start Focus (${nextAction.durationMinutes}m)" else "Start Task"
        )
        NextActionType.CONTINUE_TASK -> Triple(
            CyanAccent,
            Icons.Rounded.AutoAwesome,
            if (nextAction.durationMinutes > 0) "Continue (${nextAction.durationMinutes}m)" else "Continue Task"
        )
        NextActionType.TAKE_BREAK -> Triple(
            MintEmerald,
            Icons.Rounded.Coffee,
            if (nextAction.durationMinutes > 0) "Take ${nextAction.durationMinutes}m Break" else "Take Break"
        )
        NextActionType.MEAL -> Triple(
            WarmAmber,
            Icons.Rounded.Fastfood,
            if (nextAction.durationMinutes > 0) "Dinner Buffer (${nextAction.durationMinutes}m)" else "Enjoy Meal"
        )
        NextActionType.REST -> Triple(
            ElectricIndigo,
            Icons.Rounded.SelfImprovement,
            "Rest & Recover"
        )
        NextActionType.STOP_FOR_TONIGHT -> Triple(
            if (nextAction.urgencyBadge == "COMPLETE") MintEmerald else TextSecondary,
            if (nextAction.urgencyBadge == "COMPLETE") Icons.Rounded.DoneAll else Icons.Rounded.Bedtime,
            if (nextAction.urgencyBadge == "COMPLETE") "Wrap Up Tonight" else "Rest for Tomorrow"
        )
        NextActionType.RECOVER_FROM_DELAY -> Triple(
            CoralRed,
            Icons.Rounded.Refresh,
            "Recalibrate Plan"
        )
    }

    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            accentColor.copy(alpha = 0.18f),
            DarkSurfaceVariant.copy(alpha = 0.85f)
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier
                .background(gradientBrush)
                .padding(20.dp)
        ) {
            // Header Bar: "SARAH'S NEXT MOVE" + Urgency Pill
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
                            .background(accentColor.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SARAH'S NEXT MOVE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        letterSpacing = 1.sp
                    )
                }

                if (nextAction.urgencyBadge.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.20f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = nextAction.urgencyBadge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Title
            Text(
                text = nextAction.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (nextAction.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = nextAction.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Reason Callout
            if (nextAction.reason.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "💡 ${nextAction.reason}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onPrimaryActionClick(nextAction) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = if (accentColor == CyanAccent || accentColor == MintEmerald || accentColor == WarmAmber) DarkSurface else TextPrimary
                    )
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = buttonLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // If associated with a task, allow completing directly
                if (nextAction.taskId != null && (nextAction.actionType == NextActionType.START_TASK || nextAction.actionType == NextActionType.CONTINUE_TASK)) {
                    OutlinedButton(
                        onClick = { onMarkCompletedClick(nextAction.taskId) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MintEmerald),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MintEmerald.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Mark Complete",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Done",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

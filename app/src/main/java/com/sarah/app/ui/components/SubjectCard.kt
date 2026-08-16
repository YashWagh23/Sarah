package com.sarah.app.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AssignmentLate
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarah.app.domain.model.Subject
import com.sarah.app.ui.theme.SarahError
import com.sarah.app.ui.theme.SarahErrorContainer
import com.sarah.app.ui.theme.SarahOnErrorContainer
import com.sarah.app.ui.theme.SarahOnSurface
import com.sarah.app.ui.theme.SarahOnSurfaceVariant
import com.sarah.app.ui.theme.SarahPrimary
import com.sarah.app.ui.theme.SarahSurfaceContainerHigh
import com.sarah.app.ui.theme.SarahSurfaceContainerLow
import com.sarah.app.ui.theme.SarahSurfaceContainerLowest
import com.sarah.app.ui.theme.SarahSurfaceVariant

/**
 * Bento-style subject card matching the reference design:
 * - 24dp rounded card on white surface
 * - 6dp left-edge color strip (subject accent color)
 * - Course code badge, subject name, professor row
 * - Circular attendance ring via Canvas (right side)
 * - Bottom divider + stat chips (tasks / notes)
 */
@Composable
fun SubjectCard(
    subject          : Subject,
    pendingTasksCount: Int    = 0,
    notesCount       : Int    = 0,
    onClick          : () -> Unit = {},
    modifier         : Modifier   = Modifier
) {
    val subjectColor = runCatching {
        Color(android.graphics.Color.parseColor(subject.colorHex))
    }.getOrDefault(SarahPrimary)

    val attendance    = subject.currentAttendancePercentage.coerceIn(0, 100)
    val isLowAttend   = attendance < subject.targetAttendancePercentage
    val ringColor     = if (isLowAttend) SarahError else subjectColor
    val trackColor    = SarahSurfaceContainerHigh

    Box(
        modifier = modifier
            .shadow(
                elevation    = 2.dp,
                shape        = RoundedCornerShape(24.dp),
                ambientColor = Color.Black.copy(alpha = 0.03f),
                spotColor    = Color.Black.copy(alpha = 0.04f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(SarahSurfaceContainerLowest)
            .border(1.dp, Color.Black.copy(alpha = 0.04f), RoundedCornerShape(24.dp))
            .clickable { onClick() }
    ) {
        // Left color strip (6dp wide full-height)
        Box(
            modifier = Modifier
                .width(6.dp)
                .matchParentSize()
                .background(subjectColor.copy(alpha = 0.85f))
                .align(Alignment.CenterStart)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
        ) {
            // ── Top: badge + title + professor | attendance ring ──────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Course code badge
                    if (subject.code.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(subjectColor.copy(alpha = 0.10f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text          = subject.code,
                                style         = MaterialTheme.typography.labelSmall,
                                color         = subjectColor,
                                fontWeight    = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                    // Subject name
                    Text(
                        text       = subject.name,
                        style      = MaterialTheme.typography.headlineSmall,
                        color      = SarahOnSurface,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 24.sp
                    )
                    // Professor
                    if (subject.professorName.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.Person,
                                contentDescription = null,
                                tint               = SarahOnSurfaceVariant,
                                modifier           = Modifier.size(14.dp)
                            )
                            Text(
                                text  = subject.professorName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SarahOnSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Circular attendance ring
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(52.dp)) {
                    Canvas(modifier = Modifier.size(52.dp)) {
                        val strokeWidth = 6.dp.toPx()
                        val sweep       = (attendance / 100f) * 360f
                        // Track
                        drawArc(
                            color      = trackColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter  = false,
                            style      = Stroke(strokeWidth)
                        )
                        // Progress
                        drawArc(
                            color      = ringColor,
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter  = false,
                            style      = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text       = "$attendance%",
                        style      = MaterialTheme.typography.labelSmall,
                        color      = if (isLowAttend) SarahError else SarahOnSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Bottom divider + stat chips ───────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SarahSurfaceVariant)
            )
            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Pending tasks chip
                if (pendingTasksCount > 0) {
                    StatChip(
                        icon       = Icons.Outlined.AssignmentLate,
                        label      = "$pendingTasksCount Tasks",
                        bg         = SarahErrorContainer.copy(alpha = 0.5f),
                        iconTint   = SarahOnErrorContainer,
                        textColor  = SarahOnErrorContainer
                    )
                } else {
                    StatChip(
                        icon      = Icons.Outlined.AssignmentTurnedIn,
                        label     = "All clear",
                        bg        = SarahSurfaceContainerLow,
                        iconTint  = SarahOnSurface,
                        textColor = SarahOnSurface
                    )
                }
                if (notesCount > 0) {
                    StatChip(
                        icon      = Icons.Outlined.Description,
                        label     = "$notesCount Notes",
                        bg        = SarahSurfaceContainerLow,
                        iconTint  = SarahOnSurface,
                        textColor = SarahOnSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    icon     : androidx.compose.ui.graphics.vector.ImageVector,
    label    : String,
    bg       : Color,
    iconTint : Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = iconTint,
                modifier           = Modifier.size(13.dp)
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor
            )
        }
    }
}

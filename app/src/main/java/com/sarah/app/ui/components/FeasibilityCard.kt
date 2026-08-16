package com.sarah.app.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarah.app.domain.model.FeasibilityReport
import com.sarah.app.domain.model.FeasibilityStatus
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

@Composable
fun FeasibilityCard(
    report: FeasibilityReport,
    modifier: Modifier = Modifier
) {
    val statusColor = when (report.status) {
        FeasibilityStatus.OPTIMAL -> SarahPrimary
        FeasibilityStatus.MANAGEABLE -> SarahPrimaryFixedDim
        FeasibilityStatus.TIGHT -> SarahTertiary
        FeasibilityStatus.OVERLOADED -> SarahError
    }

    val progress = if (report.realisticProductiveMinutes > 0) {
        (report.totalRequiredMinutes.toFloat() / report.realisticProductiveMinutes.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        SarahSurfaceContainerLowest,
                        SarahSurfaceContainer
                    )
                )
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(statusColor.copy(alpha = 0.5f), SarahOutlineVariant)
                ),
                RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        // Status Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = report.status.title.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Bedtime,
                    contentDescription = "Sleep Time",
                    tint = SarahOnSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${formatDuration(report.minutesUntilSleep)} to sleep",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SarahOnSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Time Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Productive Time
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SarahSurfaceContainerLowest)
                    .border(1.dp, SarahOutlineVariant, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = "Productive Capacity",
                        tint = SarahPrimaryFixedDim,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PRODUCTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = SarahSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDuration(report.realisticProductiveMinutes),
                    style = MaterialTheme.typography.headlineSmall,
                    color = SarahOnSurface,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Workload Required
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SarahSurfaceContainerLowest)
                    .border(1.dp, SarahOutlineVariant, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = "Work Required",
                        tint = if (report.status == FeasibilityStatus.OVERLOADED) SarahError else SarahTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "WORK REQUIRED",
                        style = MaterialTheme.typography.labelSmall,
                        color = SarahSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDuration(report.totalRequiredMinutes),
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (report.status == FeasibilityStatus.OVERLOADED) SarahError else SarahOnSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Capacity Progress Bar
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Capacity Load",
                    style = MaterialTheme.typography.labelSmall,
                    color = SarahOnSurfaceVariant
                )
                val loadPercent = if (report.realisticProductiveMinutes > 0) {
                    (report.totalRequiredMinutes * 100) / report.realisticProductiveMinutes
                } else 0
                Text(
                    text = "$loadPercent%",
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = statusColor,
                trackColor = SarahSurfaceContainerLowest
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Guidance Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(statusColor.copy(alpha = 0.1f))
                .border(1.dp, statusColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (report.status == FeasibilityStatus.OVERLOADED) Icons.Rounded.Warning else Icons.Rounded.Info,
                contentDescription = "Guidance",
                tint = statusColor,
                modifier = Modifier
                    .size(18.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = report.guidanceMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = SarahOnSurface
            )
        }
    }
}

fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

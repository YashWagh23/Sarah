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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarah.app.domain.model.AgendaItem
import com.sarah.app.ui.theme.SarahOnSurface
import com.sarah.app.ui.theme.SarahOnSurfaceVariant
import com.sarah.app.ui.theme.SarahOutlineVariant
import com.sarah.app.ui.theme.SarahPrimary
import com.sarah.app.ui.theme.SarahPrimaryFixedDim
import com.sarah.app.ui.theme.SarahSecondary
import com.sarah.app.ui.theme.SarahSurfaceContainer
import com.sarah.app.ui.theme.SarahSurfaceContainerLowest
import com.sarah.app.ui.theme.SarahTertiary

@Composable
fun AgendaTimeline(
    agendaItems: List<AgendaItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SarahSurfaceContainerLowest)
            .border(1.dp, SarahOutlineVariant, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SUGGESTED EVENING AGENDA",
                style = MaterialTheme.typography.labelSmall,
                color = SarahPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Adaptive Timeline",
                style = MaterialTheme.typography.labelSmall,
                color = SarahSecondary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (agendaItems.isEmpty()) {
            Text(
                text = "No study agenda required tonight! All priority tasks are cleared.",
                style = MaterialTheme.typography.bodyMedium,
                color = SarahOnSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            agendaItems.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Time Column
                    Column(
                        modifier = Modifier.width(90.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = item.startTimeFormatted,
                            style = MaterialTheme.typography.labelMedium,
                            color = SarahOnSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = item.endTimeFormatted,
                            style = MaterialTheme.typography.labelSmall,
                            color = SarahSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Timeline Indicator Node & Line
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        val nodeColor = when {
                            item.title.contains("Sleep") || item.title.contains("Bed") -> SarahPrimary
                            item.isBreak -> SarahTertiary
                            else -> SarahPrimary
                        }
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(nodeColor.copy(alpha = 0.2f))
                                .border(2.dp, nodeColor, CircleShape)
                        )
                        if (index < agendaItems.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(38.dp)
                                    .background(SarahOutlineVariant)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Item Content Card
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (item.isBreak) SarahSurfaceContainer.copy(alpha = 0.5f) else SarahSurfaceContainer)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (item.isBreak) SarahOnSurfaceVariant else SarahOnSurface,
                                fontWeight = if (item.isBreak) FontWeight.Normal else FontWeight.SemiBold
                            )
                            Text(
                                text = "${item.durationMinutes}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (item.isBreak) SarahTertiary else SarahPrimaryFixedDim
                            )
                        }
                        if (item.subtitle.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = SarahSecondary
                            )
                        }
                    }
                }
                if (index < agendaItems.lastIndex) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

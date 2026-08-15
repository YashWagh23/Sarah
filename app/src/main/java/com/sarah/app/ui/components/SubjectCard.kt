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
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarah.app.domain.model.Subject
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
fun SubjectCard(
    subject: Subject,
    pendingTasksCount: Int = 0,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val subjectColor = runCatching {
        Color(android.graphics.Color.parseColor(subject.colorHex))
    }.getOrDefault(ElectricIndigo)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(subjectColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Book,
                        contentDescription = null,
                        tint = subjectColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    if (subject.code.isNotBlank()) {
                        Text(
                            text = subject.code,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }

            if (pendingTasksCount > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(WarmAmber.copy(alpha = 0.15f))
                        .border(1.dp, WarmAmber.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$pendingTasksCount due",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarmAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (subject.professorName.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = subject.professorName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Attendance & Weekly Hours Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurfaceVariant)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${subject.weeklyHours} hrs / week",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Attendance: ",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                val attColor = if (subject.currentAttendancePercentage >= subject.targetAttendancePercentage) MintEmerald else WarmAmber
                Text(
                    text = "${subject.currentAttendancePercentage}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = attColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

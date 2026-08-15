package com.sarah.app.ui.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarah.app.ui.theme.CyanAccent
import com.sarah.app.ui.theme.DarkBackground
import com.sarah.app.ui.theme.DarkBorder
import com.sarah.app.ui.theme.DarkSurface
import com.sarah.app.ui.theme.DarkSurfaceVariant
import com.sarah.app.ui.theme.ElectricIndigo
import com.sarah.app.ui.theme.MintEmerald
import com.sarah.app.ui.theme.TextMuted
import com.sarah.app.ui.theme.TextPrimary
import com.sarah.app.ui.theme.TextSecondary
import com.sarah.app.ui.theme.WarmAmber
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ElectricIndigo)
        }
    } else {
        val schedule = uiState.schedule
        var wakeTimeMins by remember(schedule) { mutableFloatStateOf(schedule.wakeTimeMinutes.toFloat()) }
        var sleepTimeMins by remember(schedule) { mutableFloatStateOf(schedule.sleepTimeMinutes.toFloat()) }
        var collegeStartMins by remember(schedule) { mutableFloatStateOf(schedule.collegeStartTimeMinutes.toFloat()) }
        var collegeEndMins by remember(schedule) { mutableFloatStateOf(schedule.collegeEndTimeMinutes.toFloat()) }
        var commuteMins by remember(schedule) { mutableFloatStateOf(schedule.commuteMinutes.toFloat()) }
        var dinnerBufferMins by remember(schedule) { mutableFloatStateOf(schedule.dinnerBufferMinutes.toFloat()) }
        var sessionLengthMins by remember(schedule) { mutableFloatStateOf(schedule.preferredSessionLengthMinutes.toFloat()) }
        var breakDurationMins by remember(schedule) { mutableFloatStateOf(schedule.breakDurationMinutes.toFloat()) }

        LaunchedEffect(uiState.isSaved) {
            if (uiState.isSaved) {
                snackbarHostState.showSnackbar("Schedule constraints updated successfully!")
            }
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DarkBackground)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Column {
                        Text(
                            text = "ACADEMIC CONSTRAINTS",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricIndigo,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Daily Rhythm & College Hours",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sarah uses these constraints to compute your realistic study capacity and protect your sleep",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                // Section 1: Sleep & Wake Cycle
                item {
                    ScheduleCard(
                        title = "SLEEP & WAKE CYCLE",
                        icon = Icons.Rounded.Bedtime,
                        accentColor = MintEmerald
                    ) {
                        SliderSettingItem(
                            label = "Wake Time",
                            valueFormatted = formatTimeFromMinutes(wakeTimeMins.roundToInt()),
                            value = wakeTimeMins,
                            onValueChange = { wakeTimeMins = it },
                            valueRange = 300f..600f, // 5:00 AM to 10:00 AM
                            steps = 19
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SliderSettingItem(
                            label = "Planned Sleep Time",
                            valueFormatted = formatTimeFromMinutes(sleepTimeMins.roundToInt()),
                            value = sleepTimeMins,
                            onValueChange = { sleepTimeMins = it },
                            valueRange = 1260f..1440f, // 9:00 PM to 12:00 AM
                            steps = 11
                        )
                    }
                }

                // Section 2: College Hours & Commute
                item {
                    ScheduleCard(
                        title = "COLLEGE HOURS & COMMUTE",
                        icon = Icons.Rounded.School,
                        accentColor = CyanAccent
                    ) {
                        SliderSettingItem(
                            label = "College Starts",
                            valueFormatted = formatTimeFromMinutes(collegeStartMins.roundToInt()),
                            value = collegeStartMins,
                            onValueChange = { collegeStartMins = it },
                            valueRange = 480f..720f, // 8:00 AM to 12:00 PM
                            steps = 15
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SliderSettingItem(
                            label = "College Ends",
                            valueFormatted = formatTimeFromMinutes(collegeEndMins.roundToInt()),
                            value = collegeEndMins,
                            onValueChange = { collegeEndMins = it },
                            valueRange = 780f..1140f, // 1:00 PM to 7:00 PM
                            steps = 23
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SliderSettingItem(
                            label = "One-Way Commute",
                            valueFormatted = "${commuteMins.roundToInt()} minutes",
                            value = commuteMins,
                            onValueChange = { commuteMins = it },
                            valueRange = 10f..120f,
                            steps = 21
                        )
                    }
                }

                // Section 3: Study Habits & Rest
                item {
                    ScheduleCard(
                        title = "STUDY FOCUS & REST BUFFERS",
                        icon = Icons.Rounded.Timer,
                        accentColor = WarmAmber
                    ) {
                        SliderSettingItem(
                            label = "Dinner / Rest Buffer",
                            valueFormatted = "${dinnerBufferMins.roundToInt()} minutes",
                            value = dinnerBufferMins,
                            onValueChange = { dinnerBufferMins = it },
                            valueRange = 15f..90f,
                            steps = 14
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SliderSettingItem(
                            label = "Focus Session Target",
                            valueFormatted = "${sessionLengthMins.roundToInt()} minutes",
                            value = sessionLengthMins,
                            onValueChange = { sessionLengthMins = it },
                            valueRange = 20f..90f,
                            steps = 13
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SliderSettingItem(
                            label = "Restorative Break Duration",
                            valueFormatted = "${breakDurationMins.roundToInt()} minutes",
                            value = breakDurationMins,
                            onValueChange = { breakDurationMins = it },
                            valueRange = 5f..30f,
                            steps = 5
                        )
                    }
                }

                // Save Button
                item {
                    Button(
                        onClick = {
                            viewModel.updateSchedule(
                                wakeTimeMinutes = wakeTimeMins.roundToInt(),
                                sleepTimeMinutes = sleepTimeMins.roundToInt(),
                                collegeStartTimeMinutes = collegeStartMins.roundToInt(),
                                collegeEndTimeMinutes = collegeEndMins.roundToInt(),
                                commuteMinutes = commuteMins.roundToInt(),
                                dinnerBufferMinutes = dinnerBufferMins.roundToInt(),
                                breakDurationMinutes = breakDurationMins.roundToInt(),
                                preferredSessionLengthMinutes = sessionLengthMins.roundToInt()
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricIndigo,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Save Schedule Constraints",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun ScheduleCard(
    title: String,
    icon: ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        content()
    }
}

@Composable
fun SliderSettingItem(
    label: String,
    valueFormatted: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            Text(
                text = valueFormatted,
                style = MaterialTheme.typography.labelLarge,
                color = CyanAccent,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = ElectricIndigo,
                activeTrackColor = ElectricIndigo,
                inactiveTrackColor = DarkSurfaceVariant
            )
        )
    }
}

fun formatTimeFromMinutes(minsFromMidnight: Int): String {
    val totalMins = minsFromMidnight % (24 * 60)
    val hour24 = totalMins / 60
    val mins = totalMins % 60
    val hour12 = when (val h = hour24 % 12) {
        0 -> 12
        else -> h
    }
    val amPm = if (hour24 < 12) "AM" else "PM"
    return String.format(Locale.US, "%d:%02d %s", hour12, mins, amPm)
}

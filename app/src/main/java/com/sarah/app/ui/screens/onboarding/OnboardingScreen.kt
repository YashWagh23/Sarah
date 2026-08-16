package com.sarah.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarah.app.ui.screens.schedule.formatTimeFromMinutes
import com.sarah.app.ui.theme.SarahPrimaryFixedDim
import com.sarah.app.ui.theme.SarahBackground
import com.sarah.app.ui.theme.SarahOutlineVariant
import com.sarah.app.ui.theme.SarahSurfaceContainerLowest
import com.sarah.app.ui.theme.SarahSurfaceContainer
import com.sarah.app.ui.theme.SarahPrimary
import com.sarah.app.ui.theme.SarahPrimary
import com.sarah.app.ui.theme.SarahSecondary
import com.sarah.app.ui.theme.SarahOnSurface
import com.sarah.app.ui.theme.SarahOnSurfaceVariant
import kotlin.math.roundToInt

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onComplete()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SarahBackground)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Step Progress Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val isCurrent = index == uiState.currentStep
                    val isPast = index < uiState.currentStep
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (isCurrent) 28.dp else 12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                when {
                                    isCurrent -> SarahPrimary
                                    isPast -> SarahPrimary
                                    else -> SarahOutlineVariant
                                }
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            when (uiState.currentStep) {
                0 -> StepWelcome(
                    name = uiState.name,
                    onNameChange = { viewModel.updateName(it) },
                    college = uiState.collegeName,
                    onCollegeChange = { viewModel.updateCollege(it) },
                    department = uiState.department,
                    onDepartmentChange = { viewModel.updateDepartment(it) }
                )
                1 -> StepRhythm(
                    sleepMins = uiState.sleepTimeMinutes,
                    onSleepChange = { viewModel.updateSleepTime(it) },
                    startMins = uiState.collegeStartTimeMinutes,
                    endMins = uiState.collegeEndTimeMinutes,
                    onCollegeHoursChange = { s, e -> viewModel.updateCollegeHours(s, e) }
                )
                2 -> StepSummary(
                    name = uiState.name,
                    college = uiState.collegeName,
                    department = uiState.department,
                    sleepMins = uiState.sleepTimeMinutes
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(20.dp))

            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.currentStep > 0) {
                    TextButton(onClick = { viewModel.prevStep() }) {
                        Text("Back", color = SarahOnSurfaceVariant)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        if (uiState.currentStep < 2) {
                            viewModel.nextStep()
                        } else {
                            viewModel.completeOnboarding()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SarahPrimary,
                        contentColor = SarahOnSurface
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (uiState.currentStep < 2) "Continue" else "Launch Sarah",
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun StepWelcome(
    name: String,
    onNameChange: (String) -> Unit,
    college: String,
    onCollegeChange: (String) -> Unit,
    department: String,
    onDepartmentChange: (String) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        listOf(SarahPrimary, SarahPrimaryFixedDim)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = SarahBackground,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Welcome to Sarah",
            style = MaterialTheme.typography.displayLarge,
            color = SarahOnSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Your AI-Powered Personal Academic Operating System",
            style = MaterialTheme.typography.bodyLarge,
            color = SarahPrimaryFixedDim,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SarahSurfaceContainerLowest)
                .border(1.dp, SarahOutlineVariant, RoundedCornerShape(20.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("What is your name?") },
                placeholder = { Text("e.g. Alex") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SarahPrimary,
                    unfocusedBorderColor = SarahOutlineVariant,
                    focusedTextColor = SarahOnSurface,
                    unfocusedTextColor = SarahOnSurface
                )
            )

            OutlinedTextField(
                value = college,
                onValueChange = onCollegeChange,
                label = { Text("College / University") },
                placeholder = { Text("e.g. Stanford / IIT / MIT") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SarahPrimary,
                    unfocusedBorderColor = SarahOutlineVariant,
                    focusedTextColor = SarahOnSurface,
                    unfocusedTextColor = SarahOnSurface
                )
            )

            OutlinedTextField(
                value = department,
                onValueChange = onDepartmentChange,
                label = { Text("Department / Major") },
                placeholder = { Text("e.g. Computer Science") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SarahPrimary,
                    unfocusedBorderColor = SarahOutlineVariant,
                    focusedTextColor = SarahOnSurface,
                    unfocusedTextColor = SarahOnSurface
                )
            )
        }
    }
}

@Composable
fun StepRhythm(
    sleepMins: Int,
    onSleepChange: (Int) -> Unit,
    startMins: Int,
    endMins: Int,
    onCollegeHoursChange: (Int, Int) -> Unit
) {
    var sleepVal by remember { mutableFloatStateOf(sleepMins.toFloat()) }
    var collegeStartVal by remember { mutableFloatStateOf(startMins.toFloat()) }
    var collegeEndVal by remember { mutableFloatStateOf(endMins.toFloat()) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Rounded.Bedtime,
            contentDescription = null,
            tint = SarahPrimary,
            modifier = Modifier.size(44.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your Academic Rhythm",
            style = MaterialTheme.typography.headlineMedium,
            color = SarahOnSurface,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Sarah protects your sleep and calculates realistic study time",
            style = MaterialTheme.typography.bodyMedium,
            color = SarahOnSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SarahSurfaceContainerLowest)
                .border(1.dp, SarahOutlineVariant, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Planned Sleep Time", color = SarahOnSurface, style = MaterialTheme.typography.bodyLarge)
                Text(formatTimeFromMinutes(sleepVal.roundToInt()), color = SarahPrimary, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = sleepVal,
                onValueChange = {
                    sleepVal = it
                    onSleepChange(it.roundToInt())
                },
                valueRange = 1260f..1440f,
                steps = 11,
                colors = SliderDefaults.colors(
                    thumbColor = SarahPrimary,
                    activeTrackColor = SarahPrimary,
                    inactiveTrackColor = SarahSurfaceContainer
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("College Start Time", color = SarahOnSurface, style = MaterialTheme.typography.bodyLarge)
                Text(formatTimeFromMinutes(collegeStartVal.roundToInt()), color = SarahPrimaryFixedDim, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = collegeStartVal,
                onValueChange = {
                    collegeStartVal = it
                    onCollegeHoursChange(it.roundToInt(), collegeEndVal.roundToInt())
                },
                valueRange = 480f..720f,
                steps = 15,
                colors = SliderDefaults.colors(
                    thumbColor = SarahPrimaryFixedDim,
                    activeTrackColor = SarahPrimaryFixedDim,
                    inactiveTrackColor = SarahSurfaceContainer
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("College End Time", color = SarahOnSurface, style = MaterialTheme.typography.bodyLarge)
                Text(formatTimeFromMinutes(collegeEndVal.roundToInt()), color = SarahPrimaryFixedDim, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = collegeEndVal,
                onValueChange = {
                    collegeEndVal = it
                    onCollegeHoursChange(collegeStartVal.roundToInt(), it.roundToInt())
                },
                valueRange = 780f..1140f,
                steps = 23,
                colors = SliderDefaults.colors(
                    thumbColor = SarahPrimaryFixedDim,
                    activeTrackColor = SarahPrimaryFixedDim,
                    inactiveTrackColor = SarahSurfaceContainer
                )
            )
        }
    }
}

@Composable
fun StepSummary(
    name: String,
    college: String,
    department: String,
    sleepMins: Int
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Rounded.Psychology,
            contentDescription = null,
            tint = SarahPrimary,
            modifier = Modifier.size(52.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Ready to Focus",
            style = MaterialTheme.typography.headlineMedium,
            color = SarahOnSurface,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Sarah will now monitor your academic workload and tell you what to do next every evening.",
            style = MaterialTheme.typography.bodyMedium,
            color = SarahOnSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SarahSurfaceContainerLowest)
                .border(1.dp, SarahOutlineVariant, RoundedCornerShape(20.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryItem(label = "Student", value = name.ifBlank { "Alex" })
            SummaryItem(label = "Department", value = department.ifBlank { "Computer Science" })
            SummaryItem(label = "College", value = college.ifBlank { "College of Engineering" })
            SummaryItem(label = "Target Sleep", value = formatTimeFromMinutes(sleepMins))
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = SarahSecondary)
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = SarahOnSurface, fontWeight = FontWeight.Bold)
    }
}

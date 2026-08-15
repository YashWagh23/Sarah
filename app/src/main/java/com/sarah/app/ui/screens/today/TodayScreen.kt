package com.sarah.app.ui.screens.today

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarah.app.ui.components.AgendaTimeline
import com.sarah.app.ui.components.EnergyLevelPicker
import com.sarah.app.ui.components.FeasibilityCard
import com.sarah.app.ui.components.TaskCard
import com.sarah.app.ui.theme.CoralRed
import com.sarah.app.ui.theme.CyanAccent
import com.sarah.app.ui.theme.DarkBackground
import com.sarah.app.ui.theme.ElectricIndigo
import com.sarah.app.ui.theme.MintEmerald
import com.sarah.app.ui.theme.TextMuted
import com.sarah.app.ui.theme.TextPrimary
import com.sarah.app.ui.theme.TextSecondary
import com.sarah.app.ui.theme.WarmAmber

import com.sarah.app.ui.components.QuickCaptureBottomSheet
import com.sarah.app.ui.screens.quickcapture.QuickCaptureViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import com.sarah.app.ui.components.SarahNextActionCard

@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    quickCaptureViewModel: QuickCaptureViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var isQuickCaptureOpen by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isQuickCaptureOpen = true },
                containerColor = ElectricIndigo,
                contentColor = TextPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Quick Add Task",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ElectricIndigo)
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Header Greeting
                item {
                    Column {
                        val studentName = uiState.userProfile?.name?.takeIf { it.isNotBlank() } ?: "Student"
                        Text(
                            text = "${uiState.greeting.uppercase()}, $studentName".uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "What should I do next?",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Your real-time academic operating plan for tonight",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                // Sarah's Next Move (Live Guidance Card)
                uiState.nextAction?.let { action ->
                    item {
                        SarahNextActionCard(
                            nextAction = action,
                            onPrimaryActionClick = { viewModel.handlePrimaryNextAction(it) },
                            onMarkCompletedClick = { viewModel.completeTaskById(it) }
                        )
                    }
                }

                // Feasibility Card (Hero)
                uiState.feasibilityReport?.let { report ->
                    item {
                        FeasibilityCard(report = report)
                    }
                }

                // Interactive Energy Level Picker
                item {
                    EnergyLevelPicker(
                        currentEnergy = uiState.energyLevel,
                        onEnergySelected = { viewModel.setEnergyLevel(it) }
                    )
                }

                // Section: Must Do Tonight
                uiState.feasibilityReport?.mustDoTasks?.let { mustDos ->
                    if (mustDos.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "MUST DO TONIGHT",
                                subtitle = "Urgent submissions & practicals",
                                accentColor = CoralRed
                            )
                        }
                        items(mustDos, key = { "must_${it.id}" }) { task ->
                            TaskCard(
                                task = task,
                                onStatusToggle = { viewModel.toggleTaskStatus(it) }
                            )
                        }
                    }
                }

                // Section: Should Do Tonight
                uiState.feasibilityReport?.shouldDoTasks?.let { shouldDos ->
                    if (shouldDos.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "SHOULD DO TONIGHT",
                                subtitle = "Important assignments & core revisions",
                                accentColor = WarmAmber
                            )
                        }
                        items(shouldDos, key = { "should_${it.id}" }) { task ->
                            TaskCard(
                                task = task,
                                onStatusToggle = { viewModel.toggleTaskStatus(it) }
                            )
                        }
                    }
                }

                // Section: Can Defer
                uiState.feasibilityReport?.canDeferTasks?.let { canDefers ->
                    if (canDefers.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "CAN DEFER",
                                subtitle = "Postponable reading / future tasks",
                                accentColor = TextMuted
                            )
                        }
                        items(canDefers, key = { "defer_${it.id}" }) { task ->
                            TaskCard(
                                task = task,
                                onStatusToggle = { viewModel.toggleTaskStatus(it) }
                            )
                        }
                    }
                }

                // Section: Suggested Evening Agenda
                uiState.feasibilityReport?.suggestedAgenda?.let { agenda ->
                    item {
                        AgendaTimeline(agendaItems = agenda)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }

        if (isQuickCaptureOpen) {
            QuickCaptureBottomSheet(
                viewModel = quickCaptureViewModel,
                onDismiss = { isQuickCaptureOpen = false }
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    accentColor: androidx.compose.ui.graphics.Color
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

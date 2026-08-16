package com.sarah.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.ui.theme.SarahBackground
import com.sarah.app.ui.theme.SarahOnSurface
import com.sarah.app.ui.theme.SarahOnSurfaceVariant
import com.sarah.app.ui.theme.SarahOutlineVariant
import com.sarah.app.ui.theme.SarahPrimary
import com.sarah.app.ui.theme.SarahPrimaryFixedDim
import com.sarah.app.ui.theme.SarahSecondary
import com.sarah.app.ui.theme.SarahSurfaceContainer
import com.sarah.app.ui.theme.SarahSurfaceContainerLowest

@Composable
fun ProfileScreenContent(
    uiState: ProfileUiState,
    onDeadlineRemindersToggled: (Boolean) -> Unit,
    onCustomRemindersToggled: (Boolean) -> Unit,
    onSaveProfile: (
        name: String,
        collegeName: String,
        department: String,
        semesterYear: String,
        defaultEnergy: EnergyLevel
    ) -> Unit,
    onNavigateToNotes: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = SarahPrimary)
        }
    } else {
        val profile = uiState.userProfile
        var name by remember(profile) { mutableStateOf(profile.name) }
        var collegeName by remember(profile) { mutableStateOf(profile.collegeName) }
        var department by remember(profile) { mutableStateOf(profile.department) }
        var semesterYear by remember(profile) { mutableStateOf(profile.semesterYear) }

        LaunchedEffect(uiState.isSaved) {
            if (uiState.isSaved) {
                snackbarHostState.showSnackbar("Profile updated successfully!")
            }
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(SarahBackground)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Banner
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SarahSurfaceContainerLowest)
                            .border(1.dp, SarahOutlineVariant, RoundedCornerShape(24.dp))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(SarahPrimary.copy(alpha = 0.2f))
                                .border(2.dp, SarahPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccountCircle,
                                contentDescription = null,
                                tint = SarahPrimary,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = profile.name.ifBlank { "Student Profile" },
                            style = MaterialTheme.typography.titleLarge,
                            color = SarahOnSurface,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "${profile.department.ifBlank { "Computer Science" }} • ${profile.collegeName.ifBlank { "University" }}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SarahOnSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SarahPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Verified,
                                contentDescription = null,
                                tint = SarahPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SARAH OS ACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                color = SarahPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Profile Fields Card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SarahSurfaceContainerLowest)
                            .border(1.dp, SarahOutlineVariant, RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "STUDENT DETAILS",
                            style = MaterialTheme.typography.labelSmall,
                            color = SarahPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Your Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SarahPrimary,
                                unfocusedBorderColor = SarahOutlineVariant,
                                focusedTextColor = SarahOnSurface,
                                unfocusedTextColor = SarahOnSurface,
                                focusedLabelColor = SarahPrimary,
                                unfocusedLabelColor = SarahSecondary
                            )
                        )

                        OutlinedTextField(
                            value = collegeName,
                            onValueChange = { collegeName = it },
                            label = { Text("College / University") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SarahPrimary,
                                unfocusedBorderColor = SarahOutlineVariant,
                                focusedTextColor = SarahOnSurface,
                                unfocusedTextColor = SarahOnSurface,
                                focusedLabelColor = SarahPrimary,
                                unfocusedLabelColor = SarahSecondary
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = department,
                                onValueChange = { department = it },
                                label = { Text("Department / Major") },
                                modifier = Modifier.weight(1.3f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SarahPrimary,
                                    unfocusedBorderColor = SarahOutlineVariant,
                                    focusedTextColor = SarahOnSurface,
                                    unfocusedTextColor = SarahOnSurface,
                                    focusedLabelColor = SarahPrimary,
                                    unfocusedLabelColor = SarahSecondary
                                )
                            )

                            OutlinedTextField(
                                value = semesterYear,
                                onValueChange = { semesterYear = it },
                                label = { Text("Year / Sem") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SarahPrimary,
                                    unfocusedBorderColor = SarahOutlineVariant,
                                    focusedTextColor = SarahOnSurface,
                                    unfocusedTextColor = SarahOnSurface,
                                    focusedLabelColor = SarahPrimary,
                                    unfocusedLabelColor = SarahSecondary
                                )
                            )
                        }
                    }
                }

                // Reminders & Notifications Settings Card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SarahSurfaceContainerLowest)
                            .border(1.dp, SarahOutlineVariant, RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "REMINDERS & NOTIFICATIONS",
                            style = MaterialTheme.typography.labelSmall,
                            color = SarahPrimaryFixedDim,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        // Deadline Reminders Switch Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Deadline Reminders",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SarahOnSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Alerts 1 day and 2 hours before submissions",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SarahSecondary
                                )
                            }
                            Switch(
                                checked = uiState.isDeadlineRemindersEnabled,
                                onCheckedChange = onDeadlineRemindersToggled,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SarahOnSurface,
                                    checkedTrackColor = SarahPrimary,
                                    uncheckedThumbColor = SarahSecondary,
                                    uncheckedTrackColor = SarahSurfaceContainer
                                )
                            )
                        }

                        // Custom Reminders Switch Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Custom Quick Reminders",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SarahOnSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Alerts for records, lab manuals, and notes",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SarahSecondary
                                )
                            }
                            Switch(
                                checked = uiState.isCustomRemindersEnabled,
                                onCheckedChange = onCustomRemindersToggled,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SarahOnSurface,
                                    checkedTrackColor = SarahPrimaryFixedDim,
                                    uncheckedThumbColor = SarahSecondary,
                                    uncheckedTrackColor = SarahSurfaceContainer
                                )
                            )
                        }

                        Text(
                            text = "Sarah uses notifications to remind you about assignments, exams, and things your teachers asked you to bring.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SarahOnSurfaceVariant,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Academic Notes & Classroom Memos Entry Card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SarahSurfaceContainerLowest)
                            .border(1.dp, SarahOutlineVariant, RoundedCornerShape(20.dp))
                            .clickable { onNavigateToNotes() }
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(SarahPrimary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Description,
                                        contentDescription = null,
                                        tint = SarahPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Academic Notes & Memos",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = SarahOnSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Classroom instructions, syllabus & tips",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SarahSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onNavigateToNotes,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SarahSurfaceContainer,
                                contentColor = SarahOnSurface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Open Academic Notes",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Architecture & Privacy Info
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SarahSurfaceContainer.copy(alpha = 0.5f))
                            .border(1.dp, SarahOutlineVariant, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                tint = SarahPrimaryFixedDim,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "About Sarah OS (Phase 4A)",
                                style = MaterialTheme.typography.labelMedium,
                                color = SarahPrimaryFixedDim,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Offline-first personal academic intelligence. Local Room database ensures all your academic records, constraints, and tasks remain strictly private on your device.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SarahOnSurfaceVariant
                        )
                    }
                }

                // Save Profile Button
                item {
                    Button(
                        onClick = {
                            onSaveProfile(
                                name.trim(),
                                collegeName.trim(),
                                department.trim(),
                                semesterYear.trim(),
                                profile.defaultEnergyLevel
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SarahPrimary,
                            contentColor = SarahOnSurface
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Save Profile",
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

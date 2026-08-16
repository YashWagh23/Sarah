@file:JvmName("ProfileScreenAndroidKt")
package com.sarah.app.ui.screens.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToNotes: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    ProfileScreenContent(
        uiState = uiState,
        onDeadlineRemindersToggled = { viewModel.setDeadlineRemindersEnabled(it) },
        onCustomRemindersToggled = { viewModel.setCustomRemindersEnabled(it) },
        onSaveProfile = { name, college, dept, sem, energy ->
            viewModel.saveProfile(name, college, dept, sem, energy)
        },
        onNavigateToNotes = onNavigateToNotes,
        modifier = modifier
    )
}

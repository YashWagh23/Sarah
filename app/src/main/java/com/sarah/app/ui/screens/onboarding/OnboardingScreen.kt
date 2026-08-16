@file:JvmName("OnboardingScreenAndroidKt")
package com.sarah.app.ui.screens.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    OnboardingScreenContent(
        uiState = uiState,
        onNameChange = { viewModel.updateName(it) },
        onCollegeChange = { viewModel.updateCollege(it) },
        onDepartmentChange = { viewModel.updateDepartment(it) },
        onSleepChange = { viewModel.updateSleepTime(it) },
        onCollegeHoursChange = { s, e -> viewModel.updateCollegeHours(s, e) },
        onNextStep = { viewModel.nextStep() },
        onPrevStep = { viewModel.prevStep() },
        onCompleteOnboarding = { viewModel.completeOnboarding() },
        onCompleted = onComplete,
        modifier = modifier
    )
}

@file:JvmName("ScheduleScreenAndroidKt")
package com.sarah.app.ui.screens.schedule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    ScheduleScreenContent(
        uiState = uiState,
        onUpdateSchedule = { wake, sleep, start, end, commute, dinner, brk, session ->
            viewModel.updateSchedule(wake, sleep, start, end, commute, dinner, brk, session)
        },
        modifier = modifier
    )
}

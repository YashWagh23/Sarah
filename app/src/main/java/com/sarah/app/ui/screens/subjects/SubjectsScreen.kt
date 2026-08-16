@file:JvmName("SubjectsScreenAndroidKt")
package com.sarah.app.ui.screens.subjects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
fun SubjectsScreen(
    viewModel: SubjectsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    SubjectsScreenContent(
        uiState = uiState,
        onOpenAddSubjectDialog = { viewModel.openAddSubjectDialog() },
        onOpenEditSubjectDialog = { viewModel.openEditSubjectDialog(it) },
        onCloseAddEditDialog = { viewModel.closeAddEditDialog() },
        onSaveSubject = { name, code, prof, color, hours, targetAtt, currAtt ->
            viewModel.saveSubject(name, code, prof, color, hours, targetAtt, currAtt)
        },
        onDeleteSubject = { viewModel.deleteSubject(it) },
        modifier = modifier
    )
}

package com.sarah.app.ui.screens.quickcapture

import com.sarah.app.domain.model.CaptureSourceType
import com.sarah.app.domain.model.ExtractedTaskDraft
import com.sarah.app.domain.model.Subject

enum class QuickCaptureTab(val title: String) {
    TEXT("Natural Text"),
    PDF("Import PDF"),
    IMAGE("Import Image")
}

data class QuickCaptureUiState(
    val selectedTab: QuickCaptureTab = QuickCaptureTab.TEXT,
    val naturalLanguageInput: String = "",
    val isProcessing: Boolean = false,
    val processingMessage: String = "",
    val draft: ExtractedTaskDraft? = null,
    val availableSubjects: List<Subject> = emptyList(),
    val isSavedSuccess: Boolean = false,
    val errorMessage: String? = null
)

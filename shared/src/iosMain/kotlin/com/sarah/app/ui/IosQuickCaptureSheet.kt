package com.sarah.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sarah.app.di.SarahAppContainer
import com.sarah.app.domain.model.CaptureSourceType
import com.sarah.app.domain.model.ExtractedTaskDraft
import com.sarah.app.domain.model.Subject
import com.sarah.app.media.IosMediaPickerBridge
import com.sarah.app.ui.components.ExtractedTaskReviewCard
import com.sarah.app.ui.screens.quickcapture.QuickCaptureTab
import com.sarah.app.ui.theme.SarahBackground
import com.sarah.app.ui.theme.SarahError
import com.sarah.app.ui.theme.SarahErrorContainer
import com.sarah.app.ui.theme.SarahOnPrimary
import com.sarah.app.ui.theme.SarahOnSurface
import com.sarah.app.ui.theme.SarahOnSurfaceVariant
import com.sarah.app.ui.theme.SarahOnTertiary
import com.sarah.app.ui.theme.SarahPrimary
import com.sarah.app.ui.theme.SarahPrimaryFixed
import com.sarah.app.ui.theme.SarahSecondary
import com.sarah.app.ui.theme.SarahSecondaryContainer
import com.sarah.app.ui.theme.SarahSurfaceContainer
import com.sarah.app.ui.theme.SarahSurfaceContainerHigh
import com.sarah.app.ui.theme.SarahSurfaceContainerLowest
import com.sarah.app.ui.theme.SarahTertiary
import com.sarah.app.ui.theme.SarahTertiaryContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun IosQuickCaptureSheet(
    container: SarahAppContainer,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val subjects by container.subjectRepository.getActiveSubjects().collectAsState(initial = emptyList())
    val mediaPicker = remember { IosMediaPickerBridge() }

    var selectedAction by remember { mutableStateOf<QuickCaptureTab?>(QuickCaptureTab.TEXT) }
    var naturalLanguageInput by remember { mutableStateOf("") }
    var draft by remember { mutableStateOf<ExtractedTaskDraft?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var processingMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var panelVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(30)
        panelVisible = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = panelVisible,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(280)) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200)) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(SarahSurfaceContainerLowest)
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                        )
                        .clickable(enabled = false) {}
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Drag Handle
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(SarahOnSurfaceVariant.copy(alpha = 0.25f))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Quick Capture",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = SarahOnSurface
                            )
                            Text(
                                text = "AI-powered parsing for college tasks",
                                style = MaterialTheme.typography.bodySmall,
                                color = SarahOnSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SarahSurfaceContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Close",
                                tint = SarahOnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3-Col Action Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IosQuickActionButton(
                            icon = Icons.Outlined.AutoAwesome,
                            label = "Task",
                            sublabel = "NLP Parsed",
                            containerColor = SarahPrimaryFixed,
                            contentColor = SarahPrimary,
                            isActive = selectedAction == QuickCaptureTab.TEXT,
                            onClick = { selectedAction = QuickCaptureTab.TEXT },
                            modifier = Modifier.weight(1f)
                        )
                        IosQuickActionButton(
                            icon = Icons.Outlined.EditNote,
                            label = "Note",
                            sublabel = "Class Memo",
                            containerColor = SarahTertiaryContainer,
                            contentColor = SarahOnTertiary,
                            isActive = selectedAction == QuickCaptureTab.NOTE,
                            onClick = { selectedAction = QuickCaptureTab.NOTE },
                            modifier = Modifier.weight(1f)
                        )
                        IosQuickActionButton(
                            icon = Icons.Outlined.NotificationsActive,
                            label = "Reminder",
                            sublabel = "Quick Alert",
                            containerColor = SarahSecondaryContainer,
                            contentColor = SarahSecondary,
                            isActive = selectedAction == QuickCaptureTab.REMINDER,
                            onClick = { selectedAction = QuickCaptureTab.REMINDER },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Import PDF and Photo Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IosImportActionButton(
                            icon = Icons.Outlined.PictureAsPdf,
                            label = "Import PDF",
                            onClick = {
                                selectedAction = QuickCaptureTab.PDF
                                isProcessing = true
                                processingMessage = "Reading PDF document with PDFKit..."
                                errorMessage = null
                                mediaPicker.pickPdf { bytes ->
                                    if (bytes != null && bytes.isNotEmpty()) {
                                        coroutineScope.launch {
                                            try {
                                                val text = container.documentTextExtractor.extractFromPdf(bytes)
                                                if (text.isNotBlank()) {
                                                    draft = container.naturalLanguageTaskParser.parse(
                                                        rawText = text,
                                                        availableSubjects = subjects,
                                                        sourceType = CaptureSourceType.PDF_DOCUMENT
                                                    )
                                                    naturalLanguageInput = text.take(160)
                                                } else {
                                                    errorMessage = "Unable to extract text from the PDF."
                                                }
                                            } catch (e: Exception) {
                                                errorMessage = "PDF import failed: ${e.message}"
                                            } finally {
                                                isProcessing = false
                                            }
                                        }
                                    } else {
                                        isProcessing = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        IosImportActionButton(
                            icon = Icons.Outlined.Image,
                            label = "Import Photo",
                            onClick = {
                                selectedAction = QuickCaptureTab.IMAGE
                                isProcessing = true
                                processingMessage = "Scanning image with Vision OCR..."
                                errorMessage = null
                                mediaPicker.pickImage { bytes ->
                                    if (bytes != null && bytes.isNotEmpty()) {
                                        coroutineScope.launch {
                                            try {
                                                val text = container.documentTextExtractor.extractFromImage(bytes)
                                                if (text.isNotBlank()) {
                                                    draft = container.naturalLanguageTaskParser.parse(
                                                        rawText = text,
                                                        availableSubjects = subjects,
                                                        sourceType = CaptureSourceType.IMAGE_GALLERY
                                                    )
                                                    naturalLanguageInput = text.take(160)
                                                } else {
                                                    errorMessage = "No readable text detected in photo."
                                                }
                                            } catch (e: Exception) {
                                                errorMessage = "Photo scan failed: ${e.message}"
                                            } finally {
                                                isProcessing = false
                                            }
                                        }
                                    } else {
                                        isProcessing = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Processing Indicator
                    if (isProcessing) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SarahSurfaceContainerHigh)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = SarahPrimary,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = processingMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = SarahOnSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Error Message
                    errorMessage?.let { error ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SarahErrorContainer)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Warning,
                                contentDescription = null,
                                tint = SarahError,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = SarahError
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Text Input
                    if (selectedAction == QuickCaptureTab.TEXT || selectedAction == null) {
                        OutlinedTextField(
                            value = naturalLanguageInput,
                            onValueChange = { input ->
                                naturalLanguageInput = input
                                if (input.trim().length >= 3) {
                                    draft = container.naturalLanguageTaskParser.parse(
                                        rawText = input,
                                        availableSubjects = subjects,
                                        sourceType = CaptureSourceType.NATURAL_LANGUAGE
                                    )
                                } else {
                                    draft = null
                                }
                            },
                            placeholder = {
                                Text(
                                    text = "e.g. Sir gave 3 Java programs for OS. Submit Monday 5pm. Urgent!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SarahOnSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SarahSurfaceContainerLowest,
                                unfocusedContainerColor = SarahSurfaceContainerLowest,
                                focusedBorderColor = SarahPrimary,
                                unfocusedBorderColor = SarahSurfaceContainerHigh
                            ),
                            minLines = 3,
                            maxLines = 5
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Extracted Draft Review Card
                    draft?.let { currentDraft ->
                        ExtractedTaskReviewCard(
                            draft = currentDraft,
                            availableSubjects = subjects,
                            onTitleChange = { draft = currentDraft.copy(title = it) },
                            onSubjectChange = { subjId ->
                                val subj = subjects.find { it.id == subjId }
                                draft = currentDraft.copy(subjectId = subjId, subjectName = subj?.name ?: currentDraft.subjectName)
                            },
                            onTypeChange = { draft = currentDraft.copy(type = it) },
                            onPriorityChange = { draft = currentDraft.copy(priority = it) },
                            onEstimatedMinutesChange = { draft = currentDraft.copy(estimatedMinutes = it) },
                            onSave = {
                                coroutineScope.launch {
                                    val task = currentDraft.toTask()
                                    val newId = container.taskRepository.insertTask(task)
                                    if (container.preferences.isDeadlineRemindersEnabled) {
                                        val reminders = container.deadlineReminderEngine.generateDeadlineReminders(task.copy(id = newId))
                                        reminders.forEach { rem ->
                                            val remId = container.reminderRepository.insertReminder(rem)
                                            container.reminderScheduler.scheduleReminder(rem.copy(id = remId))
                                        }
                                    }
                                    onDismiss()
                                }
                            },
                            onDiscard = {
                                draft = null
                                naturalLanguageInput = ""
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun IosQuickActionButton(
    icon: ImageVector,
    label: String,
    sublabel: String,
    containerColor: Color,
    contentColor: Color,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isActive) containerColor else SarahSurfaceContainer)
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = if (isActive) contentColor.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) contentColor else SarahOnSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isActive) contentColor else SarahOnSurface
        )
        Text(
            text = sublabel,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            color = if (isActive) contentColor.copy(alpha = 0.8f) else SarahOnSurfaceVariant
        )
    }
}

@Composable
private fun IosImportActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SarahSurfaceContainer)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = SarahPrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = SarahOnSurface
        )
    }
}

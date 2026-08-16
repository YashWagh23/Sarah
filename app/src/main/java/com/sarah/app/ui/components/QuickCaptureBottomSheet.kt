package com.sarah.app.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sarah.app.ui.screens.quickcapture.QuickCaptureTab
import com.sarah.app.ui.screens.quickcapture.QuickCaptureViewModel
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

/**
 * Full-screen Quick Capture overlay matching the reference design:
 * - Dimmed + slightly blurred scrim
 * - Glass panel slides up from bottom with staggered item animations
 * - 3-col action grid: Task (primary), Note (tertiary), Reminder (secondary)
 * - 2-col row: Import PDF, Import Image
 * - Inline natural-language text input (shown when Task is tapped)
 * - Processing / Error states
 * - Dismiss X button + close tap outside
 */
@Composable
fun QuickCaptureBottomSheet(
    viewModel: QuickCaptureViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Track which action is selected for inline input
    var selectedAction by remember { mutableStateOf<QuickCaptureTab?>(null) }

    // Stagger visibility for panel entrance
    var panelVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(30)
        panelVisible = true
    }

    // File launchers (preserved from original)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedAction = QuickCaptureTab.IMAGE
            viewModel.processImageUri(context, it)
        }
    }
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedAction = QuickCaptureTab.PDF
            viewModel.processPdfUri(context, it)
        }
    }

    Dialog(
        onDismissRequest = {
            viewModel.reset()
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        // Full-screen scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(indication = null, interactionSource = remember {
                    androidx.compose.foundation.interaction.MutableInteractionSource()
                }) {
                    viewModel.reset()
                    onDismiss()
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            // Glass panel — slides in from bottom
            AnimatedVisibility(
                visible = panelVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec  = tween(380)
                ) + fadeIn(animationSpec = tween(250)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(280)
                ) + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(indication = null, interactionSource = remember {
                            androidx.compose.foundation.interaction.MutableInteractionSource()
                        }) { /* consume click so tapping inside doesn't close */ }
                        .shadow(
                            elevation   = 24.dp,
                            shape       = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                            ambientColor = Color.Black.copy(alpha = 0.08f),
                            spotColor   = Color.Black.copy(alpha = 0.12f)
                        )
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(Color.White.copy(alpha = 0.82f))
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.65f),
                            RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                            .padding(top = 20.dp, bottom = 40.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        // ── Header ──────────────────────────────────────────
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector        = Icons.Outlined.AutoAwesome,
                                        contentDescription = null,
                                        tint               = SarahPrimary,
                                        modifier           = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text          = "QUICK CAPTURE",
                                        style         = MaterialTheme.typography.labelSmall,
                                        color         = SarahPrimary,
                                        fontWeight    = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Text(
                                    text       = "Capture College Workload",
                                    style      = MaterialTheme.typography.headlineSmall,
                                    color      = SarahOnSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = {
                                viewModel.reset()
                                onDismiss()
                            }) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(SarahSurfaceContainerHigh),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector        = Icons.Outlined.Close,
                                        contentDescription = "Close",
                                        tint               = SarahSecondary,
                                        modifier           = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // ── 3-col primary action grid ────────────────────────
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CaptureActionButton(
                                icon         = Icons.Outlined.EditNote,
                                label        = "Task",
                                bg           = SarahPrimary.copy(alpha = 0.10f),
                                iconTint     = SarahPrimary,
                                isActive     = selectedAction == QuickCaptureTab.TEXT,
                                onClick      = {
                                    selectedAction = QuickCaptureTab.TEXT
                                    viewModel.selectTab(QuickCaptureTab.TEXT)
                                },
                                modifier     = Modifier.weight(1f)
                            )
                            CaptureActionButton(
                                icon         = Icons.Outlined.AutoAwesome,
                                label        = "Note",
                                bg           = SarahTertiary.copy(alpha = 0.10f),
                                iconTint     = SarahTertiary,
                                isActive     = selectedAction == null,
                                onClick      = {
                                    selectedAction = null
                                    viewModel.selectTab(QuickCaptureTab.TEXT)
                                },
                                modifier     = Modifier.weight(1f)
                            )
                            CaptureActionButton(
                                icon         = Icons.Outlined.NotificationsActive,
                                label        = "Reminder",
                                bg           = SarahSecondary.copy(alpha = 0.08f),
                                iconTint     = SarahSecondary,
                                isActive     = false,
                                onClick      = { /* Future: open add reminder directly */ },
                                modifier     = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // ── 2-col import row ──────────────────────────────────
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ImportCard(
                                icon     = Icons.Outlined.PictureAsPdf,
                                label    = "Import PDF",
                                subLabel = "Assignment sheets, syllabus",
                                iconBg   = SarahError.copy(alpha = 0.10f),
                                iconTint = SarahError,
                                onClick  = { pdfPickerLauncher.launch("application/pdf") },
                                modifier = Modifier.weight(1f)
                            )
                            ImportCard(
                                icon     = Icons.Outlined.Image,
                                label    = "Import Image",
                                subLabel = "Board photos, notices",
                                iconBg   = SarahPrimary.copy(alpha = 0.10f),
                                iconTint = SarahPrimary,
                                onClick  = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // ── Inline text input (shown when Task is selected) ───
                        AnimatedVisibility(
                            visible = selectedAction == QuickCaptureTab.TEXT,
                            enter = fadeIn() + slideInVertically(),
                            exit  = fadeOut() + slideOutVertically()
                        ) {
                            Column {
                                Spacer(Modifier.height(16.dp))
                                OutlinedTextField(
                                    value         = uiState.naturalLanguageInput,
                                    onValueChange = { viewModel.updateNaturalLanguageInput(it) },
                                    label         = { Text("What did you get from college?") },
                                    placeholder   = { Text("e.g., Sir gave 3 Java programs. Submit Monday.") },
                                    modifier      = Modifier.fillMaxWidth(),
                                    colors        = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor   = SarahPrimary,
                                        unfocusedBorderColor = SarahSurfaceContainerHigh,
                                        focusedLabelColor    = SarahPrimary,
                                        unfocusedLabelColor  = SarahOnSurfaceVariant
                                    ),
                                    maxLines = 4
                                )
                                Spacer(Modifier.height(8.dp))
                                // Suggestion chips
                                Text(
                                    text  = "Try an example:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SarahOnSurfaceVariant
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(
                                        "Sir gave 3 Java programs. Submit Monday.",
                                        "DBMS assignment due Friday by 5 PM",
                                        "OS practical record book before Wednesday"
                                    ).forEach { example ->
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(SarahSurfaceContainer)
                                                .clickable { viewModel.updateNaturalLanguageInput(example) }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text  = example,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SarahOnSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ── Processing indicator ─────────────────────────────
                        if (uiState.isProcessing) {
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SarahPrimary.copy(alpha = 0.08f))
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(20.dp),
                                    color       = SarahPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text  = uiState.processingMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SarahOnSurface
                                )
                            }
                        }

                        // ── Error message ────────────────────────────────────
                        uiState.errorMessage?.let { error ->
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SarahErrorContainer.copy(alpha = 0.5f))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector        = Icons.Outlined.Warning,
                                    contentDescription = null,
                                    tint               = SarahError,
                                    modifier           = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text  = error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SarahOnSurface
                                )
                            }
                        }

                        // ── Extracted Task Review Card ───────────────────────
                        uiState.draft?.let { draft ->
                            Spacer(Modifier.height(18.dp))
                            ExtractedTaskReviewCard(
                                draft             = draft,
                                availableSubjects = uiState.availableSubjects,
                                onUpdateDraft     = { title, subjId, type, desc, deadline, duration, priority, diff, energy ->
                                    viewModel.updateDraftFields(title, subjId, type, desc, deadline, duration, priority, diff, energy)
                                },
                                onConfirm         = {
                                    viewModel.saveDraftToPlan { onDismiss() }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Composable helpers ────────────────────────────────────────────────────────

@Composable
private fun CaptureActionButton(
    icon    : ImageVector,
    label   : String,
    bg      : Color,
    iconTint: Color,
    isActive: Boolean,
    onClick : () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isActive) bg.copy(alpha = bg.alpha * 1.6f) else SarahSurfaceContainerLowest)
            .border(
                1.dp,
                if (isActive) iconTint.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.04f),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = iconTint,
                modifier           = Modifier.size(24.dp)
            )
        }
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelMedium,
            color      = if (isActive) iconTint else SarahOnSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ImportCard(
    icon    : ImageVector,
    label   : String,
    subLabel: String,
    iconBg  : Color,
    iconTint: Color,
    onClick : () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SarahSurfaceContainerLowest)
            .border(1.dp, Color.Black.copy(alpha = 0.04f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = iconTint,
                modifier           = Modifier.size(20.dp)
            )
        }
        Text(
            text       = label,
            style      = MaterialTheme.typography.titleSmall,
            color      = SarahOnSurface,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text  = subLabel,
            style = MaterialTheme.typography.bodySmall,
            color = SarahOnSurfaceVariant
        )
    }
}

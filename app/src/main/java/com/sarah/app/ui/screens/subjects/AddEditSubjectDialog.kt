package com.sarah.app.ui.screens.subjects

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sarah.app.domain.model.Subject
import com.sarah.app.ui.theme.CoralRed
import com.sarah.app.ui.theme.DarkBorder
import com.sarah.app.ui.theme.DarkSurface
import com.sarah.app.ui.theme.DarkSurfaceVariant
import com.sarah.app.ui.theme.ElectricIndigo
import com.sarah.app.ui.theme.MintEmerald
import com.sarah.app.ui.theme.TextMuted
import com.sarah.app.ui.theme.TextPrimary
import com.sarah.app.ui.theme.TextSecondary
import com.sarah.app.ui.theme.WarmAmber
import kotlin.math.roundToInt

@Composable
fun AddEditSubjectDialog(
    subject: Subject?,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        code: String,
        professorName: String,
        colorHex: String,
        weeklyHours: Int,
        targetAttendancePercentage: Int,
        currentAttendancePercentage: Int
    ) -> Unit,
    onDelete: (Subject) -> Unit = {}
) {
    var name by remember { mutableStateOf(subject?.name ?: "") }
    var code by remember { mutableStateOf(subject?.code ?: "") }
    var professorName by remember { mutableStateOf(subject?.professorName ?: "") }
    var selectedColorHex by remember { mutableStateOf(subject?.colorHex ?: "#7C4DFF") }
    var weeklyHours by remember { mutableFloatStateOf((subject?.weeklyHours ?: 4).toFloat()) }
    var targetAttendance by remember { mutableFloatStateOf((subject?.targetAttendancePercentage ?: 75).toFloat()) }
    var currentAttendance by remember { mutableFloatStateOf((subject?.currentAttendancePercentage ?: 85).toFloat()) }

    val colorPalette = listOf(
        "#7C4DFF", "#3B82F6", "#10B981", "#F59E0B",
        "#EC4899", "#8B5CF6", "#14B8A6", "#F97316"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(24.dp)),
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (subject == null) "New Subject" else "Edit Subject",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Subject Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Name *") },
                    placeholder = { Text("e.g. Operating Systems") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricIndigo,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = ElectricIndigo,
                        unfocusedLabelColor = TextMuted
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subject Code & Professor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Code") },
                        placeholder = { Text("CS301") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricIndigo,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = ElectricIndigo,
                            unfocusedLabelColor = TextMuted
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = professorName,
                        onValueChange = { professorName = it },
                        label = { Text("Professor") },
                        placeholder = { Text("Prof. Sharma") },
                        modifier = Modifier.weight(1.4f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricIndigo,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = ElectricIndigo,
                            unfocusedLabelColor = TextMuted
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Color Palette
                Text(
                    text = "ACCENT COLOR",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colorPalette.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = hex.equals(selectedColorHex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    if (isSelected) 3.dp else 1.dp,
                                    if (isSelected) TextPrimary else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { selectedColorHex = hex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Weekly Hours
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "WEEKLY HOURS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${weeklyHours.roundToInt()} hrs / week",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = weeklyHours,
                    onValueChange = { weeklyHours = it },
                    valueRange = 1f..12f,
                    steps = 10,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricIndigo,
                        activeTrackColor = ElectricIndigo,
                        inactiveTrackColor = DarkSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Current Attendance %
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "CURRENT ATTENDANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${currentAttendance.roundToInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (currentAttendance >= targetAttendance) MintEmerald else WarmAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = currentAttendance,
                    onValueChange = { currentAttendance = it },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = if (currentAttendance >= targetAttendance) MintEmerald else WarmAmber,
                        activeTrackColor = if (currentAttendance >= targetAttendance) MintEmerald else WarmAmber,
                        inactiveTrackColor = DarkSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (subject != null) {
                        IconButton(onClick = { onDelete(subject) }) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Delete Subject",
                                tint = CoralRed
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    onSave(
                                        name.trim(),
                                        code.trim(),
                                        professorName.trim(),
                                        selectedColorHex,
                                        weeklyHours.roundToInt(),
                                        targetAttendance.roundToInt(),
                                        currentAttendance.roundToInt()
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricIndigo,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = name.isNotBlank()
                        ) {
                            Text(if (subject == null) "Create Subject" else "Save Changes")
                        }
                    }
                }
            }
        }
    }
}

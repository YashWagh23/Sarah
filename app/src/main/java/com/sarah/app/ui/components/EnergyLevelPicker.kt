package com.sarah.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Battery0Bar
import androidx.compose.material.icons.rounded.Battery3Bar
import androidx.compose.material.icons.rounded.Battery5Bar
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.ui.theme.CoralRed
import com.sarah.app.ui.theme.CyanAccent
import com.sarah.app.ui.theme.DarkBorder
import com.sarah.app.ui.theme.DarkSurface
import com.sarah.app.ui.theme.DarkSurfaceVariant
import com.sarah.app.ui.theme.MintEmerald
import com.sarah.app.ui.theme.TextMuted
import com.sarah.app.ui.theme.TextPrimary
import com.sarah.app.ui.theme.TextSecondary
import com.sarah.app.ui.theme.WarmAmber

@Composable
fun EnergyLevelPicker(
    currentEnergy: EnergyLevel,
    onEnergySelected: (EnergyLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CURRENT ENERGY",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanAccent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = currentEnergy.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "Focus: ${(currentEnergy.focusMultiplier * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                color = getEnergyColor(currentEnergy)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EnergyLevel.values().forEach { level ->
                val isSelected = level == currentEnergy
                val targetBg = if (isSelected) getEnergyColor(level).copy(alpha = 0.2f) else DarkSurfaceVariant
                val targetBorder = if (isSelected) getEnergyColor(level) else Color.Transparent
                val animatedBg by animateColorAsState(targetValue = targetBg, animationSpec = spring(), label = "energyBg")
                val animatedBorder by animateColorAsState(targetValue = targetBorder, animationSpec = spring(), label = "energyBorder")

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(animatedBg)
                        .border(1.dp, animatedBorder, RoundedCornerShape(12.dp))
                        .clickable { onEnergySelected(level) }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = getEnergyIcon(level),
                        contentDescription = level.displayName,
                        tint = if (isSelected) getEnergyColor(level) else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = when (level) {
                            EnergyLevel.HIGH -> "High"
                            EnergyLevel.NORMAL -> "Normal"
                            EnergyLevel.LOW -> "Low"
                            EnergyLevel.EXHAUSTED -> "Tired"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

fun getEnergyColor(level: EnergyLevel): Color {
    return when (level) {
        EnergyLevel.HIGH -> CyanAccent
        EnergyLevel.NORMAL -> MintEmerald
        EnergyLevel.LOW -> WarmAmber
        EnergyLevel.EXHAUSTED -> CoralRed
    }
}

fun getEnergyIcon(level: EnergyLevel): ImageVector {
    return when (level) {
        EnergyLevel.HIGH -> Icons.Rounded.BatteryChargingFull
        EnergyLevel.NORMAL -> Icons.Rounded.Battery5Bar
        EnergyLevel.LOW -> Icons.Rounded.Battery3Bar
        EnergyLevel.EXHAUSTED -> Icons.Rounded.Battery0Bar
    }
}

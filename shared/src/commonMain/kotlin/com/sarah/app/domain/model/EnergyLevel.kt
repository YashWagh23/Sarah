package com.sarah.app.domain.model

enum class EnergyLevel(
    val displayName: String,
    val focusMultiplier: Float,
    val recommendedBreakIntervalMinutes: Int
) {
    HIGH("High Energy", 1.0f, 50),
    NORMAL("Normal Energy", 0.85f, 45),
    LOW("Low Energy", 0.65f, 30),
    EXHAUSTED("Exhausted", 0.45f, 20)
}

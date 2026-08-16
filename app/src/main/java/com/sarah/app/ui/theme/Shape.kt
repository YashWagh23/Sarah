package com.sarah.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),   // tags / badges
    small      = RoundedCornerShape(8.dp),   // chips / small cards
    medium     = RoundedCornerShape(12.dp),  // filter chips, action rows
    large      = RoundedCornerShape(16.dp),  // task cards, nav bar
    extraLarge = RoundedCornerShape(24.dp),  // hero cards, subject cards, glass panels
)

package com.sarah.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─── Font Family ────────────────────────────────────────────────────────────
val ManropeFontFamily = FontFamily.Default

// ─── Typography (matches Stitch reference exactly) ──────────────────────────
//
//  display            : 40sp / 48sp / Bold  / -0.02em
//  headlineLarge      : 28sp / 34sp / Bold  / -0.01em   (desktop)
//  headlineMedium     : 24sp / 30sp / Bold  / -0.01em   (mobile headline-lg)
//  headlineSmall      : 20sp / 26sp / SemiBold
//  titleLarge         : 17sp / 24sp / Normal             (body-lg)
//  titleMedium        : 15sp / 21sp / Normal             (body-md)
//  labelSmall         : 12sp / 16sp / Medium / +0.01em

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily   = ManropeFontFamily,
        fontWeight   = FontWeight.Bold,
        fontSize     = 40.sp,
        lineHeight   = 48.sp,
        letterSpacing = (-0.8).sp  // -0.02em @ 40sp
    ),
    displayMedium = TextStyle(
        fontFamily   = ManropeFontFamily,
        fontWeight   = FontWeight.Bold,
        fontSize     = 32.sp,
        lineHeight   = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    displaySmall = TextStyle(
        fontFamily   = ManropeFontFamily,
        fontWeight   = FontWeight.Bold,
        fontSize     = 28.sp,
        lineHeight   = 34.sp,
        letterSpacing = (-0.28).sp  // -0.01em @ 28sp
    ),
    headlineLarge = TextStyle(
        fontFamily   = ManropeFontFamily,
        fontWeight   = FontWeight.Bold,
        fontSize     = 28.sp,
        lineHeight   = 34.sp,
        letterSpacing = (-0.28).sp
    ),
    headlineMedium = TextStyle(
        fontFamily   = ManropeFontFamily,
        fontWeight   = FontWeight.Bold,
        fontSize     = 24.sp,
        lineHeight   = 30.sp,
        letterSpacing = (-0.24).sp
    ),
    headlineSmall = TextStyle(
        fontFamily   = ManropeFontFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 20.sp,
        lineHeight   = 26.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily   = ManropeFontFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 17.sp,
        lineHeight   = 24.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily   = ManropeFontFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 16.sp,
        lineHeight   = 22.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily   = ManropeFontFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 14.sp,
        lineHeight   = 20.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily   = ManropeFontFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 17.sp,
        lineHeight   = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily   = ManropeFontFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 15.sp,
        lineHeight   = 21.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily   = ManropeFontFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 13.sp,
        lineHeight   = 18.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily   = ManropeFontFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 14.sp,
        lineHeight   = 20.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily   = ManropeFontFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 12.sp,
        lineHeight   = 16.sp,
        letterSpacing = (0.12).sp
    ),
    labelSmall = TextStyle(
        fontFamily   = ManropeFontFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 12.sp,
        lineHeight   = 16.sp,
        letterSpacing = (0.12).sp  // +0.01em @ 12sp
    ),
)

package com.sarah.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

@Composable
fun SarahApp() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9FB)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Sarah — AI College Companion",
            style = MaterialTheme.typography.titleLarge
        )
    }
}

fun MainViewController(): UIViewController = ComposeUIViewController {
    SarahApp()
}

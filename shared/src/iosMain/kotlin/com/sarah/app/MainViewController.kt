package com.sarah.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.sarah.app.data.IosSarahAppContainer
import com.sarah.app.di.SarahAppContainer
import com.sarah.app.ui.IosAppNavigation
import com.sarah.app.ui.theme.SarahTheme
import platform.UIKit.UIViewController

@Composable
fun SarahIosApp(
    container: SarahAppContainer = remember { IosSarahAppContainer() }
) {
    SarahTheme {
        IosAppNavigation(container = container)
    }
}

fun MainViewController(): UIViewController = ComposeUIViewController {
    SarahIosApp()
}

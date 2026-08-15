package com.sarah.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sarah.app.ui.navigation.AppNavigation
import com.sarah.app.ui.theme.SarahTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as SarahApp

        setContent {
            SarahTheme {
                AppNavigation(app = app)
            }
        }
    }
}

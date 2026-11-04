package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.example.ui.MainLauncherScreen
import com.example.ui.theme.SideLauncherTheme
import com.example.ui.viewmodel.LauncherViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            val accentColor = try {
                Color(android.graphics.Color.parseColor(uiState.settings.accentColorHex))
            } catch (e: Exception) {
                Color(0xFF6C8CFF)
            }

            SideLauncherTheme(accentColor = accentColor) {
                MainLauncherScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Per specification: Home button on remote triggers onNewIntent (singleTask),
        // closing all overlays and returning to Home page with focus on first element.
        if (Intent.ACTION_MAIN == intent.action && intent.hasCategory(Intent.CATEGORY_HOME)) {
            viewModel.onHomeKeyPressed()
        } else {
            viewModel.onHomeKeyPressed()
        }
    }
}

package com.elendheim.melodymaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elendheim.melodymaker.ui.MelodyScreen
import com.elendheim.melodymaker.ui.theme.MelodyMakerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            val melodyViewModel: MelodyViewModel = viewModel()
            MelodyMakerTheme(
                largeText = melodyViewModel.largeText,
                highContrast = melodyViewModel.highContrast
            ) {
                MelodyScreen(melodyViewModel)
            }
        }
    }
}

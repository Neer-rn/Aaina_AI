package com.example.aainaai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.aainaai.ui.navigation.KYCNavGraph
import com.example.aainaai.ui.theme.AainaAITheme

/**
 * Main Activity for Nepali Citizenship KYC Verification App
 * Phase 1: Mobile Frontend with Camera + Liveness Detection
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AainaAITheme {
                KYCApp()
            }
        }
    }
}

@Composable
fun KYCApp() {
    val navController = rememberNavController()

    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxSize(),
        color = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) {
        KYCNavGraph(navController = navController)
    }
}
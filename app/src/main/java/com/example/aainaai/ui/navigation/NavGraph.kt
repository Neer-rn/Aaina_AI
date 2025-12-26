package com.example.aainaai.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.aainaai.ui.auth.MobileAuthScreen
import com.example.aainaai.ui.form.PersonalDetailsScreen
import com.example.aainaai.ui.citizenship.CitizenshipScanScreen
import com.example.aainaai.ui.liveness.LivenessScreen
import com.example.aainaai.ui.onboarding.OnboardingScreen

/**
 * Navigation routes for the KYC app
 */
sealed class Screen(val route: String) {
    object MobileAuth : Screen("mobile_auth")
    object Onboarding : Screen("onboarding")
    object PersonalDetails : Screen("personal_details")
    object CitizenshipScan : Screen("citizenship_scan")
    object Liveness : Screen("liveness")
    object Success : Screen("success")
}

/**
 * Main navigation graph with animated transitions
 */
@Composable
fun KYCNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Onboarding.route
) {
    NavHost(
        navController = navController,
        startDestination = Screen.MobileAuth.route, // Start with Mobile Auth
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { 1000 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -1000 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -1000 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { 1000 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        // Mobile Auth Screen (New Start)
        composable(Screen.MobileAuth.route) {
            MobileAuthScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.MobileAuth.route) { inclusive = true }
                    }
                }
            )
        }

        // Onboarding Screen
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onStartClick = {
                    navController.navigate(Screen.PersonalDetails.route)
                }
            )
        }

        // Personal Details Form
        composable(Screen.PersonalDetails.route) {
            PersonalDetailsScreen(
                onDetailsSubmitted = {
                    navController.navigate(Screen.CitizenshipScan.route)
                }
            )
        }

        // Citizenship Card Scan Screen
        composable(Screen.CitizenshipScan.route) {
            CitizenshipScanScreen(
                onCardCaptured = {
                    navController.navigate(Screen.Liveness.route)
                }
            )
        }

        // Active Liveness Detection Screen
        composable(Screen.Liveness.route) {
            LivenessScreen(
                onLivenessComplete = {
                    navController.navigate(Screen.Success.route) {
                        // Clear backstack to prevent going back
                        popUpTo(Screen.MobileAuth.route) { // Clear all way back to start
                            inclusive = true
                        }
                    }
                }
            )
        }

        // Success Screen (Placeholder for now)
        composable(Screen.Success.route) {
            SuccessScreen()
        }
    }
}

/**
 * Temporary success screen placeholder
 * TODO: Replace with actual success screen in Phase 2
 */
@Composable
private fun SuccessScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )

            Text(
                text = "Verification Complete!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Your citizenship has been successfully verified.\nAPI integration will be added in Phase 2.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

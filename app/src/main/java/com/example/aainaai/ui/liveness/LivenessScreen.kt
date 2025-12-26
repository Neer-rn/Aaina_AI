package com.example.aainaai.ui.liveness

import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aainaai.camera.CameraManager
import com.example.aainaai.camera.FaceImageAnalyzer
import com.example.aainaai.ui.components.CameraOverlay
import com.example.aainaai.ui.components.CutoutType
import com.example.aainaai.ui.theme.OfficialGold
import com.example.aainaai.ui.theme.SuccessGreen
import com.example.aainaai.util.rememberHapticFeedback
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Active Liveness Detection Screen
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LivenessScreen(
    onLivenessComplete: () -> Unit,
    viewModel: LivenessViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()

    // Remember these across recompositions
    val cameraManager = remember { CameraManager(context) }
    val faceAnalyzer = remember {
        FaceImageAnalyzer { result ->
            viewModel.processFaceAnalysis(result)
        }
    }

    // Track when PreviewView is ready
    val previewViewReady = remember { mutableStateOf<PreviewView?>(null) }
    val cameraInitialized = remember { mutableStateOf(false) }

    // Track previous challenge for haptics
    var previousChallenge by remember { mutableStateOf<LivenessChallenge>(LivenessChallenge.Initializing) }

    // Haptic effects
    LaunchedEffect(uiState.challenge) {
        if (previousChallenge != uiState.challenge &&
            previousChallenge !is LivenessChallenge.Initializing) {
            haptic.success()
        }
        previousChallenge = uiState.challenge
        
        // Handle Auto-Capture logic
        if (uiState.challenge is LivenessChallenge.FaceCapture) {
             // 1. Wait for stabilization
             delay(800) 
             
             // 2. Capture
             try {
                 val file = cameraManager.capturePhoto()
                 if (file != null) {
                     haptic.photoCapture()
                     viewModel.onFaceCaptured(file)
                 } else {
                     // Fallback if capture fails?? for now just retry or proceed
                     // viewModel.onFaceCaptured(...)
                 }
             } catch (e: Exception) {
                 e.printStackTrace()
             }
        }

        if (uiState.challenge is LivenessChallenge.Completed) {
            delay(500)
            haptic.complete()
            delay(1500) // Longer delay to show success animation
            onLivenessComplete()
        }
    }

    // Permission handling
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    // Camera Init
    LaunchedEffect(previewViewReady.value, cameraPermissionState.status.isGranted) {
        val view = previewViewReady.value
        if (view != null && cameraPermissionState.status.isGranted && !cameraInitialized.value) {
            try {
                cameraManager.startCamera(
                    previewView = view,
                    lifecycleOwner = lifecycleOwner,
                    lensFacing = CameraSelector.LENS_FACING_FRONT,
                    imageAnalyzer = faceAnalyzer
                )
                cameraInitialized.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (cameraPermissionState.status.isGranted) {
            // Camera Preview
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        previewViewReady.value = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Dynamic Overlay
            val overlayColor = when {
                uiState.challenge is LivenessChallenge.Completed -> SuccessGreen
                uiState.faceDetected && uiState.faceSizeSufficient -> OfficialGold
                else -> Color.White.copy(alpha = 0.5f)
            }
            
            CameraOverlay(
                cutoutType = CutoutType.OVAL_FACE,
                modifier = Modifier.fillMaxSize(),
                borderColor = overlayColor
            )

            // UI Content Layer
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top: Progress Bar
                Column(
                    modifier = Modifier.padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SegmentedProgressBar(
                        steps = 4, // Increased to 4 (L, R, Smile, Capture)
                        currentStep = when(uiState.challenge) {
                            is LivenessChallenge.TurnLeft -> 1
                            is LivenessChallenge.TurnRight -> 2
                            is LivenessChallenge.Smile -> 3
                            is LivenessChallenge.FaceCapture -> 4
                            is LivenessChallenge.Completed -> 4
                            else -> 0
                        }
                    )
                }

                // Center: Instruction HUD
                Box(contentAlignment = Alignment.Center) {
                    // Success Burst Animation
                    androidx.compose.animation.AnimatedVisibility(
                        visible = uiState.challenge is LivenessChallenge.Completed,
                        enter = scaleIn(tween(500)) + fadeIn(),
                        exit = fadeOut()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(120.dp)
                        )
                    }

                    // Instruction Card
                    androidx.compose.animation.AnimatedVisibility(
                        visible = uiState.challenge !is LivenessChallenge.Completed,
                        enter = fadeIn() + slideInVertically { 50 },
                        exit = fadeOut() + slideOutVertically { -50 }
                    ) {
                         GlassInstructionCard(
                            instruction = uiState.currentInstruction,
                            subInstruction = uiState.errorMessage ?: "Hold steady",
                            isError = uiState.errorMessage != null
                        )
                    }
                }

                // Bottom: Status Indicators
                // Simplified for the challenge
                Row(
                   modifier = Modifier
                       .fillMaxWidth()
                       .padding(bottom = 32.dp),
                   horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                   StatusChip(
                       label = "L", 
                       isActive = uiState.challenge is LivenessChallenge.TurnLeft,
                       isDone = LivenessChallenge.TurnLeft in uiState.completedSteps
                   )
                   StatusChip(
                       label = "R", 
                       isActive = uiState.challenge is LivenessChallenge.TurnRight,
                       isDone = LivenessChallenge.TurnRight in uiState.completedSteps
                   )
                   StatusChip(
                       label = "Smile", 
                       isActive = uiState.challenge is LivenessChallenge.Smile,
                       isDone = LivenessChallenge.Smile in uiState.completedSteps
                   )
                   // Hidden chip for capture phase
                   StatusChip(
                       label = "Pic", 
                       isActive = uiState.challenge is LivenessChallenge.FaceCapture,
                       isDone = uiState.challenge is LivenessChallenge.Completed
                   )
                }
            }
        }
    }
    
    // Cleanup
    DisposableEffect(lifecycleOwner) {
        onDispose {
            faceAnalyzer.close()
            cameraManager.shutdown()
            cameraInitialized.value = false
        }
    }
}

@Composable
fun GlassInstructionCard(
    instruction: String,
    subInstruction: String,
    isError: Boolean
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Black.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isError) MaterialTheme.colorScheme.error else Color.White.copy(alpha = 0.3f)
        ),
        modifier = Modifier.wrapContentSize()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = instruction,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = subInstruction,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) MaterialTheme.colorScheme.error else Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SegmentedProgressBar(steps: Int, currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(steps) { index ->
            val active = index < currentStep
            val color = if (active) OfficialGold else Color.White.copy(alpha = 0.2f)
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun StatusChip(label: String, isActive: Boolean, isDone: Boolean) {
    val backgroundColor = when {
        isDone -> SuccessGreen
        isActive -> OfficialGold
        else -> Color.White.copy(alpha = 0.1f)
    }
    
    val contentColor = if (isActive || isDone) Color.White else Color.White.copy(alpha = 0.5f)
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                 Text(
                    text = label.ifEmpty { "•" },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )
            }
        }
    }
}

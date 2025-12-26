package com.example.aainaai.ui.citizenship

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.aainaai.camera.CameraManager
import com.example.aainaai.ui.components.CameraOverlay
import com.example.aainaai.ui.components.CutoutType
import com.example.aainaai.ui.theme.DarkOverlay
import com.example.aainaai.ui.theme.OfficialGold
import com.example.aainaai.ui.theme.SuccessGreen
import com.example.aainaai.util.rememberHapticFeedback
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CitizenshipScanScreen(
    onCardCaptured: () -> Unit,
    viewModel: CitizenshipScanViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()
    
    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Convert URI to File immediately to ensure consistent path handling
            val file = copyUriToFile(context, uri)
            if (file != null) {
                viewModel.onImageCaptured(file)
            }
        }
    }

    var cameraManager by remember { mutableStateOf<CameraManager?>(null) }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }
    
    // Auto-navigate on completion
    LaunchedEffect(uiState.currentStep) {
        if (uiState.currentStep == ScanStep.COMPLETED) {
            onCardCaptured()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // CONTENT LAYER: Changes based on State
        if (uiState.currentStep == ScanStep.FRONT_SCAN || uiState.currentStep == ScanStep.BACK_SCAN) {
             // === SCANNING STATE ===
            if (cameraPermissionState.status.isGranted) {
                // Camera Preview
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            val manager = CameraManager(ctx)
                            cameraManager = manager
                            scope.launch {
                                try {
                                    manager.startCamera(
                                        previewView = this@apply,
                                        lifecycleOwner = lifecycleOwner,
                                        lensFacing = CameraSelector.LENS_FACING_BACK
                                    )
                                } catch (e: Exception) { }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay
                CameraOverlay(
                    cutoutType = CutoutType.LANDSCAPE_CARD,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Controls Layer
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Bar
                    ScanTopBar(
                        step = if (uiState.currentStep == ScanStep.FRONT_SCAN) 1 else 2, 
                        title = if (uiState.currentStep == ScanStep.FRONT_SCAN) "Scan Front Side" else "Scan Back Side"
                    )
                    
                    // Bottom Controls
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Gallery Button
                            IconButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(Icons.Default.Image, null, tint = Color.White)
                            }
                            
                            // SHUTTER BUTTON
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(88.dp)
                                    .clickable {
                                        if (!uiState.isProcessing) {
                                            viewModel.setProcessing(true)
                                            haptic.photoCapture()
                                            scope.launch {
                                                try {
                                                    val file = cameraManager?.capturePhoto()
                                                    if (file != null) viewModel.onImageCaptured(file)
                                                } catch(e: Exception) {
                                                    viewModel.setProcessing(false)
                                                }
                                            }
                                        }
                                    }
                            ) {
                                // Outer Ring
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(4.dp, Color.White, CircleShape)
                                )
                                // Inner Circle (Animated)
                                Box(
                                    modifier = Modifier
                                        .size(if (uiState.isProcessing) 72.dp else 64.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                                if (uiState.isProcessing) {
                                    CircularProgressIndicator(
                                        color = OfficialGold,
                                        modifier = Modifier.size(88.dp)
                                    )
                                }
                            }
                            
                            // Spacer to balance layout
                            Spacer(modifier = Modifier.size(56.dp))
                        }
                    }
                }
            }
        } else if (uiState.currentStep == ScanStep.FRONT_CONFIRM || uiState.currentStep == ScanStep.BACK_CONFIRM) {
             // === PREVIEW/CONFIRM STATE ===
             val imageUri = if (uiState.currentStep == ScanStep.FRONT_CONFIRM) {
                 uiState.frontImageUri ?: Uri.fromFile(uiState.frontImage)
             } else {
                 uiState.backImageUri ?: Uri.fromFile(uiState.backImage)
             }
             
             Box(modifier = Modifier.fillMaxSize()) {
                 // Image Preview
                 Image(
                     painter = rememberAsyncImagePainter(imageUri),
                     contentDescription = "Preview",
                     modifier = Modifier.fillMaxSize(),
                     contentScale = ContentScale.Fit
                 )
                 
                 // Bottom Confirmation Bar
                 Column(
                     modifier = Modifier
                         .align(Alignment.BottomCenter)
                         .fillMaxWidth()
                         .background(Color.Black.copy(alpha = 0.9f))
                         .padding(24.dp)
                 ) {
                     Text(
                         text = if (uiState.currentStep == ScanStep.FRONT_CONFIRM) "Use this Front Side?" else "Use this Back Side?",
                         style = MaterialTheme.typography.titleMedium,
                         color = Color.White,
                         modifier = Modifier.align(Alignment.CenterHorizontally)
                     )
                     
                     Spacer(modifier = Modifier.height(24.dp))
                     
                     Row(
                         modifier = Modifier.fillMaxWidth(),
                         horizontalArrangement = Arrangement.spacedBy(16.dp)
                     ) {
                         // Retake Button
                         OutlinedButton(
                             onClick = viewModel::retake,
                             modifier = Modifier.weight(1f).height(56.dp),
                             shape = RoundedCornerShape(12.dp),
                             border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                         ) {
                             Icon(Icons.Default.Cached, null, modifier = Modifier.size(18.dp))
                             Spacer(modifier = Modifier.width(8.dp))
                             Text("Retake")
                         }
                         
                         // Confirm Button
                         Button(
                             onClick = viewModel::confirmResult,
                             modifier = Modifier.weight(1f).height(56.dp),
                             shape = RoundedCornerShape(12.dp),
                             colors = ButtonDefaults.buttonColors(containerColor = OfficialGold)
                         ) {
                             Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                             Spacer(modifier = Modifier.width(8.dp))
                             Text("Confirm", color = Color.Black)
                         }
                     }
                 }
             }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraManager?.shutdown()
        }
    }
}

@Composable
fun ScanTopBar(step: Int, title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(top = 48.dp, bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Step $step of 2",
                style = MaterialTheme.typography.labelMedium,
                color = OfficialGold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }
    }
}

private fun copyUriToFile(context: android.content.Context, uri: Uri): java.io.File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = java.io.File(context.cacheDir, "temp_scan_${System.currentTimeMillis()}.jpg")
        val outputStream = java.io.FileOutputStream(tempFile)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

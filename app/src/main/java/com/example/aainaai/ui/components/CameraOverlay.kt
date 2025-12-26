package com.example.aainaai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.aainaai.ui.theme.DarkOverlay
import com.example.aainaai.ui.theme.OfficialGold

/**
 * Camera overlay with cutout for document or face scanning
 * Creates a semi-transparent dark overlay with a clear cutout in the center
 */
@Composable
fun CameraOverlay(
    modifier: Modifier = Modifier,
    cutoutType: CutoutType = CutoutType.LANDSCAPE_CARD,
    borderColor: Color = OfficialGold
) {
    // Animation for scanning effect
    val infiniteTransition = rememberInfiniteTransition(label = "scan_line")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_line"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Calculate cutout dimensions based on type
        // Use hardcoded pixel conversion for simplicity or relative values
        val (cutoutWidth, cutoutHeight, cornerRadius) = when (cutoutType) {
            CutoutType.LANDSCAPE_CARD -> {
                // Landscape card (1.6:1 ratio)
                val width = canvasWidth * 0.85f
                val height = width / 1.6f
                Triple(width, height, 48f) // 48px approx 16dp
            }
            CutoutType.OVAL_FACE -> {
                // Oval for face detection
                val width = canvasWidth * 0.75f
                val height = width * 1.3f
                Triple(width, height, width / 2f)
            }
        }

        // Center the cutout
        val cutoutLeft = (canvasWidth - cutoutWidth) / 2f
        val cutoutTop = (canvasHeight - cutoutHeight) / 2f

        // Draw dark overlay with cutout
        val overlayPath = Path().apply {
            // Outer rectangle (full canvas)
            addRect(Rect(0f, 0f, canvasWidth, canvasHeight))
            
            // Inner cutout (subtract from overlay)
            addRoundRect(
                RoundRect(
                    left = cutoutLeft,
                    top = cutoutTop,
                    right = cutoutLeft + cutoutWidth,
                    bottom = cutoutTop + cutoutHeight,
                    cornerRadius = CornerRadius(cornerRadius)
                )
            )
        }

        // Fill with dark overlay using even-odd rule to create cutout
        drawPath(
            path = overlayPath,
            color = DarkOverlay,
            alpha = 0.85f // Slightly darker for focus
        )

        // Draw border around cutout
        drawRoundRect(
            color = borderColor,
            topLeft = Offset(cutoutLeft, cutoutTop),
            size = Size(cutoutWidth, cutoutHeight),
            cornerRadius = CornerRadius(cornerRadius),
            style = Stroke(width = 8f)
        )
        
        // Draw Scanning Line
        val scanY = cutoutTop + (cutoutHeight * scanLineY)
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    borderColor.copy(alpha = 0f),
                    borderColor,
                    borderColor.copy(alpha = 0f)
                )
            ),
            start = Offset(cutoutLeft, scanY),
            end = Offset(cutoutLeft + cutoutWidth, scanY),
            strokeWidth = 4f
        )
        
        // Draw trailing gradient for scan line
        val gradientHeight = 100f
        if (scanY - cutoutTop > 0) {
            // Ensure we don't draw outside the top of the box
            val rectTop = kotlin.math.max(cutoutTop, scanY - gradientHeight)
            val rectHeight = kotlin.math.min(gradientHeight, scanY - cutoutTop)
            
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        borderColor.copy(alpha = 0f),
                        borderColor.copy(alpha = 0.3f)
                    ),
                    startY = scanY - gradientHeight,
                    endY = scanY
                ),
                topLeft = Offset(cutoutLeft, rectTop),
                size = Size(cutoutWidth, rectHeight)
            )
        }
    }
}

/**
 * Type of cutout shape
 */
enum class CutoutType {
    LANDSCAPE_CARD,  // For citizenship card scanning
    OVAL_FACE        // For face liveness detection
}

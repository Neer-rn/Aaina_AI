package com.example.aainaai.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Modern, rounded shape system (16dp as specified)
val Shapes = Shapes(
    // Small components (chips, tags)
    small = RoundedCornerShape(8.dp),
    
    // Medium components (buttons, inputs)
    medium = RoundedCornerShape(12.dp),
    
    // Large components (cards, dialogs)
    large = RoundedCornerShape(16.dp),
    
    // Extra large (bottom sheets, modals)
    extraLarge = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
)

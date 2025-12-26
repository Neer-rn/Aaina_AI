package com.example.aainaai.ui.form

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aainaai.ui.theme.DeepEmerald
import com.example.aainaai.ui.theme.OfficialGold
import com.example.aainaai.ui.theme.PremiumGreenGradientEnd
import com.example.aainaai.ui.theme.PremiumGreenGradientStart

@Composable
fun PersonalDetailsScreen(
    onDetailsSubmitted: () -> Unit,
    viewModel: PersonalDetailsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Premium Background
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            PremiumGreenGradientStart,
            Color(0xFF00251F)
        )
    )

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onDetailsSubmitted()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Header Icon
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AssignmentInd,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = OfficialGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Personal Details",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )

            Text(
                text = "Please verify your information below",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f)
            )

            // Auto-Fill Button (For Test)
            TextButton(
                onClick = viewModel::autoFill,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Auto-Fill (Demo)", color = OfficialGold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Form Container
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Name Input
                    GlassTextField(
                        value = uiState.fullName,
                        onValueChange = viewModel::onNameChange,
                        label = "Full Name",
                        icon = Icons.Default.Person
                    )

                    // Citizenship Number Input
                    GlassTextField(
                        value = uiState.citizenshipNumber,
                        onValueChange = viewModel::onCitizenshipNumberChange,
                        label = "Citizenship Number",
                        icon = Icons.Default.Badge,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )

                    // DOB Input
                    GlassTextField(
                        value = uiState.dateOfBirth,
                        onValueChange = viewModel::onDobChange,
                        label = "Date of Birth (YYYY-MM-DD)",
                        icon = Icons.Default.CalendarToday,
                         keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Next Button
            Button(
                onClick = viewModel::submit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OfficialGold,
                    contentColor = DeepEmerald
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = uiState.isFormValid && !uiState.isSubmitting
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = DeepEmerald
                    )
                } else {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White.copy(alpha = 0.7f)) },
        leadingIcon = { Icon(icon, null, tint = OfficialGold) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = OfficialGold,
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = OfficialGold
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

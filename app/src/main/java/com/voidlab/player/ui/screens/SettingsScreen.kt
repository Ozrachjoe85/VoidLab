package com.voidlab.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voidlab.player.ui.theme.*

@Composable
fun SettingsScreen() {
    var audioQuality by remember { mutableStateOf("High") }
    var enableCrossfade by remember { mutableStateOf(false) }
    var crossfadeDuration by remember { mutableStateOf(3f) }
    var enableNotifications by remember { mutableStateOf(true) }
    var themeBrightness by remember { mutableStateOf(50f) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(VoidBlack, VoidBlackLight)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "SETTINGS",
                style = MaterialTheme.typography.headlineMedium,
                color = VoidCyan,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Audio Settings
            SettingsSection(title = "AUDIO") {
                SettingsDropdown(
                    icon = Icons.Default.HighQuality,
                    label = "Audio Quality",
                    value = audioQuality,
                    options = listOf("Low", "Medium", "High", "Ultra"),
                    onValueChange = { audioQuality = it }
                )
                
                SettingsSwitch(
                    icon = Icons.Default.SwapHoriz,
                    label = "Crossfade",
                    description = "Smooth transition between tracks",
                    checked = enableCrossfade,
                    onCheckedChange = { enableCrossfade = it }
                )
                
                if (enableCrossfade) {
                    SettingsSlider(
                        label = "Crossfade Duration",
                        value = crossfadeDuration,
                        valueRange = 0f..10f,
                        steps = 19,
                        onValueChange = { crossfadeDuration = it },
                        valueLabel = "${crossfadeDuration.toInt()}s"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Appearance Settings
            SettingsSection(title = "APPEARANCE") {
                SettingsSlider(
                    label = "Theme Brightness",
                    value = themeBrightness,
                    valueRange = 0f..100f,
                    onValueChange = { themeBrightness = it },
                    valueLabel = "${themeBrightness.toInt()}%"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Notification Settings
            SettingsSection(title = "NOTIFICATIONS") {
                SettingsSwitch(
                    icon = Icons.Default.Notifications,
                    label = "Media Notifications",
                    description = "Show playback controls in notification",
                    checked = enableNotifications,
                    onCheckedChange = { enableNotifications = it }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // About Section
            SettingsSection(title = "ABOUT") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    label = "Version",
                    value = "1.0.0"
                )
                
                SettingsItem(
                    icon = Icons.Default.Copyright,
                    label = "Build",
                    value = "Void Lab Alpha"
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = VoidCyan,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Surface(
            color = VoidBlackLight,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = VoidCyan,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = VoidCyan
            )
        }
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = VoidCyan.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun SettingsSwitch(
    icon: ImageVector,
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = VoidCyan,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VoidCyan
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = VoidCyan.copy(alpha = 0.6f)
                )
            }
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = VoidCyan,
                checkedTrackColor = VoidCyan.copy(alpha = 0.5f),
                uncheckedThumbColor = VoidCyan.copy(alpha = 0.3f),
                uncheckedTrackColor = VoidBlack
            )
        )
    }
}

@Composable
fun SettingsDropdown(
    icon: ImageVector,
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = VoidCyan,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = VoidCyan
            )
        }
        
        Box {
            TextButton(onClick = { expanded = true }) {
                Text(
                    text = value,
                    color = VoidCyan.copy(alpha = 0.8f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = VoidCyan
                )
            }
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    onValueChange: (Float) -> Unit,
    valueLabel: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = VoidCyan
            )
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = VoidCyan.copy(alpha = 0.6f)
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = VoidCyan,
                activeTrackColor = VoidCyan,
                inactiveTrackColor = VoidBlack
            )
        )
    }
}

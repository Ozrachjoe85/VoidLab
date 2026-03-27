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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voidlab.player.ui.theme.*

@Composable
fun SettingsScreen() {
    var showAboutDialog by remember { mutableStateOf(false) }
    
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
            Text(
                text = "SETTINGS",
                style = MaterialTheme.typography.headlineMedium,
                color = VoidCyan,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Playback Section
            SettingsSection(title = "PLAYBACK") {
                SettingItem(
                    icon = Icons.Default.Speed,
                    title = "Gapless Playback",
                    description = "Seamless transitions between tracks",
                    trailing = {
                        Switch(
                            checked = true,
                            onCheckedChange = { },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = VoidCyan,
                                checkedTrackColor = VoidCyan.copy(alpha = 0.5f)
                            )
                        )
                    }
                )
                
                // Audio quality setting REMOVED - VoidLab is ALWAYS maximum quality
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Appearance Section
            SettingsSection(title = "APPEARANCE") {
                SettingItem(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    description = "Void Dark (Default)",
                    trailing = {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = VoidCyan.copy(alpha = 0.5f)
                        )
                    }
                )
                
                SettingItem(
                    icon = Icons.Default.ColorLens,
                    title = "Accent Color",
                    description = "Cyan",
                    trailing = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(VoidCyan, RoundedCornerShape(4.dp))
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Library Section
            SettingsSection(title = "LIBRARY") {
                SettingItem(
                    icon = Icons.Default.Refresh,
                    title = "Scan Library",
                    description = "Refresh music collection",
                    trailing = {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = VoidCyan.copy(alpha = 0.5f)
                        )
                    }
                )
                
                SettingItem(
                    icon = Icons.Default.Folder,
                    title = "Music Folders",
                    description = "Manage scan locations",
                    trailing = {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = VoidCyan.copy(alpha = 0.5f)
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Auto EQ Section
            SettingsSection(title = "AUTO EQ") {
                SettingItem(
                    icon = Icons.Default.AutoAwesome,
                    title = "Learning Mode",
                    description = "Automatically optimize EQ per song",
                    trailing = {
                        Switch(
                            checked = true,
                            onCheckedChange = { },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = VoidCyan,
                                checkedTrackColor = VoidCyan.copy(alpha = 0.5f)
                            )
                        )
                    }
                )
                
                SettingItem(
                    icon = Icons.Default.Delete,
                    title = "Clear Learned Profiles",
                    description = "Reset all Auto EQ data",
                    trailing = {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = VoidCyan.copy(alpha = 0.5f)
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // About Section
            SettingsSection(title = "ABOUT") {
                SettingItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    description = "VoidLab 1.0.0",
                    onClick = { showAboutDialog = true }
                )
                
                SettingItem(
                    icon = Icons.Default.Code,
                    title = "Open Source",
                    description = "View on GitHub",
                    trailing = {
                        Icon(
                            Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = VoidCyan.copy(alpha = 0.5f)
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = VoidBlackLight,
            title = {
                Text(
                    "VoidLab",
                    color = VoidCyan,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Version 1.0.0",
                        color = VoidCyan.copy(alpha = 0.7f)
                    )
                    Text(
                        "A futuristic music player with Auto EQ learning, real-time visualizers, and studio-grade audio processing.",
                        color = VoidCyan.copy(alpha = 0.7f)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK", color = VoidCyan)
                }
            }
        )
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
            style = MaterialTheme.typography.titleSmall,
            color = VoidCyan.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Surface(
            color = VoidBlackLight,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Surface(
        onClick = onClick ?: {},
        color = VoidBlackLight,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = VoidCyan,
                    modifier = Modifier.size(24.dp)
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        color = VoidCyan,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (description != null) {
                        Text(
                            text = description,
                            color = VoidCyan.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            if (trailing != null) {
                trailing()
            }
        }
    }
}

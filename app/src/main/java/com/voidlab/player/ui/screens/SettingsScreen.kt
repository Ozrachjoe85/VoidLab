package com.voidlab.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(VoidBlack, VoidDarkGray)
                )
            )
    ) {
        // Header
        Surface(
            color = VoidDarkGray,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SETTINGS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Audio Section
            item {
                SettingsSectionHeader(title = "AUDIO")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.VolumeUp,
                    title = "Audio Output",
                    subtitle = "Default device",
                    onClick = { }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.GraphicEq,
                    title = "Audio Quality",
                    subtitle = "High (320 kbps)",
                    onClick = { }
                )
            }
            
            item {
                var gaplessEnabled by remember { mutableStateOf(true) }
                SettingsToggleItem(
                    icon = Icons.Default.SkipNext,
                    title = "Gapless Playback",
                    subtitle = "Seamless track transitions",
                    checked = gaplessEnabled,
                    onCheckedChange = { gaplessEnabled = it }
                )
            }
            
            // Appearance Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSectionHeader(title = "APPEARANCE")
            }
            
            item {
                var darkMode by remember { mutableStateOf(true) }
                SettingsToggleItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Always enabled (Void aesthetic)",
                    checked = darkMode,
                    onCheckedChange = { darkMode = it },
                    enabled = false
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.ColorLens,
                    title = "Accent Color",
                    subtitle = "Void Cyan (custom themes coming soon)",
                    onClick = { }
                )
            }
            
            // Notifications Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSectionHeader(title = "NOTIFICATIONS")
            }
            
            item {
                var notificationsEnabled by remember { mutableStateOf(true) }
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Playback Notifications",
                    subtitle = "Show lock screen controls",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }
            
            // Storage Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSectionHeader(title = "STORAGE")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "Cache Size",
                    subtitle = "Clear album art cache",
                    onClick = { }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.FolderOpen,
                    title = "Music Folders",
                    subtitle = "Scan custom directories",
                    onClick = { }
                )
            }
            
            // About Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSectionHeader(title = "ABOUT")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    subtitle = "1.0.0 (Beta)",
                    onClick = { }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Code,
                    title = "Open Source Licenses",
                    subtitle = "View third-party licenses",
                    onClick = { }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.BugReport,
                    title = "Report a Bug",
                    subtitle = "Help us improve Void Lab",
                    onClick = { }
                )
            }
            
            // Branding
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "VOID LAB",
                        style = MaterialTheme.typography.headlineLarge,
                        color = VoidCyan,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Your Music, Perfected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Made with ♥ for audiophiles",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = VoidCyan,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = VoidGray.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = VoidCyan,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = VoidGray.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (enabled) VoidCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = VoidCyan,
                    checkedTrackColor = VoidCyan.copy(alpha = 0.5f)
                )
            )
        }
    }
}

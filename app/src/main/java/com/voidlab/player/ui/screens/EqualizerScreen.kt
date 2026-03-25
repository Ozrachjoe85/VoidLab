package com.voidlab.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.voidlab.player.ui.viewmodels.EQViewModel
import com.voidlab.player.ui.viewmodels.ViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    viewModel: EQViewModel
) {
    val isAutoMode by viewModel.isAutoMode.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val learnedProfiles by viewModel.learnedProfiles.collectAsState()
    val learnedProfileCount by viewModel.learnedProfileCount.collectAsState()
    
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
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "EQUALIZER",
                style = MaterialTheme.typography.headlineMedium,
                color = VoidCyan,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Auto Mode Toggle
            Surface(
                color = VoidBlackLight,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto EQ Mode",
                            style = MaterialTheme.typography.titleMedium,
                            color = VoidCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Learn optimal settings per track",
                            style = MaterialTheme.typography.bodySmall,
                            color = VoidCyan.copy(alpha = 0.6f)
                        )
                    }
                    
                    Switch(
                        checked = isAutoMode,
                        onCheckedChange = { viewModel.toggleAutoMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VoidCyan,
                            checkedTrackColor = VoidCyan.copy(alpha = 0.5f),
                            uncheckedThumbColor = VoidCyan.copy(alpha = 0.3f),
                            uncheckedTrackColor = VoidBlack
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // View Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = viewMode == ViewMode.CURVE,
                    onClick = { viewModel.setViewMode(ViewMode.CURVE) },
                    label = { Text("CURVE") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.ShowChart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VoidCyan,
                        selectedLabelColor = VoidBlack
                    )
                )
                
                FilterChip(
                    selected = viewMode == ViewMode.MIXER,
                    onClick = { viewModel.setViewMode(ViewMode.MIXER) },
                    label = { Text("MIXER") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VoidCyan,
                        selectedLabelColor = VoidBlack
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // EQ Controls
            when (viewMode) {
                ViewMode.CURVE -> EQCurveView(currentProfile?.getBands() ?: List(10) { 0f })
                ViewMode.MIXER -> EQMixerView(
                    bands = currentProfile?.getBands() ?: List(10) { 0f },
                    onBandChange = { index, value ->
                        viewModel.updateBandLevel(index, value)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Presets
            Text(
                text = "PRESETS",
                style = MaterialTheme.typography.titleMedium,
                color = VoidCyan,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.presets.take(3).forEach { preset ->
                    Button(
                        onClick = { viewModel.applyPreset(preset) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VoidBlackLight,
                            contentColor = VoidCyan
                        )
                    ) {
                        Text(preset.name)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.presets.drop(3).forEach { preset ->
                    Button(
                        onClick = { viewModel.applyPreset(preset) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VoidBlackLight,
                            contentColor = VoidCyan
                        )
                    ) {
                        Text(preset.name)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Learned Profiles
            if (learnedProfileCount > 0) {
                Text(
                    text = "LEARNED PROFILES ($learnedProfileCount)",
                    style = MaterialTheme.typography.titleMedium,
                    color = VoidCyan,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(learnedProfiles) { profile ->
                        Surface(
                            color = VoidBlackLight,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = profile.songTitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = VoidCyan,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = profile.songArtist,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = VoidCyan.copy(alpha = 0.6f)
                                    )
                                }
                                
                                IconButton(onClick = { viewModel.deleteProfile(profile) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = VoidPink
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EQCurveView(bands: List<Float>) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        color = VoidBlackLight,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "EQ Curve Visualization",
                color = VoidCyan.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun EQMixerView(
    bands: List<Float>,
    onBandChange: (Int, Float) -> Unit
) {
    val bandLabels = listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = VoidBlackLight,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                bands.forEachIndexed { index, value ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = String.format("%.1f", value),
                            style = MaterialTheme.typography.labelSmall,
                            color = VoidCyan
                        )
                        Slider(
                            value = value,
                            onValueChange = { onBandChange(index, it) },
                            valueRange = -12f..12f,
                            modifier = Modifier.width(40.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = VoidCyan,
                                activeTrackColor = VoidCyan,
                                inactiveTrackColor = VoidBlack
                            )
                        )
                        Text(
                            text = bandLabels[index],
                            style = MaterialTheme.typography.labelSmall,
                            color = VoidCyan.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

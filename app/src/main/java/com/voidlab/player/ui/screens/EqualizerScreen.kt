package com.voidlab.player.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voidlab.player.data.models.EQProfile
import com.voidlab.player.ui.theme.*
import com.voidlab.player.ui.viewmodels.EQViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(viewModel: EQViewModel) {
    val isAutoMode by viewModel.isAutoMode.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val learnedProfiles by viewModel.learnedProfiles.collectAsState()
    val learnedProfileCount by viewModel.learnedProfileCount.collectAsState()
    
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
                    text = "EQUALIZER",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Auto/Manual Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAutoMode) "AUTO MODE" else "MANUAL MODE",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isAutoMode) VoidGreen else VoidPink,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Switch(
                        checked = isAutoMode,
                        onCheckedChange = { viewModel.toggleAutoMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VoidGreen,
                            checkedTrackColor = VoidGreen.copy(alpha = 0.5f),
                            uncheckedThumbColor = VoidPink,
                            uncheckedTrackColor = VoidPink.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // View Mode Selector (Curve vs Mixer)
        if (!isAutoMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.setViewMode(EQViewModel.ViewMode.CURVE) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (viewMode == EQViewModel.ViewMode.CURVE) VoidCyan.copy(alpha = 0.2f) else Color.Transparent,
                        contentColor = if (viewMode == EQViewModel.ViewMode.CURVE) VoidCyan else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("CURVE VIEW")
                }
                
                OutlinedButton(
                    onClick = { viewModel.setViewMode(EQViewModel.ViewMode.MIXER) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (viewMode == EQViewModel.ViewMode.MIXER) VoidCyan.copy(alpha = 0.2f) else Color.Transparent,
                        contentColor = if (viewMode == EQViewModel.ViewMode.MIXER) VoidCyan else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("MIXER VIEW")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // EQ Visualization
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
        ) {
            when {
                isAutoMode -> AutoModeDisplay(learnedProfileCount = learnedProfileCount)
                viewMode == EQViewModel.ViewMode.CURVE -> EQCurveView(
                    profile = currentProfile,
                    onBandChange = { index, value -> viewModel.updateBandLevel(index, value) }
                )
                else -> EQMixerView(
                    profile = currentProfile,
                    onBandChange = { index, value -> viewModel.updateBandLevel(index, value) }
                )
            }
        }
        
        // Preset Buttons
        if (!isAutoMode) {
            PresetButtons(onPresetSelected = { preset -> viewModel.applyPreset(preset) })
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Learned Profiles
        if (learnedProfiles.isNotEmpty()) {
            LearnedProfilesSection(
                profiles = learnedProfiles,
                onDeleteProfile = { viewModel.deleteProfile(it) },
                onClearAll = { viewModel.clearAllProfiles() }
            )
        }
    }
}

@Composable
fun AutoModeDisplay(learnedProfileCount: Int) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "Auto EQ",
            modifier = Modifier.size(120.dp),
            tint = VoidGreen
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "AUTO EQ ACTIVE",
            style = MaterialTheme.typography.headlineMedium,
            color = VoidGreen,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "$learnedProfileCount profiles learned",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Play a song to see Auto EQ in action",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EQCurveView(
    profile: EQProfile,
    onBandChange: (Int, Float) -> Unit
) {
    val bands = profile.toBandList()
    
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val x = change.position.x
                    val y = change.position.y
                    val width = size.width
                    val height = size.height
                    
                    val bandIndex = ((x / width) * 10).toInt().coerceIn(0, 9)
                    val normalizedY = (height / 2 - y) / (height / 2)
                    val gainDb = (normalizedY * 12).coerceIn(-12f, 12f)
                    
                    onBandChange(bandIndex, gainDb)
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        
        // Draw grid
        drawLine(
            color = VoidGray,
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 2f
        )
        
        for (i in 0..10) {
            val x = (i / 10f) * width
            drawLine(
                color = VoidGray.copy(alpha = 0.3f),
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1f
            )
        }
        
        // Draw EQ curve
        val path = Path()
        val points = bands.mapIndexed { index, gain ->
            val x = (index / 9f) * width
            val y = centerY - (gain / 12f) * centerY
            Offset(x, y)
        }
        
        path.moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            val prevPoint = points[i - 1]
            val currentPoint = points[i]
            val controlPoint1 = Offset(
                prevPoint.x + (currentPoint.x - prevPoint.x) / 2,
                prevPoint.y
            )
            val controlPoint2 = Offset(
                prevPoint.x + (currentPoint.x - prevPoint.x) / 2,
                currentPoint.y
            )
            path.cubicTo(
                controlPoint1.x, controlPoint1.y,
                controlPoint2.x, controlPoint2.y,
                currentPoint.x, currentPoint.y
            )
        }
        
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(VoidPurple, VoidCyan, VoidPink, VoidGreen)
            ),
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )
        
        // Draw control points
        points.forEach { point ->
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(VoidCyan, VoidPurple)
                ),
                radius = 8f,
                center = point
            )
        }
    }
}

@Composable
fun EQMixerView(
    profile: EQProfile,
    onBandChange: (Int, Float) -> Unit
) {
    val bands = profile.toBandList()
    val bandLabels = listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")
    
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        bands.forEachIndexed { index, gain ->
            EQSlider(
                label = bandLabels[index],
                value = gain,
                onValueChange = { onBandChange(index, it) }
            )
        }
    }
}

@Composable
fun EQSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(32.dp)
    ) {
        Text(
            text = "+%.1f".format(value),
            style = MaterialTheme.typography.labelSmall,
            color = if (value > 0) VoidGreen else if (value < 0) VoidPink else MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = -12f..12f,
            modifier = Modifier
                .height(200.dp)
                .width(32.dp),
            colors = SliderDefaults.colors(
                thumbColor = VoidCyan,
                activeTrackColor = VoidCyan,
                inactiveTrackColor = VoidGray
            )
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PresetButtons(onPresetSelected: (EQViewModel.Preset) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(EQViewModel.Preset.values()) { preset ->
            OutlinedButton(
                onClick = { onPresetSelected(preset) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(preset.name)
            }
        }
    }
}

@Composable
fun LearnedProfilesSection(
    profiles: List<EQProfile>,
    onDeleteProfile: (EQProfile) -> Unit,
    onClearAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LEARNED PROFILES (${profiles.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            
            TextButton(onClick = onClearAll) {
                Text("CLEAR ALL", color = VoidPink)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(profiles, key = { it.songId }) { profile ->
                ProfileCard(
                    profile = profile,
                    onDelete = { onDeleteProfile(profile) }
                )
            }
        }
    }
}

@Composable
fun ProfileCard(
    profile: EQProfile,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.width(120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = VoidGray)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Song ${profile.songId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = VoidPink,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = java.text.SimpleDateFormat("MMM d, h:mm a", java.util.Locale.getDefault())
                    .format(java.util.Date(profile.learnedAt)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

package com.voidlab.player.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voidlab.player.ui.theme.*
import com.voidlab.player.ui.viewmodels.EQViewModel
import com.voidlab.player.ui.viewmodels.ViewMode
import kotlin.math.abs

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
    
    // REAL-TIME SPECTRUM - ALIVE!
    val currentSpectrum by viewModel.currentSpectrum.collectAsState()
    
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
                            text = if (isAutoMode) "✓ Learning active" else "Manual control",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isAutoMode) VoidGreen else VoidCyan.copy(alpha = 0.6f)
                        )
                    }
                    
                    Switch(
                        checked = isAutoMode,
                        onCheckedChange = { viewModel.toggleAutoMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VoidCyan,
                            checkedTrackColor = VoidCyan.copy(alpha = 0.5f),
                            uncheckedThumbColor = VoidCyan.copy(alpha = 0.3f),
                            uncheckedTrackColor = VoidCyan.copy(alpha = 0.1f)
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // View Mode Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ViewModeButton(
                    label = "Curve",
                    icon = Icons.Default.ShowChart,
                    isSelected = viewMode == ViewMode.CURVE,
                    onClick = { viewModel.setViewMode(ViewMode.CURVE) },
                    modifier = Modifier.weight(1f)
                )
                ViewModeButton(
                    label = "Mixer",
                    icon = Icons.Default.Settings,
                    isSelected = viewMode == ViewMode.MIXER,
                    onClick = { viewModel.setViewMode(ViewMode.MIXER) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // EQ Visualization - WITH SPECTRUM!
            val currentBands = currentProfile?.getBands() ?: List(10) { 0f }
            
            Surface(
                color = VoidBlackLight,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (viewMode) {
                    ViewMode.CURVE -> {
                        InteractiveCurveView(
                            bands = currentBands,
                            spectrum = currentSpectrum, // PASS SPECTRUM!
                            onBandChange = { index, value ->
                                if (!isAutoMode) {
                                    viewModel.updateBandLevel(index, value)
                                }
                            }
                        )
                    }
                    ViewMode.MIXER -> {
                        ProMixerBoardView(
                            bands = currentBands,
                            spectrum = currentSpectrum, // PASS SPECTRUM!
                            onBandChange = { index, value ->
                                if (!isAutoMode) {
                                    viewModel.updateBandLevel(index, value)
                                }
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Presets
            Text(
                text = "PRESETS",
                style = MaterialTheme.typography.titleSmall,
                color = VoidCyan.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.presets.take(4).forEach { preset ->
                    PresetButton(
                        preset = preset,
                        onClick = { viewModel.applyPreset(preset) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ViewModeButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) VoidCyan else VoidBlackLight,
            contentColor = if (isSelected) VoidBlack else VoidCyan
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun PresetButton(
    preset: com.voidlab.player.data.models.EQPreset,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = VoidCyan.copy(alpha = 0.1f),
            contentColor = VoidCyan
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            preset.displayName,
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun InteractiveCurveView(
    bands: List<Float>,
    spectrum: FloatArray = FloatArray(10),
    onBandChange: (Int, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var dragIndex by remember { mutableStateOf(-1) }
    val bandCount = 10
    
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val index = (offset.x / size.width * bandCount).toInt().coerceIn(0, bandCount - 1)
                        dragIndex = index
                    },
                    onDrag = { change, _ ->
                        if (dragIndex >= 0) {
                            val normalizedY = 1f - (change.position.y / size.height)
                            val value = (normalizedY * 24f - 12f).coerceIn(-12f, 12f)
                            onBandChange(dragIndex, value)
                        }
                    },
                    onDragEnd = {
                        dragIndex = -1
                    }
                )
            }
    ) {
        val width = size.width
        val height = size.height
        val bandSpacing = width / (bandCount - 1)
        
        // DRAW SPECTRUM BARS FIRST (behind everything) - ALIVE!
        spectrum.forEachIndexed { index, value ->
            val x = index * bandSpacing
            val barHeight = value * height * 0.8f // 80% max height
            val barWidth = bandSpacing * 0.6f
            
            drawRect(
                color = VoidCyan.copy(alpha = 0.15f),
                topLeft = Offset(x - barWidth / 2, height - barHeight),
                size = Size(barWidth, barHeight)
            )
        }
        
        // Grid lines
        for (i in 0..4) {
            val y = height * i / 4
            drawLine(
                color = VoidCyan.copy(alpha = 0.1f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }
        
        // Center line (0 dB)
        drawLine(
            color = VoidCyan.copy(alpha = 0.3f),
            start = Offset(0f, height / 2),
            end = Offset(width, height / 2),
            strokeWidth = 2f
        )
        
        // EQ Curve
        val path = Path()
        bands.forEachIndexed { index, value ->
            val x = index * bandSpacing
            val normalizedValue = (value + 12f) / 24f
            val y = height * (1f - normalizedValue)
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        // Draw curve
        drawPath(
            path = path,
            color = VoidCyan,
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )
        
        // Draw control points
        bands.forEachIndexed { index, value ->
            val x = index * bandSpacing
            val normalizedValue = (value + 12f) / 24f
            val y = height * (1f - normalizedValue)
            
            drawCircle(
                color = VoidCyan,
                radius = if (dragIndex == index) 12f else 8f,
                center = Offset(x, y)
            )
            drawCircle(
                color = VoidBlack,
                radius = if (dragIndex == index) 6f else 4f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun ProMixerBoardView(
    bands: List<Float>,
    spectrum: FloatArray = FloatArray(10),
    onBandChange: (Int, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val frequencies = listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")
    
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        bands.forEachIndexed { index, value ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Frequency label
                Text(
                    text = frequencies[index],
                    style = MaterialTheme.typography.labelSmall,
                    color = VoidCyan.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Fader track with SPECTRUM behind it - ALIVE!
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(40.dp)
                ) {
                    // SPECTRUM BAR - Draw FIRST (behind fader)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val barHeight = spectrum.getOrNull(index)?.times(size.height * 0.9f) ?: 0f
                        drawRect(
                            color = VoidCyan.copy(alpha = 0.2f),
                            topLeft = Offset(0f, size.height - barHeight),
                            size = Size(size.width, barHeight)
                        )
                    }
                    
                    // FADER - Draw SECOND (on top)
                    FaderControl(
                        value = value,
                        onValueChange = { newValue ->
                            onBandChange(index, newValue)
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Value label
                Text(
                    text = "${value.toInt()}dB",
                    style = MaterialTheme.typography.labelSmall,
                    color = VoidCyan,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FaderControl(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    val normalizedY = 1f - (change.position.y / size.height)
                    val newValue = (normalizedY * 24f - 12f).coerceIn(-12f, 12f)
                    onValueChange(newValue)
                }
            }
    ) {
        // Track
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawLine(
                color = VoidCyan.copy(alpha = 0.3f),
                start = Offset(size.width / 2, 0f),
                end = Offset(size.width / 2, size.height),
                strokeWidth = 4f
            )
        }
        
        // Knob
        val normalizedValue = (value + 12f) / 24f
        val knobY = size.height * (1f - normalizedValue)
        
        Box(
            modifier = Modifier
                .offset(y = knobY.dp)
                .size(32.dp)
                .align(Alignment.TopCenter)
                .background(VoidCyan, RoundedCornerShape(4.dp))
        )
    }
}

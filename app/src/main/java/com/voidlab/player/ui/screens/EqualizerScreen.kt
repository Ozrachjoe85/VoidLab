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
    
    // REAL-TIME SPECTRUM - Makes EQ ALIVE!
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EQUALIZER",
                    style = MaterialTheme.typography.headlineMedium,
                    color = VoidCyan,
                    fontWeight = FontWeight.Bold
                )
                
                if (isAutoMode && learnedProfileCount > 0) {
                    Surface(
                        color = VoidCyan.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Album,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = VoidCyan
                            )
                            Text(
                                text = "$learnedProfileCount learned",
                                style = MaterialTheme.typography.labelSmall,
                                color = VoidCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
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
                            spectrum = currentSpectrum,
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
                            spectrum = currentSpectrum,
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
        
        // SPECTRUM BARS - Draw FIRST (behind everything) - ALIVE!
        spectrum.forEachIndexed { index, value ->
            val x = index * bandSpacing
            val barHeight = value * height * 0.8f
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
            ProFaderChannel(
                value = value,
                label = frequencies[index],
                spectrumLevel = spectrum.getOrNull(index)?.times(96f) ?: 0f,
                onValueChange = { newValue ->
                    onBandChange(index, newValue)
                }
            )
        }
    }
}

@Composable
fun ProFaderChannel(
    value: Float,
    label: String,
    spectrumLevel: Float,
    onValueChange: (Float) -> Unit
) {
    var dragValue by remember { mutableStateOf(value) }
    
    LaunchedEffect(value) {
        dragValue = value
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight()
    ) {
        // LED dB display
        Surface(
            color = VoidBlack,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.size(width = 45.dp, height = 22.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = String.format("%+.1f", dragValue),
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        dragValue > 6f -> VoidPink
                        dragValue > 0f -> VoidCyan
                        else -> VoidGreen
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Fader track with spectrum visualization - ALIVE!
        Box(
            modifier = Modifier
                .width(50.dp)
                .weight(1f)
        ) {
            Canvas(modifier = Modifier
                .width(8.dp)
                .fillMaxHeight()
                .align(Alignment.Center)
            ) {
                // SPECTRUM LEVEL - Draw FIRST (background) - ALIVE!
                val normalizedSpec = ((spectrumLevel + 96f) / 96f).coerceIn(0f, 1f)
                val specHeight = size.height * normalizedSpec
                
                drawRect(
                    color = VoidCyan.copy(alpha = 0.2f),
                    topLeft = Offset(0f, size.height - specHeight),
                    size = Size(size.width, specHeight)
                )
                
                // Track
                drawRoundRect(
                    color = Color(0xFF0d0d0d),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                )
                
                // Center notch
                val centerY = size.height / 2f
                drawLine(
                    color = VoidCyan.copy(alpha = 0.5f),
                    start = Offset(-6.dp.toPx(), centerY),
                    end = Offset(size.width + 6.dp.toPx(), centerY),
                    strokeWidth = 2.dp.toPx()
                )
                
                // Active section
                val normalizedValue = (dragValue + 12f) / 24f
                val faderY = size.height * (1f - normalizedValue)
                
                if (faderY < centerY) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(VoidPink.copy(alpha = 0.6f), VoidCyan.copy(alpha = 0.4f)),
                            startY = faderY,
                            endY = centerY
                        ),
                        topLeft = Offset(0f, faderY),
                        size = Size(size.width, centerY - faderY)
                    )
                } else {
                    drawRect(
                        color = VoidPurple.copy(alpha = 0.4f),
                        topLeft = Offset(0f, centerY),
                        size = Size(size.width, faderY - centerY)
                    )
                }
            }
            
            // Fader knob
            val normalizedValue = (dragValue + 12f) / 24f
            val maxOffset = 320.dp
            
            Box(
                modifier = Modifier
                    .offset(y = maxOffset * (1f - normalizedValue) - 12.dp)
                    .size(50.dp, 24.dp)
                    .align(Alignment.TopCenter)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { change, dragAmount ->
                            change.consume()
                            val sensitivity = 0.15f
                            val delta = -dragAmount * sensitivity
                            dragValue = (dragValue + delta).coerceIn(-12f, 12f)
                        }
                    }
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = { onValueChange(dragValue) },
                            onVerticalDrag = { _, _ -> }
                        )
                    }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF2a2a2a),
                    shadowElevation = 4.dp
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF404040), Color(0xFF1a1a1a))
                            ),
                            size = size
                        )
                        
                        drawLine(
                            color = VoidCyan,
                            start = Offset(size.width * 0.2f, size.height / 2),
                            end = Offset(size.width * 0.8f, size.height / 2),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = VoidCyan.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
    }
}

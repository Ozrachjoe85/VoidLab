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
    
    // Real-time spectrum from FrequencyAnalyzer
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
            
            // EQ Controls with REAL-TIME spectrum visualization
            when (viewMode) {
                ViewMode.CURVE -> InteractiveCurveView(
                    eqBands = currentProfile?.getBands() ?: List(10) { 0f },
                    spectrumData = currentSpectrum,
                    onBandChange = { index, value -> viewModel.updateBandLevel(index, value) }
                )
                ViewMode.MIXER -> ProMixerBoardView(
                    bands = currentProfile?.getBands() ?: List(10) { 0f },
                    spectrumData = currentSpectrum,
                    onBandChange = { index, value -> viewModel.updateBandLevel(index, value) }
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
                        Text(preset.displayName, maxLines = 1)
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
                        Text(preset.displayName, maxLines = 1)
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
fun InteractiveCurveView(
    eqBands: List<Float>,
    spectrumData: FloatArray,
    onBandChange: (Int, Float) -> Unit
) {
    var draggedBands by remember { mutableStateOf(eqBands) }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        color = VoidBlackLight,
        shape = RoundedCornerShape(12.dp)
    ) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        // Apply all changes
                        draggedBands.forEachIndexed { index, value ->
                            onBandChange(index, value)
                        }
                    },
                    onDrag = { change, _ ->
                        val x = change.position.x
                        val y = change.position.y
                        
                        // Determine which band was touched
                        val bandIndex = (x / (size.width / eqBands.size)).toInt()
                            .coerceIn(0, eqBands.size - 1)
                        
                        // Calculate new value from Y position
                        val centerY = size.height / 2f
                        val maxBarHeight = size.height * 0.4f
                        val normalizedY = (centerY - y) / maxBarHeight
                        val newValue = (normalizedY * 12f).coerceIn(-12f, 12f)
                        
                        draggedBands = draggedBands.toMutableList().apply {
                            this[bandIndex] = newValue
                        }
                    }
                )
            }
        ) {
            val barCount = eqBands.size
            val barWidth = size.width / (barCount * 1.5f)
            val spacing = barWidth * 0.5f
            val centerY = size.height / 2f
            val maxBarHeight = size.height * 0.4f
            
            // Draw real-time spectrum (background - showing what's playing)
            spectrumData.take(barCount).forEachIndexed { index, specValue ->
                val x = index * (barWidth + spacing) + spacing
                val normalizedSpec = (specValue + 96f) / 96f // Normalize -96 to 0 dB
                val specHeight = maxBarHeight * normalizedSpec.coerceIn(0f, 1f)
                
                // Spectrum bars (ghosted)
                drawRect(
                    color = VoidPurple.copy(alpha = 0.2f),
                    topLeft = Offset(x, centerY - specHeight / 2),
                    size = Size(barWidth, specHeight)
                )
            }
            
            // Draw EQ adjustment bars
            draggedBands.forEachIndexed { index, value ->
                val x = index * (barWidth + spacing) + spacing
                val normalizedValue = value / 12f
                val barHeight = normalizedValue * maxBarHeight
                
                val color = when {
                    value > 6f -> VoidPink
                    value > 0f -> VoidCyan
                    value > -6f -> VoidPurple
                    else -> VoidCyan.copy(alpha = 0.5f)
                }
                
                // Bar with glow
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(color.copy(alpha = 0.2f), color.copy(alpha = 0.4f)),
                        startY = centerY - kotlin.math.abs(barHeight) - 8f,
                        endY = centerY + kotlin.math.abs(barHeight) + 8f
                    ),
                    topLeft = Offset(x - 4f, centerY - kotlin.math.abs(barHeight) / 2 - 4f),
                    size = Size(barWidth + 8f, kotlin.math.abs(barHeight) + 8f)
                )
                
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(color, color.copy(alpha = 0.5f))
                    ),
                    topLeft = Offset(x, centerY - kotlin.math.abs(barHeight) / 2),
                    size = Size(barWidth, kotlin.math.abs(barHeight))
                )
            }
            
            // Center line
            drawLine(
                color = VoidCyan.copy(alpha = 0.3f),
                start = Offset(0f, centerY),
                end = Offset(size.width, centerY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            
            // Draw smooth curve through points
            val path = Path().apply {
                draggedBands.forEachIndexed { index, value ->
                    val x = index * (barWidth + spacing) + spacing + barWidth / 2
                    val normalizedValue = value / 12f
                    val y = centerY - normalizedValue * maxBarHeight
                    
                    if (index == 0) {
                        moveTo(x, y)
                    } else {
                        lineTo(x, y)
                    }
                }
            }
            
            drawPath(
                path = path,
                color = VoidCyan.copy(alpha = 0.6f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun ProMixerBoardView(
    bands: List<Float>,
    spectrumData: FloatArray,
    onBandChange: (Int, Float) -> Unit
) {
    val bandLabels = listOf("31Hz", "62Hz", "125Hz", "250Hz", "500Hz", "1kHz", "2kHz", "4kHz", "8kHz", "16kHz")
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        color = Color(0xFF1a1a1a),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            bands.forEachIndexed { index, value ->
                val specValue = if (index < spectrumData.size) spectrumData[index] else 0f
                ProFaderChannel(
                    value = value,
                    label = bandLabels[index],
                    spectrumLevel = specValue,
                    onValueChange = { onBandChange(index, it) }
                )
            }
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
        
        // Fader track with spectrum visualization
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
                // Spectrum level indicator (background)
                val normalizedSpec = ((spectrumLevel + 96f) / 96f).coerceIn(0f, 1f)
                val specHeight = size.height * normalizedSpec
                
                drawRect(
                    color = VoidPurple.copy(alpha = 0.3f),
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

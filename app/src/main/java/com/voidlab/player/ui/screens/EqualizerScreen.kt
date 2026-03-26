package com.voidlab.player.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
                ViewMode.MIXER -> ProMixerBoardView(
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
    // Animated bars showing EQ activity
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        color = VoidBlackLight,
        shape = RoundedCornerShape(12.dp)
    ) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
        ) {
            val barCount = bands.size
            val barWidth = size.width / (barCount * 2f)
            val spacing = barWidth
            val centerY = size.height / 2f
            val maxBarHeight = size.height * 0.4f
            
            bands.forEachIndexed { index, value ->
                val x = index * (barWidth + spacing) + barWidth / 2
                val normalizedValue = value / 12f // -12 to +12 range
                val barHeight = normalizedValue * maxBarHeight
                
                // Gradient color based on value
                val color = when {
                    value > 6f -> VoidPink
                    value > 0f -> VoidCyan
                    value > -6f -> VoidPurple
                    else -> VoidCyan.copy(alpha = 0.5f)
                }
                
                // Draw bar
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(color, color.copy(alpha = 0.3f))
                    ),
                    topLeft = Offset(x, centerY - abs(barHeight) / 2),
                    size = Size(barWidth, abs(barHeight))
                )
                
                // Glow effect
                drawRect(
                    color = color.copy(alpha = 0.2f),
                    topLeft = Offset(x - 2.dp.toPx(), centerY - abs(barHeight) / 2 - 2.dp.toPx()),
                    size = Size(barWidth + 4.dp.toPx(), abs(barHeight) + 4.dp.toPx())
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
        }
    }
}

@Composable
fun ProMixerBoardView(
    bands: List<Float>,
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
                ProFaderChannel(
                    value = value,
                    label = bandLabels[index],
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
    onValueChange: (Float) -> Unit
) {
    var dragValue by remember { mutableStateOf(value) }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight()
    ) {
        // dB value display (LED style)
        Surface(
            color = VoidBlack,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.size(width = 45.dp, height = 20.dp)
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
        
        // Fader track
        Box(
            modifier = Modifier
                .width(50.dp)
                .weight(1f)
        ) {
            // Track background
            Canvas(modifier = Modifier
                .width(6.dp)
                .fillMaxHeight()
                .align(Alignment.Center)
            ) {
                // Dark track
                drawRoundRect(
                    color = Color(0xFF0d0d0d),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                )
                
                // Center notch
                val centerY = size.height / 2f
                drawLine(
                    color = VoidCyan.copy(alpha = 0.5f),
                    start = Offset(-4.dp.toPx(), centerY),
                    end = Offset(size.width + 4.dp.toPx(), centerY),
                    strokeWidth = 2.dp.toPx()
                )
                
                // Active section gradient
                val normalizedValue = (dragValue + 12f) / 24f // 0 to 1
                val faderY = size.height * (1f - normalizedValue)
                
                if (faderY < centerY) {
                    // Boost (cyan/pink)
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
                    // Cut (purple)
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
                    .offset(y = maxOffset * (1f - normalizedValue) - 10.dp)
                    .size(50.dp, 20.dp)
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
                            onDragEnd = {
                                onValueChange(dragValue)
                            },
                            onVerticalDrag = { _, _ -> }
                        )
                    }
            ) {
                // Fader cap (metallic look)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF2a2a2a),
                    shadowElevation = 4.dp
                ) {
                    Box {
                        // Top highlight
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF404040),
                                        Color(0xFF1a1a1a)
                                    )
                                ),
                                size = size
                            )
                            
                            // Center line indicator
                            drawLine(
                                color = VoidCyan,
                                start = Offset(size.width * 0.2f, size.height / 2),
                                end = Offset(size.width * 0.8f, size.height / 2),
                                strokeWidth = 1.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Frequency label
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = VoidCyan.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
    }
}

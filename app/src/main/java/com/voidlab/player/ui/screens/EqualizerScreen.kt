package com.voidlab.player.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voidlab.player.data.models.EQProfile
import com.voidlab.player.ui.theme.*
import com.voidlab.player.ui.viewmodels.EQViewModel
import com.voidlab.player.ui.viewmodels.ViewMode

val bandLabels = listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")

@Composable
fun EqualizerScreen(
    viewModel: EQViewModel
) {
    val currentProfile by viewModel.currentProfile.collectAsState()
    val isAutoMode by viewModel.isAutoMode.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val currentSpectrum by viewModel.currentSpectrum.collectAsState()
    val learnedProfileCount by viewModel.learnedProfileCount.collectAsState()
    
    // Track the original (mastered) profile before Auto EQ adjustments
    var originalProfile by remember { mutableStateOf<EQProfile?>(null) }
    
    // When entering Auto mode, capture the current profile as "original"
    LaunchedEffect(isAutoMode) {
        if (isAutoMode && originalProfile == null) {
            originalProfile = currentProfile
        } else if (!isAutoMode) {
            originalProfile = null
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "EQUALIZER",
                    style = MaterialTheme.typography.headlineMedium,
                    color = VoidCyan,
                    fontWeight = FontWeight.Bold
                )
                
                if (isAutoMode) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = VoidPink,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "AUTO EQ ACTIVE",
                            style = MaterialTheme.typography.bodyMedium,
                            color = VoidPink,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // View mode toggle
            Surface(
                onClick = {
                    viewModel.setViewMode(
                        if (viewMode == ViewMode.CURVE) ViewMode.MIXER else ViewMode.CURVE
                    )
                },
                color = VoidBlackLight,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        if (viewMode == ViewMode.CURVE) Icons.Default.ShowChart else Icons.Default.Tune,
                        contentDescription = null,
                        tint = VoidCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (viewMode == ViewMode.CURVE) "CURVE" else "MIXER",
                        color = VoidCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Auto EQ toggle
        Surface(
            onClick = { viewModel.toggleAutoMode() },
            color = if (isAutoMode) VoidCyan.copy(alpha = 0.15f) else VoidBlackLight,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isAutoMode) VoidCyan else VoidCyan.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Auto EQ Learning",
                            color = if (isAutoMode) VoidCyan else VoidCyan.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$learnedProfileCount profiles learned",
                            color = VoidCyan.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                }
                
                Switch(
                    checked = isAutoMode,
                    onCheckedChange = { viewModel.toggleAutoMode() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = VoidCyan,
                        checkedTrackColor = VoidCyan.copy(alpha = 0.3f)
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // EQ Visualization
        when (viewMode) {
            ViewMode.CURVE -> InteractiveCurveView(
                currentProfile = currentProfile,
                originalProfile = originalProfile,
                spectrum = currentSpectrum,
                isAutoMode = isAutoMode,
                onBandChange = viewModel::updateBandLevel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
            ViewMode.MIXER -> ProMixerBoardView(
                currentProfile = currentProfile,
                originalProfile = originalProfile,
                spectrum = currentSpectrum,
                isAutoMode = isAutoMode,
                onBandChange = viewModel::updateBandLevel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
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
            viewModel.presets.take(4).forEach { preset ->
                Surface(
                    onClick = { viewModel.applyPreset(preset) },
                    color = VoidBlackLight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = preset.displayName,
                        color = VoidCyan,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun InteractiveCurveView(
    currentProfile: EQProfile?,
    originalProfile: EQProfile?,
    spectrum: FloatArray,
    isAutoMode: Boolean,
    onBandChange: (Int, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val bands = currentProfile?.getBands() ?: List(10) { 0f }
    val originalBands = originalProfile?.getBands()
    
    Canvas(modifier = modifier
        .clip(RoundedCornerShape(16.dp))
        .background(VoidBlackLight)
        .pointerInput(Unit) {
            detectDragGestures { change, _ ->
                val x = change.position.x
                val y = change.position.y
                val bandIndex = (x / size.width * 10).toInt().coerceIn(0, 9)
                val value = ((size.height - y) / size.height * 24f - 12f).coerceIn(-12f, 12f)
                onBandChange(bandIndex, value)
            }
        }
    ) {
        val width = size.width
        val height = size.height
        val bandWidth = width / 10
        
        // Draw spectrum bars (ghosted in background)
        for (i in 0 until 10) {
            val specValue = spectrum.getOrNull(i) ?: 0f
            val barHeight = specValue * height * 0.3f
            
            drawRect(
                color = VoidCyan.copy(alpha = 0.15f),
                topLeft = Offset(i * bandWidth + bandWidth * 0.2f, height - barHeight),
                size = Size(bandWidth * 0.6f, barHeight)
            )
        }
        
        // Draw original profile curve (if Auto EQ is active)
        if (isAutoMode && originalBands != null) {
            val originalPath = Path()
            originalBands.forEachIndexed { index, value ->
                val x = index * bandWidth + bandWidth / 2
                val y = height / 2 - (value / 12f * height / 2)
                
                if (index == 0) {
                    originalPath.moveTo(x, y)
                } else {
                    originalPath.lineTo(x, y)
                }
            }
            
            drawPath(
                path = originalPath,
                color = VoidCyan.copy(alpha = 0.3f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        // Draw current EQ curve
        val path = Path()
        bands.forEachIndexed { index, value ->
            val x = index * bandWidth + bandWidth / 2
            val y = height / 2 - (value / 12f * height / 2)
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        drawPath(
            path = path,
            color = if (isAutoMode) VoidPink else VoidCyan,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Draw control points
        bands.forEachIndexed { index, value ->
            val x = index * bandWidth + bandWidth / 2
            val y = height / 2 - (value / 12f * height / 2)
            
            drawCircle(
                color = if (isAutoMode) VoidPink else VoidCyan,
                radius = 6.dp.toPx(),
                center = Offset(x, y)
            )
        }
        
        // Draw center line
        drawLine(
            color = VoidCyan.copy(alpha = 0.2f),
            start = Offset(0f, height / 2),
            end = Offset(width, height / 2),
            strokeWidth = 1.dp.toPx()
        )
    }
}

@Composable
fun ProMixerBoardView(
    currentProfile: EQProfile?,
    originalProfile: EQProfile?,
    spectrum: FloatArray,
    isAutoMode: Boolean,
    onBandChange: (Int, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val bands = currentProfile?.getBands() ?: List(10) { 0f }
    val originalBands = originalProfile?.getBands()
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(VoidBlackLight)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        bands.forEachIndexed { index, value ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Label
                Text(
                    text = bandLabels[index],
                    color = VoidCyan.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Fader with spectrum and original overlay
                Box(
                    modifier = Modifier
                        .width(30.dp)
                        .height(200.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        
                        // Draw spectrum bar
                        val specValue = spectrum.getOrNull(index) ?: 0f
                        val barHeight = specValue * height * 0.5f
                        
                        drawRect(
                            color = VoidCyan.copy(alpha = 0.2f),
                            topLeft = Offset(0f, height - barHeight),
                            size = Size(width, barHeight)
                        )
                        
                        // Draw original fader position (if Auto EQ active)
                        if (isAutoMode && originalBands != null) {
                            val originalValue = originalBands[index]
                            val originalY = height / 2 - (originalValue / 12f * height / 2)
                            
                            drawRect(
                                color = VoidCyan.copy(alpha = 0.3f),
                                topLeft = Offset(0f, originalY - 2.dp.toPx()),
                                size = Size(width, 4.dp.toPx())
                            )
                        }
                        
                        // Draw track
                        drawRect(
                            color = VoidBlack,
                            topLeft = Offset(width / 2 - 2.dp.toPx(), 0f),
                            size = Size(4.dp.toPx(), height)
                        )
                    }
                    
                    // Draggable knob
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .offset(y = (-100.dp * (value / 12f)).coerceIn(-100.dp, 100.dp))
                            .clip(CircleShape)
                            .background(if (isAutoMode) VoidPink else VoidCyan)
                            .pointerInput(index) {
                                detectDragGestures { change, dragAmount ->
                                    val newValue = (value - dragAmount.y / 200f * 24f).coerceIn(-12f, 12f)
                                    onBandChange(index, newValue)
                                }
                            }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Value display
                Text(
                    text = "${value.toInt()}dB",
                    color = if (isAutoMode) VoidPink else VoidCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

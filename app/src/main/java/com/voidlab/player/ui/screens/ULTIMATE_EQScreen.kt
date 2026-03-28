package com.voidlab.player.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
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
    val currentSpectrum by viewModel.currentSpectrum.collectAsState()
    val profileCount by viewModel.learnedProfileCount.collectAsState()
    
    var showPresetsSheet by remember { mutableStateOf(false) }
    
    val bands = currentProfile?.getBands() ?: List(10) { 0f }
    val freqLabels = listOf("31", "62", "125", "250", "500", "1K", "2K", "4K", "8K", "16K")
    
    // Auto EQ should be on by default
    LaunchedEffect(Unit) {
        if (!isAutoMode) {
            viewModel.toggleAutoMode()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DeepBlack,
                        DarkGray.copy(alpha = 0.5f),
                        DeepBlack
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with glow effect
            Box {
                // Glow background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .blur(40.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    NeonCyan.copy(alpha = 0.3f),
                                    NeonMagenta.copy(alpha = 0.3f)
                                )
                            )
                        )
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "EQ",
                            style = MaterialTheme.typography.headlineLarge,
                            color = NeonCyan,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp
                        )
                        if (isAutoMode) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PulsingIndicator()
                                Text(
                                    text = "AUTO LEARNING • $profileCount PROFILES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HotPink,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    // Auto EQ toggle
                    Surface(
                        onClick = { viewModel.toggleAutoMode() },
                        color = if (isAutoMode) NeonCyan else MidGray,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = if (isAutoMode) 8.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (isAutoMode) DeepBlack else NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "AUTO",
                                color = if (isAutoMode) DeepBlack else NeonCyan,
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // View mode selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CyberButton(
                    label = "CURVE",
                    icon = Icons.Default.ShowChart,
                    isSelected = viewMode == ViewMode.CURVE,
                    onClick = { viewModel.setViewMode(ViewMode.CURVE) },
                    modifier = Modifier.weight(1f)
                )
                CyberButton(
                    label = "FADERS",
                    icon = Icons.Default.Tune,
                    isSelected = viewMode == ViewMode.MIXER,
                    onClick = { viewModel.setViewMode(ViewMode.MIXER) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Main EQ view
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (viewMode) {
                    ViewMode.CURVE -> {
                        InteractiveCurveCanvas(
                            bands = bands,
                            spectrum = currentSpectrum,
                            freqLabels = freqLabels,
                            onBandChange = { index, value -> viewModel.updateBandLevel(index, value) }
                        )
                    }
                    
                    ViewMode.MIXER -> {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MidGray.copy(alpha = 0.3f),
                                            DeepBlack.copy(alpha = 0.8f)
                                        )
                                    )
                                )
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            bands.forEachIndexed { index, value ->
                                CyberFader(
                                    label = freqLabels[index],
                                    value = value,
                                    spectrumValue = currentSpectrum.getOrNull(index) ?: 0f,
                                    onValueChange = { viewModel.updateBandLevel(index, it) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Presets button
            Button(
                onClick = { showPresetsSheet = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(NeonMagenta, NeonPurple)
                            ),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.List, contentDescription = null, tint = Color.White)
                        Text("PRESETS", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
    
    // Presets sheet
    if (showPresetsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPresetsSheet = false },
            containerColor = MidGray
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "EQ PRESETS",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonCyan,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                viewModel.presets.forEach { preset ->
                    Surface(
                        onClick = {
                            viewModel.applyPreset(preset)
                            showPresetsSheet = false
                        },
                        color = DarkGray,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = preset.displayName,
                                color = NeonCyan,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = HotPink,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun PulsingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(HotPink.copy(alpha = alpha))
    )
}

@Composable
fun InteractiveCurveCanvas(
    bands: List<Float>,
    spectrum: FloatArray,
    freqLabels: List<String>,
    onBandChange: (Int, Float) -> Unit
) {
    val density = LocalDensity.current
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MidGray.copy(alpha = 0.3f),
                        DeepBlack.copy(alpha = 0.8f)
                    )
                )
            )
            .padding(24.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        val bandIndex = (offset.x / (size.width / 10f)).toInt().coerceIn(0, 9)
                        draggingIndex = bandIndex
                    },
                    onDragEnd = { draggingIndex = null },
                    onDragCancel = { draggingIndex = null }
                ) { change, _ ->
                    val bandIndex = (change.position.x / (size.width / 10f)).toInt().coerceIn(0, 9)
                    val normalizedY = ((size.height / 2f - change.position.y) / (size.height / 2f)).coerceIn(-1f, 1f)
                    val dbValue = (normalizedY * 12f).coerceIn(-12f, 12f)
                    onBandChange(bandIndex, dbValue)
                    draggingIndex = bandIndex
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val bandWidth = width / 10f
        val centerY = height / 2f
        
        // Draw grid lines
        for (i in 0..4) {
            val y = (i / 4f) * height
            drawLine(
                color = LightGray.copy(alpha = 0.1f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }
        
        // Draw spectrum bars (background)
        spectrum.take(10).forEachIndexed { index, value ->
            val x = index * bandWidth + bandWidth / 2
            val barHeight = value * height * 0.4f
            
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ElectricBlue.copy(alpha = 0.6f),
                        ElectricBlue.copy(alpha = 0.1f)
                    ),
                    startY = centerY - barHeight,
                    endY = centerY
                ),
                start = Offset(x, centerY),
                end = Offset(x, centerY - barHeight),
                strokeWidth = bandWidth * 0.7f,
                cap = StrokeCap.Round
            )
        }
        
        // Draw EQ curve
        val path = Path()
        bands.forEachIndexed { index, db ->
            val x = index * bandWidth + bandWidth / 2
            val y = centerY - (db / 12f) * (height / 2f)
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                val prevX = (index - 1) * bandWidth + bandWidth / 2
                val prevDb = bands[index - 1]
                val prevY = centerY - (prevDb / 12f) * (height / 2f)
                
                val midX = (prevX + x) / 2f
                path.cubicTo(
                    midX, prevY,
                    midX, y,
                    x, y
                )
            }
        }
        
        // Draw glow behind curve
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    NeonCyan.copy(alpha = 0.5f),
                    NeonMagenta.copy(alpha = 0.5f)
                )
            ),
            style = Stroke(width = 12f, cap = StrokeCap.Round)
        )
        
        // Draw main curve
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(NeonCyan, NeonMagenta, NeonPurple)
            ),
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )
        
        // Draw control dots
        bands.forEachIndexed { index, db ->
            val x = index * bandWidth + bandWidth / 2
            val y = centerY - (db / 12f) * (height / 2f)
            
            val isDragging = draggingIndex == index
            val dotColor = if (isDragging) NeonYellow else NeonCyan
            val dotSize = if (isDragging) 16f else 12f
            
            // Glow
            drawCircle(
                color = dotColor.copy(alpha = 0.3f),
                radius = dotSize * 2f,
                center = Offset(x, y)
            )
            
            // Dot
            drawCircle(
                color = dotColor,
                radius = dotSize,
                center = Offset(x, y)
            )
            
            // Inner highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = dotSize / 2f,
                center = Offset(x, y)
            )
        }
        
        // Draw center line
        drawLine(
            color = NeonCyan.copy(alpha = 0.2f),
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 2f
        )
        
        // Draw frequency labels
        freqLabels.forEachIndexed { index, label ->
            val x = index * bandWidth + bandWidth / 2
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#00FFF0")
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                }
                drawText(label, x, height - 10f, paint)
            }
        }
    }
}

@Composable
fun CyberFader(
    label: String,
    value: Float,
    spectrumValue: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var faderHeight by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Value display
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonCyan, NeonPurple)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${value.toInt()}",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        BoxWithConstraints(
            modifier = Modifier
                .width(48.dp)
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(DeepBlack)
        ) {
            val maxHeightPx = constraints.maxHeight.toFloat()
            faderHeight = maxHeightPx
            
            // Spectrum background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(spectrumValue.coerceIn(0f, 1f))
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                ElectricBlue.copy(alpha = 0.5f),
                                ElectricBlue.copy(alpha = 0.1f)
                            )
                        )
                    )
            )
            
            // Fader track
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .align(Alignment.Center)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(NeonCyan.copy(alpha = 0.3f), NeonMagenta.copy(alpha = 0.3f))
                        )
                    )
            )
            
            // Fader knob
            val knobPosition = ((12f - value) / 24f).coerceIn(0f, 1f)
            val knobOffsetDp = with(density) { (knobPosition * (maxHeightPx / density.density - 32f)).dp }
            
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(32.dp)
                    .offset(y = knobOffsetDp)
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isDragging) 
                            Brush.horizontalGradient(listOf(NeonYellow, HotPink))
                        else 
                            Brush.horizontalGradient(listOf(NeonCyan, NeonPurple))
                    )
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = { isDragging = false },
                            onDragCancel = { isDragging = false }
                        ) { _, dragAmount ->
                            val delta = (dragAmount / faderHeight) * 24f
                            val newValue = (value - delta).coerceIn(-12f, 12f)
                            onValueChange(newValue)
                        }
                    }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Frequency label
        Text(
            text = label,
            color = NeonCyan.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CyberButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.Transparent else Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isSelected)
                        Brush.horizontalGradient(listOf(NeonCyan, NeonMagenta))
                    else
                        Brush.horizontalGradient(listOf(MidGray, LightGray)),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    label,
                    color = if (isSelected) Color.White else NeonCyan,
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                )
            }
        }
    }
}

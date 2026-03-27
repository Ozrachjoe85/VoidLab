package com.voidlab.player.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voidlab.player.ui.theme.*
import com.voidlab.player.ui.viewmodels.PlayerViewModel
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class VisualizerMode {
    COSMOS, MORPH, PULSE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualizerScreen(
    viewModel: PlayerViewModel
) {
    var currentMode by remember { mutableStateOf(VisualizerMode.COSMOS) }
    var isFullscreen by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val spectrum by viewModel.currentSpectrum.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        // Visualizer content
        when (currentMode) {
            VisualizerMode.COSMOS -> CosmosVisualizer(spectrum = spectrum)
            VisualizerMode.MORPH -> MorphVisualizer(spectrum = spectrum)
            VisualizerMode.PULSE -> PulseVisualizer(spectrum = spectrum)
        }
        
        // UI Overlay (hidden in fullscreen)
        AnimatedVisibility(
            visible = !isFullscreen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Hamburger menu button
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = VoidCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        
        // Menu drawer
        if (showMenu) {
            ModalBottomSheet(
                onDismissRequest = { showMenu = false },
                containerColor = VoidBlackLight
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "VISUALIZER OPTIONS",
                        style = MaterialTheme.typography.titleLarge,
                        color = VoidCyan,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Mode selection
                    Text(
                        text = "MODE",
                        style = MaterialTheme.typography.titleSmall,
                        color = VoidCyan.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    VisualizerMode.values().forEach { mode ->
                        MenuOption(
                            label = mode.name,
                            icon = when (mode) {
                                VisualizerMode.COSMOS -> Icons.Default.Star
                                VisualizerMode.MORPH -> Icons.Default.Brightness4
                                VisualizerMode.PULSE -> Icons.Default.RadioButtonChecked
                            },
                            isSelected = currentMode == mode,
                            onClick = {
                                currentMode = mode
                                showMenu = false
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Fullscreen toggle
                    Text(
                        text = "DISPLAY",
                        style = MaterialTheme.typography.titleSmall,
                        color = VoidCyan.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Surface(
                        onClick = {
                            isFullscreen = !isFullscreen
                            showMenu = false
                        },
                        color = if (isFullscreen) VoidCyan.copy(alpha = 0.2f) else VoidBlack,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Fullscreen,
                                    contentDescription = null,
                                    tint = if (isFullscreen) VoidCyan else VoidCyan.copy(alpha = 0.6f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Fullscreen",
                                    color = if (isFullscreen) VoidCyan else VoidCyan.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isFullscreen) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            
                            if (isFullscreen) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = VoidCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun MenuOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) VoidCyan.copy(alpha = 0.2f) else VoidBlack,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isSelected) VoidCyan else VoidCyan.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = label,
                    color = if (isSelected) VoidCyan else VoidCyan.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
            
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = VoidCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ============================================================================
// COSMOS VISUALIZER - COMPLETELY REDESIGNED
// ============================================================================
@Composable
fun CosmosVisualizer(
    spectrum: FloatArray,
    modifier: Modifier = Modifier
) {
    // Galaxy spiral arms
    val spiralArms = remember {
        List(3) {
            SpiralArm(
                angleOffset = it * 120f,
                radius = 200f + it * 50f,
                particleCount = 300
            )
        }
    }
    
    var time by remember { mutableStateOf(0f) }
    val bass = spectrum.take(2).average().toFloat()
    val mids = spectrum.slice(3..6).average().toFloat()
    val treble = spectrum.slice(7..9).average().toFloat()
    
    // Energy waves
    var waveIntensity by remember { mutableStateOf(0f) }
    
    LaunchedEffect(bass) {
        if (bass > 0.7f) {
            waveIntensity = bass
        }
    }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            time += 0.016f
            if (waveIntensity > 0f) {
                waveIntensity *= 0.95f
            }
        }
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        
        // Background energy field
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    VoidPurple.copy(alpha = mids * 0.4f),
                    VoidBlack
                ),
                center = Offset(centerX, centerY),
                radius = size.width * 0.6f
            ),
            radius = size.width * 0.6f,
            center = Offset(centerX, centerY)
        )
        
        // Spiral galaxy arms
        spiralArms.forEachIndexed { index, arm ->
            for (i in 0 until arm.particleCount) {
                val progress = i.toFloat() / arm.particleCount
                val angle = arm.angleOffset + progress * 720f + time * 10f
                val radius = arm.radius * progress
                
                val x = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * radius
                val y = centerY + sin(Math.toRadians(angle.toDouble())).toFloat() * radius
                
                // Spectrum-based color
                val hue = (index * 120f + time * 5f) % 360f
                val alpha = (1f - progress) * (0.3f + spectrum.average().toFloat() * 0.7f)
                
                drawCircle(
                    color = Color.hsv(hue, 0.8f, 1f).copy(alpha = alpha),
                    radius = 2f + treble * 3f,
                    center = Offset(x, y)
                )
            }
        }
        
        // Energy waves on bass
        if (waveIntensity > 0.3f) {
            for (i in 0..3) {
                val waveRadius = (waveIntensity * size.width * 0.3f) + i * 50f
                drawCircle(
                    color = VoidCyan.copy(alpha = waveIntensity * (1f - i * 0.2f)),
                    radius = waveRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2f)
                )
            }
        }
        
        // Central core
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.8f + bass * 0.2f),
                    VoidCyan.copy(alpha = 0.5f),
                    Color.Transparent
                ),
                center = Offset(centerX, centerY),
                radius = 30f + bass * 40f
            ),
            radius = 30f + bass * 40f,
            center = Offset(centerX, centerY)
        )
    }
}

data class SpiralArm(
    val angleOffset: Float,
    val radius: Float,
    val particleCount: Int
)

// ============================================================================
// MORPH VISUALIZER - LIQUID GEOMETRIC MORPHING
// ============================================================================
@Composable
fun MorphVisualizer(
    spectrum: FloatArray,
    modifier: Modifier = Modifier
) {
    var rotation by remember { mutableStateOf(0f) }
    var morphPhase by remember { mutableStateOf(0f) }
    
    val bass = spectrum.take(2).average().toFloat()
    val mids = spectrum.slice(3..6).average().toFloat()
    val treble = spectrum.slice(7..9).average().toFloat()
    
    val shapes = remember {
        listOf(
            MorphShape(sides = 32, radius = 150f, hue = 180f),
            MorphShape(sides = 3, radius = 130f, hue = 60f),
            MorphShape(sides = 4, radius = 140f, hue = 300f),
            MorphShape(sides = 5, radius = 135f, hue = 120f),
            MorphShape(sides = 6, radius = 140f, hue = 240f)
        )
    }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            rotation += 0.3f + mids * 1.5f
            morphPhase += 0.005f + bass * 0.01f
        }
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    VoidPurple.copy(alpha = bass * 0.3f),
                    Color.Transparent
                ),
                center = Offset(centerX, centerY),
                radius = 300f
            ),
            radius = 300f,
            center = Offset(centerX, centerY)
        )
        
        shapes.forEachIndexed { index, shape ->
            val layerRotation = rotation + index * 72f
            val warpAmount = bass * 30f
            val scaleAmount = 1f + treble * 0.3f
            
            rotate(degrees = layerRotation, pivot = Offset(centerX, centerY)) {
                val path = Path()
                val vertices = shape.sides
                val radius = shape.radius * scaleAmount
                
                for (i in 0..vertices) {
                    val angle = (i * 2 * PI / vertices).toFloat()
                    val warp = sin(morphPhase * 5f + i) * warpAmount
                    val r = radius + warp
                    
                    val x = centerX + cos(angle) * r
                    val y = centerY + sin(angle) * r
                    
                    if (i == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }
                path.close()
                
                val color = Color.hsv(
                    hue = (shape.hue + morphPhase * 20f) % 360f,
                    saturation = 0.8f,
                    value = 1f
                )
                
                drawPath(path = path, color = color.copy(alpha = 0.1f + mids * 0.3f))
                drawPath(path = path, color = color.copy(alpha = 0.5f + treble * 0.3f), style = Stroke(width = 2f))
            }
        }
        
        drawCircle(
            color = VoidCyan.copy(alpha = 0.6f + bass * 0.4f),
            radius = 10f + bass * 20f,
            center = Offset(centerX, centerY)
        )
    }
}

data class MorphShape(
    val sides: Int,
    val radius: Float,
    val hue: Float
)

// ============================================================================
// PULSE VISUALIZER
// ============================================================================
@Composable
fun PulseVisualizer(
    spectrum: FloatArray,
    modifier: Modifier = Modifier
) {
    val pulses = remember { mutableStateListOf<Pulse>() }
    val bass = spectrum.take(2).average().toFloat()
    var lastBeatTime by remember { mutableStateOf(0L) }
    
    LaunchedEffect(bass) {
        val currentTime = System.currentTimeMillis()
        if (bass > 0.6f && (currentTime - lastBeatTime) > 200) {
            pulses.add(
                Pulse(
                    radius = 0f,
                    maxRadius = 1.5f,
                    alpha = 1f,
                    speed = 0.01f + bass * 0.02f,
                    color = when {
                        bass > 0.8f -> VoidPink
                        bass > 0.7f -> VoidCyan
                        else -> VoidPurple
                    }
                )
            )
            lastBeatTime = currentTime
        }
    }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            pulses.removeAll { it.alpha <= 0f }
            pulses.forEach { pulse ->
                pulse.radius += pulse.speed
                pulse.alpha = 1f - (pulse.radius / pulse.maxRadius)
            }
        }
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val maxDimension = maxOf(size.width, size.height)
        
        val glowIntensity = spectrum.average().toFloat()
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    VoidCyan.copy(alpha = glowIntensity * 0.3f),
                    Color.Transparent
                ),
                center = Offset(centerX, centerY),
                radius = maxDimension * 0.5f
            ),
            radius = maxDimension * 0.5f,
            center = Offset(centerX, centerY)
        )
        
        pulses.forEach { pulse ->
            val radius = pulse.radius * maxDimension
            drawCircle(
                color = pulse.color.copy(alpha = pulse.alpha * 0.8f),
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 4.dp.toPx())
            )
        }
        
        drawCircle(
            color = VoidCyan.copy(alpha = 0.8f + bass * 0.2f),
            radius = 8.dp.toPx() * (1f + bass * 0.5f),
            center = Offset(centerX, centerY)
        )
    }
}

data class Pulse(
    var radius: Float,
    val maxRadius: Float,
    var alpha: Float,
    val speed: Float,
    val color: Color
)

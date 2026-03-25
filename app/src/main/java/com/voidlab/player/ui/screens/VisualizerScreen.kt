package com.voidlab.player.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voidlab.player.ui.theme.*
import kotlin.math.*
import kotlin.random.Random

enum class VisualizerMode {
    SPECTRUM, WAVE, COSMOS, MORPH
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualizerScreen() {
    var mode by remember { mutableStateOf(VisualizerMode.SPECTRUM) }
    var isFullscreen by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        // Main Visualizer
        when (mode) {
            VisualizerMode.SPECTRUM -> SpectrumVisualizer()
            VisualizerMode.WAVE -> WaveVisualizer()
            VisualizerMode.COSMOS -> CosmosVisualizer()
            VisualizerMode.MORPH -> MorphVisualizer()
        }
        
        // Controls Overlay (hidden in fullscreen)
        if (!isFullscreen) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            ) {
                // Header
                Surface(
                    color = VoidDarkGray.copy(alpha = 0.9f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "VISUALIZER",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                        
                        IconButton(onClick = { isFullscreen = true }) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Fullscreen",
                                tint = VoidCyan
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Mode Selector
                Surface(
                    color = VoidDarkGray.copy(alpha = 0.9f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        VisualizerMode.values().forEach { visualizerMode ->
                            VisualizerModeButton(
                                mode = visualizerMode,
                                isSelected = mode == visualizerMode,
                                onClick = { mode = visualizerMode }
                            )
                        }
                    }
                }
            }
        } else {
            // Tap to exit fullscreen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { isFullscreen = false }
            )
        }
    }
}

@Composable
fun VisualizerModeButton(
    mode: VisualizerMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (mode) {
        VisualizerMode.SPECTRUM -> Icons.Default.BarChart
        VisualizerMode.WAVE -> Icons.Default.Waves
        VisualizerMode.COSMOS -> Icons.Default.Brightness7
        VisualizerMode.MORPH -> Icons.Default.Blur
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            shape = CircleShape,
            color = if (isSelected) VoidCyan.copy(alpha = 0.3f) else Color.Transparent,
            modifier = Modifier.size(56.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = mode.name,
                    tint = if (isSelected) VoidCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = mode.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) VoidCyan else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SpectrumVisualizer() {
    // Simulate 64 frequency bands
    val bandCount = 64
    var bands by remember { mutableStateOf(FloatArray(bandCount) { 0f }) }
    
    // Animate bands
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(50)
            bands = FloatArray(bandCount) { index ->
                // Simulate bass-heavy spectrum
                val baseHeight = when {
                    index < 10 -> Random.nextFloat() * 0.8f + 0.2f  // Bass
                    index < 30 -> Random.nextFloat() * 0.6f + 0.1f  // Mids
                    else -> Random.nextFloat() * 0.4f               // Highs
                }
                baseHeight
            }
        }
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val barWidth = width / bandCount
        
        bands.forEachIndexed { index, magnitude ->
            val x = index * barWidth
            val barHeight = magnitude * height * 0.8f
            val y = height - barHeight
            
            // Color gradient based on frequency
            val color = when {
                index < 16 -> VoidPurple
                index < 32 -> VoidCyan
                index < 48 -> VoidPink
                else -> VoidGreen
            }
            
            // Draw bar with gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(color, color.copy(alpha = 0.3f)),
                    startY = y,
                    endY = height
                ),
                topLeft = Offset(x + barWidth * 0.1f, y),
                size = androidx.compose.ui.geometry.Size(barWidth * 0.8f, barHeight)
            )
        }
    }
}

@Composable
fun WaveVisualizer() {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )
    
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )
    
    val phase3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase3"
    )
    
    val phase4 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase4"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        
        drawWave(width, centerY, phase1, VoidPurple, 40f, 1.5f)
        drawWave(width, centerY, phase2, VoidCyan, 60f, 2f)
        drawWave(width, centerY, phase3, VoidPink, 50f, 1.8f)
        drawWave(width, centerY, phase4, VoidGreen, 70f, 2.2f)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWave(
    width: Float,
    centerY: Float,
    phase: Float,
    color: Color,
    amplitude: Float,
    frequency: Float
) {
    val path = Path()
    val points = 200
    
    path.moveTo(0f, centerY)
    
    for (i in 0..points) {
        val x = (i.toFloat() / points) * width
        val normalizedX = (x / width) * 2 * PI.toFloat() * frequency
        val y = centerY + sin(normalizedX + phase) * amplitude
        
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 3f, cap = StrokeCap.Round)
    )
}

@Composable
fun CosmosVisualizer() {
    data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        val color: Color
    )
    
    val particleCount = 600
    var particles by remember {
        mutableStateOf(
            List(particleCount) {
                Particle(
                    x = Random.nextFloat(),
                    y = Random.nextFloat(),
                    vx = (Random.nextFloat() - 0.5f) * 0.002f,
                    vy = (Random.nextFloat() - 0.5f) * 0.002f,
                    color = listOf(VoidPurple, VoidCyan, VoidPink, VoidGreen).random()
                )
            }
        )
    }
    
    // Animate particles
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(16) // ~60fps
            particles = particles.map { particle ->
                var newX = particle.x + particle.vx
                var newY = particle.y + particle.vy
                var newVx = particle.vx
                var newVy = particle.vy
                
                // Wrap around edges
                if (newX < 0f) newX = 1f
                if (newX > 1f) newX = 0f
                if (newY < 0f) newY = 1f
                if (newY > 1f) newY = 0f
                
                // Gravity pull towards center
                val dx = 0.5f - newX
                val dy = 0.5f - newY
                newVx += dx * 0.00001f
                newVy += dy * 0.00001f
                
                // Random "bass hit" burst
                if (Random.nextFloat() < 0.001f) {
                    newVx += (Random.nextFloat() - 0.5f) * 0.01f
                    newVy += (Random.nextFloat() - 0.5f) * 0.01f
                }
                
                particle.copy(x = newX, y = newY, vx = newVx, vy = newVy)
            }
        }
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // Draw connections between nearby particles
        for (i in particles.indices) {
            val p1 = particles[i]
            val x1 = p1.x * width
            val y1 = p1.y * height
            
            for (j in i + 1 until particles.size) {
                val p2 = particles[j]
                val x2 = p2.x * width
                val y2 = p2.y * height
                
                val distance = sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
                
                if (distance < 100f) {
                    val alpha = (1f - distance / 100f) * 0.3f
                    drawLine(
                        color = p1.color.copy(alpha = alpha),
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 1f
                    )
                }
            }
        }
        
        // Draw particles
        particles.forEach { particle ->
            drawCircle(
                color = particle.color,
                radius = 2f,
                center = Offset(particle.x * width, particle.y * height)
            )
        }
    }
}

@Composable
fun MorphVisualizer() {
    var glitchIntensity by remember { mutableStateOf(0f) }
    
    // Animate glitch
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(100)
            glitchIntensity = if (Random.nextFloat() < 0.3f) {
                Random.nextFloat() * 0.5f
            } else {
                0f
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        VoidPurple.copy(alpha = 0.3f),
                        VoidCyan.copy(alpha = 0.2f),
                        VoidPink.copy(alpha = 0.3f),
                        VoidBlack
                    )
                )
            )
    ) {
        // Simulated album art
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (glitchIntensity > 0) {
                        Modifier.blur(radius = (glitchIntensity * 20).dp)
                    } else {
                        Modifier
                    }
                )
        ) {
            val width = size.width
            val height = size.height
            
            // Draw chromatic aberration effect
            if (glitchIntensity > 0) {
                val offset = glitchIntensity * 20f
                
                // Red channel
                drawRect(
                    color = Color.Red.copy(alpha = 0.3f),
                    topLeft = Offset(-offset, 0f),
                    size = androidx.compose.ui.geometry.Size(width, height)
                )
                
                // Blue channel
                drawRect(
                    color = Color.Blue.copy(alpha = 0.3f),
                    topLeft = Offset(offset, 0f),
                    size = androidx.compose.ui.geometry.Size(width, height)
                )
            }
            
            // Draw scanlines
            for (i in 0 until height.toInt() step 4) {
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(0f, i.toFloat()),
                    end = Offset(width, i.toFloat()),
                    strokeWidth = 1f
                )
            }
            
            // Draw random glitch blocks
            if (glitchIntensity > 0.3f) {
                repeat(5) {
                    val x = Random.nextFloat() * width
                    val y = Random.nextFloat() * height
                    val w = Random.nextFloat() * 200f
                    val h = Random.nextFloat() * 50f
                    
                    drawRect(
                        color = listOf(VoidPurple, VoidCyan, VoidPink).random().copy(alpha = 0.6f),
                        topLeft = Offset(x, y),
                        size = androidx.compose.ui.geometry.Size(w, h)
                    )
                }
            }
        }
    }
}

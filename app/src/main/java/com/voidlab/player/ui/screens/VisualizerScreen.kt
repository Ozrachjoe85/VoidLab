package com.voidlab.player.ui.screens

import androidx.compose.animation.core.*
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voidlab.player.ui.theme.*
import kotlin.math.*
import kotlin.random.Random

enum class VisualizerMode {
    SPECTRUM, WAVE, COSMOS, MORPH
}

@Composable
fun VisualizerScreen() {
    var currentMode by remember { mutableStateOf(VisualizerMode.SPECTRUM) }
    var isFullscreen by remember { mutableStateOf(false) }
    
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
            if (!isFullscreen) {
                // Header
                Text(
                    text = "VISUALIZER",
                    style = MaterialTheme.typography.headlineMedium,
                    color = VoidCyan,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Mode Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VisualizerMode.entries.forEach { mode ->
                        FilterChip(
                            selected = currentMode == mode,
                            onClick = { currentMode = mode },
                            label = { Text(mode.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VoidCyan,
                                selectedLabelColor = VoidBlack,
                                containerColor = VoidBlackLight,
                                labelColor = VoidCyan
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Visualizer Display
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                color = VoidBlack,
                shape = if (!isFullscreen) RoundedCornerShape(12.dp) else RoundedCornerShape(0.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (currentMode) {
                        VisualizerMode.SPECTRUM -> ActiveSpectrumVisualizer()
                        VisualizerMode.WAVE -> ActiveWaveVisualizer()
                        VisualizerMode.COSMOS -> ActiveCosmosVisualizer()
                        VisualizerMode.MORPH -> ActiveMorphVisualizer()
                    }
                    
                    // Fullscreen Toggle
                    IconButton(
                        onClick = { isFullscreen = !isFullscreen },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = if (isFullscreen) "Exit Fullscreen" else "Fullscreen",
                            tint = VoidCyan.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            if (!isFullscreen) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Info
                Surface(
                    color = VoidBlackLight,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = getModeDescription(currentMode),
                            style = MaterialTheme.typography.bodyMedium,
                            color = VoidCyan.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveSpectrumVisualizer() {
    // Simulated FFT data - replace with real audio analysis
    val infiniteTransition = rememberInfiniteTransition(label = "spectrum")
    
    // 10-band spectrum analyzer
    val bands = List(10) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.1f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (400 + index * 50),
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "band$index"
        )
    }
    
    Canvas(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)
    ) {
        val barCount = bands.size
        val barWidth = size.width / (barCount * 2.2f)
        val spacing = barWidth * 0.6f
        val maxHeight = size.height * 0.85f
        
        bands.forEachIndexed { index, animatedValue ->
            val x = index * (barWidth + spacing) + spacing
            val barHeight = maxHeight * animatedValue.value
            val y = size.height - barHeight
            
            // Color gradient based on frequency
            val color = when (index) {
                in 0..2 -> VoidPink      // Bass
                in 3..6 -> VoidCyan      // Mids
                else -> VoidPurple        // Treble
            }
            
            // Glow effect
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = 0.1f),
                        color.copy(alpha = 0.3f)
                    ),
                    startY = y,
                    endY = size.height
                ),
                topLeft = Offset(x - 8f, y - 8f),
                size = Size(barWidth + 16f, barHeight + 16f)
            )
            
            // Main bar
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(color, color.copy(alpha = 0.5f)),
                    startY = y,
                    endY = size.height
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight)
            )
        }
    }
}

@Composable
fun ActiveWaveVisualizer() {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    
    Canvas(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)
    ) {
        val centerY = size.height / 2f
        val amplitude = size.height * 0.3f
        val frequency = 3f
        val pointCount = 200
        
        val path = Path().apply {
            moveTo(0f, centerY)
            
            for (i in 0..pointCount) {
                val x = (i.toFloat() / pointCount) * size.width
                val radians = ((x / size.width) * frequency * 2 * PI + Math.toRadians(phase.value.toDouble())).toFloat()
                val y = centerY + sin(radians) * amplitude
                
                if (i == 0) {
                    moveTo(x, y)
                } else {
                    lineTo(x, y)
                }
            }
        }
        
        // Glow
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(VoidCyan.copy(alpha = 0.3f), VoidPink.copy(alpha = 0.3f))
            ),
            style = Stroke(width = 12f, cap = StrokeCap.Round)
        )
        
        // Main wave
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(VoidCyan, VoidPurple, VoidPink)
            ),
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun ActiveCosmosVisualizer() {
    // Particle system
    val particles = remember {
        List(60) {
            ParticleState(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                speed = Random.nextFloat() * 0.5f + 0.2f,
                angle = Random.nextFloat() * 360f,
                size = Random.nextFloat() * 4f + 2f,
                color = when (Random.nextInt(3)) {
                    0 -> VoidCyan
                    1 -> VoidPink
                    else -> VoidPurple
                }
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "cosmos")
    val time = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 50000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val radians = Math.toRadians(particle.angle.toDouble() + time.value).toFloat()
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = min(size.width, size.height) * 0.4f
            
            val x = centerX + cos(radians) * radius * particle.speed
            val y = centerY + sin(radians) * radius * particle.speed
            
            // Glow
            drawCircle(
                color = particle.color.copy(alpha = 0.2f),
                radius = particle.size * 3f,
                center = Offset(x, y)
            )
            
            // Particle
            drawCircle(
                color = particle.color,
                radius = particle.size,
                center = Offset(x, y)
            )
        }
    }
}

data class ParticleState(
    val x: Float,
    val y: Float,
    val speed: Float,
    val angle: Float,
    val size: Float,
    val color: Color
)

@Composable
fun ActiveMorphVisualizer() {
    val infiniteTransition = rememberInfiniteTransition(label = "morph")
    val morph = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "morph"
    )
    
    val rotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = min(size.width, size.height) * 0.3f
        val sides = 6
        
        // Create morphing polygon
        val path = Path().apply {
            for (i in 0..sides) {
                val angle = (i.toFloat() / sides) * 2 * PI + Math.toRadians(rotation.value.toDouble())
                val morphFactor = 0.7f + morph.value * 0.6f
                val r = radius * morphFactor * (1f + sin(angle * 3).toFloat() * 0.2f)
                
                val x = centerX + cos(angle).toFloat() * r
                val y = centerY + sin(angle).toFloat() * r
                
                if (i == 0) {
                    moveTo(x, y)
                } else {
                    lineTo(x, y)
                }
            }
            close()
        }
        
        // Outer glow
        drawPath(
            path = path,
            brush = Brush.radialGradient(
                colors = listOf(VoidPink.copy(alpha = 0.3f), Color.Transparent),
                center = Offset(centerX, centerY),
                radius = radius * 1.5f
            ),
            style = Stroke(width = 20f)
        )
        
        // Main shape
        drawPath(
            path = path,
            brush = Brush.sweepGradient(
                colors = listOf(VoidCyan, VoidPurple, VoidPink, VoidCyan),
                center = Offset(centerX, centerY)
            ),
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )
    }
}

fun getModeDescription(mode: VisualizerMode): String {
    return when (mode) {
        VisualizerMode.SPECTRUM -> "Real-time frequency spectrum analysis across 10 bands from 31Hz to 16kHz. Watch the music's energy distribution."
        VisualizerMode.WAVE -> "Classic oscilloscope-style waveform display. See the raw audio signal as it flows through the Void."
        VisualizerMode.COSMOS -> "Immersive particle system that reacts to bass, mids, and treble. Each frequency range controls particle behavior."
        VisualizerMode.MORPH -> "Organic geometric shapes that morph and pulse with the music's rhythm and intensity."
    }
}

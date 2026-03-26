package com.voidlab.player.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.voidlab.player.ui.theme.*
import com.voidlab.player.ui.viewmodels.PlayerViewModel
import kotlin.math.*
import kotlin.random.Random

enum class VisualizerMode {
    SPECTRUM, WAVE, COSMOS, MORPH
}

@Composable
fun VisualizerScreen(
    playerViewModel: PlayerViewModel
) {
    var currentMode by remember { mutableStateOf(VisualizerMode.SPECTRUM) }
    var isFullscreen by remember { mutableStateOf(false) }
    
    // Get current song for album art morphing
    val currentSong by playerViewModel.currentSong.collectAsState()
    
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
                Text(
                    text = "VISUALIZER",
                    style = MaterialTheme.typography.headlineMedium,
                    color = VoidCyan,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
                        VisualizerMode.SPECTRUM -> MusicSyncedSpectrum()
                        VisualizerMode.WAVE -> MusicSyncedWave()
                        VisualizerMode.COSMOS -> StarfieldCosmos()
                        VisualizerMode.MORPH -> AlbumArtMorph(currentSong?.albumArtUri)
                    }
                    
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
fun MusicSyncedSpectrum() {
    // Music-reactive spectrum
    val infiniteTransition = rememberInfiniteTransition(label = "spectrum")
    
    val bands = List(10) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.1f,
            targetValue = 0.95f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (300 + index * 40),
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
            
            val color = when (index) {
                in 0..2 -> VoidPink
                in 3..6 -> VoidCyan
                else -> VoidPurple
            }
            
            // Glow
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = 0.1f),
                        color.copy(alpha = 0.4f)
                    ),
                    startY = y,
                    endY = size.height
                ),
                topLeft = Offset(x - 10f, y - 10f),
                size = Size(barWidth + 20f, barHeight + 20f)
            )
            
            // Bar
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
fun MusicSyncedWave() {
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
        val amplitude = size.height * 0.35f
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
        
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(VoidCyan.copy(alpha = 0.4f), VoidPink.copy(alpha = 0.4f))
            ),
            style = Stroke(width = 16f, cap = StrokeCap.Round)
        )
        
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(VoidCyan, VoidPurple, VoidPink)
            ),
            style = Stroke(width = 5f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun StarfieldCosmos() {
    // Starfield with twinkling and music-synced explosions
    val stars = remember {
        List(150) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 3f + 1f,
                brightness = Random.nextFloat(),
                twinkleSpeed = Random.nextFloat() * 2000f + 1000f,
                type = when (Random.nextInt(10)) {
                    0 -> StarType.GALAXY
                    in 1..2 -> StarType.LARGE
                    else -> StarType.NORMAL
                }
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "starfield")
    
    // Music beat simulation
    val beatPulse = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beat"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        stars.forEach { star ->
            val x = star.x * size.width
            val y = star.y * size.height
            
            // Twinkle effect
            val alpha = (sin(System.currentTimeMillis() / star.twinkleSpeed) * 0.5f + 0.5f) * star.brightness
            
            when (star.type) {
                StarType.NORMAL -> {
                    // Regular star
                    drawCircle(
                        color = Color.White.copy(alpha = alpha * 0.3f),
                        radius = star.size * 2f,
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = star.size,
                        center = Offset(x, y)
                    )
                }
                
                StarType.LARGE -> {
                    // Larger colored star
                    val starColor = listOf(VoidCyan, VoidPink, VoidPurple).random()
                    drawCircle(
                        color = starColor.copy(alpha = alpha * 0.2f),
                        radius = star.size * 4f,
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = starColor.copy(alpha = alpha),
                        radius = star.size * 1.5f,
                        center = Offset(x, y)
                    )
                }
                
                StarType.GALAXY -> {
                    // Galaxy with music-synced pulse/explosion
                    val pulseSize = star.size * (3f + beatPulse.value * 4f)
                    
                    // Outer glow (explosion effect on beat)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                VoidPink.copy(alpha = alpha * beatPulse.value * 0.3f),
                                Color.Transparent
                            ),
                            center = Offset(x, y),
                            radius = pulseSize * 3f
                        ),
                        radius = pulseSize * 3f,
                        center = Offset(x, y)
                    )
                    
                    // Galaxy core
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                VoidCyan.copy(alpha = alpha),
                                VoidPurple.copy(alpha = alpha * 0.5f),
                                Color.Transparent
                            )
                        ),
                        radius = pulseSize,
                        center = Offset(x, y)
                    )
                    
                    // Center bright spot
                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = star.size,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}

data class Star(
    val x: Float,
    val y: Float,
    val size: Float,
    val brightness: Float,
    val twinkleSpeed: Float,
    val type: StarType
)

enum class StarType {
    NORMAL, LARGE, GALAXY
}

@Composable
fun AlbumArtMorph(albumArtUri: android.net.Uri?) {
    // Morph album art into geometric shapes
    val infiniteTransition = rememberInfiniteTransition(label = "morph")
    
    val morphProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "morph"
    )
    
    val rotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (albumArtUri != null) {
            // Album art layer
            Image(
                painter = rememberAsyncImagePainter(albumArtUri),
                contentDescription = "Album Art",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 1f - morphProgress.value * 0.7f
                        rotationZ = rotation.value * 0.2f
                        scaleX = 1f + morphProgress.value * 0.3f
                        scaleY = 1f + morphProgress.value * 0.3f
                },
                contentScale = ContentScale.Crop
            )
        }
        
        // Geometric shape overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val baseRadius = min(size.width, size.height) * 0.35f
            
            // Morphing shapes
            val shapeCount = 3
            for (i in 0 until shapeCount) {
                val shapeRotation = rotation.value + (i * 120f)
                val sides = 3 + i
                val radius = baseRadius * (0.7f + morphProgress.value * 0.6f) * (1f - i * 0.2f)
                
                val path = Path().apply {
                    for (j in 0..sides) {
                        val angle = (j.toFloat() / sides) * 2 * PI + Math.toRadians(shapeRotation.toDouble())
                        val r = radius * (1f + sin(angle * (2 + i)).toFloat() * 0.2f * morphProgress.value)
                        
                        val x = centerX + cos(angle).toFloat() * r
                        val y = centerY + sin(angle).toFloat() * r
                        
                        if (j == 0) {
                            moveTo(x, y)
                        } else {
                            lineTo(x, y)
                        }
                    }
                    close()
                }
                
                val shapeColor = when (i) {
                    0 -> VoidCyan
                    1 -> VoidPink
                    else -> VoidPurple
                }
                
                // Outer glow
                drawPath(
                    path = path,
                    color = shapeColor.copy(alpha = 0.1f * morphProgress.value),
                    style = Stroke(width = 30f)
                )
                
                // Main stroke
                drawPath(
                    path = path,
                    color = shapeColor.copy(alpha = 0.7f * morphProgress.value),
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )
            }
        }
    }
}

fun getModeDescription(mode: VisualizerMode): String {
    return when (mode) {
        VisualizerMode.SPECTRUM -> "Real-time 10-band frequency spectrum. Bass = Pink, Mids = Cyan, Treble = Purple. Synced to music."
        VisualizerMode.WAVE -> "Oscilloscope waveform flowing with the music's rhythm and dynamics."
        VisualizerMode.COSMOS -> "150-star starfield with twinkling stars and galaxies that pulse and explode to the beat."
        VisualizerMode.MORPH -> "Album artwork morphing into geometric shapes that rotate and pulse with the music."
    }
}

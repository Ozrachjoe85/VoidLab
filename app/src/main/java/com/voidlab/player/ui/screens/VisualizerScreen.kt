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
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.voidlab.player.ui.theme.*
import com.voidlab.player.ui.viewmodels.PlayerViewModel
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

// REMOVED: SPECTRUM, WAVE
// ADDED: PULSE, PARTICLES
enum class VisualizerMode {
    COSMOS, MORPH, PULSE, PARTICLES
}

@Composable
fun VisualizerScreen(
    playerViewModel: PlayerViewModel
) {
    var currentMode by remember { mutableStateOf(VisualizerMode.COSMOS) }
    var isFullscreen by remember { mutableStateOf(false) }
    
    val currentSong by playerViewModel.currentSong.collectAsState()
    val spectrum by playerViewModel.currentSpectrum.collectAsState()
    
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
                color = VoidBlack,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (currentMode) {
                    VisualizerMode.COSMOS -> CosmosVisualizer(spectrum = spectrum)
                    VisualizerMode.MORPH -> MorphVisualizer(
                        spectrum = spectrum,
                        albumArtUri = currentSong?.albumArtUri
                    )
                    VisualizerMode.PULSE -> PulseVisualizer(spectrum = spectrum)
                    VisualizerMode.PARTICLES -> ParticlesVisualizer(spectrum = spectrum)
                }
            }
        }
    }
}

// ============================================================================
// EPIC COSMOS - 800 Stars, Nebulae, Supernovae, Shooting Stars
// ============================================================================
@Composable
fun CosmosVisualizer(
    spectrum: FloatArray,
    modifier: Modifier = Modifier
) {
    // EPIC: 800 stars instead of 150
    val stars = remember {
        List(800) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 2f + 0.5f,
                speed = Random.nextFloat() * 0.0005f + 0.0001f,
                depth = Random.nextFloat()
            )
        }
    }
    
    val nebulae = remember {
        List(5) {
            Nebula(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 0.3f + 0.2f,
                hue = Random.nextFloat() * 360f
            )
        }
    }
    
    var time by remember { mutableStateOf(0f) }
    val bass = spectrum.take(2).average().toFloat()
    val treble = spectrum.slice(7..9).average().toFloat()
    
    var supernovaIntensity by remember { mutableStateOf(0f) }
    val shootingStars = remember { mutableStateListOf<ShootingStar>() }
    
    LaunchedEffect(bass) {
        if (bass > 0.7f && Random.nextFloat() > 0.95f) {
            supernovaIntensity = 1f
            while (supernovaIntensity > 0f) {
                delay(16)
                supernovaIntensity = (supernovaIntensity - 0.05f).coerceAtLeast(0f)
            }
        }
    }
    
    LaunchedEffect(treble) {
        if (treble > 0.6f && Random.nextFloat() > 0.9f) {
            shootingStars.add(
                ShootingStar(
                    x = Random.nextFloat(),
                    y = Random.nextFloat() * 0.3f,
                    vx = Random.nextFloat() * 0.002f + 0.001f,
                    vy = Random.nextFloat() * 0.001f + 0.0005f,
                    life = 1f
                )
            )
        }
    }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            time += 0.016f
            shootingStars.removeAll { it.life <= 0f }
            shootingStars.forEach {
                it.x += it.vx
                it.y += it.vy
                it.life -= 0.02f
            }
        }
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        // NEBULA CLOUDS
        nebulae.forEach { nebula ->
            val centerX = nebula.x * size.width
            val centerY = nebula.y * size.height
            val radius = nebula.radius * size.width
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.hsv(nebula.hue, 0.7f, 0.3f).copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = radius
                ),
                radius = radius,
                center = Offset(centerX, centerY)
            )
        }
        
        // BASS SHOCKWAVE
        if (bass > 0.5f) {
            val ringRadius = (bass * size.width * 0.5f).coerceAtMost(size.width)
            drawCircle(
                color = VoidCyan.copy(alpha = bass * 0.2f),
                radius = ringRadius,
                center = Offset(size.width / 2, size.height / 2),
                style = Stroke(width = 3f)
            )
        }
        
        // STARS with parallax depth
        stars.forEach { star ->
            val offsetX = (time * star.speed * star.depth * size.width) % size.width
            val x = (star.x * size.width + offsetX) % size.width
            val y = star.y * size.height
            
            val twinkle = 0.3f + spectrum.getOrNull((star.x * 10).toInt() % 10)?.times(0.7f) ?: 0f
            val alpha = twinkle * (0.5f + star.depth * 0.5f)
            val visualSize = star.size * (0.5f + star.depth * 1.5f)
            
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = visualSize,
                center = Offset(x, y)
            )
        }
        
        // SHOOTING STARS
        shootingStars.forEach { star ->
            val x = star.x * size.width
            val y = star.y * size.height
            val tailLength = 40f
            
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = star.life),
                        Color.Transparent
                    ),
                    start = Offset(x, y),
                    end = Offset(x - star.vx * tailLength * 1000, y - star.vy * tailLength * 1000)
                ),
                start = Offset(x, y),
                end = Offset(x - star.vx * tailLength * 1000, y - star.vy * tailLength * 1000),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }
        
        // SUPERNOVA FLASH
        if (supernovaIntensity > 0f) {
            drawRect(
                color = Color.White.copy(alpha = supernovaIntensity * 0.6f),
                size = size
            )
        }
    }
}

// ============================================================================
// MORPH - Album Art with Vertex Warping
// ============================================================================
@Composable
fun MorphVisualizer(
    spectrum: FloatArray,
    albumArtUri: android.net.Uri?,
    modifier: Modifier = Modifier
) {
    val painter = rememberAsyncImagePainter(albumArtUri)
    
    var time by remember { mutableStateOf(0f) }
    val bass = spectrum.take(2).average().toFloat()
    val mids = spectrum.slice(3..6).average().toFloat()
    val treble = spectrum.slice(7..9).average().toFloat()
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            time += 0.016f
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Album art base layer
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.3f
        )
        
        // Morphing geometric overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val baseRadius = minOf(size.width, size.height) * 0.3f
            
            // Circle that morphs with bass
            val circleCount = 6
            repeat(circleCount) { i ->
                val angle = (i.toFloat() / circleCount) * 2f * PI.toFloat() + time
                val radius = baseRadius * (1f + bass * 0.3f)
                
                // Warp offset based on mids
                val warpX = cos(angle + mids * PI.toFloat()) * mids * 50f
                val warpY = sin(angle + mids * PI.toFloat()) * mids * 50f
                
                val x = centerX + cos(angle) * radius + warpX
                val y = centerY + sin(angle) * radius + warpY
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            VoidCyan.copy(alpha = 0.3f + treble * 0.3f),
                            Color.Transparent
                        )
                    ),
                    radius = 40f + bass * 60f,
                    center = Offset(x, y)
                )
            }
            
            // Triangular warp grid
            val gridSize = 8
            for (row in 0 until gridSize) {
                for (col in 0 until gridSize) {
                    val x = (col.toFloat() / gridSize) * size.width
                    val y = (row.toFloat() / gridSize) * size.height
                    
                    // Bass warps vertices
                    val warpAmount = bass * 30f
                    val warpX = sin(time + row * 0.5f) * warpAmount
                    val warpY = cos(time + col * 0.5f) * warpAmount
                    
                    drawCircle(
                        color = VoidPurple.copy(alpha = 0.1f + mids * 0.2f),
                        radius = 2f + treble * 4f,
                        center = Offset(x + warpX, y + warpY)
                    )
                }
            }
        }
    }
}

// ============================================================================
// PULSE - Beat-Reactive Concentric Rings
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

// ============================================================================
// PARTICLES - 3D Swirling Particle Field
// ============================================================================
@Composable
fun ParticlesVisualizer(
    spectrum: FloatArray,
    modifier: Modifier = Modifier
) {
    val particles = remember {
        List(200) {
            Particle(
                x = Random.nextFloat() * 2f - 1f,
                y = Random.nextFloat() * 2f - 1f,
                z = Random.nextFloat() * 2f - 1f,
                vx = (Random.nextFloat() - 0.5f) * 0.002f,
                vy = (Random.nextFloat() - 0.5f) * 0.002f,
                vz = (Random.nextFloat() - 0.5f) * 0.002f,
                size = Random.nextFloat() * 3f + 1f,
                hue = Random.nextFloat() * 360f
            )
        }
    }
    
    var rotationAngle by remember { mutableStateOf(0f) }
    val bass = spectrum.take(2).average().toFloat()
    val mids = spectrum.slice(3..6).average().toFloat()
    val treble = spectrum.slice(7..9).average().toFloat()
    var explosionForce by remember { mutableStateOf(0f) }
    
    LaunchedEffect(bass) {
        if (bass > 0.8f) {
            explosionForce = 0.05f
        }
    }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            rotationAngle += 0.01f + mids * 0.02f
            
            if (explosionForce > 0f) {
                explosionForce *= 0.95f
            }
            
            particles.forEach { p ->
                val angle = rotationAngle + p.z * 0.1f
                val cos = cos(angle)
                val sin = sin(angle)
                
                val newX = p.x * cos - p.y * sin
                val newY = p.x * sin + p.y * cos
                
                p.x = newX + p.vx
                p.y = newY + p.vy
                p.z += p.vz
                
                if (explosionForce > 0f) {
                    val dist = sqrt(p.x * p.x + p.y * p.y + p.z * p.z)
                    if (dist > 0.1f) {
                        p.x += (p.x / dist) * explosionForce
                        p.y += (p.y / dist) * explosionForce
                        p.z += (p.z / dist) * explosionForce
                    }
                }
                
                if (p.x < -1.5f) p.x = 1.5f
                if (p.x > 1.5f) p.x = -1.5f
                if (p.y < -1.5f) p.y = 1.5f
                if (p.y > 1.5f) p.y = -1.5f
                if (p.z < -1.5f) p.z = 1.5f
                if (p.z > 1.5f) p.z = -1.5f
            }
        }
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val scale = minOf(size.width, size.height) * 0.3f
        
        val sortedParticles = particles.sortedBy { it.z }
        
        sortedParticles.forEach { p ->
            val perspective = 1f / (1f + p.z * 0.5f)
            val screenX = centerX + p.x * scale * perspective
            val screenY = centerY + p.y * scale * perspective
            
            val visualSize = p.size * perspective * (0.5f + treble * 0.5f)
            val hueShift = spectrum.average().toFloat() * 60f
            val color = Color.hsv((p.hue + hueShift) % 360f, 0.8f, 1f)
            val alpha = (0.3f + perspective * 0.7f) * (0.5f + mids * 0.5f)
            
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = visualSize,
                center = Offset(screenX, screenY)
            )
        }
    }
}

// ============================================================================
// DATA CLASSES
// ============================================================================
data class Star(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val depth: Float
)

data class Nebula(
    val x: Float,
    val y: Float,
    val radius: Float,
    val hue: Float
)

data class ShootingStar(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    var life: Float
)

data class Pulse(
    var radius: Float,
    val maxRadius: Float,
    var alpha: Float,
    val speed: Float,
    val color: Color
)

data class Particle(
    var x: Float,
    var y: Float,
    var z: Float,
    val vx: Float,
    val vy: Float,
    val vz: Float,
    val size: Float,
    val hue: Float
)

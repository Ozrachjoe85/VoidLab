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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voidlab.player.ui.theme.*
import com.voidlab.player.ui.viewmodels.PlayerViewModel
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

enum class VisualizerMode {
    COSMOS, MORPH, PULSE, PARTICLES
}

@Composable
fun VisualizerScreen(
    viewModel: PlayerViewModel
) {
    var currentMode by remember { mutableStateOf(VisualizerMode.COSMOS) }
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
            VisualizerMode.PARTICLES -> ParticlesVisualizer(spectrum = spectrum)
        }
        
        // Mode selector overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Surface(
                color = VoidBlackLight.copy(alpha = 0.9f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VisualizerMode.values().forEach { mode ->
                        ModeButton(
                            mode = mode,
                            isSelected = currentMode == mode,
                            onClick = { currentMode = mode }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModeButton(
    mode: VisualizerMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(80.dp, 60.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) VoidCyan else VoidBlackLight,
            contentColor = if (isSelected) VoidBlack else VoidCyan
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                when (mode) {
                    VisualizerMode.COSMOS -> Icons.Default.Star
                    VisualizerMode.MORPH -> Icons.Default.Brightness4
                    VisualizerMode.PULSE -> Icons.Default.RadioButtonChecked
                    VisualizerMode.PARTICLES -> Icons.Default.Grain
                },
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                mode.name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

// ============================================================================
// COSMOS VISUALIZER - EPIC VERSION WITH 800 STARS
// ============================================================================
@Composable
fun CosmosVisualizer(
    spectrum: FloatArray,
    modifier: Modifier = Modifier
) {
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
    val mids = spectrum.slice(3..6).average().toFloat()
    val treble = spectrum.slice(7..9).average().toFloat()
    
    var supernovaIntensity by remember { mutableStateOf(0f) }
    
    LaunchedEffect(bass) {
        if (bass > 0.7f && Random.nextFloat() > 0.95f) {
            supernovaIntensity = 1f
            while (supernovaIntensity > 0f) {
                delay(16)
                supernovaIntensity = (supernovaIntensity - 0.05f).coerceAtLeast(0f)
            }
        }
    }
    
    val shootingStars = remember { mutableStateListOf<ShootingStar>() }
    
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
        
        // STARS with parallax
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
    
    // Morph shapes (circle → triangle → square → pentagon → hexagon)
    val shapes = remember {
        listOf(
            MorphShape(sides = 32, radius = 150f, hue = 180f), // Circle
            MorphShape(sides = 3, radius = 130f, hue = 60f),   // Triangle
            MorphShape(sides = 4, radius = 140f, hue = 300f),  // Square
            MorphShape(sides = 5, radius = 135f, hue = 120f),  // Pentagon
            MorphShape(sides = 6, radius = 140f, hue = 240f)   // Hexagon
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
        
        // Background glow
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
        
        // Draw morphing shapes
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
                    
                    // Add bass-reactive warping
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
                
                // Draw shape with spectrum-reactive color
                val color = Color.hsv(
                    hue = (shape.hue + morphPhase * 20f) % 360f,
                    saturation = 0.8f,
                    value = 1f
                )
                
                drawPath(
                    path = path,
                    color = color.copy(alpha = 0.1f + mids * 0.3f)
                )
                
                drawPath(
                    path = path,
                    color = color.copy(alpha = 0.5f + treble * 0.3f),
                    style = Stroke(width = 2f)
                )
            }
        }
        
        // Center pulse
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
// PULSE VISUALIZER - BEAT-REACTIVE CONCENTRIC RINGS
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
        
        // Background glow
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
        
        // Active pulses
        pulses.forEach { pulse ->
            val radius = pulse.radius * maxDimension
            drawCircle(
                color = pulse.color.copy(alpha = pulse.alpha * 0.8f),
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 4.dp.toPx())
            )
        }
        
        // Center dot
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

// ============================================================================
// PARTICLES VISUALIZER - 3D SWIRLING PARTICLE FIELD
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
                val cosVal = cos(angle)
                val sinVal = sin(angle)
                
                val newX = p.x * cosVal - p.y * sinVal
                val newY = p.x * sinVal + p.y * cosVal
                
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

package com.voidlab.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voidlab.player.ui.theme.*

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
                        VisualizerMode.SPECTRUM -> SpectrumVisualizer()
                        VisualizerMode.WAVE -> WaveVisualizer()
                        VisualizerMode.COSMOS -> CosmosVisualizer()
                        VisualizerMode.MORPH -> MorphVisualizer()
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
fun SpectrumVisualizer() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = VoidCyan.copy(alpha = 0.3f)
            )
            Text(
                text = "Spectrum Analyzer",
                style = MaterialTheme.typography.bodyLarge,
                color = VoidCyan.copy(alpha = 0.5f)
            )
            Text(
                text = "10-band frequency visualization",
                style = MaterialTheme.typography.bodySmall,
                color = VoidCyan.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun WaveVisualizer() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.WavingHand,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = VoidPurple.copy(alpha = 0.3f)
            )
            Text(
                text = "Waveform",
                style = MaterialTheme.typography.bodyLarge,
                color = VoidPurple.copy(alpha = 0.5f)
            )
            Text(
                text = "Real-time audio waveform",
                style = MaterialTheme.typography.bodySmall,
                color = VoidPurple.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun CosmosVisualizer() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CameraRoll,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = VoidPink.copy(alpha = 0.3f)
            )
            Text(
                text = "Cosmos Mode",
                style = MaterialTheme.typography.bodyLarge,
                color = VoidPink.copy(alpha = 0.5f)
            )
            Text(
                text = "Particle system visualization",
                style = MaterialTheme.typography.bodySmall,
                color = VoidPink.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun MorphVisualizer() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Autorenew,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = VoidGreen.copy(alpha = 0.3f)
            )
            Text(
                text = "Morph Mode",
                style = MaterialTheme.typography.bodyLarge,
                color = VoidGreen.copy(alpha = 0.5f)
            )
            Text(
                text = "Morphing geometric shapes",
                style = MaterialTheme.typography.bodySmall,
                color = VoidGreen.copy(alpha = 0.3f)
            )
        }
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

package com.voidlab.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voidlab.player.ui.theme.*
import com.voidlab.player.ui.viewmodels.EQViewModel
import com.voidlab.player.ui.viewmodels.ViewMode

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
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .padding(16.dp)
    ) {
        // Header with Auto EQ toggle
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
                    Text(
                        text = "Auto EQ Active • $profileCount learned",
                        style = MaterialTheme.typography.bodySmall,
                        color = VoidPurple
                    )
                }
            }
            
            // Auto EQ toggle
            Surface(
                onClick = { viewModel.toggleAutoMode() },
                color = if (isAutoMode) VoidCyan else VoidBlackLight,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isAutoMode) VoidBlack else VoidCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "AUTO",
                        color = if (isAutoMode) VoidBlack else VoidCyan,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // View mode selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ViewModeButton(
                label = "CURVE",
                icon = Icons.Default.ShowChart,
                isSelected = viewMode == ViewMode.CURVE,
                onClick = { viewModel.setViewMode(ViewMode.CURVE) },
                modifier = Modifier.weight(1f)
            )
            ViewModeButton(
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
                    // Curve view with spectrum background
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(VoidBlackLight)
                            .padding(16.dp)
                    ) {
                        // Spectrum bars
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            currentSpectrum.take(10).forEachIndexed { index, value ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(value.coerceIn(0f, 1f))
                                        .padding(horizontal = 2.dp)
                                        .background(
                                            VoidCyan.copy(alpha = 0.3f),
                                            RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                        )
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Band sliders
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            bands.forEachIndexed { index, value ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = freqLabels[index],
                                        color = VoidCyan,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.width(40.dp)
                                    )
                                    
                                    Slider(
                                        value = value,
                                        onValueChange = { viewModel.updateBandLevel(index, it) },
                                        valueRange = -12f..12f,
                                        modifier = Modifier.weight(1f),
                                        colors = SliderDefaults.colors(
                                            thumbColor = VoidPurple,
                                            activeTrackColor = VoidCyan,
                                            inactiveTrackColor = VoidCyan.copy(alpha = 0.3f)
                                        )
                                    )
                                    
                                    Text(
                                        text = "${value.toInt()}dB",
                                        color = VoidCyan.copy(alpha = 0.7f),
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.width(50.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                ViewMode.MIXER -> {
                    // Fader view
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(VoidBlackLight)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        bands.forEachIndexed { index, value ->
                            FaderControl(
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
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = VoidCyan,
                contentColor = VoidBlack
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("PRESETS", fontWeight = FontWeight.Bold)
        }
    }
    
    // Presets sheet
    if (showPresetsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPresetsSheet = false },
            containerColor = VoidBlackLight
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "EQ PRESETS",
                    style = MaterialTheme.typography.titleLarge,
                    color = VoidCyan,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                viewModel.presets.forEach { preset ->
                    Surface(
                        onClick = {
                            viewModel.applyPreset(preset)
                            showPresetsSheet = false
                        },
                        color = VoidBlack,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = preset.displayName,
                                color = VoidCyan,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = VoidCyan.copy(alpha = 0.6f),
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
fun FaderControl(
    label: String,
    value: Float,
    spectrumValue: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var faderHeight by remember { mutableStateOf(0f) }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${value.toInt()}",
            color = VoidCyan,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        BoxWithConstraints(
            modifier = Modifier
                .width(40.dp)
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(VoidBlack)
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
                                VoidCyan.copy(alpha = 0.3f),
                                VoidCyan.copy(alpha = 0.1f)
                            )
                        )
                    )
            )
            
            // Fader track
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .align(Alignment.Center)
                    .background(VoidCyan.copy(alpha = 0.3f))
            )
            
            // Fader knob
            val knobPosition = ((12f - value) / 24f).coerceIn(0f, 1f)
            val knobOffsetDp = (knobPosition * (maxHeightPx / density - 20f)).dp
            
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(20.dp)
                    .offset(y = knobOffsetDp)
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isDragging) VoidCyan else VoidPurple)
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
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            color = VoidCyan.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun ViewModeButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) VoidCyan else VoidBlackLight,
            contentColor = if (isSelected) VoidBlack else VoidCyan
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

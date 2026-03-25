package com.voidlab.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.voidlab.player.data.models.AutoEQState
import com.voidlab.player.ui.theme.*
import com.voidlab.player.ui.viewmodels.PlayerViewModel

@Composable
fun NowPlayingScreen(
    viewModel: PlayerViewModel
) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val autoEQState by viewModel.autoEQState.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Auto EQ Badge
            if (autoEQState is AutoEQState.Learned) {
                Surface(
                    color = VoidCyan.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Auto EQ",
                            tint = VoidCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AUTO EQ ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = VoidCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Album Art
            Surface(
                modifier = Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(16.dp)),
                color = VoidBlackLight,
                shadowElevation = 8.dp
            ) {
                if (currentSong?.albumArtUri != null) {
                    AsyncImage(
                        model = currentSong!!.albumArtUri,
                        contentDescription = "Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(120.dp),
                            tint = VoidCyan.copy(alpha = 0.3f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Song Info
            Text(
                text = currentSong?.title ?: "No song playing",
                style = MaterialTheme.typography.headlineMedium,
                color = VoidCyan,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = currentSong?.artist ?: "Unknown Artist",
                style = MaterialTheme.typography.bodyLarge,
                color = VoidCyan.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Progress Bar (placeholder)
            LinearProgressIndicator(
                progress = { 0.5f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = VoidCyan,
                trackColor = VoidBlackLight,
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(
                    onClick = { viewModel.toggleShuffle() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffleEnabled) VoidCyan else VoidCyan.copy(alpha = 0.3f),
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // Previous
                IconButton(
                    onClick = { viewModel.skipToPrevious() }
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = VoidCyan,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                // Play/Pause
                FloatingActionButton(
                    onClick = { viewModel.playPause() },
                    containerColor = VoidCyan,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = VoidBlack,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                // Next
                IconButton(
                    onClick = { viewModel.skipToNext() }
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = VoidCyan,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                // Repeat
                IconButton(
                    onClick = { viewModel.cycleRepeatMode() }
                ) {
                    val icon = when (repeatMode) {
                        1 -> Icons.Default.Repeat // REPEAT_MODE_ALL
                        2 -> Icons.Default.RepeatOne // REPEAT_MODE_ONE
                        else -> Icons.Default.Repeat
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = "Repeat",
                        tint = if (repeatMode != 0) VoidCyan else VoidCyan.copy(alpha = 0.3f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Favorite button
            IconButton(
                onClick = { viewModel.toggleFavorite() }
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) VoidPink else VoidCyan,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

package com.voidlab.player.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.voidlab.player.R
import com.voidlab.player.data.models.Song
import com.voidlab.player.ui.theme.*
import com.voidlab.player.ui.viewmodels.LibraryViewModel
import com.voidlab.player.ui.viewmodels.PlayerViewModel
import com.voidlab.player.ui.viewmodels.SortMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    playerViewModel: PlayerViewModel,
    libraryViewModel: LibraryViewModel
) {
    val songs by libraryViewModel.songs.collectAsState()
    val searchQuery by libraryViewModel.searchQuery.collectAsState()
    val sortMode by libraryViewModel.sortMode.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "LIBRARY",
            style = MaterialTheme.typography.headlineMedium,
            color = VoidCyan,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { libraryViewModel.setSearchQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search songs...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { libraryViewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VoidCyan,
                unfocusedBorderColor = VoidCyan.copy(alpha = 0.5f),
                focusedTextColor = VoidCyan,
                unfocusedTextColor = VoidCyan
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sort options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SortMode.entries.take(3).forEach { mode ->
                FilterChip(
                    selected = sortMode == mode,
                    onClick = { libraryViewModel.setSortMode(mode) },
                    label = { Text(mode.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VoidCyan,
                        selectedLabelColor = VoidBlack
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SortMode.entries.drop(3).forEach { mode ->
                FilterChip(
                    selected = sortMode == mode,
                    onClick = { libraryViewModel.setSortMode(mode) },
                    label = { Text(mode.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VoidCyan,
                        selectedLabelColor = VoidBlack
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Song count
        Text(
            text = "${songs.size} songs",
            style = MaterialTheme.typography.bodyMedium,
            color = VoidCyan.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Songs list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(songs, key = { it.id }) { song ->
                SongItem(
                    song = song,
                    onClick = {
                        // CRITICAL: Pass the ENTIRE song list as a playlist!
                        val allSongs = songs
                        val clickedIndex = allSongs.indexOf(song)
                        playerViewModel.playPlaylist(allSongs, clickedIndex)
                    }
                )
            }
        }
    }
}

@Composable
fun SongItem(
    song: Song,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = VoidBlackLight,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art thumbnail
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = VoidBlack
            ) {
                if (song.albumArtUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(song.albumArtUri)
                            .crossfade(true)
                            .error(R.drawable.ic_launcher_foreground)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .build(),
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
                            tint = VoidCyan.copy(alpha = 0.3f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Song info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = VoidCyan,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${song.artist} • ${song.album}",
                    style = MaterialTheme.typography.bodySmall,
                    color = VoidCyan.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Duration
            Text(
                text = formatDuration(song.duration),
                style = MaterialTheme.typography.bodySmall,
                color = VoidCyan.copy(alpha = 0.5f)
            )
        }
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

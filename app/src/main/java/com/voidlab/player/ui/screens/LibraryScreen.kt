package com.voidlab.player.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.voidlab.player.data.models.Song
import com.voidlab.player.ui.theme.*
import com.voidlab.player.ui.viewmodels.LibraryViewModel
import com.voidlab.player.ui.viewmodels.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel
) {
    val songs by libraryViewModel.filteredSongs.collectAsState()
    val searchQuery by libraryViewModel.searchQuery.collectAsState()
    val isLoading by libraryViewModel.isLoading.collectAsState()
    val showFavoritesOnly by libraryViewModel.showFavoritesOnly.collectAsState()
    
    var showFilterSheet by remember { mutableStateOf(false) }
    
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
            modifier = Modifier.fillMaxSize()
        ) {
            Surface(
                color = VoidBlackLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIBRARY",
                            style = MaterialTheme.typography.headlineMedium,
                            color = VoidCyan,
                            fontWeight = FontWeight.Bold
                        )
                        
                        IconButton(onClick = { showFilterSheet = true }) {
                            Badge(
                                containerColor = if (showFavoritesOnly) VoidCyan else VoidCyan.copy(alpha = 0.3f)
                            ) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = "Filter",
                                    tint = if (showFavoritesOnly) VoidBlack else VoidCyan
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { libraryViewModel.setSearchQuery(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("Search songs, artists, albums...", color = VoidCyan.copy(alpha = 0.5f))
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = VoidCyan)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { libraryViewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = VoidCyan)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = VoidCyan,
                            unfocusedTextColor = VoidCyan,
                            focusedBorderColor = VoidCyan,
                            unfocusedBorderColor = VoidCyan.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = VoidCyan)
                }
            } else if (songs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = VoidCyan.copy(alpha = 0.3f)
                        )
                        Text(
                            text = if (showFavoritesOnly) "No favorites yet" else "No songs found",
                            color = VoidCyan.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (showFavoritesOnly) {
                            TextButton(onClick = { libraryViewModel.setShowFavoritesOnly(false) }) {
                                Text("Show all songs", color = VoidCyan)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(songs, key = { it.id }) { song ->
                        SongItem(
                            song = song,
                            onClick = {
                                playerViewModel.playPlaylist(songs, songs.indexOf(song))
                            }
                        )
                    }
                }
            }
        }
        
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                containerColor = VoidBlackLight
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "FILTER",
                        style = MaterialTheme.typography.titleLarge,
                        color = VoidCyan,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    FilterOption(
                        icon = Icons.Default.LibraryMusic,
                        label = "All Songs",
                        count = songs.size,
                        isSelected = !showFavoritesOnly,
                        onClick = {
                            libraryViewModel.setShowFavoritesOnly(false)
                            showFilterSheet = false
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    FilterOption(
                        icon = Icons.Default.Favorite,
                        label = "Favorites",
                        count = null,
                        isSelected = showFavoritesOnly,
                        onClick = {
                            libraryViewModel.setShowFavoritesOnly(true)
                            showFilterSheet = false
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun FilterOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int?,
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
            
            if (count != null) {
                Text(
                    text = "$count",
                    color = VoidCyan.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyMedium
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongItem(
    song: Song,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = VoidBlackLight,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(VoidBlack)
            ) {
                if (song.albumArtUri != null) {
                    AsyncImage(
                        model = song.albumArtUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Album,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        tint = VoidCyan.copy(alpha = 0.3f)
                    )
                }
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = song.title,
                    color = VoidCyan,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${song.artist} • ${song.album}",
                    color = VoidCyan.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Text(
                text = formatDuration(song.duration),
                color = VoidCyan.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format("%d:%02d", minutes, seconds)
}

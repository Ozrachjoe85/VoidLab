package com.voidlab.player.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidlab.player.audio.playback.PlaybackManager
import com.voidlab.player.data.models.Song
import com.voidlab.player.data.repository.FavoriteRepository
import com.voidlab.player.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playbackManager: PlaybackManager,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _sortOrder = MutableStateFlow(SortOrder.TITLE)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()
    
    val songs: StateFlow<List<Song>> = musicRepository.getSongs()
        .combine(searchQuery) { songs, query ->
            if (query.isBlank()) songs
            else songs.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true) ||
                it.album.contains(query, ignoreCase = true)
            }
        }
        .combine(sortOrder) { songs, order ->
            when (order) {
                SortOrder.TITLE -> songs.sortedBy { it.title }
                SortOrder.ARTIST -> songs.sortedBy { it.artist }
                SortOrder.ALBUM -> songs.sortedBy { it.album }
                SortOrder.DATE_ADDED -> songs.sortedByDescending { it.dateAdded }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val currentlyPlayingSong: StateFlow<Song?> = playbackManager.currentSong
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }
    
    fun playSong(song: Song) {
        playbackManager.playSong(song)
    }
    
    fun toggleFavorite(songId: Long) {
        viewModelScope.launch {
            favoriteRepository.toggleFavorite(songId)
        }
    }
    
    fun isFavorite(songId: Long): StateFlow<Boolean> {
        return favoriteRepository.isFavorite(songId)
            .stateIn(viewModelScope, SharingStarted.Lazily, false)
    }
    
    enum class SortOrder {
        TITLE, ARTIST, ALBUM, DATE_ADDED
    }
}

package com.voidlab.player.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {
    
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Filter mode: ALL or FAVORITES
    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()
    
    private val _favoriteIds = MutableStateFlow<Set<Long>>(emptySet())
    
    // Filtered songs based on search and favorites
    val filteredSongs: StateFlow<List<Song>> = combine(
        _songs,
        _searchQuery,
        _showFavoritesOnly,
        _favoriteIds
    ) { songs, query, favoritesOnly, favoriteIds ->
        var filtered = songs
        
        // Apply favorites filter first
        if (favoritesOnly) {
            filtered = filtered.filter { it.id in favoriteIds }
        }
        
        // Apply search filter
        if (query.isNotBlank()) {
            val lowerQuery = query.lowercase()
            filtered = filtered.filter {
                it.title.lowercase().contains(lowerQuery) ||
                it.artist.lowercase().contains(lowerQuery) ||
                it.album.lowercase().contains(lowerQuery)
            }
        }
        
        filtered
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    init {
        loadSongs()
        loadFavorites()
    }
    
    private fun loadSongs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                musicRepository.getAllSongs().collect { songList ->
                    _songs.value = songList
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun loadFavorites() {
        viewModelScope.launch {
            favoriteRepository.getAllFavorites().collect { favorites ->
                _favoriteIds.value = favorites.map { it.songId }.toSet()
            }
        }
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun toggleFavoritesFilter() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }
    
    fun setShowFavoritesOnly(show: Boolean) {
        _showFavoritesOnly.value = show
    }
    
    fun refreshLibrary() {
        loadSongs()
        loadFavorites()
    }
}

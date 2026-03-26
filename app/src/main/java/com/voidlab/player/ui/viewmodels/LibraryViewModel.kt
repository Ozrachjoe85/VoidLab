package com.voidlab.player.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidlab.player.data.models.Song
import com.voidlab.player.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortMode {
    TITLE, ARTIST, ALBUM, DATE_ADDED, DURATION
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {
    
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _sortMode = MutableStateFlow(SortMode.TITLE)
    val sortMode: StateFlow<SortMode> = _sortMode.asStateFlow()
    
    init {
        loadSongs()
    }
    
    private fun loadSongs() {
        viewModelScope.launch {
            musicRepository.getAllSongs().collect { songList ->
                _songs.value = applySortAndFilter(songList)
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        loadSongs()
    }
    
    fun setSortMode(mode: SortMode) {
        _sortMode.value = mode
        loadSongs()
    }
    
    private fun applySortAndFilter(songList: List<Song>): List<Song> {
        var filtered = songList
        
        // Apply search filter
        if (_searchQuery.value.isNotEmpty()) {
            filtered = filtered.filter { song ->
                song.title.contains(_searchQuery.value, ignoreCase = true) ||
                song.artist.contains(_searchQuery.value, ignoreCase = true) ||
                song.album.contains(_searchQuery.value, ignoreCase = true)
            }
        }
        
        // Apply sorting
        return when (_sortMode.value) {
            SortMode.TITLE -> filtered.sortedBy { it.title }
            SortMode.ARTIST -> filtered.sortedBy { it.artist }
            SortMode.ALBUM -> filtered.sortedBy { it.album }
            SortMode.DATE_ADDED -> filtered.sortedByDescending { it.dateAdded }
            SortMode.DURATION -> filtered.sortedByDescending { it.duration }
        }
    }
}

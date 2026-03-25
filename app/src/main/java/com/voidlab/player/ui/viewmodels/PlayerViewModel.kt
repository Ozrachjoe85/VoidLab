package com.voidlab.player.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidlab.player.audio.playback.PlaybackManager
import com.voidlab.player.data.models.Song
import com.voidlab.player.data.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playbackManager: PlaybackManager,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {
    
    val currentSong: StateFlow<Song?> = playbackManager.currentSong
    val isPlaying: StateFlow<Boolean> = playbackManager.isPlaying
    val currentPosition: StateFlow<Long> = playbackManager.currentPosition
    val isAutoEQEnabled: StateFlow<Boolean> = playbackManager.isAutoEQEnabled
    val isLearning: StateFlow<Boolean> = playbackManager.isLearning
    val learningProgress: StateFlow<Float> = playbackManager.learningProgress
    
    fun togglePlayPause() {
        playbackManager.togglePlayPause()
    }
    
    fun seekTo(positionMs: Long) {
        playbackManager.seekTo(positionMs)
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
    
    override fun onCleared() {
        playbackManager.release()
        super.onCleared()
    }
}

package com.voidlab.player.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.voidlab.player.data.models.AutoEQState
import com.voidlab.player.data.models.Song
import com.voidlab.player.data.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {
    
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _autoEQState = MutableStateFlow<AutoEQState>(AutoEQState.Idle)
    val autoEQState: StateFlow<AutoEQState> = _autoEQState.asStateFlow()
    
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()
    
    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()
    
    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()
    
    private var mediaController: MediaController? = null
    
    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }
        
        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _isShuffleEnabled.value = shuffleModeEnabled
        }
        
        override fun onRepeatModeChanged(repeatMode: Int) {
            _repeatMode.value = repeatMode
        }
    }
    
    fun setMediaController(controller: MediaController) {
        mediaController?.removeListener(playerListener)
        mediaController = controller
        mediaController?.addListener(playerListener)
        _isPlaying.value = controller.isPlaying
        _isShuffleEnabled.value = controller.shuffleModeEnabled
        _repeatMode.value = controller.repeatMode
    }
    
    fun playSong(song: Song) {
        _currentSong.value = song
        
        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.uri)
            .build()
        
        mediaController?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
        
        checkIfFavorite(song.id)
    }
    
    fun playPause() {
        mediaController?.apply {
            if (isPlaying) {
                pause()
            } else {
                play()
            }
        }
    }
    
    fun skipToNext() {
        mediaController?.seekToNext()
    }
    
    fun skipToPrevious() {
        mediaController?.seekToPrevious()
    }
    
    fun toggleShuffle() {
        mediaController?.shuffleModeEnabled = !(_isShuffleEnabled.value)
    }
    
    fun cycleRepeatMode() {
        val newMode = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        mediaController?.repeatMode = newMode
    }
    
    fun toggleFavorite() {
        val song = _currentSong.value ?: return
        viewModelScope.launch {
            favoriteRepository.toggleFavorite(song.id)
            checkIfFavorite(song.id)
        }
    }
    
    private fun checkIfFavorite(songId: Long) {
        viewModelScope.launch {
            favoriteRepository.isFavorite(songId).collect { isFav ->
    _isFavorite.value = isFav
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        mediaController?.removeListener(playerListener)
    }
}

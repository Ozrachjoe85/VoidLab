package com.voidlab.player.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.voidlab.player.data.models.AutoEQState
import com.voidlab.player.data.models.Song
import com.voidlab.player.data.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
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
            Log.d("PlayerViewModel", "Player onIsPlayingChanged: $isPlaying")
            _isPlaying.value = isPlaying
            if (isPlaying) {
                startProgressTracking()
            }
        }
        
        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _isShuffleEnabled.value = shuffleModeEnabled
        }
        
        override fun onRepeatModeChanged(repeatMode: Int) {
            _repeatMode.value = repeatMode
        }
        
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            // Update duration when song changes
            mediaController?.let {
                _duration.value = it.duration
            }
        }
    }
    
    init {
        // Start progress tracking loop
        startProgressTracking()
    }
    
    private fun startProgressTracking() {
        viewModelScope.launch {
            while (true) {
                mediaController?.let { controller ->
                    if (controller.isPlaying) {
                        _currentPosition.value = controller.currentPosition
                        _duration.value = controller.duration
                    }
                }
                delay(100) // Update every 100ms for smooth progress
            }
        }
    }
    
    fun setMediaController(controller: MediaController) {
        Log.d("PlayerViewModel", "========================================")
        Log.d("PlayerViewModel", "setMediaController called on instance: ${this.hashCode()}")
        mediaController?.removeListener(playerListener)
        mediaController = controller
        mediaController?.addListener(playerListener)
        _isPlaying.value = controller.isPlaying
        _isShuffleEnabled.value = controller.shuffleModeEnabled
        _repeatMode.value = controller.repeatMode
        Log.d("PlayerViewModel", "MediaController set successfully!")
        Log.d("PlayerViewModel", "MediaController.isPlaying: ${controller.isPlaying}")
        Log.d("PlayerViewModel", "========================================")
    }
    
    fun playSong(song: Song) {
        Log.d("PlayerViewModel", "========================================")
        Log.d("PlayerViewModel", "playSong called on instance: ${this.hashCode()}")
        Log.d("PlayerViewModel", "Song: ${song.title}")
        Log.d("PlayerViewModel", "Song URI: ${song.uri}")
        Log.d("PlayerViewModel", "MediaController is null: ${mediaController == null}")
        
        _currentSong.value = song
        
        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.uri)
            .build()
        
        Log.d("PlayerViewModel", "MediaItem created with URI: ${song.uri}")
        
        mediaController?.apply {
            Log.d("PlayerViewModel", "MediaController exists, setting media item")
            setMediaItem(mediaItem)
            Log.d("PlayerViewModel", "Calling prepare()")
            prepare()
            Log.d("PlayerViewModel", "Calling play()")
            play()
            Log.d("PlayerViewModel", "Play command sent")
            Log.d("PlayerViewModel", "MediaController.isPlaying: $isPlaying")
            Log.d("PlayerViewModel", "MediaController.playbackState: $playbackState")
            
            // Update duration
            _duration.value = duration
        } ?: run {
            Log.e("PlayerViewModel", "ERROR: MediaController is NULL! Cannot play song")
            Log.e("PlayerViewModel", "This instance (${this.hashCode()}) doesn't have MediaController")
        }
        
        Log.d("PlayerViewModel", "========================================")
        
        checkIfFavorite(song.id)
    }
    
    fun playPause() {
        Log.d("PlayerViewModel", "playPause called")
        mediaController?.apply {
            if (isPlaying) {
                Log.d("PlayerViewModel", "Pausing playback")
                pause()
            } else {
                Log.d("PlayerViewModel", "Resuming playback")
                play()
            }
        } ?: Log.e("PlayerViewModel", "MediaController is null in playPause()")
    }
    
    fun skipToNext() {
        Log.d("PlayerViewModel", "skipToNext called")
        mediaController?.seekToNext()
    }
    
    fun skipToPrevious() {
        Log.d("PlayerViewModel", "skipToPrevious called")
        mediaController?.seekToPrevious()
    }
    
    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
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
        Log.d("PlayerViewModel", "onCleared (instance ${this.hashCode()}) - removing listener")
        mediaController?.removeListener(playerListener)
    }
}

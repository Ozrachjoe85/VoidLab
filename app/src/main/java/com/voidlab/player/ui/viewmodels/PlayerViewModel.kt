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
    
    // PLAYLIST/QUEUE MANAGEMENT
    private val _playlist = MutableStateFlow<List<Song>>(emptyList())
    val playlist: StateFlow<List<Song>> = _playlist.asStateFlow()
    
    private var currentIndex = 0
    
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
            Log.d("PlayerViewModel", "Media item transition: ${mediaItem?.mediaId}")
            // Update current song when track changes
            mediaItem?.mediaId?.toLongOrNull()?.let { songId ->
                val song = _playlist.value.find { it.id == songId }
                if (song != null) {
                    _currentSong.value = song
                    currentIndex = _playlist.value.indexOf(song)
                    checkIfFavorite(songId)
                }
            }
            
            // Update duration
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
    
    /**
     * Play a full playlist/queue starting at a specific index.
     * This enables Next/Previous navigation and auto-advance.
     */
    fun playPlaylist(songs: List<Song>, startIndex: Int = 0) {
        Log.d("PlayerViewModel", "========================================")
        Log.d("PlayerViewModel", "playPlaylist called with ${songs.size} songs, startIndex=$startIndex")
        
        if (songs.isEmpty()) {
            Log.e("PlayerViewModel", "Empty playlist!")
            return
        }
        
        _playlist.value = songs
        currentIndex = startIndex.coerceIn(0, songs.size - 1)
        _currentSong.value = songs[currentIndex]
        
        // Build MediaItem list for ExoPlayer
        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.uri)
                .build()
        }
        
        Log.d("PlayerViewModel", "Built ${mediaItems.size} MediaItems")
        
        mediaController?.apply {
            Log.d("PlayerViewModel", "Setting media items and starting playback")
            setMediaItems(mediaItems, currentIndex, 0)
            prepare()
            play()
            
            // Update duration
            _duration.value = duration
            
            Log.d("PlayerViewModel", "Playback started!")
            Log.d("PlayerViewModel", "MediaController.isPlaying: $isPlaying")
            Log.d("PlayerViewModel", "MediaController.playbackState: $playbackState")
        } ?: run {
            Log.e("PlayerViewModel", "ERROR: MediaController is NULL!")
        }
        
        Log.d("PlayerViewModel", "========================================")
        
        checkIfFavorite(songs[currentIndex].id)
    }
    
    /**
     * Play a single song (creates a 1-song playlist).
     * Use playPlaylist() for better queue management.
     */
    fun playSong(song: Song) {
        playPlaylist(listOf(song), 0)
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
        Log.d("PlayerViewModel", "skipToNext called, currentIndex=$currentIndex, playlistSize=${_playlist.value.size}")
        
        mediaController?.apply {
            if (hasNextMediaItem()) {
                seekToNext()
                Log.d("PlayerViewModel", "Skipped to next track")
            } else {
                Log.d("PlayerViewModel", "No next track available")
            }
        } ?: Log.e("PlayerViewModel", "MediaController is null in skipToNext()")
    }
    
    fun skipToPrevious() {
        Log.d("PlayerViewModel", "skipToPrevious called")
        
        mediaController?.apply {
            if (hasPreviousMediaItem()) {
                seekToPrevious()
                Log.d("PlayerViewModel", "Skipped to previous track")
            } else {
                Log.d("PlayerViewModel", "No previous track available")
            }
        } ?: Log.e("PlayerViewModel", "MediaController is null in skipToPrevious()")
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

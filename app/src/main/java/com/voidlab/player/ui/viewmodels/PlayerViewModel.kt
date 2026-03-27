package com.voidlab.player.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.voidlab.player.audio.analysis.FrequencyAnalyzer
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
    private val favoriteRepository: FavoriteRepository,
    private val frequencyAnalyzer: FrequencyAnalyzer  // INJECTED!
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
    
    // REAL-TIME SPECTRUM for visualizers - ALIVE!
    val currentSpectrum = frequencyAnalyzer.currentSpectrum
    
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
                    _currentPosition.value = controller.currentPosition
                    if (_duration.value <= 0) {
                        _duration.value = controller.duration
                    }
                }
                delay(100)
            }
        }
    }
    
    fun setMediaController(controller: MediaController) {
        mediaController?.removeListener(playerListener)
        mediaController = controller
        controller.addListener(playerListener)
        
        // Initialize state from controller
        _isPlaying.value = controller.isPlaying
        _isShuffleEnabled.value = controller.shuffleModeEnabled
        _repeatMode.value = controller.repeatMode
        _duration.value = controller.duration
    }
    
    fun playSong(song: Song) {
        Log.d("PlayerViewModel", "playSong: ${song.title}")
        playPlaylist(listOf(song), 0)
    }
    
    fun playPlaylist(songs: List<Song>, startIndex: Int = 0) {
        Log.d("PlayerViewModel", "playPlaylist: ${songs.size} songs, startIndex: $startIndex")
        _playlist.value = songs
        currentIndex = startIndex
        
        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.uri)
                .build()
        }
        
        mediaController?.let { controller ->
            controller.setMediaItems(mediaItems, startIndex, 0)
            controller.prepare()
            controller.play()
            
            _currentSong.value = songs.getOrNull(startIndex)
            _currentSong.value?.let { checkIfFavorite(it.id) }
        }
    }
    
    fun togglePlayPause() {
        mediaController?.let { controller ->
            if (controller.isPlaying) {
                controller.pause()
            } else {
                controller.play()
            }
        }
    }
    
    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }
    
    fun skipToNext() {
        mediaController?.seekToNext()
    }
    
    fun skipToPrevious() {
        mediaController?.seekToPrevious()
    }
    
    fun toggleShuffle() {
        mediaController?.let { controller ->
            controller.shuffleModeEnabled = !controller.shuffleModeEnabled
        }
    }
    
    fun cycleRepeatMode() {
        mediaController?.let { controller ->
            controller.repeatMode = when (controller.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
        }
    }
    
    fun toggleFavorite() {
        _currentSong.value?.let { song ->
            viewModelScope.launch {
                favoriteRepository.toggleFavorite(song.id)
                checkIfFavorite(song.id)
            }
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
        mediaController?.removeListener(playerListener)
        super.onCleared()
    }
}

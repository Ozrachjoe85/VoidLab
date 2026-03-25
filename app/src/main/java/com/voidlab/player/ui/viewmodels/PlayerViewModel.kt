package com.voidlab.player.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.voidlab.player.data.models.AutoEQState
import com.voidlab.player.data.models.Song
import com.voidlab.player.data.repository.EQRepository
import com.voidlab.player.data.repository.FavoriteRepository
import com.voidlab.player.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val eqRepository: EQRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {
    
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _position = MutableStateFlow(0L)
    val position: StateFlow<Long> = _position.asStateFlow()
    
    private val _autoEQState = MutableStateFlow<AutoEQState>(AutoEQState.Idle)
    val autoEQState: StateFlow<AutoEQState> = _autoEQState.asStateFlow()
    
    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()
    
    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()
    
    val isFavorite: StateFlow<Boolean> = currentSong.flatMapLatest { song ->
        if (song != null) {
            favoriteRepository.isFavorite(song.id)
        } else {
            flowOf(false)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    private var mediaController: MediaController? = null
    
    fun setMediaController(controller: MediaController) {
        mediaController = controller
        
        controller.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }
            
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.mediaId?.toLongOrNull()?.let { songId ->
                    _currentSong.value = musicRepository.findSongById(songId)
                    loadEQState(songId)
                }
            }
        })
    }
    
    fun playSong(song: Song) {
        val mediaItem = MediaItem.Builder()
            .setUri(song.uri)
            .setMediaId(song.id.toString())
            .build()
        
        mediaController?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
        
        _currentSong.value = song
        loadEQState(song.id)
    }
    
    fun playPause() {
        mediaController?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
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
        mediaController?.let {
            val newShuffleMode = !it.shuffleModeEnabled
            it.shuffleModeEnabled = newShuffleMode
            _isShuffleEnabled.value = newShuffleMode
        }
    }
    
    fun cycleRepeatMode() {
        mediaController?.let {
            val newMode = when (it.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
            it.repeatMode = newMode
            _repeatMode.value = newMode
        }
    }
    
    fun toggleFavorite() {
        viewModelScope.launch {
            currentSong.value?.let { song ->
                favoriteRepository.toggleFavorite(song.id)
            }
        }
    }
    
    private fun loadEQState(songId: Long) {
        viewModelScope.launch {
            val profile = eqRepository.getProfileForSong(songId)
            _autoEQState.value = if (profile != null && profile.isAutoLearned) {
                AutoEQState.Learned(profile)
            } else {
                AutoEQState.Idle
            }
        }
    }
}

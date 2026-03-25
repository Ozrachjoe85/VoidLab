package com.voidlab.player.audio.playback

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.voidlab.player.audio.analysis.AutoEQEngine
import com.voidlab.player.audio.analysis.FFTAnalyzer
import com.voidlab.player.audio.effects.EqualizerEngine
import com.voidlab.player.data.models.Song
import com.voidlab.player.data.repository.EQRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackManager @Inject constructor(
    private val eqRepository: EQRepository
) {
    private var player: ExoPlayer? = null
    private var fftAnalyzer: FFTAnalyzer? = null
    private var equalizerEngine: EqualizerEngine? = null
    private var autoEQEngine: AutoEQEngine? = null
    
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _isAutoEQEnabled = MutableStateFlow(true)
    val isAutoEQEnabled: StateFlow<Boolean> = _isAutoEQEnabled.asStateFlow()
    
    private val _isLearning = MutableStateFlow(false)
    val isLearning: StateFlow<Boolean> = _isLearning.asStateFlow()
    
    private val _learningProgress = MutableStateFlow(0f)
    val learningProgress: StateFlow<Float> = _learningProgress.asStateFlow()
    
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var learningJob: Job? = null
    private var positionUpdateJob: Job? = null
    
    fun initialize(exoPlayer: ExoPlayer) {
        player = exoPlayer
        
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                
                if (isPlaying) {
                    startPositionUpdates()
                } else {
                    stopPositionUpdates()
                }
            }
        })
    }
    
    fun setAudioComponents(
        analyzer: FFTAnalyzer,
        eqEngine: EqualizerEngine,
        autoEngine: AutoEQEngine
    ) {
        fftAnalyzer = analyzer
        equalizerEngine = eqEngine
        autoEQEngine = autoEngine
    }
    
    fun playSong(song: Song) {
        val p = player ?: return
        
        _currentSong.value = song
        
        val mediaItem = MediaItem.fromUri(song.uri)
        p.setMediaItem(mediaItem)
        p.prepare()
        p.play()
        
        // Check for existing EQ profile
        scope.launch {
            val existingProfile = eqRepository.getProfileBySongId(song.id)
            if (existingProfile != null && existingProfile.learningProgress >= 100f) {
                // Apply saved profile
                equalizerEngine?.applyProfile(existingProfile)
                _isLearning.value = false
                _learningProgress.value = 100f
            } else if (_isAutoEQEnabled.value) {
                // Start learning
                startAutoEQLearning(song)
            }
        }
    }
    
    private fun startAutoEQLearning(song: Song) {
        autoEQEngine?.startLearning()
        _isLearning.value = true
        _learningProgress.value = 0f
        
        learningJob?.cancel()
        learningJob = scope.launch {
            // Capture spectrum data every ~300ms for 30 seconds
            repeat(100) {
                delay(300)
                autoEQEngine?.captureSnapshot()
                _learningProgress.value = autoEQEngine?.getLearningProgress() ?: 0f
            }
            
            // Generate and apply profile
            val profile = autoEQEngine?.generateEQProfile(song.id, song.title, song.artist)
            profile?.let {
                eqRepository.saveProfile(it)
                equalizerEngine?.applyProfile(it)
            }
            
            _isLearning.value = false
            _learningProgress.value = 100f
        }
    }
    
    fun togglePlayPause() {
        player?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }
    
    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
    }
    
    fun toggleAutoEQ(enabled: Boolean) {
        _isAutoEQEnabled.value = enabled
        
        if (!enabled) {
            learningJob?.cancel()
            _isLearning.value = false
        }
    }
    
    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch {
            while (true) {
                _currentPosition.value = player?.currentPosition ?: 0L
                delay(100)
            }
        }
    }
    
    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
    }
    
    fun release() {
        learningJob?.cancel()
        positionUpdateJob?.cancel()
    }
}

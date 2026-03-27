package com.voidlab.player.audio.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.voidlab.player.MainActivity
import com.voidlab.player.R
import com.voidlab.player.VoidLabApp
import com.voidlab.player.audio.analysis.FrequencyAnalyzer
import com.voidlab.player.data.models.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaybackService : LifecycleService() {
    
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    
    // FrequencyAnalyzer for real-time spectrum
    private var frequencyAnalyzer: FrequencyAnalyzer? = null
    
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Resume playback if we were paused by focus loss
                if (!_isPlaying.value && player.playWhenReady) {
                    player.play()
                }
                player.volume = 1.0f
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Permanent loss - stop playback
                player.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Temporary loss (e.g., phone call) - pause
                if (player.isPlaying) {
                    player.pause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lower volume but keep playing (e.g., notification sound)
                if (player.isPlaying) {
                    player.volume = 0.3f
                }
            }
        }
    }
    
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        createNotificationChannel()
        
        player = ExoPlayer.Builder(this).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                    
                    if (isPlaying) {
                        // Get REAL audio session ID from ExoPlayer
                        val realAudioSessionId = player.audioSessionId
                        
                        // Initialize FrequencyAnalyzer with REAL session ID
                        if (frequencyAnalyzer == null || frequencyAnalyzer?.audioSessionId != realAudioSessionId) {
                            frequencyAnalyzer?.stop()
                            frequencyAnalyzer = FrequencyAnalyzer(realAudioSessionId)
                        }
                        frequencyAnalyzer?.start()
                        
                        requestAudioFocus()
                    } else {
                        frequencyAnalyzer?.stop()
                        abandonAudioFocus()
                    }
                    
                    updateNotification()
                }
                
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    updateCurrentSong()
                }
            })
        }
        
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(object : MediaSession.Callback {
                override fun onPlay(session: MediaSession, controller: MediaSession.ControllerInfo) {
                    player.play()
                }
                
                override fun onPause(session: MediaSession, controller: MediaSession.ControllerInfo) {
                    player.pause()
                }
                
                override fun onSkipToNext(session: MediaSession, controller: MediaSession.ControllerInfo) {
                    player.seekToNext()
                }
                
                override fun onSkipToPrevious(session: MediaSession, controller: MediaSession.ControllerInfo) {
                    player.seekToPrevious()
                }
            })
            .build()
        
        lifecycleScope.launch {
            while (true) {
                _currentPosition.value = player.currentPosition
                _duration.value = player.duration.coerceAtLeast(0)
                kotlinx.coroutines.delay(100)
            }
        }
    }
    
    private fun requestAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()
            
            audioManager.requestAudioFocus(audioFocusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }
    
    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Media playback controls"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun updateNotification() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(_currentSong.value?.title ?: "VoidLab")
            .setContentText(_currentSong.value?.artist ?: "No song playing")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
    }
    
    private fun updateCurrentSong() {
        val currentMediaItem = player.currentMediaItem
        if (currentMediaItem != null) {
            val song = Song(
                id = currentMediaItem.mediaId.toLongOrNull() ?: 0,
                title = currentMediaItem.mediaMetadata.title?.toString() ?: "Unknown",
                artist = currentMediaItem.mediaMetadata.artist?.toString() ?: "Unknown",
                album = currentMediaItem.mediaMetadata.albumTitle?.toString() ?: "Unknown",
                duration = player.duration,
                data = "",
                albumId = 0
            )
            _currentSong.value = song
            updateNotification()
        }
    }
    
    fun playSong(song: Song) {
        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.data)
            .build()
        
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        
        _currentSong.value = song
    }
    
    fun playPlaylist(songs: List<Song>, startIndex: Int = 0) {
        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.data)
                .build()
        }
        
        player.setMediaItems(mediaItems, startIndex, 0)
        player.prepare()
        player.play()
        
        _currentSong.value = songs.getOrNull(startIndex)
    }
    
    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }
    
    fun seekTo(position: Long) {
        player.seekTo(position)
    }
    
    fun skipToNext() {
        player.seekToNext()
    }
    
    fun skipToPrevious() {
        player.seekToPrevious()
    }
    
    override fun onDestroy() {
        frequencyAnalyzer?.stop()
        abandonAudioFocus()
        player.release()
        mediaSession.release()
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return PlaybackServiceBinder(this)
    }
    
    companion object {
        private const val CHANNEL_ID = "playback_channel"
        private const val NOTIFICATION_ID = 1
    }
}

package com.voidlab.player.audio.playback

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.voidlab.player.MainActivity
import com.voidlab.player.VoidLabApp
import com.voidlab.player.audio.analysis.AutoEQLearner
import com.voidlab.player.audio.analysis.FrequencyAnalyzer
import com.voidlab.player.audio.effects.EqualizerEngine
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    
    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    private var equalizerEngine: EqualizerEngine? = null
    private var autoEQLearner: AutoEQLearner? = null
    
    @Inject
    lateinit var frequencyAnalyzer: FrequencyAnalyzer
    
    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                player?.volume = 1.0f
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                player?.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                player?.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                player?.volume = 0.3f
            }
        }
    }
    
    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            mediaItem?.mediaId?.toLongOrNull()?.let { songId ->
                Log.d("PlaybackService", "Media transition to song: $songId")
                serviceScope.launch {
                    loadAndApplyEQProfile(songId)
                }
            }
        }
        
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                player?.audioSessionId?.let { realSessionId ->
                    Log.d("PlaybackService", "Music playing, updating analyzer with session: $realSessionId")
                    frequencyAnalyzer.updateAudioSession(realSessionId)
                    frequencyAnalyzer.start()
                }
                requestAudioFocus()
            } else {
                frequencyAnalyzer.stop()
                abandonAudioFocus()
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        player = ExoPlayer.Builder(this).build().apply {
            addListener(playerListener)
        }
        
        player?.audioSessionId?.let { sessionId ->
            Log.d("PlaybackService", "Initializing with audio session: $sessionId")
            equalizerEngine = EqualizerEngine(sessionId)
            frequencyAnalyzer.updateAudioSession(sessionId)
            autoEQLearner = AutoEQLearner()
        }
        
        val sessionActivityIntent = Intent(this, MainActivity::class.java)
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            sessionActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        mediaSession = MediaSession.Builder(this, player!!)
            .setSessionActivity(sessionActivityPendingIntent)
            .setCallback(object : MediaSession.Callback {
                override fun onAddMediaItems(
                    mediaSession: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    mediaItems: List<MediaItem>
                ): ListenableFuture<List<MediaItem>> {
                    val updatedMediaItems = mediaItems.map { mediaItem ->
                        mediaItem.buildUpon()
                            .setUri(mediaItem.requestMetadata.mediaUri ?: mediaItem.localConfiguration?.uri)
                            .build()
                    }
                    return Futures.immediateFuture(updatedMediaItems)
                }
            })
            .build()
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
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }
    
    override fun onDestroy() {
        abandonAudioFocus()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        equalizerEngine?.release()
        frequencyAnalyzer.stop()
        player = null
        super.onDestroy()
    }
    
    private suspend fun loadAndApplyEQProfile(songId: Long) {
        val app = application as VoidLabApp
        val eqRepository = app.eqRepository
        val musicRepository = app.musicRepository
        
        Log.d("AutoEQ", "Loading profile for song: $songId")
        
        val profile = eqRepository.getProfileForSong(songId)
        val song = musicRepository.findSongById(songId)
        
        if (profile != null && profile.isAutoLearned) {
            Log.d("AutoEQ", "Applying existing profile: ${profile.songTitle}")
            equalizerEngine?.applyProfile(profile)
        } else if (song != null) {
            Log.d("AutoEQ", "Starting learning for: ${song.title}")
            frequencyAnalyzer.start()
            
            autoEQLearner?.startLearning(
                analyzer = frequencyAnalyzer,
                songId = songId,
                songTitle = song.title,
                songArtist = song.artist
            ) { learnedProfile ->
                Log.d("AutoEQ", "Profile learned! Bands: ${learnedProfile.getBands()}")
                serviceScope.launch {
                    eqRepository.saveProfile(learnedProfile)
                    Log.d("AutoEQ", "Profile saved to database")
                    equalizerEngine?.applyProfile(learnedProfile)
                }
            }
        } else {
            Log.w("AutoEQ", "Song not found for ID: $songId")
        }
    }
}

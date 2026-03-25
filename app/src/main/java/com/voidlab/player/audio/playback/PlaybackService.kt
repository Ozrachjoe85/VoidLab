package com.voidlab.player.audio.playback

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.voidlab.player.MainActivity
import com.voidlab.player.audio.analysis.AutoEQLearner
import com.voidlab.player.audio.analysis.FrequencyAnalyzer
import com.voidlab.player.audio.effects.EqualizerEngine
import com.voidlab.player.data.repository.EQRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    
    @Inject
    lateinit var eqRepository: EQRepository
    
    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    private var equalizerEngine: EqualizerEngine? = null
    private var frequencyAnalyzer: FrequencyAnalyzer? = null
    private var autoEQLearner: AutoEQLearner? = null
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            mediaItem?.mediaId?.toLongOrNull()?.let { songId ->
                serviceScope.launch {
                    loadAndApplyEQProfile(songId)
                }
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        
        player = ExoPlayer.Builder(this).build().apply {
            addListener(playerListener)
        }
        
        player?.audioSessionId?.let { sessionId ->
            equalizerEngine = EqualizerEngine(sessionId)
            frequencyAnalyzer = FrequencyAnalyzer(sessionId)
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
            .build()
    }
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }
    
    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        equalizerEngine?.release()
        frequencyAnalyzer?.stop()
        player = null
        super.onDestroy()
    }
    
    private suspend fun loadAndApplyEQProfile(songId: Long) {
        val profile = eqRepository.getProfile(songId)
        
        if (profile != null && profile.isLearned) {
            // Apply existing learned profile
            equalizerEngine?.applyProfile(profile)
        } else {
            // Start Auto EQ learning
            frequencyAnalyzer?.start()
            autoEQLearner?.startLearning(frequencyAnalyzer!!) { learnedProfile ->
                serviceScope.launch {
                    val profileWithId = learnedProfile.copy(songId = songId)
                    eqRepository.saveProfile(profileWithId)
                    equalizerEngine?.applyProfile(profileWithId)
                }
            }
        }
    }
}

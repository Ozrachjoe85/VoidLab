package com.voidlab.player.audio.playback

import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.voidlab.player.audio.analysis.AutoEQEngine
import com.voidlab.player.audio.analysis.FFTAnalyzer
import com.voidlab.player.audio.effects.EqualizerEngine
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    
    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    private var fftAnalyzer: FFTAnalyzer? = null
    private var equalizerEngine: EqualizerEngine? = null
    private var autoEQEngine: AutoEQEngine? = null
    
    @Inject
    lateinit var playbackManager: PlaybackManager
    
    override fun onCreate() {
        super.onCreate()
        
        player = ExoPlayer.Builder(this).build().also {
            it.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        val audioSessionId = it.audioSessionId
                        initializeAudioComponents(audioSessionId)
                    }
                }
            })
        }
        
        mediaSession = MediaSession.Builder(this, player!!)
            .build()
        
        playbackManager.initialize(player!!)
    }
    
    private fun initializeAudioComponents(audioSessionId: Int) {
        if (fftAnalyzer == null) {
            fftAnalyzer = FFTAnalyzer(audioSessionId).also {
                it.start()
                autoEQEngine = AutoEQEngine(it)
            }
        }
        
        if (equalizerEngine == null) {
            equalizerEngine = EqualizerEngine(audioSessionId).also {
                it.initialize()
            }
        }
        
        playbackManager.setAudioComponents(fftAnalyzer!!, equalizerEngine!!, autoEQEngine!!)
    }
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player?.playWhenReady == false) {
            stopSelf()
        }
    }
    
    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        fftAnalyzer?.release()
        equalizerEngine?.release()
        super.onDestroy()
    }
}

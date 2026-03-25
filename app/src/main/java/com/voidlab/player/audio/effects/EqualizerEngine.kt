package com.voidlab.player.audio.effects

import android.media.audiofx.Equalizer
import com.voidlab.player.data.models.EQProfile

class EqualizerEngine(audioSessionId: Int) {
    
    private val equalizer = Equalizer(0, audioSessionId)
    
    init {
        equalizer.enabled = true
    }
    
    fun applyProfile(profile: EQProfile) {
        val bands = profile.getBands()
        bands.forEachIndexed { index, band ->
            if (index < equalizer.numberOfBands) {
                // Convert dB to millibels (1 dB = 100 millibels)
                val millibels = (band.gain * 100).toInt().toShort()
                equalizer.setBandLevel(index.toShort(), millibels)
            }
        }
    }
    
    fun setBandLevel(bandIndex: Int, gainDb: Float) {
        if (bandIndex < equalizer.numberOfBands) {
            val millibels = (gainDb * 100).toInt().toShort()
            equalizer.setBandLevel(bandIndex.toShort(), millibels)
        }
    }
    
    fun reset() {
        for (i in 0 until equalizer.numberOfBands) {
            equalizer.setBandLevel(i.toShort(), 0)
        }
    }
    
    fun release() {
        equalizer.release()
    }
}

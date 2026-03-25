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
        val numberOfBands = equalizer.numberOfBands.toInt().coerceAtMost(10)
        
        for (i in 0 until numberOfBands) {
            val band = i.toShort()
            val gainMillibels = (bands.getOrNull(i) ?: 0f) * 100 // Convert dB to millibels
            val clampedGain = gainMillibels.coerceIn(
                equalizer.getBandLevelRange()[0].toFloat(),
                equalizer.getBandLevelRange()[1].toFloat()
            ).toInt().toShort()
            
            try {
                equalizer.setBandLevel(band, clampedGain)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun setBandLevel(bandIndex: Int, gainDb: Float) {
        if (bandIndex < 0 || bandIndex >= equalizer.numberOfBands) return
        
        val gainMillibels = (gainDb * 100).toInt().toShort()
        val clampedGain = gainMillibels.coerceIn(
            equalizer.getBandLevelRange()[0],
            equalizer.getBandLevelRange()[1]
        )
        
        try {
            equalizer.setBandLevel(bandIndex.toShort(), clampedGain)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun getBandLevel(bandIndex: Int): Float {
        if (bandIndex < 0 || bandIndex >= equalizer.numberOfBands) return 0f
        
        return try {
            equalizer.getBandLevel(bandIndex.toShort()) / 100f // Convert millibels to dB
        } catch (e: Exception) {
            0f
        }
    }
    
    fun getCurrentProfile(): List<Float> {
        val bandCount = equalizer.numberOfBands.toInt().coerceAtMost(10)
        return (0 until bandCount).map { getBandLevel(it) }
    }
    
    fun release() {
        equalizer.release()
    }
}

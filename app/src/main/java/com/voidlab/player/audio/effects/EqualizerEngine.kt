package com.voidlab.player.audio.effects

import android.media.audiofx.Equalizer
import com.voidlab.player.data.models.EQProfile

class EqualizerEngine(private val audioSessionId: Int) {
    
    private var equalizer: Equalizer? = null
    private val bandFrequencies = intArrayOf(31, 62, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)
    
    fun initialize() {
        try {
            equalizer?.release()
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun applyProfile(profile: EQProfile) {
        val eq = equalizer ?: return
        val bandValues = profile.getBandValues()
        
        try {
            val numBands = eq.numberOfBands.toInt()
            val bandRange = eq.bandLevelRange
            val minLevel = bandRange[0]
            val maxLevel = bandRange[1]
            
            for (i in 0 until numBands.coerceAtMost(10)) {
                val gainDb = bandValues.getOrNull(i) ?: 0f
                val gainMillibels = (gainDb * 100).toInt().coerceIn(minLevel.toInt(), maxLevel.toInt())
                eq.setBandLevel(i.toShort(), gainMillibels.toShort())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun applyBandValues(values: FloatArray) {
        val eq = equalizer ?: return
        
        try {
            val numBands = eq.numberOfBands.toInt()
            val bandRange = eq.bandLevelRange
            val minLevel = bandRange[0]
            val maxLevel = bandRange[1]
            
            for (i in 0 until numBands.coerceAtMost(values.size)) {
                val gainDb = values[i]
                val gainMillibels = (gainDb * 100).toInt().coerceIn(minLevel.toInt(), maxLevel.toInt())
                eq.setBandLevel(i.toShort(), gainMillibels.toShort())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun setBandLevel(bandIndex: Int, gainDb: Float) {
        val eq = equalizer ?: return
        
        try {
            val bandRange = eq.bandLevelRange
            val minLevel = bandRange[0]
            val maxLevel = bandRange[1]
            val gainMillibels = (gainDb * 100).toInt().coerceIn(minLevel.toInt(), maxLevel.toInt())
            eq.setBandLevel(bandIndex.toShort(), gainMillibels.toShort())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun getCurrentBandLevels(): FloatArray {
        val eq = equalizer ?: return FloatArray(10) { 0f }
        val levels = FloatArray(10)
        
        try {
            val numBands = eq.numberOfBands.toInt()
            for (i in 0 until numBands.coerceAtMost(10)) {
                val levelMillibels = eq.getBandLevel(i.toShort())
                levels[i] = levelMillibels / 100f
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return levels
    }
    
    fun reset() {
        applyBandValues(FloatArray(10) { 0f })
    }
    
    fun release() {
        equalizer?.enabled = false
        equalizer?.release()
        equalizer = null
    }
    
    fun setEnabled(enabled: Boolean) {
        equalizer?.enabled = enabled
    }
}

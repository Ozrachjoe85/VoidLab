package com.voidlab.player.audio.analysis

import com.voidlab.player.data.models.EQProfile
import com.voidlab.player.data.models.FrequencySnapshot
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

class AutoEQLearner {
    
    private val _learningProgress = MutableStateFlow(0f)
    val learningProgress: StateFlow<Float> = _learningProgress.asStateFlow()
    
    private val _isLearning = MutableStateFlow(false)
    val isLearning: StateFlow<Boolean> = _isLearning.asStateFlow()
    
    private var learningJob: Job? = null
    
    companion object {
        private const val LEARNING_DURATION_MS = 30_000L // 30 seconds
        private const val REFERENCE_DB = -20f // Target reference level
    }
    
    fun startLearning(
        analyzer: FrequencyAnalyzer,
        onComplete: (EQProfile) -> Unit
    ) {
        learningJob?.cancel()
        analyzer.clearSnapshots()
        _isLearning.value = true
        _learningProgress.value = 0f
        
        learningJob = CoroutineScope(Dispatchers.Default).launch {
            val startTime = System.currentTimeMillis()
            
            while (isActive && System.currentTimeMillis() - startTime < LEARNING_DURATION_MS) {
                val elapsed = System.currentTimeMillis() - startTime
                _learningProgress.value = elapsed.toFloat() / LEARNING_DURATION_MS
                delay(100)
            }
            
            if (isActive) {
                val profile = computeProfile(analyzer.getSnapshots())
                _isLearning.value = false
                _learningProgress.value = 1f
                withContext(Dispatchers.Main) {
                    onComplete(profile)
                }
            }
        }
    }
    
    fun stopLearning() {
        learningJob?.cancel()
        _isLearning.value = false
        _learningProgress.value = 0f
    }
    
    private fun computeProfile(snapshots: List<FrequencySnapshot>): EQProfile {
        if (snapshots.isEmpty()) {
            return EQProfile(
                songId = 0,
                isLearned = false
            )
        }
        
        // Calculate median values for each band
        val bandCount = 10
        val bandMedians = FloatArray(bandCount)
        
        for (bandIndex in 0 until bandCount) {
            val bandValues = snapshots.map { it.bands[bandIndex] }.sorted()
            bandMedians[bandIndex] = bandValues[bandValues.size / 2]
        }
        
        // Calculate adjustment needed to reach reference level
        val adjustments = bandMedians.map { median ->
            val adjustment = REFERENCE_DB - median
            // Clamp to reasonable range (-12 dB to +12 dB)
            adjustment.coerceIn(-12f, 12f)
        }
        
        // Apply smoothing to avoid harsh transitions
        val smoothedAdjustments = smoothBands(adjustments)
        
        return EQProfile(
            songId = 0, // Will be set by caller
            band31Hz = smoothedAdjustments[0],
            band62Hz = smoothedAdjustments[1],
            band125Hz = smoothedAdjustments[2],
            band250Hz = smoothedAdjustments[3],
            band500Hz = smoothedAdjustments[4],
            band1kHz = smoothedAdjustments[5],
            band2kHz = smoothedAdjustments[6],
            band4kHz = smoothedAdjustments[7],
            band8kHz = smoothedAdjustments[8],
            band16kHz = smoothedAdjustments[9],
            isLearned = true,
            learnedAt = System.currentTimeMillis()
        )
    }
    
    private fun smoothBands(values: List<Float>): List<Float> {
        // Apply simple moving average smoothing
        return values.mapIndexed { index, value ->
            val prev = if (index > 0) values[index - 1] else value
            val next = if (index < values.size - 1) values[index + 1] else value
            (prev + value + next) / 3f
        }
    }
}

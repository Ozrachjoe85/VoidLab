package com.voidlab.player.audio.analysis

import com.voidlab.player.data.models.EQProfile
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
        songId: Long,
        songTitle: String,
        songArtist: String,
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
                val profile = computeProfile(songId, songTitle, songArtist, analyzer.getSnapshots())
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
    
    private fun computeProfile(
        songId: Long,
        songTitle: String,
        songArtist: String,
        snapshots: List<FloatArray>
    ): EQProfile {
        if (snapshots.isEmpty()) {
            return EQProfile(
                songId = songId,
                songTitle = songTitle,
                songArtist = songArtist,
                isAutoLearned = false
            )
        }
        
        // Calculate median values for each band
        val bandCount = 10
        val bandMedians = FloatArray(bandCount)
        
        for (bandIndex in 0 until bandCount) {
            val bandValues = snapshots.map { it[bandIndex] }.sorted()
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
        
        return EQProfile.fromBands(
            songId = songId,
            songTitle = songTitle,
            songArtist = songArtist,
            bands = smoothedAdjustments,
            isAutoLearned = true
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

package com.voidlab.player.audio.analysis

import android.media.audiofx.Visualizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.log10
import kotlin.math.sqrt

@Singleton
class FrequencyAnalyzer @Inject constructor() {
    
    private var visualizer: Visualizer? = null
    private val snapshots = mutableListOf<FloatArray>()
    private var audioSessionId: Int = 0
    
    private val _currentSpectrum = MutableStateFlow(FloatArray(10))
    val currentSpectrum: StateFlow<FloatArray> = _currentSpectrum.asStateFlow()
    
    // Called by PlaybackService when it gets the REAL audio session
    fun updateAudioSession(newSessionId: Int) {
        if (audioSessionId != newSessionId) {
            stop()
            audioSessionId = newSessionId
        }
    }
    
    fun start() {
        if (audioSessionId == 0) {
            // Can't start without a valid session
            return
        }
        
        try {
            visualizer = Visualizer(audioSessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int
                        ) {}
                        
                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) {
                            fft?.let { processFft(it) }
                        }
                    },
                    Visualizer.getMaxCaptureRate(),
                    false,
                    true
                )
                enabled = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun stop() {
        visualizer?.enabled = false
        visualizer?.release()
        visualizer = null
    }
    
    // Public methods for AutoEQLearner
    fun clearSnapshots() {
        snapshots.clear()
    }
    
    fun getSnapshots(): List<FloatArray> {
        return snapshots.toList()
    }
    
    private fun processFft(fft: ByteArray) {
        val magnitudes = FloatArray(10)
        
        // 10-band frequency ranges (Hz)
        val bands = arrayOf(
            31..62,    // Sub-bass
            62..125,   // Bass
            125..250,  // Low mids
            250..500,  // Mids
            500..1000, // Upper mids
            1000..2000,
            2000..4000,
            4000..8000,
            8000..16000,
            16000..22050
        )
        
        val sampleRate = 44100
        val numSamples = fft.size / 2
        
        bands.forEachIndexed { index, range ->
            val startBin = (range.first * numSamples / (sampleRate / 2)).coerceAtLeast(1)
            val endBin = (range.last * numSamples / (sampleRate / 2)).coerceAtMost(numSamples - 1)
            
            var sum = 0f
            var count = 0
            
            for (i in startBin..endBin) {
                val real = fft[i * 2].toFloat()
                val imag = fft[i * 2 + 1].toFloat()
                val magnitude = sqrt(real * real + imag * imag)
                sum += magnitude
                count++
            }
            
            val avgMagnitude = if (count > 0) sum / count else 0f
            val db = if (avgMagnitude > 0) 20 * log10(avgMagnitude) else -96f
            magnitudes[index] = ((db + 96f) / 96f).coerceIn(0f, 1f)
        }
        
        snapshots.add(magnitudes)
        if (snapshots.size > 10) snapshots.removeAt(0)
        
        val smoothed = FloatArray(10) { i ->
            snapshots.map { it[i] }.average().toFloat()
        }
        
        _currentSpectrum.value = smoothed
    }
}

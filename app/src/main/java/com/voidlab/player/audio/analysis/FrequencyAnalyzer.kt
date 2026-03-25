package com.voidlab.player.audio.analysis

import android.media.audiofx.Visualizer
import com.voidlab.player.data.models.FrequencySnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.log10
import kotlin.math.sqrt

class FrequencyAnalyzer(private val audioSessionId: Int) {
    
    private var visualizer: Visualizer? = null
    private val snapshots = mutableListOf<FrequencySnapshot>()
    
    private val _currentSpectrum = MutableStateFlow(FloatArray(10))
    val currentSpectrum: StateFlow<FloatArray> = _currentSpectrum.asStateFlow()
    
    fun start() {
        try {
            visualizer = Visualizer(audioSessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int
                        ) {
                            // Not used for EQ analysis
                        }
                        
                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) {
                            fft?.let { processFft(it) }
                        }
                    },
                    Visualizer.getMaxCaptureRate() / 2,
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
        visualizer?.apply {
            enabled = false
            release()
        }
        visualizer = null
    }
    
    private fun processFft(fft: ByteArray) {
        val bands = FloatArray(10)
        val fftSize = fft.size / 2
        
        // Map FFT bins to 10 frequency bands
        val bandRanges = listOf(
            0 to 2,      // 31 Hz
            2 to 4,      // 62 Hz
            4 to 8,      // 125 Hz
            8 to 16,     // 250 Hz
            16 to 32,    // 500 Hz
            32 to 64,    // 1 kHz
            64 to 128,   // 2 kHz
            128 to 256,  // 4 kHz
            256 to 512,  // 8 kHz
            512 to fftSize.coerceAtMost(1024) // 16 kHz
        )
        
        bandRanges.forEachIndexed { index, (start, end) ->
            var sum = 0f
            var count = 0
            
            for (i in start until end.coerceAtMost(fftSize)) {
                val real = fft[i * 2].toFloat()
                val imag = fft[i * 2 + 1].toFloat()
                val magnitude = sqrt(real * real + imag * imag)
                sum += magnitude
                count++
            }
            
            val average = if (count > 0) sum / count else 0f
            bands[index] = if (average > 0) 20 * log10(average.coerceAtLeast(1f)) else -96f
        }
        
        _currentSpectrum.value = bands
        snapshots.add(FrequencySnapshot(System.currentTimeMillis(), bands))
    }
    
    fun getSnapshots(): List<FrequencySnapshot> = snapshots.toList()
    
    fun clearSnapshots() {
        snapshots.clear()
    }
}

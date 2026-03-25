package com.voidlab.player.audio.analysis

import android.media.audiofx.Visualizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.log10
import kotlin.math.sqrt

class FFTAnalyzer(private val audioSessionId: Int) {
    
    private var visualizer: Visualizer? = null
    private val _fftData = MutableStateFlow(FloatArray(512))
    val fftData: StateFlow<FloatArray> = _fftData.asStateFlow()
    
    private val _waveformData = MutableStateFlow(ByteArray(1024))
    val waveformData: StateFlow<ByteArray> = _waveformData.asStateFlow()
    
    // Frequency spectrum for Auto EQ learning
    private val _frequencySpectrum = MutableStateFlow(FloatArray(10))
    val frequencySpectrum: StateFlow<FloatArray> = _frequencySpectrum.asStateFlow()
    
    fun start() {
        try {
            visualizer?.release()
            visualizer = Visualizer(audioSessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int
                        ) {
                            waveform?.let { _waveformData.value = it }
                        }
                        
                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) {
                            fft?.let {
                                val magnitudes = processFft(it)
                                _fftData.value = magnitudes
                                _frequencySpectrum.value = calculateFrequencyBands(magnitudes, samplingRate)
                            }
                        }
                    },
                    Visualizer.getMaxCaptureRate(),
                    true,
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
    
    private fun processFft(fft: ByteArray): FloatArray {
        val n = fft.size / 2
        val magnitudes = FloatArray(n)
        
        for (i in 0 until n) {
            val real = fft[i * 2].toFloat()
            val imaginary = fft[i * 2 + 1].toFloat()
            magnitudes[i] = sqrt(real * real + imaginary * imaginary)
        }
        
        return magnitudes
    }
    
    private fun calculateFrequencyBands(magnitudes: FloatArray, samplingRate: Int): FloatArray {
        // Map FFT bins to 10 EQ bands: 31, 62, 125, 250, 500, 1k, 2k, 4k, 8k, 16k Hz
        val bands = FloatArray(10)
        val frequencies = intArrayOf(31, 62, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)
        
        for (i in frequencies.indices) {
            val centerFreq = frequencies[i]
            val binStart = (centerFreq * 0.8 * magnitudes.size / (samplingRate / 2.0)).toInt()
            val binEnd = (centerFreq * 1.2 * magnitudes.size / (samplingRate / 2.0)).toInt()
            
            var sum = 0f
            var count = 0
            for (j in binStart..binEnd.coerceAtMost(magnitudes.size - 1)) {
                sum += magnitudes[j]
                count++
            }
            
            bands[i] = if (count > 0) {
                // Convert to dB scale
                val average = sum / count
                20 * log10(average.coerceAtLeast(1f))
            } else {
                0f
            }
        }
        
        return bands
    }
    
    fun release() {
        stop()
    }
}

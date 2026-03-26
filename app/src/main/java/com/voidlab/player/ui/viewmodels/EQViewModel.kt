package com.voidlab.player.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidlab.player.audio.analysis.FrequencyAnalyzer
import com.voidlab.player.data.models.EQPreset
import com.voidlab.player.data.models.EQProfile
import com.voidlab.player.data.repository.EQRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ViewMode {
    CURVE, MIXER
}

@HiltViewModel
class EQViewModel @Inject constructor(
    private val eqRepository: EQRepository,
    private val frequencyAnalyzer: FrequencyAnalyzer
) : ViewModel() {
    
    private val _isAutoMode = MutableStateFlow(true)
    val isAutoMode: StateFlow<Boolean> = _isAutoMode.asStateFlow()
    
    private val _currentProfile = MutableStateFlow<EQProfile?>(null)
    val currentProfile: StateFlow<EQProfile?> = _currentProfile.asStateFlow()
    
    private val _viewMode = MutableStateFlow(ViewMode.CURVE)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()
    
    private val _learnedProfiles = MutableStateFlow<List<EQProfile>>(emptyList())
    val learnedProfiles: StateFlow<List<EQProfile>> = _learnedProfiles.asStateFlow()
    
    private val _learnedProfileCount = MutableStateFlow(0)
    val learnedProfileCount: StateFlow<Int> = _learnedProfileCount.asStateFlow()
    
    // REAL-TIME SPECTRUM DATA
    private val _currentSpectrum = MutableStateFlow(FloatArray(10))
    val currentSpectrum: StateFlow<FloatArray> = _currentSpectrum.asStateFlow()
    
    // Use the existing enum presets
    val presets = EQPreset.entries
    
    init {
        loadLearnedProfiles()
        
        viewModelScope.launch {
            frequencyAnalyzer.currentSpectrum.collect { spectrum ->
                _currentSpectrum.value = spectrum
            }
        }
    }
    
    fun toggleAutoMode() {
        _isAutoMode.value = !_isAutoMode.value
    }
    
    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }
    
    fun applyPreset(preset: EQPreset) {
        val bands = preset.bandValues
        val profile = EQProfile(
            songId = 0,
            songTitle = preset.displayName,
            songArtist = "Preset",
            band31Hz = bands[0],
            band62Hz = bands[1],
            band125Hz = bands[2],
            band250Hz = bands[3],
            band500Hz = bands[4],
            band1kHz = bands[5],
            band2kHz = bands[6],
            band4kHz = bands[7],
            band8kHz = bands[8],
            band16kHz = bands[9],
            isAutoLearned = false
        )
        _currentProfile.value = profile
    }
    
    fun updateBandLevel(index: Int, value: Float) {
        val current = _currentProfile.value
        if (current != null) {
            _currentProfile.value = when (index) {
                0 -> current.copy(band31Hz = value)
                1 -> current.copy(band62Hz = value)
                2 -> current.copy(band125Hz = value)
                3 -> current.copy(band250Hz = value)
                4 -> current.copy(band500Hz = value)
                5 -> current.copy(band1kHz = value)
                6 -> current.copy(band2kHz = value)
                7 -> current.copy(band4kHz = value)
                8 -> current.copy(band8kHz = value)
                9 -> current.copy(band16kHz = value)
                else -> current
            }
        } else {
            val profile = EQProfile(
                songId = 0,
                songTitle = "Custom",
                songArtist = "Manual",
                band31Hz = if (index == 0) value else 0f,
                band62Hz = if (index == 1) value else 0f,
                band125Hz = if (index == 2) value else 0f,
                band250Hz = if (index == 3) value else 0f,
                band500Hz = if (index == 4) value else 0f,
                band1kHz = if (index == 5) value else 0f,
                band2kHz = if (index == 6) value else 0f,
                band4kHz = if (index == 7) value else 0f,
                band8kHz = if (index == 8) value else 0f,
                band16kHz = if (index == 9) value else 0f,
                isAutoLearned = false
            )
            _currentProfile.value = profile
        }
    }
    
    fun deleteProfile(profile: EQProfile) {
        viewModelScope.launch {
            eqRepository.deleteProfile(profile)
            loadLearnedProfiles()
        }
    }
    
    private fun loadLearnedProfiles() {
        viewModelScope.launch {
            eqRepository.getAllLearnedProfiles().collect { profiles ->
                _learnedProfiles.value = profiles
                _learnedProfileCount.value = profiles.size
            }
        }
    }
}

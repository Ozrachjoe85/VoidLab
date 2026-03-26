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
            band31 = bands[0],
            band62 = bands[1],
            band125 = bands[2],
            band250 = bands[3],
            band500 = bands[4],
            band1k = bands[5],
            band2k = bands[6],
            band4k = bands[7],
            band8k = bands[8],
            band16k = bands[9],
            isAutoLearned = false
        )
        _currentProfile.value = profile
    }
    
    fun updateBandLevel(index: Int, value: Float) {
        val current = _currentProfile.value
        if (current != null) {
            _currentProfile.value = when (index) {
                0 -> current.copy(band31 = value)
                1 -> current.copy(band62 = value)
                2 -> current.copy(band125 = value)
                3 -> current.copy(band250 = value)
                4 -> current.copy(band500 = value)
                5 -> current.copy(band1k = value)
                6 -> current.copy(band2k = value)
                7 -> current.copy(band4k = value)
                8 -> current.copy(band8k = value)
                9 -> current.copy(band16k = value)
                else -> current
            }
        } else {
            val profile = EQProfile(
                songId = 0,
                songTitle = "Custom",
                songArtist = "Manual",
                band31 = if (index == 0) value else 0f,
                band62 = if (index == 1) value else 0f,
                band125 = if (index == 2) value else 0f,
                band250 = if (index == 3) value else 0f,
                band500 = if (index == 4) value else 0f,
                band1k = if (index == 5) value else 0f,
                band2k = if (index == 6) value else 0f,
                band4k = if (index == 7) value else 0f,
                band8k = if (index == 8) value else 0f,
                band16k = if (index == 9) value else 0f,
                isAutoLearned = false
            )
            _currentProfile.value = profile
        }
    }
    
    fun deleteProfile(profile: EQProfile) {
        viewModelScope.launch {
            eqRepository.deleteProfile(profile.songId)
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

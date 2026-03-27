package com.voidlab.player.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidlab.player.audio.analysis.FrequencyAnalyzer
import com.voidlab.player.data.models.EQPreset
import com.voidlab.player.data.models.EQProfile
import com.voidlab.player.data.repository.EQRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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
    
    private val _isAutoMode = MutableStateFlow(false)
    val isAutoMode: StateFlow<Boolean> = _isAutoMode.asStateFlow()
    
    private val _currentProfile = MutableStateFlow<EQProfile?>(null)
    val currentProfile: StateFlow<EQProfile?> = _currentProfile.asStateFlow()
    
    private val _viewMode = MutableStateFlow(ViewMode.CURVE)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()
    
    private val _learnedProfiles = MutableStateFlow<List<EQProfile>>(emptyList())
    val learnedProfiles: StateFlow<List<EQProfile>> = _learnedProfiles.asStateFlow()
    
    private val _learnedProfileCount = MutableStateFlow(0)
    val learnedProfileCount: StateFlow<Int> = _learnedProfileCount.asStateFlow()
    
    // REAL-TIME SPECTRUM - Makes EQ ALIVE!
    val currentSpectrum = frequencyAnalyzer.currentSpectrum
    
    val presets = listOf(
        EQPreset.FLAT,
        EQPreset.BASS_BOOST,
        EQPreset.TREBLE_BOOST,
        EQPreset.VOCAL
    )
    
    init {
        loadDefaultProfile()
        loadLearnedProfiles()
    }
    
    private fun loadDefaultProfile() {
        viewModelScope.launch {
            // Start with flat profile
            _currentProfile.value = EQProfile(
                songId = 0,
                songTitle = "Default",
                songArtist = "",
                band31Hz = 0f,
                band62Hz = 0f,
                band125Hz = 0f,
                band250Hz = 0f,
                band500Hz = 0f,
                band1kHz = 0f,
                band2kHz = 0f,
                band4kHz = 0f,
                band8kHz = 0f,
                band16kHz = 0f,
                isAutoLearned = false
            )
        }
    }
    
    private fun loadLearnedProfiles() {
        viewModelScope.launch {
            eqRepository.getAllProfiles().collect { profiles ->
                _learnedProfiles.value = profiles
                _learnedProfileCount.value = profiles.size
            }
        }
    }
    
    fun toggleAutoMode() {
        _isAutoMode.value = !_isAutoMode.value
    }
    
    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }
    
    fun updateBandLevel(index: Int, value: Float) {
        val current = _currentProfile.value ?: return
        
        val updated = when (index) {
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
        
        _currentProfile.value = updated
    }
    
    // FIXED: Create new EQProfile with preset values
    fun applyPreset(preset: EQPreset) {
        val current = _currentProfile.value ?: return
        
        val updated = EQProfile(
            songId = current.songId,
            songTitle = current.songTitle,
            songArtist = current.songArtist,
            band31Hz = preset.band31Hz,
            band62Hz = preset.band62Hz,
            band125Hz = preset.band125Hz,
            band250Hz = preset.band250Hz,
            band500Hz = preset.band500Hz,
            band1kHz = preset.band1kHz,
            band2kHz = preset.band2kHz,
            band4kHz = preset.band4kHz,
            band8kHz = preset.band8kHz,
            band16kHz = preset.band16kHz,
            isAutoLearned = false
        )
        
        _currentProfile.value = updated
    }
    
    fun deleteProfile(profile: EQProfile) {
        viewModelScope.launch {
            eqRepository.deleteProfile(profile)
        }
    }
}

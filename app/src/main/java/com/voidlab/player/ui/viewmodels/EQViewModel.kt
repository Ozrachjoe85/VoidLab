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
        EQPreset("Flat", "Neutral", List(10) { 0f }),
        EQPreset("Bass Boost", "More Bass", listOf(6f, 5f, 4f, 2f, 0f, 0f, 0f, 0f, 0f, 0f)),
        EQPreset("Treble Boost", "Crisp Highs", listOf(0f, 0f, 0f, 0f, 0f, 2f, 4f, 5f, 6f, 6f)),
        EQPreset("V-Shape", "Enhanced", listOf(5f, 4f, 2f, 0f, -2f, -2f, 0f, 2f, 4f, 5f)),
        EQPreset("Vocal", "Clear Voice", listOf(0f, -2f, -1f, 2f, 4f, 4f, 2f, 0f, -1f, -2f))
    )
    
    init {
        loadLearnedProfiles()
        
        // Initialize with flat profile
        _currentProfile.value = EQProfile(
            songId = 0L,
            songTitle = "Manual",
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
        
        val updatedProfile = when (index) {
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
        
        _currentProfile.value = updatedProfile
    }
    
    fun applyPreset(preset: EQPreset) {
        val profile = EQProfile(
            songId = 0L,
            songTitle = preset.name,
            songArtist = "",
            band31Hz = preset.bands[0],
            band62Hz = preset.bands[1],
            band125Hz = preset.bands[2],
            band250Hz = preset.bands[3],
            band500Hz = preset.bands[4],
            band1kHz = preset.bands[5],
            band2kHz = preset.bands[6],
            band4kHz = preset.bands[7],
            band8kHz = preset.bands[8],
            band16kHz = preset.bands[9],
            isAutoLearned = false
        )
        
        _currentProfile.value = profile
    }
    
    fun deleteProfile(profile: EQProfile) {
        viewModelScope.launch {
            eqRepository.deleteProfile(profile)
        }
    }
}

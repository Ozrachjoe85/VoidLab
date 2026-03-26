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
    
    // REAL-TIME SPECTRUM DATA from FrequencyAnalyzer
    private val _currentSpectrum = MutableStateFlow(FloatArray(10))
    val currentSpectrum: StateFlow<FloatArray> = _currentSpectrum.asStateFlow()
    
    val presets = listOf(
        EQPreset("Flat", List(10) { 0f }),
        EQPreset("Bass Boost", listOf(8f, 6f, 4f, 2f, 0f, 0f, 0f, 0f, 0f, 0f)),
        EQPreset("Treble", listOf(0f, 0f, 0f, 0f, 0f, 2f, 4f, 6f, 8f, 8f)),
        EQPreset("Rock", listOf(6f, 4f, 2f, 0f, -2f, -2f, 0f, 2f, 4f, 6f)),
        EQPreset("Pop", listOf(2f, 4f, 6f, 4f, 0f, -2f, -2f, 0f, 2f, 4f)),
        EQPreset("Classical", listOf(4f, 2f, 0f, 0f, 0f, 0f, 2f, 4f, 6f, 6f))
    )
    
    init {
        loadLearnedProfiles()
        
        // Start receiving real-time spectrum data
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
        val profile = EQProfile(
            songId = 0, // Manual preset
            songTitle = preset.name,
            songArtist = "Preset",
            bands = preset.bands,
            isAutoLearned = false
        )
        _currentProfile.value = profile
    }
    
    fun updateBandLevel(index: Int, value: Float) {
        val current = _currentProfile.value
        if (current != null) {
            val newBands = current.getBands().toMutableList()
            newBands[index] = value
            _currentProfile.value = current.copy(bands = newBands)
        } else {
            // Create new profile if none exists
            val newBands = MutableList(10) { 0f }
            newBands[index] = value
            _currentProfile.value = EQProfile(
                songId = 0,
                songTitle = "Custom",
                songArtist = "Manual",
                bands = newBands,
                isAutoLearned = false
            )
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

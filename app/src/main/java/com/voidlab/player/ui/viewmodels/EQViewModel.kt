package com.voidlab.player.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidlab.player.data.models.EQProfile
import com.voidlab.player.data.repository.EQRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ViewMode {
    CURVE, MIXER
}

data class Preset(val name: String, val bands: List<Float>)

@HiltViewModel
class EQViewModel @Inject constructor(
    private val eqRepository: EQRepository
) : ViewModel() {
    
    private val _isAutoMode = MutableStateFlow(false)
    val isAutoMode: StateFlow<Boolean> = _isAutoMode.asStateFlow()
    
    private val _currentProfile = MutableStateFlow<EQProfile?>(null)
    val currentProfile: StateFlow<EQProfile?> = _currentProfile.asStateFlow()
    
    private val _viewMode = MutableStateFlow(ViewMode.CURVE)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()
    
    val learnedProfiles: StateFlow<List<EQProfile>> = eqRepository.getAllLearnedProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val learnedProfileCount: StateFlow<Int> = eqRepository.getLearnedProfileCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    val presets = listOf(
        Preset("Flat", EQProfile.FLAT),
        Preset("Rock", EQProfile.ROCK),
        Preset("Pop", EQProfile.POP),
        Preset("Jazz", EQProfile.JAZZ),
        Preset("Classical", EQProfile.CLASSICAL),
        Preset("Bass Boost", EQProfile.BASS)
    )
    
    fun toggleAutoMode() {
        _isAutoMode.value = !_isAutoMode.value
    }
    
    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }
    
    fun loadProfile(songId: Long) {
        viewModelScope.launch {
            val profile = eqRepository.getProfileForSong(songId)
            _currentProfile.value = profile
        }
    }
    
    fun applyPreset(preset: Preset) {
        // Create a temporary profile for preview
        _currentProfile.value = EQProfile.fromBands(
            songId = 0L,
            songTitle = "Preview",
            songArtist = "",
            bands = preset.bands,
            isAutoLearned = false
        )
    }
    
    fun updateBandLevel(bandIndex: Int, level: Float) {
        val current = _currentProfile.value ?: return
        val bands = current.getBands().toMutableList()
        if (bandIndex in bands.indices) {
            bands[bandIndex] = level
            _currentProfile.value = EQProfile.fromBands(
                songId = current.songId,
                songTitle = current.songTitle,
                songArtist = current.songArtist,
                bands = bands,
                isAutoLearned = false
            )
        }
    }
    
    fun saveCurrentProfile() {
        viewModelScope.launch {
            _currentProfile.value?.let { profile ->
                eqRepository.saveProfile(profile)
            }
        }
    }
    
    fun deleteProfile(profile: EQProfile) {
        viewModelScope.launch {
            eqRepository.deleteProfile(profile)
        }
    }
    
    fun clearAllProfiles() {
        viewModelScope.launch {
            eqRepository.clearAllProfiles()
        }
    }
}

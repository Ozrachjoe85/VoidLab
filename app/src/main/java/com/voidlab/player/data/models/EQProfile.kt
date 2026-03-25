package com.voidlab.player.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "eq_profiles")
data class EQProfile(
    @PrimaryKey
    val songId: Long,
    val songTitle: String = "",
    val songArtist: String = "",
    val band31Hz: Float = 0f,
    val band62Hz: Float = 0f,
    val band125Hz: Float = 0f,
    val band250Hz: Float = 0f,
    val band500Hz: Float = 0f,
    val band1kHz: Float = 0f,
    val band2kHz: Float = 0f,
    val band4kHz: Float = 0f,
    val band8kHz: Float = 0f,
    val band16kHz: Float = 0f,
    val isAutoLearned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getBands(): List<Float> = listOf(
        band31Hz, band62Hz, band125Hz, band250Hz, band500Hz,
        band1kHz, band2kHz, band4kHz, band8kHz, band16kHz
    )
    
    companion object {
        fun fromBands(
            songId: Long,
            songTitle: String,
            songArtist: String,
            bands: List<Float>,
            isAutoLearned: Boolean = false
        ): EQProfile {
            require(bands.size == 10) { "Must provide exactly 10 band values" }
            return EQProfile(
                songId = songId,
                songTitle = songTitle,
                songArtist = songArtist,
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
                isAutoLearned = isAutoLearned,
                createdAt = System.currentTimeMillis()
            )
        }
        
        // Preset profiles
        val FLAT = List(10) { 0f }
        val ROCK = listOf(-1f, 0f, 1f, 2f, 1f, 0f, 2f, 3f, 2f, 1f)
        val POP = listOf(0f, 1f, 2f, 2f, 1f, 1f, 2f, 3f, 3f, 2f)
        val JAZZ = listOf(1f, 1f, 0f, 1f, 2f, 2f, 1f, 1f, 2f, 2f)
        val CLASSICAL = listOf(2f, 1f, 0f, 0f, 1f, 1f, 0f, 1f, 2f, 3f)
        val BASS = listOf(6f, 5f, 4f, 2f, 0f, -1f, 0f, 1f, 2f, 3f)
    }
}

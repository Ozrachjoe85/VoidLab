package com.voidlab.player.data.models

enum class EQPreset(val displayName: String, val bandValues: FloatArray) {
    FLAT("FLAT", floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)),
    ROCK("ROCK", floatArrayOf(5f, 3f, -2f, -3f, -1f, 2f, 5f, 7f, 7f, 7f)),
    POP("POP", floatArrayOf(-1f, -1f, 0f, 2f, 4f, 4f, 2f, 0f, -1f, -1f)),
    JAZZ("JAZZ", floatArrayOf(4f, 3f, 2f, 2f, -2f, -2f, 0f, 2f, 3f, 4f)),
    CLASSICAL("CLASSICAL", floatArrayOf(5f, 4f, 3f, 2f, -2f, -2f, 0f, 2f, 3f, 4f)),
    BASS("BASS", floatArrayOf(8f, 7f, 6f, 3f, 1f, 0f, 0f, 0f, 0f, 0f));
    
    companion object {
        fun applyPreset(preset: EQPreset): FloatArray {
            return preset.bandValues.copyOf()
        }
    }
}

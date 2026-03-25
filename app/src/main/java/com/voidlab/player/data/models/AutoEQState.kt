package com.voidlab.player.data.models

sealed class AutoEQState {
    object Idle : AutoEQState()
    data class Learning(val progress: Float) : AutoEQState()
    data class Learned(val profile: EQProfile) : AutoEQState()
    data class Error(val message: String) : AutoEQState()
}

data class FrequencySnapshot(
    val timestamp: Long,
    val bands: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FrequencySnapshot
        if (timestamp != other.timestamp) return false
        if (!bands.contentEquals(other.bands)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + bands.contentHashCode()
        return result
    }
}

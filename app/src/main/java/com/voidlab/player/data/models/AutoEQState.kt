package com.voidlab.player.data.models

sealed class AutoEQState {
    object Idle : AutoEQState()
    data class Learning(val progress: Float) : AutoEQState()
    data class Learned(val profile: EQProfile) : AutoEQState()
    data class Error(val message: String) : AutoEQState()
}

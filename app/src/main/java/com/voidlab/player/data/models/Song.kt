package com.voidlab.player.data.models

import android.net.Uri

data class Song(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val albumArtUri: Uri?,
    val dateAdded: Long,
    val size: Long
)

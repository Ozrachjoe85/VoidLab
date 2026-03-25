package com.voidlab.player.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey val songId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

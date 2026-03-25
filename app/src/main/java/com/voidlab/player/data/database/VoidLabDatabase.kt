package com.voidlab.player.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.voidlab.player.data.models.EQProfile
import com.voidlab.player.data.models.Favorite

@Database(
    entities = [EQProfile::class, Favorite::class],
    version = 1,
    exportSchema = false
)
abstract class VoidLabDatabase : RoomDatabase() {
    abstract fun eqProfileDao(): EQProfileDao
    abstract fun favoriteDao(): FavoriteDao
}

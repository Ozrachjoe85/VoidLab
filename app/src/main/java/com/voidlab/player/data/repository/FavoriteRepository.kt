package com.voidlab.player.data.repository

import com.voidlab.player.data.database.FavoriteDao
import com.voidlab.player.data.models.Favorite
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val favoriteDao: FavoriteDao
) {
    fun getAllFavorites(): Flow<List<Favorite>> = favoriteDao.getAllFavorites()
    
    fun isFavorite(songId: Long): Flow<Boolean> = favoriteDao.isFavorite(songId)
    
    suspend fun addFavorite(songId: Long) {
        favoriteDao.insertFavorite(Favorite(songId))
    }
    
    suspend fun removeFavorite(songId: Long) {
        favoriteDao.deleteFavoriteBySongId(songId)
    }
    
    suspend fun toggleFavorite(songId: Long) {
        val existing = favoriteDao.getFavorite(songId)
        if (existing != null) {
            favoriteDao.deleteFavorite(existing)
        } else {
            favoriteDao.insertFavorite(Favorite(songId))
        }
    }
}

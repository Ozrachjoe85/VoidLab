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
    
    fun getAllFavorites(): Flow<List<Favorite>> {
        return favoriteDao.getAllFavorites()
    }
    
    fun isFavorite(songId: Long): Flow<Boolean> {
        return favoriteDao.isFavorite(songId)
    }
    
    suspend fun toggleFavorite(songId: Long) {
        if (favoriteDao.isFavoriteSync(songId)) {
            favoriteDao.removeFavorite(songId)
        } else {
            favoriteDao.addFavorite(Favorite(songId))
        }
    }
    
    suspend fun clearAllFavorites() {
        favoriteDao.clearAllFavorites()
    }
}

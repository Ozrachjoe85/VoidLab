package com.voidlab.player.data.repository

import com.voidlab.player.data.database.FavoriteDao
import com.voidlab.player.data.models.Favorite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val dao: FavoriteDao
) {
    
    suspend fun toggleFavorite(songId: Long) = withContext(Dispatchers.IO) {
        val isFav = dao.isFavoriteSync(songId)
        if (isFav) {
            dao.removeFavorite(songId)
        } else {
            dao.addFavorite(Favorite(songId = songId))
        }
    }
    
    fun isFavorite(songId: Long): Flow<Boolean> {
        return dao.isFavorite(songId).flowOn(Dispatchers.IO)
    }
    
    fun getAllFavorites(): Flow<List<Favorite>> {
        return dao.getAllFavorites().flowOn(Dispatchers.IO)
    }
    
    suspend fun removeFavorite(songId: Long) = withContext(Dispatchers.IO) {
        dao.removeFavorite(songId)
    }
    
    suspend fun clearAllFavorites() = withContext(Dispatchers.IO) {
        dao.clearAllFavorites()
    }
}

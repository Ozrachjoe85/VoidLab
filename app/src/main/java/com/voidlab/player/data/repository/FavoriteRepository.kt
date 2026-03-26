package com.voidlab.player.data.repository

import com.voidlab.player.data.database.VoidLabDatabase
import com.voidlab.player.data.models.FavoriteSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val database: VoidLabDatabase
) {
    
    private val dao = database.favoriteSongDao()
    
    /**
     * Toggle favorite status for a song.
     * If favorited, removes it. If not favorited, adds it.
     */
    suspend fun toggleFavorite(songId: Long) = withContext(Dispatchers.IO) {
        if (dao.isFavorite(songId)) {
            dao.removeFavorite(songId)
        } else {
            dao.addFavorite(FavoriteSong(songId = songId))
        }
    }
    
    /**
     * Check if a song is favorited.
     * Returns a Flow that emits true/false.
     */
    fun isFavorite(songId: Long): Flow<Boolean> = flow {
        emit(dao.isFavorite(songId))
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get all favorited song IDs.
     */
    fun getAllFavorites(): Flow<List<Long>> = flow {
        val favorites = dao.getAllFavorites()
        emit(favorites.map { it.songId })
    }.flowOn(Dispatchers.IO)
    
    /**
     * Remove a song from favorites.
     */
    suspend fun removeFavorite(songId: Long) = withContext(Dispatchers.IO) {
        dao.removeFavorite(songId)
    }
    
    /**
     * Clear all favorites.
     */
    suspend fun clearAllFavorites() = withContext(Dispatchers.IO) {
        dao.clearAll()
    }
}

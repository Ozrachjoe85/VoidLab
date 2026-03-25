package com.voidlab.player.data.repository

import com.voidlab.player.data.database.EQProfileDao
import com.voidlab.player.data.models.EQProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EQRepository @Inject constructor(
    private val eqProfileDao: EQProfileDao
) {
    
    suspend fun getProfileForSong(songId: Long): EQProfile? {
        return eqProfileDao.getProfileBySongId(songId)
    }
    
    fun getProfileForSongFlow(songId: Long): Flow<EQProfile?> {
        return eqProfileDao.getProfileBySongIdFlow(songId)
    }
    
    fun getAllLearnedProfiles(): Flow<List<EQProfile>> {
        return eqProfileDao.getAllLearnedProfiles()
    }
    
    suspend fun saveProfile(profile: EQProfile) {
        eqProfileDao.insertProfile(profile)
    }
    
    suspend fun deleteProfile(profile: EQProfile) {
        eqProfileDao.deleteProfile(profile)
    }
    
    suspend fun clearAllProfiles() {
        eqProfileDao.deleteAllProfiles()
    }
    
    fun getLearnedProfileCount(): Flow<Int> {
        return eqProfileDao.getLearnedProfileCount()
    }
}

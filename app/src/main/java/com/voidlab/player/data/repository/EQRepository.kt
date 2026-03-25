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
    fun getAllProfiles(): Flow<List<EQProfile>> = eqProfileDao.getAllProfiles()
    
    suspend fun getProfileBySongId(songId: Long): EQProfile? {
        return eqProfileDao.getProfileBySongId(songId)
    }
    
    fun getProfileBySongIdFlow(songId: Long): Flow<EQProfile?> {
        return eqProfileDao.getProfileBySongIdFlow(songId)
    }
    
    suspend fun saveProfile(profile: EQProfile) {
        eqProfileDao.insertProfile(profile)
    }
    
    suspend fun updateProfile(profile: EQProfile) {
        eqProfileDao.updateProfile(profile.copy(updatedAt = System.currentTimeMillis()))
    }
    
    suspend fun deleteProfile(profile: EQProfile) {
        eqProfileDao.deleteProfile(profile)
    }
    
    suspend fun deleteAllProfiles() {
        eqProfileDao.deleteAllProfiles()
    }
    
    suspend fun getProfileCount(): Int {
        return eqProfileDao.getProfileCount()
    }
    
    fun getLearningProfiles(): Flow<List<EQProfile>> {
        return eqProfileDao.getLearningProfiles()
    }
}

package com.voidlab.player.data.database

import androidx.room.*
import com.voidlab.player.data.models.EQProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface EQProfileDao {
    
    @Query("SELECT * FROM eq_profiles WHERE songId = :songId")
    suspend fun getProfileBySongId(songId: Long): EQProfile?
    
    @Query("SELECT * FROM eq_profiles WHERE songId = :songId")
    fun getProfileBySongIdFlow(songId: Long): Flow<EQProfile?>
    
    @Query("SELECT * FROM eq_profiles WHERE isAutoLearned = 1 ORDER BY createdAt DESC")
    fun getAllLearnedProfiles(): Flow<List<EQProfile>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: EQProfile)
    
    @Delete
    suspend fun deleteProfile(profile: EQProfile)
    
    @Query("DELETE FROM eq_profiles")
    suspend fun deleteAllProfiles()
    
    @Query("SELECT COUNT(*) FROM eq_profiles WHERE isAutoLearned = 1")
    fun getLearnedProfileCount(): Flow<Int>
}

package com.voidlab.player

import android.app.Application
import androidx.room.Room
import com.voidlab.player.data.database.VoidLabDatabase
import com.voidlab.player.data.repository.EQRepository
import com.voidlab.player.data.repository.FavoriteRepository
import com.voidlab.player.data.repository.MusicRepository
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VoidLabApp : Application() {
    
    // Expose repositories for PlaybackService (which can't use Hilt)
    lateinit var eqRepository: EQRepository
        private set
    
    lateinit var musicRepository: MusicRepository
        private set
    
    lateinit var favoriteRepository: FavoriteRepository
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize database and repositories manually for PlaybackService
        val database = Room.databaseBuilder(
            this,
            VoidLabDatabase::class.java,
            "voidlab_database"
        ).build()
        
        eqRepository = EQRepository(database.eqProfileDao())
        musicRepository = MusicRepository(this)
        favoriteRepository = FavoriteRepository(database.favoriteDao())
    }
}

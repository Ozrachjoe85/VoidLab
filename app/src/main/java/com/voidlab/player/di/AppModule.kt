package com.voidlab.player.di

import android.content.Context
import androidx.room.Room
import com.voidlab.player.data.database.EQProfileDao
import com.voidlab.player.data.database.FavoriteDao
import com.voidlab.player.data.database.VoidLabDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideVoidLabDatabase(@ApplicationContext context: Context): VoidLabDatabase {
        return Room.databaseBuilder(
            context,
            VoidLabDatabase::class.java,
            "voidlab_database"
        ).build()
    }
    
    @Provides
    fun provideEQProfileDao(database: VoidLabDatabase): EQProfileDao {
        return database.eqProfileDao()
    }
    
    @Provides
    fun provideFavoriteDao(database: VoidLabDatabase): FavoriteDao {
        return database.favoriteDao()
    }
}

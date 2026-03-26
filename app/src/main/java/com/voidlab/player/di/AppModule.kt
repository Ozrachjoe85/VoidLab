package com.voidlab.player.di

import android.content.Context
import androidx.room.Room
import com.voidlab.player.data.database.VoidLabDatabase
import com.voidlab.player.data.repository.MusicRepository
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
    fun provideVoidLabDatabase(
        @ApplicationContext context: Context
    ): VoidLabDatabase {
        return Room.databaseBuilder(
            context,
            VoidLabDatabase::class.java,
            "voidlab_database"
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideEQProfileDao(database: VoidLabDatabase) = database.eqProfileDao()
    
    @Provides
    @Singleton
    fun provideFavoriteDao(database: VoidLabDatabase) = database.favoriteDao()
    
    @Provides
    @Singleton
    fun provideMusicRepository(
        @ApplicationContext context: Context
    ): MusicRepository {
        return MusicRepository(context)
    }
}

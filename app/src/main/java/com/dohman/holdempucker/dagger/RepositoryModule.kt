package com.dohman.holdempucker.dagger

import android.app.Application
import com.dohman.holdempucker.repositories.ResourceRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RepositoryModule {
    @Provides
    @Singleton
    fun providesResourceRepository(application: Application) = ResourceRepository(application)
}
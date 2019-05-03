package com.dohman.holdempucker.dagger

import com.dohman.holdempucker.MainApplication
import com.dohman.holdempucker.activities.viewmodels.GameViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [(AppModule::class), (RepositoryModule::class), (DataSourceModule::class)])
interface RepositoryComponent : DataSourceComponent {
    companion object : RepositoryComponent by MainApplication.repositoryComponent

    fun inject(viewModel: GameViewModel)
}
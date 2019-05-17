package com.dohman.holdempucker.dagger

import com.dohman.holdempucker.MainApplication
import com.dohman.holdempucker.ui.game.GameViewModel
import com.dohman.holdempucker.ui.main_menu.MainMenuViewModel
import com.dohman.holdempucker.ui.splash.SplashViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [(AppModule::class), (RepositoryModule::class), (DataSourceModule::class)])
interface RepositoryComponent : DataSourceComponent {
    companion object : RepositoryComponent by MainApplication.repositoryComponent

    fun inject(viewModel: SplashViewModel)
    fun inject(viewModel: MainMenuViewModel)
    fun inject(viewModel: GameViewModel)
}
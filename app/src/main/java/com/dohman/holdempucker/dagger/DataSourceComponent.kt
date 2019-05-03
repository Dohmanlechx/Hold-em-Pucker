package com.dohman.holdempucker.dagger

import com.dohman.holdempucker.MainApplication
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [(AppModule::class), (DataSourceModule::class)])
interface DataSourceComponent {
    companion object : DataSourceComponent by MainApplication.repositoryComponent
}
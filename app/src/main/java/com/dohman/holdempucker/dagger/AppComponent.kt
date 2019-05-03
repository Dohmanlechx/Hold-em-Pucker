package com.dohman.holdempucker.dagger

import com.dohman.holdempucker.MainApplication
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [(AppModule::class)])
interface AppComponent {
    fun inject(app: MainApplication)
}
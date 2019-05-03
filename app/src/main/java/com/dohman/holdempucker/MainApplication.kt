package com.dohman.holdempucker

import android.app.Application
import com.dohman.holdempucker.dagger.*

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        application = this
        recreateComponents()
    }

    private fun recreateComponents() {
        pAppComponent = null
        appComponent.inject(this)
    }

    companion object {
        lateinit var application: MainApplication
        lateinit var repositoryComponent: RepositoryComponent

        private var pAppComponent: AppComponent? = null
        val appComponent: AppComponent
            get() = pAppComponent.let {
                if (it == null) {
                    val appModule = AppModule(application)
                    val dataSourceModule = DataSourceModule()
                    val repoModule = RepositoryModule()
                    @Suppress("DEPRECATION")
                    pAppComponent = DaggerAppComponent.builder()
                        .appModule(appModule)
                        .build()
                    @Suppress("DEPRECATION")
                    repositoryComponent = DaggerRepositoryComponent.builder()
                        .appModule(appModule)
                        .repositoryModule(repoModule)
                        .dataSourceModule(dataSourceModule)
                        .build()
                    pAppComponent!!
                } else {
                    it
                }
            }
    }
}
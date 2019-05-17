package com.dohman.holdempucker.ui.splash

import androidx.lifecycle.ViewModel;
import com.dohman.holdempucker.dagger.RepositoryComponent

class SplashViewModel : ViewModel() {

    init {
        RepositoryComponent.inject(this)
    }
}

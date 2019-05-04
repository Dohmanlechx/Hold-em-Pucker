package com.dohman.holdempucker.ui.fragments

import androidx.lifecycle.ViewModel;
import com.dohman.holdempucker.dagger.RepositoryComponent
import com.dohman.holdempucker.repositories.ResourceRepository
import javax.inject.Inject

class MainMenuViewModel : ViewModel() {
    @Inject
    lateinit var appRepo: ResourceRepository

    init {
        RepositoryComponent.inject(this)
    }
}

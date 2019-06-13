package com.dohman.holdempucker.ui.lobbies

import androidx.lifecycle.ViewModel;
import com.dohman.holdempucker.dagger.RepositoryComponent

class LobbiesViewModel : ViewModel() {

    init {
        RepositoryComponent.inject(this)
    }
}

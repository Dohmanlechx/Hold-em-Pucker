package com.dohman.holdempucker.ui.lobbies

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel;
import com.dohman.holdempucker.dagger.RepositoryComponent
import com.dohman.holdempucker.models.OnlineLobby
import com.dohman.holdempucker.repositories.LobbyRepository
import com.dohman.holdempucker.repositories.OnlinePlayRepository
import javax.inject.Inject

class LobbiesViewModel : ViewModel() {
    @Inject
    lateinit var lobbyRepo: LobbyRepository
    @Inject
    lateinit var onlineRepo: OnlinePlayRepository

    val lobbyNotifier = MutableLiveData<List<OnlineLobby>>()

    private val lobbyObserver = Observer<List<OnlineLobby>> { lobbies -> lobbyNotifier.value = lobbies }

    init {
        RepositoryComponent.inject(this)
        lobbyRepo.lobbies.observeForever(lobbyObserver)
    }

    fun getAmountPlayersOfLobby(lobbyId: String?, fReturnedValue: (Int) -> Unit) =
        lobbyRepo.getAmountPlayersOfLobby(lobbyId) {
            fReturnedValue.invoke(it)
        }

    fun isPasswordValid(lobbyId: String?, lobbyPassword: String, fReturnedValue: (Boolean) -> Unit) =
        onlineRepo.checkPassword(lobbyId, lobbyPassword) { isValid -> fReturnedValue.invoke(isValid) }

    override fun onCleared() {
        super.onCleared()
        lobbyRepo.lobbies.removeObserver(lobbyObserver)
    }
}

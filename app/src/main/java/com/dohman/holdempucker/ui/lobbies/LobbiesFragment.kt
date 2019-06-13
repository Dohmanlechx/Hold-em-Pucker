package com.dohman.holdempucker.ui.lobbies

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.dohman.holdempucker.R
import com.dohman.holdempucker.models.OnlineLobby
import com.dohman.holdempucker.ui.items.LobbyItem
import com.dohman.holdempucker.util.Constants
import com.dohman.holdempucker.util.Constants.Companion.currentGameMode
import com.dohman.holdempucker.util.Constants.Companion.lobbyId
import com.dohman.holdempucker.util.Constants.Companion.period
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.android.synthetic.main.lobbies_fragment.*


class LobbiesFragment : Fragment() {
    private lateinit var vm: LobbiesViewModel

    private val itemAdapter = ItemAdapter<AbstractItem<*, *>>()
    private val fastAdapter = FastAdapter.with<AbstractItem<*, *>, ItemAdapter<AbstractItem<*, *>>>(itemAdapter)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        vm = ViewModelProviders.of(this).get(LobbiesViewModel::class.java)

        vm.lobbyNotifier.observe(viewLifecycleOwner, Observer { lobbies ->
            updateLobbyRecycler(lobbies)
        })

        return inflater.inflate(R.layout.lobbies_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLobbiesRecycler()
    }

    override fun onResume() {
        super.onResume()
        setupOnClickListeners()
        lobbyId = ""
    }

    private fun clearTeams() {
        for (index in 0..5) {
            Constants.teamTop[index] = null
            Constants.teamBottom[index] = null
        }
    }

    private fun setupOnClickListeners() {
        v_fab_create_server.setOnClickListener {
            currentGameMode = Constants.GameMode.ONLINE
            navigateToGameFragment()
        }

        v_fab_play_offline_multiplayer.setOnClickListener {
            currentGameMode = Constants.GameMode.FRIEND
            navigateToGameFragment()
        }
    }

    private fun removeAllOnClickListeners() {
        v_fab_create_server.setOnClickListener(null)
        v_fab_play_offline_multiplayer.setOnClickListener(null)
    }

    private fun navigateToGameFragment() {
        removeAllOnClickListeners()
        clearTeams()
        view?.findNavController()?.navigate(R.id.action_lobbiesFragment_to_gameFragment)
    }

    private fun setupLobbiesRecycler() = v_lobbies_recycler.apply {
        itemAnimator = DefaultItemAnimator()
        layoutManager = LinearLayoutManager(requireContext())
        adapter = fastAdapter
    }

    private fun updateLobbyRecycler(lobbies: List<OnlineLobby>) {
        itemAdapter.clear()
        lobbies.forEach { lobby ->
            vm.getAmountPlayersOfLobby(lobby.id!!) { amountPlayers ->
                itemAdapter.add(LobbyItem(lobby.id, amountPlayers))
            }
        }
    }
}

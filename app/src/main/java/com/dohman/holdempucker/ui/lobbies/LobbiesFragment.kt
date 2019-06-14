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
import com.dohman.holdempucker.util.ViewUtil
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
    }

    private fun clearTeams() {
        for (index in 0..5) {
            Constants.teamPurple[index] = null
            Constants.teamGreen[index] = null
        }
    }

    private fun setupOnClickListeners() {
        v_fab_create_server.setOnClickListener {
            ViewUtil.buildLobbyNameDialog(requireContext()) { lobbyName ->
                currentGameMode = Constants.GameMode.ONLINE
                navigateToGameFragment(null, lobbyName)
            }
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

    private fun navigateToGameFragment(lobbyId: String? = null, lobbyName: String? = null) {
        removeAllOnClickListeners()
        clearTeams()
        if (lobbyId != null) {
            val action = LobbiesFragmentDirections.actionLobbiesFragmentToGameFragment(lobbyId, null)
            view?.findNavController()?.navigate(action)
        } else if (lobbyName != null) {
            val action = LobbiesFragmentDirections.actionLobbiesFragmentToGameFragment(null, lobbyName)
            view?.findNavController()?.navigate(action)
        }
    }

    private fun setupLobbiesRecycler() = v_lobbies_recycler.apply {
        itemAnimator = DefaultItemAnimator()
        layoutManager = LinearLayoutManager(requireContext())
        adapter = fastAdapter
    }

    private fun updateLobbyRecycler(lobbies: List<OnlineLobby>) {
        if (lobbies.isNullOrEmpty()) {
            v_lobbies_recycler.visibility = View.GONE
            txt_no_lobbies.visibility = View.VISIBLE
        } else {
            v_lobbies_recycler.visibility = View.VISIBLE
            txt_no_lobbies.visibility = View.GONE
        }

        itemAdapter.clear()
        lobbies.forEach { lobby ->
            vm.getAmountPlayersOfLobby(lobby.id) { amountPlayers ->
                itemAdapter.add(LobbyItem(lobby.id, lobby.name, amountPlayers) { lobbyId ->
                    // OnClick
                    currentGameMode = Constants.GameMode.ONLINE
                    navigateToGameFragment(lobbyId)
                })
            }
        }
    }
}

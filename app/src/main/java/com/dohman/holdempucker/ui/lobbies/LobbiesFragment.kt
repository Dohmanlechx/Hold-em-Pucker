package com.dohman.holdempucker.ui.lobbies

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.dohman.holdempucker.R
import com.dohman.holdempucker.models.OnlineLobby
import com.dohman.holdempucker.ui.items.LobbyItem
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

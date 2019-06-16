package com.dohman.holdempucker.ui.lobbies

import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.dohman.holdempucker.R
import com.dohman.holdempucker.models.OnlineLobby
import com.dohman.holdempucker.ui.items.LobbyItem
import com.dohman.holdempucker.util.Animations
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

        txt_online_beta.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.apply {
                type = "plain/text"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("dohman_92@hotmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Hold'em Pucker Online - Bug Report")
            }
            startActivity(Intent.createChooser(emailIntent, "Choose email client..."))
        }
    }

    override fun onResume() {
        super.onResume()
        setupOnClickListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        v_lobbies_recycler.adapter = null
    }

    private fun clearTeams() {
        for (index in 0..5) {
            Constants.teamPurple[index] = null
            Constants.teamGreen[index] = null
        }
    }

    private fun setupOnClickListeners() {
        v_fab_create_server.setOnClickListener {
            ViewUtil.buildLobbyNameDialog(requireContext()) { lobbyName, lobbyPassword ->
                currentGameMode = Constants.GameMode.ONLINE
                navigateToGameFragment(null, lobbyName, lobbyPassword)
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

    private fun navigateToGameFragment(
        lobbyId: String? = null,
        lobbyName: String? = null,
        lobbyPassword: String? = null
    ) {
        removeAllOnClickListeners()
        clearTeams()
        when {
            lobbyId != null -> {
                val action = LobbiesFragmentDirections.actionLobbiesFragmentToGameFragment(lobbyId, null)
                view?.findNavController()?.navigate(action)
            }
            lobbyName != null -> {
                val action =
                    LobbiesFragmentDirections.actionLobbiesFragmentToGameFragment(null, lobbyName, lobbyPassword)
                view?.findNavController()?.navigate(action)
            }
            else -> view?.findNavController()?.navigate(R.id.action_lobbiesFragment_to_gameFragment)
        }
    }

    private fun setupLobbiesRecycler() = v_lobbies_recycler.apply {
        itemAnimator = null
        layoutManager = LinearLayoutManager(requireContext())
        adapter = fastAdapter
    }

    private fun updateLobbyRecycler(lobbies: List<OnlineLobby>) {
        if (lobbies.isNullOrEmpty()) {
            v_lobbies_recycler.apply {
                visibility = View.GONE
                scaleX = 0.0f
                scaleY = 0.0f
            }
            txt_no_lobbies.visibility = View.VISIBLE
        } else {
            v_lobbies_recycler.visibility = View.VISIBLE
            txt_no_lobbies.visibility = View.GONE
        }

        if (lobbies.size == itemAdapter.adapterItemCount) return

        Animations.animateLobbyRecycler(v_lobbies_recycler, true) {
            // onStop
            itemAdapter.clear()
            lobbies.forEach { lobby ->
                vm.getAmountPlayersOfLobby(lobby.id) { amountPlayers ->
                    itemAdapter.add(
                        LobbyItem(
                            lobby.id,
                            lobby.name,
                            lobby.password,
                            amountPlayers
                        ) { lobbyId, lobbyPassword ->
                            // OnClick
                            currentGameMode = Constants.GameMode.ONLINE
                            if (lobbyPassword.isNullOrBlank()) {
                                navigateToGameFragment(lobbyId)
                            } else {
                                ViewUtil.buildLobbyPasswordInput(requireContext()) { password ->
                                    vm.isPasswordValid(lobbyId, password) { isValid ->
                                        if (isValid) navigateToGameFragment(lobbyId)
                                        else Toast.makeText(
                                            requireContext(),
                                            "Wrong password",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        })
                }
            }

            Animations.animateLobbyRecycler(v_lobbies_recycler, false)
        }
    }
}

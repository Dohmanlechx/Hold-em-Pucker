package com.dohman.holdempucker.ui.fragments

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.dohman.holdempucker.R
import com.dohman.holdempucker.util.Constants
import com.dohman.holdempucker.util.Constants.Companion.currentGameMode
import com.dohman.holdempucker.util.Constants.Companion.period
import com.dohman.holdempucker.util.Constants.Companion.teamBottom
import com.dohman.holdempucker.util.Constants.Companion.teamTop
import kotlinx.android.synthetic.main.main_menu_fragment.*

class MainMenuFragment : Fragment() {
    private lateinit var vm: MainMenuViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        vm = ViewModelProviders.of(this).get(MainMenuViewModel::class.java)
        return inflater.inflate(R.layout.main_menu_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setGradientOnTexts()

        tv_ai_random.setOnClickListener {
            currentGameMode = Constants.GameMode.RANDOM
            navigateToGameFragment()
        }

        tv_ai_developer.setOnClickListener {
            currentGameMode = Constants.GameMode.DEVELOPER
            navigateToGameFragment()
        }

        tv_vs_friend.setOnClickListener {
            currentGameMode = Constants.GameMode.FRIEND
            navigateToGameFragment()
        }
    }

    private fun clearTeams() {
        for (index in 0..5) {
            teamTop[index] = null
            teamBottom[index] = null
        }
    }

    private fun setGradientOnTexts() {
        tv_sub_header.paint.shader = vm.getLinearGradient(tv_sub_header)
    }

    private fun navigateToGameFragment() {
        clearTeams()
        period = 1
        view?.findNavController()?.navigate(R.id.action_mainMenuFragment_to_gameFragment)
    }
}

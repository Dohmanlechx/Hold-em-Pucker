package com.dohman.holdempucker.ui.main_menu

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.dohman.holdempucker.R
import com.dohman.holdempucker.ui.how_to_play.HowToPlayDialogFragment
import com.dohman.holdempucker.ui.how_to_play.HowToPlayDialogFragment.Companion.isHowToPlayDialogShown
import com.dohman.holdempucker.util.Animations
import com.dohman.holdempucker.util.Constants
import com.dohman.holdempucker.util.Constants.Companion.currentGameMode
import com.dohman.holdempucker.util.Constants.Companion.period
import com.dohman.holdempucker.util.Constants.Companion.teamBottom
import com.dohman.holdempucker.util.Constants.Companion.teamTop
import com.dohman.holdempucker.util.Util
import kotlinx.android.synthetic.main.main_menu_fragment.*

class MainMenuFragment : Fragment() {
    private lateinit var vm: MainMenuViewModel

    private var howToPlayDialogFragment: HowToPlayDialogFragment? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        vm = ViewModelProviders.of(this).get(MainMenuViewModel::class.java)
        return inflater.inflate(R.layout.main_menu_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_how_to_play.setOnClickListener {
            if (!isHowToPlayDialogShown) showHowToPlayDialog(it)
        }

        btn_easy_mode.setOnClickListener {
            currentGameMode = Constants.GameMode.RANDOM
            navigateToGameFragment(it)
        }

        btn_hard_mode.setOnClickListener {
            currentGameMode = Constants.GameMode.DEVELOPER
            navigateToGameFragment(it)
        }

        btn_multiplayer.setOnClickListener {
            currentGameMode = Constants.GameMode.FRIEND
            navigateToGameFragment(it)
        }
    }

    override fun onStop() {
        super.onStop()
        Animations.stopAllAnimations()

        howToPlayDialogFragment?.dismiss()
        howToPlayDialogFragment = null
    }

    private fun clearTeams() {
        for (index in 0..5) {
            teamTop[index] = null
            teamBottom[index] = null
        }
    }

    private fun showHowToPlayDialog(button: View) {
        Util.vibrate(requireContext(), true)
        Animations.animateButton(button) {
            if (howToPlayDialogFragment == null)
                howToPlayDialogFragment = HowToPlayDialogFragment()

            howToPlayDialogFragment?.show(childFragmentManager, "Dialog")
        }
    }

    private fun navigateToGameFragment(button: View) {
        clearTeams()
        period = 1

        Util.vibrate(requireContext(), true)
        Animations.animateButton(button) {
            view?.findNavController()?.navigate(R.id.action_mainMenuFragment_to_gameFragment)
        }
    }
}

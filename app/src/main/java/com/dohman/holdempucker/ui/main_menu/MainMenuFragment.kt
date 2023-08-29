package com.dohman.holdempucker.ui.main_menu

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.dohman.holdempucker.BuildConfig
import com.dohman.holdempucker.R
import com.dohman.holdempucker.databinding.MainMenuFragmentBinding
import com.dohman.holdempucker.ui.how_to_play.HowToPlayDialogFragment
import com.dohman.holdempucker.ui.how_to_play.HowToPlayDialogFragment.Companion.isHowToPlayDialogShown
import com.dohman.holdempucker.util.Animations
import com.dohman.holdempucker.util.Constants
import com.dohman.holdempucker.util.Constants.Companion.currentGameMode
import com.dohman.holdempucker.util.Constants.Companion.teamGreen
import com.dohman.holdempucker.util.Constants.Companion.teamPurple
import com.dohman.holdempucker.util.Util

class MainMenuFragment : Fragment() {
    private lateinit var vm: MainMenuViewModel
    private var howToPlayDialogFragment: HowToPlayDialogFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainMenuFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var _binding: MainMenuFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtVersion.text = String.format(getString(R.string.version, BuildConfig.VERSION_NAME))
    }

    override fun onResume() {
        super.onResume()

        binding.btnHowToPlay.setOnClickListener {
            if (!isHowToPlayDialogShown) showHowToPlayDialog(it)
            isHowToPlayDialogShown = true
        }

        binding.btnEasyMode.setOnClickListener {
            currentGameMode = Constants.GameMode.RANDOM
            navigateToGameFragment(it)
        }

        binding.btnHardMode.setOnClickListener {
            currentGameMode = Constants.GameMode.DEVELOPER
            navigateToGameFragment(it)
        }

        binding.btnMultiplayer.setOnClickListener {
            navigateToLobbiesFragment(it)
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
            teamPurple[index] = null
            teamGreen[index] = null
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
        removeAllOnClickListeners()

        clearTeams()

        Util.vibrate(requireContext(), true)
        Animations.animateButton(button) {
            view?.findNavController()?.navigate(R.id.action_mainMenuFragment_to_gameFragment)
        }
    }

    private fun navigateToLobbiesFragment(button: View) {
        removeAllOnClickListeners()

        clearTeams()

        Util.vibrate(requireContext(), true)
        Animations.animateButton(button) {
            view?.findNavController()?.navigate(R.id.action_mainMenuFragment_to_lobbiesFragment)
        }

    }

    private fun removeAllOnClickListeners() {
        binding.btnHowToPlay.setOnClickListener(null)
        binding.btnEasyMode.setOnClickListener(null)
        binding.btnHardMode.setOnClickListener(null)
        binding.btnMultiplayer.setOnClickListener(null)
    }
}

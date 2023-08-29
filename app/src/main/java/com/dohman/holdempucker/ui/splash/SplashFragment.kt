package com.dohman.holdempucker.ui.splash

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController

import com.dohman.holdempucker.databinding.SplashFragmentBinding
import com.dohman.holdempucker.util.Animations

class SplashFragment : Fragment() {
    private lateinit var vm: SplashViewModel

    private var wasSplashShown = false

    private var _binding: SplashFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SplashFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Animations.stopAllAnimations()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        if (!wasSplashShown) {
            wasSplashShown = true
            binding.tvTitle.post {
                Animations.animateSplashText(binding.tvTitle) { /* OnStop */ navigateToMainMenu() }
            }
        } else {
            navigateToMainMenu()
        }
    }

    private fun navigateToMainMenu() {
        val action = SplashFragmentDirections.actionSplashFragmentToMainMenuFragment()
        view?.findNavController()?.navigate(action)
    }
}

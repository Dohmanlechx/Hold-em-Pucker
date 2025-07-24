package com.dohman.holdempucker.ui.splash

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.navigation.Navigation
import androidx.navigation.findNavController

import com.dohman.holdempucker.R
import com.dohman.holdempucker.databinding.SplashFragmentBinding
import com.dohman.holdempucker.util.Animations

class SplashFragment : Fragment() {
    private lateinit var binding: SplashFragmentBinding
    private lateinit var vm: SplashViewModel

    private var wasSplashShown = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        vm = ViewModelProviders.of(this).get(SplashViewModel::class.java)
        binding = SplashFragmentBinding.inflate(inflater, container, false)
        return binding.root
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

    override fun onDestroyView() {
        super.onDestroyView()
        Animations.stopAllAnimations()
    }

    private fun navigateToMainMenu() {
        val action = SplashFragmentDirections.actionSplashFragmentToMainMenuFragment()
        view?.findNavController()?.navigate(action)
    }
}

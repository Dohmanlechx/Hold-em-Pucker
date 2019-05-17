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
import com.dohman.holdempucker.util.Animations
import kotlinx.android.synthetic.main.splash_fragment.*

class SplashFragment : Fragment() {
    private lateinit var vm: SplashViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        vm = ViewModelProviders.of(this).get(SplashViewModel::class.java)
        return inflater.inflate(R.layout.splash_fragment, container, false)
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)

        tv_title.post {
            Animations.animateSplashText(tv_title) {
                // OnStop
                val action = SplashFragmentDirections.actionSplashFragmentToMainMenuFragment()
                view?.findNavController()?.navigate(action)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Animations.stopAllAnimations()
    }
}

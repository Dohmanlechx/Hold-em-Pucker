package com.dohman.holdempucker.ui.splash

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.dohman.holdempucker.R

class SplashFragment : Fragment() {
    private lateinit var vm: SplashViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        vm = ViewModelProviders.of(this).get(SplashViewModel::class.java)

        return inflater.inflate(R.layout.splash_fragment, container, false)
    }

}

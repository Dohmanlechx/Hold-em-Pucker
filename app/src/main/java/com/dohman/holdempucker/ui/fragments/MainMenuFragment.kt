package com.dohman.holdempucker.ui.fragments

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation

import com.dohman.holdempucker.R
import com.dohman.holdempucker.util.Constants
import com.dohman.holdempucker.util.Constants.Companion.currentGameMode
import com.dohman.holdempucker.util.Constants.Companion.period
import com.dohman.holdempucker.util.Constants.Companion.teamBottom
import com.dohman.holdempucker.util.Constants.Companion.teamTop
import kotlinx.android.synthetic.main.main_menu_fragment.*

class MainMenuFragment : Fragment(), View.OnTouchListener {
    private lateinit var vm: MainMenuViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        vm = ViewModelProviders.of(this).get(MainMenuViewModel::class.java)

        return inflater.inflate(R.layout.main_menu_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setOnTouchListeners()
        setGradientOnTexts()

        tv_ai_random.setOnClickListener {
            if (currentGameMode != Constants.GameMode.RANDOM) clearTeams()
            currentGameMode = Constants.GameMode.RANDOM
            navigateToGameFragment()
        }

        tv_ai_developer.setOnClickListener {
            if (currentGameMode != Constants.GameMode.DEVELOPER) clearTeams()
            currentGameMode = Constants.GameMode.DEVELOPER
            navigateToGameFragment()
        }

        tv_vs_friend.setOnClickListener {
            if (currentGameMode != Constants.GameMode.FRIEND) clearTeams()
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
//        tv_title.paint.shader = vm.getLinearGradient(tv_title)
        tv_sub_header.paint.shader = vm.getLinearGradient(tv_sub_header)
    }

    private fun navigateToGameFragment() {
        period = 1
        view?.let { Navigation.findNavController(it).navigate(R.id.action_mainMenuFragment_to_gameFragment) }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//        if (event == null || v == null) return false
//
//        when (event.action) {
//            MotionEvent.ACTION_MOVE -> {
//                val fingerX = event.rawX - (v.width / 2)
//                v.x = fingerX
//                if (fingerX >= tv_vs_friend.width.toFloat()) {
//                    currentGameMode = Constants.GameMode.FRIEND
//                    navigateToGameFragment()
//                }
//            }
//            MotionEvent.ACTION_UP -> v.performClick()
//        }
//
//        return true
        return true
    }

    private fun setOnTouchListeners() {
        puck_vs_friend.setOnTouchListener(this)
    }
}

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
    }

    private fun setGradientOnTexts() {
        tv_title.paint.shader = vm.getLinearGradient(tv_title)
    }

    private fun navigateToGameFragment() {
        view?.let { Navigation.findNavController(it).navigate(R.id.action_mainMenuFragment_to_gameFragment) }
        puck_vs_friend.setOnTouchListener(null)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event == null || v == null) return false

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                val fingerX = event.rawX - (v.width / 2)
                v.x = fingerX
                if (fingerX >= tv_vs_friend.width.toFloat()) navigateToGameFragment()
            }
            MotionEvent.ACTION_UP -> v.performClick()
        }

        return true
    }

    private fun setOnTouchListeners() {
        puck_vs_friend.setOnTouchListener(this)
    }
}

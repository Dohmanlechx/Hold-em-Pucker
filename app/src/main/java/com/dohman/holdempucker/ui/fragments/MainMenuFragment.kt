package com.dohman.holdempucker.ui.fragments

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation

import com.dohman.holdempucker.R
import kotlinx.android.synthetic.main.main_menu_fragment.*

class MainMenuFragment : Fragment(), View.OnClickListener {
    private lateinit var vm: MainMenuViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm = ViewModelProviders.of(this).get(MainMenuViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_menu_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setOnClickListeners()
        setGradientOnTexts()
    }

    private fun setGradientOnTexts() {
        val titleWidth = tv_title.paint.measureText(tv_title.text.toString())
        tv_title.paint.shader = LinearGradient(
            0f, 0f, titleWidth, tv_title.textSize,
            intArrayOf(
                Color.parseColor("#F97C3C"),
                Color.parseColor("#FDB54E"),
                Color.parseColor("#64B678"),
                Color.parseColor("#478AEA"),
                Color.parseColor("#8446CC")
            ),
            null, Shader.TileMode.CLAMP
        )
    }

    private fun goToGameFragment(view: View?) {
        view?.let { Navigation.findNavController(it).navigate(R.id.action_mainMenuFragment_to_gameFragment) }
    }

    override fun onClick(v: View?) {
        when (v) {
            tv_vs_friend -> goToGameFragment(v)
        }
    }

    private fun setOnClickListeners() {
        tv_vs_friend.setOnClickListener(this)
    }
}

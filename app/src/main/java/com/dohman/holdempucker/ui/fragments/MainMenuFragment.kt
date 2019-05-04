package com.dohman.holdempucker.ui.fragments

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation

import com.dohman.holdempucker.R
import kotlinx.android.synthetic.main.main_menu_fragment.*

class MainMenuFragment : Fragment() {
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

        tv_vs_friend.setOnClickListener { goToGameFragment(it) }
    }

    private fun goToGameFragment(view: View) {
        Navigation.findNavController(view).navigate(R.id.action_mainMenuFragment_to_gameFragment)
    }

}

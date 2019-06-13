package com.dohman.holdempucker.ui.lobbies

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dohman.holdempucker.R


class LobbiesFragment : Fragment() {
    private lateinit var vm: LobbiesViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        vm = ViewModelProviders.of(this).get(LobbiesViewModel::class.java)
        return inflater.inflate(R.layout.lobbies_fragment, container, false)
    }

}

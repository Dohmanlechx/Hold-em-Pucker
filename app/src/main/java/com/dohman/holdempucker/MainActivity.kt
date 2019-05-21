package com.dohman.holdempucker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
    }

    override fun onBackPressed() {
        if (findNavController(R.id.nav_host_fragment).currentDestination?.id == R.id.gameFragment) buildDialog()
        else super.onBackPressed()
    }

    private fun buildDialog() = AlertDialog.Builder(this).apply {
        this.setTitle(getString(R.string.dialog_back_header))
        this.setMessage(getString(R.string.dialog_back_message))

        setPositiveButton(getString(R.string.dialog_back_positive)) { _, _ -> findNavController(R.id.nav_host_fragment).popBackStack() }
        setNegativeButton(getString(R.string.dialog_back_negative)) { _, _ -> }

        this.create()
        this.show()
    }
}

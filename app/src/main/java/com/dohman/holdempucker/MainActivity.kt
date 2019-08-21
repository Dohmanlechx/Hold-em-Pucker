package com.dohman.holdempucker

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.navigation.findNavController
import com.dohman.holdempucker.util.Constants.Companion.isOnlineMode
import com.dohman.holdempucker.util.Constants.Companion.isWinnerDeclared
import com.dohman.holdempucker.util.buildSimpleDialog

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main)
    }

    override fun onBackPressed() {
        if (findNavController(R.id.nav_host_fragment).currentDestination?.id == R.id.gameFragment && !isWinnerDeclared) showDialog()
        else super.onBackPressed()
    }

    private fun showDialog() = this.buildSimpleDialog(
        title = R.string.dialog_back_header,
        message = if (isOnlineMode()) R.string.dialog_back_message_online else R.string.dialog_back_message,
        positiveText = if (isOnlineMode()) R.string.dialog_back_positive_online else R.string.dialog_back_positive,
        negativeText = R.string.dialog_back_negative,
        positiveListener = DialogInterface.OnClickListener { _, _ ->
            findNavController(R.id.nav_host_fragment).popBackStack()
        },
        negativeListener = DialogInterface.OnClickListener { _, _ -> }
    )
}

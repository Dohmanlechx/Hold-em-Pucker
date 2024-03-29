package com.dohman.holdempucker.ui.how_to_play

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.dohman.holdempucker.R
import kotlinx.android.synthetic.main.dialog_htp.*

class HowToPlayDialogFragment : DialogFragment() {

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.ExtrasDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_htp, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        whole_dialog_view.setOnClickListener {
            this@HowToPlayDialogFragment.dismiss()
            isHowToPlayDialogShown = false
        }
    }

    override fun onPause() {
        super.onPause()
        this@HowToPlayDialogFragment.dismiss()
        isHowToPlayDialogShown = false
    }

    companion object {
        // The inbuilt isVisible() apparently doesn't work, it is why I have got an own value here
        var isHowToPlayDialogShown = false
    }
}

package com.dohman.holdempucker.util

import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.github.florent37.viewanimator.ViewAnimator
import com.wajahatkarim3.easyflipview.EasyFlipView

object NewAnimations {

    /*
    * Animation functions
    * */

    fun animateLamp(lampView: View) {
        ViewAnimator
            .animate(lampView)
                .alpha(0.3f)
                .duration(100)
                .repeatCount(ViewAnimator.INFINITE)
            .start()
    }

    fun flipPlayingCard(
        flipView: EasyFlipView,
        cardsLeftText: AppCompatTextView,
        isBadCard: Boolean,
        doNotShowMessage: Boolean = false,
        fIsBadCard: () -> Unit,
        fSetOnClickListeners: () -> Unit,
        fNotifyMessage: (message: String) -> Unit
    ) {
        cardsLeftText.apply {
            scaleX = 1.3f
            scaleY = 1.3f
        }

        ViewAnimator
            .animate(flipView)
                .translationX(60f)
                .duration(100)
                .onStop {
                    flipView.flipTheView()
                    if (!isBadCard) fSetOnClickListeners.invoke() else fIsBadCard.invoke()
                    if (!Constants.isOngoingGame && !doNotShowMessage && !Constants.justShotAtGoalie) fNotifyMessage.invoke("Please choose a position.")
                    if (Constants.justShotAtGoalie) Constants.justShotAtGoalie = false
                }
            .thenAnimate(cardsLeftText)
                .scale(1.3f, 1.0f)
                .duration(350)
            .start()
    }
}
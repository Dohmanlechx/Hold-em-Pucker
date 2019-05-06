package com.dohman.holdempucker.util

import android.view.View
import android.view.animation.AnticipateInterpolator
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

    fun animateFlipPlayingCard(
        flipView: EasyFlipView,
        cardsLeftText: AppCompatTextView,
        doNotShowMessage: Boolean = false,
        fOnFlipPlayingCardEnd: () -> Unit,
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
                    if (!Constants.isOngoingGame
                        && !doNotShowMessage
                        && !Constants.justShotAtGoalie) fNotifyMessage.invoke("Please choose a position.")

                    fOnFlipPlayingCardEnd.invoke()
                }
            .thenAnimate(cardsLeftText)
                .scale(1.3f, 1.0f)
                .duration(350)
            .start()
    }

    fun animateBadCard(
        flipView: EasyFlipView,
        screenWidth: Int,
        fRemoveAllOnClickListeners: () -> Unit,
        fOnBadCardEnd: () -> Unit
    ) {
        ViewAnimator
            .animate(flipView)
                .translationX(screenWidth.toFloat())
                .startDelay(750)
                .duration(750)
                .interpolator(AnticipateInterpolator(1f))
                .onStart { fRemoveAllOnClickListeners.invoke() }
                .onStop { fOnBadCardEnd.invoke() }
            .start()
    }
}
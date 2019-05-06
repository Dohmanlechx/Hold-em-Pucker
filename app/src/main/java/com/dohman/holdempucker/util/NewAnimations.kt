package com.dohman.holdempucker.util

import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.dohman.holdempucker.util.Constants.Companion.possibleMovesIndexes
import com.github.florent37.viewanimator.ViewAnimator
import com.wajahatkarim3.easyflipview.EasyFlipView

object NewAnimations {

    private val listOfPulseAnimations = mutableListOf<ViewAnimator>()

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
                        && !Constants.justShotAtGoalie
                    ) fNotifyMessage.invoke("Please choose a position.")

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

    fun animatePulsingCards(viewsToPulse: List<AppCompatImageView>, fNotifyMessage: (message: String) -> Unit) {

        val plural = if (possibleMovesIndexes.size == 1) "move" else "moves"
        val numberToText = when (possibleMovesIndexes.size) {
            1 -> "One"
            2 -> "Two"
            3 -> "Three"
            else -> "${possibleMovesIndexes.size}"
        }
        fNotifyMessage.invoke("$numberToText possible $plural. Go Attack!")

        viewsToPulse.forEach {
            listOfPulseAnimations.add(
                ViewAnimator
                    .animate(it)
                        .pulse()
                        .duration(620)
                        .repeatCount(ViewAnimator.INFINITE)
                        .onStop { it.apply {
                            scaleX = 1f
                            scaleY = 1f
                            }
                        }
                    .start()
            )
        }
    }

    fun stopAllPulsingCards() = listOfPulseAnimations.let {
        it.forEach { anim -> anim.cancel() }
        it.clear()
    }
}

















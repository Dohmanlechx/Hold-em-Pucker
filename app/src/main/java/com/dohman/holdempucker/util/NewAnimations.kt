package com.dohman.holdempucker.util

import android.view.View
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.dohman.holdempucker.util.Constants.Companion.possibleMovesIndexes
import com.github.florent37.viewanimator.ViewAnimator

object NewAnimations {

    private val listOfPulseAnimations = mutableListOf<ViewAnimator>()

    /*
    * Animation functions
    * */

    fun animatePuck(puck: View, team: String) {
        val distance = if (team.toLowerCase() == "bottom") 100f else -100f

        ViewAnimator
            .animate(puck)
                .translationY(distance)
                .duration(300)
                .interpolator(OvershootInterpolator(2.0f))
            .start()
    }

    fun animateLamp(lampView: View) {
        ViewAnimator
            .animate(lampView)
                .alpha(0.3f)
                .duration(100)
                .repeatCount(ViewAnimator.INFINITE)
            .start()
    }

    fun animateFlipPlayingCard(
        flipView: View,
        cardsLeftText: View,
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
        flipView: View,
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

    fun animatePulsingCards(viewsToPulse: List<View>, fNotifyMessage: (message: String) -> Unit) {

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
                        .bounce()
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

    fun animateAddPlayer(attacker: View, target: View, fOnAddPlayerEnd: () -> Unit) {
        ViewAnimator
            .animate(attacker)
                .translationX(target.x + 60f - attacker.x)
                .translationY(target.y - attacker.y)
                .duration(400)
                .interpolator(LinearOutSlowInInterpolator())
                .onStop { fOnAddPlayerEnd.invoke() }
            .start()
    }

    fun animateAttackPlayer(attacker: View, target: View, screenWidth: Int, fOnAttackPlayerEnd: () -> Unit) {
        target.bringToFront()
        attacker.bringToFront()

        ViewAnimator
            .animate(attacker)
                .translationX(target.x - attacker.x - 20f)
                .translationY(target.y - attacker.y + 20f)
                .duration(500)
                .interpolator(LinearOutSlowInInterpolator())
            .thenAnimate(attacker, target)
                .translationX(screenWidth.toFloat())
                .duration(500)
                .interpolator(AnticipateInterpolator(1.5f))
                .onStop { fOnAttackPlayerEnd.invoke() }
            .start()
    }
}

















package com.dohman.holdempucker.util

import android.view.View
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.dohman.holdempucker.cards.Card
import com.dohman.holdempucker.util.Constants.Companion.possibleMovesIndexes
import com.dohman.holdempucker.util.Constants.Companion.whoseTurn
import com.dohman.holdempucker.util.Constants.WhoseTurn.Companion.isBotMoving
import com.github.florent37.viewanimator.ViewAnimator
import com.wajahatkarim3.easyflipview.EasyFlipView

object Animations {

    private val listOfAllAnimations = mutableListOf<ViewAnimator>()
    private val listOfPulseAnimations = mutableListOf<ViewAnimator>()

    /*
    * Animations removal
    * */

    fun stopAllAnimations() = listOfAllAnimations.let {
        it.forEach { anim -> anim.cancel() }
        it.clear()
    }

    fun stopAllPulsingCards() = listOfPulseAnimations.let {
        it.forEach { anim -> anim.cancel() }
        it.clear()
    }

    /*
    * Animation functions
    * */

    fun animatePuck(puck: View, team: String) {
        val vector = if (team.toLowerCase() == "bottom") 100f else -100f

        listOfAllAnimations.add(
        ViewAnimator
            .animate(puck)
                .translationY(vector)
                .duration(300)
                .interpolator(OvershootInterpolator(2.0f))
            .start()
        )
    }

    fun animateComputerText(textView: View) {
        listOfAllAnimations.add(
        ViewAnimator
            .animate(textView)
                .newsPaper()
                .duration(100)
            .start()
        )
    }

    fun animateLamp(lampView: View) {
        listOfAllAnimations.add(
        ViewAnimator
            .animate(lampView)
                .alpha(0.3f)
                .duration(100)
                .repeatCount(ViewAnimator.INFINITE)
            .start()
        )
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

        listOfAllAnimations.add(
        ViewAnimator
            .animate(flipView)
                .translationX(60f)
                .duration(100)
                .onStart {
                    if (Constants.isRestoringPlayers
                        && !doNotShowMessage
                        && !Constants.isJustShotAtGoalie
                    ) fNotifyMessage.invoke("Please choose a position.")
                }
                .onStop { fOnFlipPlayingCardEnd.invoke() }
            .thenAnimate(cardsLeftText)
                .scale(1.3f, 1.0f)
                .duration(350)
            .start()
        )
    }

    fun animateBadCard(
        flipView: View,
        screenWidth: Int,
        fRemoveAllOnClickListeners: () -> Unit,
        fOnBadCardEnd: () -> Unit
    ) {
        listOfAllAnimations.add(
        ViewAnimator
            .animate(flipView)
                .translationX(screenWidth.toFloat())
                .startDelay(750)
                .duration(750)
                .interpolator(AnticipateInterpolator(1f))
                .onStart { fRemoveAllOnClickListeners.invoke() }
                .onStop { fOnBadCardEnd.invoke() }
            .start()
        )
    }

    fun animatePulsingCards(viewsToPulse: List<View>, fNotifyMessage: (message: String) -> Unit) {
        fNotifyMessage.invoke(Util.pulseCardsText(possibleMovesIndexes.size))

        if (isBotMoving()) return

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

    fun animateAddPlayer(attacker: View, target: View, fOnAddPlayerEnd: () -> Unit) {
        listOfAllAnimations.add(
        ViewAnimator
            .animate(attacker)
                .translationX(target.x + 60f - attacker.x)
                .translationY(target.y - attacker.y)
                .startDelay(Util.getDelay())
                .duration(400)
                .interpolator(LinearOutSlowInInterpolator())
                .onStop { fOnAddPlayerEnd.invoke() }
            .start()
        )
    }

    fun animateAddGoalie(flipView: View, goalie: View, xForAttacker: Float, delay: Long, fOnAddGoalieEnd: () -> Unit) {
        listOfAllAnimations.add(
        ViewAnimator
            .animate(flipView)
                .translationX(xForAttacker - flipView.x)
                .translationY(goalie.midHeight() - flipView.midHeight())
                .rotation(90f)
                .interpolator(LinearOutSlowInInterpolator())
                .startDelay(delay)
                .duration(500)
                .onStop { fOnAddGoalieEnd.invoke() }
            .start()
        )
    }

    fun animateAttackPlayer(attacker: View, target: View, screenWidth: Int, fOnAttackPlayerEnd: () -> Unit) {
        target.bringToFront()
        attacker.bringToFront()

        listOfAllAnimations.add(
        ViewAnimator
            .animate(attacker)
                .translationX(target.x - attacker.x - 20f)
                .translationY(target.y - attacker.y + 20f)
                .startDelay(Util.getDelay())
                .duration(500)
                .interpolator(LinearOutSlowInInterpolator())
            .thenAnimate(attacker, target)
                .translationX(screenWidth.toFloat())
                .duration(500)
                .interpolator(AnticipateInterpolator(1.0f))
                .onStop { fOnAttackPlayerEnd.invoke() }
            .start()
        )
    }

    fun animateGoalieSaved(
        fadingScreen: View,
        attacker: View,
        goalie: EasyFlipView,
        screenWidth: Int,
        xForAttacker: Float,
        goalieCard: Card?,
        fNotifyMessage: (message: String) -> Unit,
        fOnGoalieSavedEnd: () -> Unit
        ) {

        fadingScreen.visibility = View.VISIBLE
        fadingScreen.bringToFront()
        attacker.bringToFront()
        goalie.bringToFront()

        val vector = when (whoseTurn) {
            Constants.WhoseTurn.BOTTOM -> goalie.bottomYWithOffset() - attacker.y
            else -> goalie.y - attacker.bottomYWithOffset()
        }

        listOfAllAnimations.add(
        ViewAnimator
            .animate(fadingScreen)
                .alpha(0.0f, 0.3f)
            .andAnimate(attacker)
                .translationX(xForAttacker + 60f - attacker.x)
                .translationY(vector)
                .startDelay(1500)
                .duration(1500)
                .interpolator(LinearOutSlowInInterpolator())
            .thenAnimate(goalie)
                .tada()
                .startDelay(1000)
                .duration(500)
                .onStop { goalie.flipTheView() }
            .thenAnimate(attacker)
                .swing()
                .startDelay(1000)
                .duration(500)
            .thenAnimate(fadingScreen)
                .alpha(0.3f, 0.0f)
                .duration(1000)
                .onStart { fNotifyMessage.invoke("...\nof rank ${Util.rankToWord(goalieCard?.rank)} and the goalie SAVED!") }
            .thenAnimate(attacker, goalie)
                .translationX(screenWidth.toFloat())
                .startDelay(500)
                .duration(500)
                .interpolator(AnticipateInterpolator(1.0f))
                .onStop { fOnGoalieSavedEnd.invoke() }
            .start()
        )
    }

    fun animateScoredAtGoalie(
        fadingScreen: View,
        attacker: View,
        goalie: EasyFlipView,
        screenWidth: Int,
        xForAttacker: Float,
        goalieCard: Card?,
        fNotifyMessage: (message: String) -> Unit,
        fOnGoalieSavedEnd: () -> Unit
    ) {
        fadingScreen.visibility = View.VISIBLE
        fadingScreen.bringToFront()
        attacker.bringToFront()
        goalie.bringToFront()

        val vector = when (whoseTurn) {
            Constants.WhoseTurn.BOTTOM -> goalie.bottomYWithOffset() - attacker.y
            else -> goalie.y - attacker.bottomYWithOffset()
        }

        listOfAllAnimations.add(
        ViewAnimator
            .animate(fadingScreen)
                .alpha(0.0f, 0.3f)
            .andAnimate(attacker)
                .translationX(xForAttacker + 60f - attacker.x)
                .translationY(vector)
                .startDelay(1500)
                .duration(1500)
                .interpolator(LinearOutSlowInInterpolator())
            .thenAnimate(goalie)
                .rubber()
                .startDelay(1000)
                .duration(500)
                .onStop { goalie.flipTheView() }
            .thenAnimate(attacker)
                .rotation(720f)
                .startDelay(1000)
                .duration(500)
            .thenAnimate(fadingScreen)
                .alpha(0.3f, 0.0f)
                .duration(1000)
                .onStart { fNotifyMessage.invoke("...\nof rank ${Util.rankToWord(goalieCard?.rank)} and it's GOAL!") }
            .thenAnimate(attacker, goalie)
                .translationX(screenWidth.toFloat())
                .startDelay(500)
                .duration(500)
                .interpolator(AnticipateInterpolator(1.0f))
                .onStop { fOnGoalieSavedEnd.invoke() }
            .start()
        )
    }


    /* Extensions */

    private fun View.bottomYWithOffset() = y + (height + 8f)
    private fun View.midHeight() = y + (height / 2).toFloat()
}
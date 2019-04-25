package com.dohman.holdempucker.util

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.util.Log
import android.view.View
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.dohman.holdempucker.cards.Card
import com.dohman.holdempucker.util.Constants.Companion.TAG_GAMEACTIVITY
import com.dohman.holdempucker.util.Constants.Companion.isAnimationRunning
import com.dohman.holdempucker.util.Constants.Companion.isOngoingGame
import com.dohman.holdempucker.util.Constants.Companion.possibleMovesIndexes
import com.dohman.holdempucker.util.Constants.Companion.restoringPlayers
import com.dohman.holdempucker.util.Constants.Companion.teamBottomScore
import com.dohman.holdempucker.util.Constants.Companion.teamBottomViews
import com.dohman.holdempucker.util.Constants.Companion.teamTopScore
import com.dohman.holdempucker.util.Constants.Companion.teamTopViews
import com.dohman.holdempucker.util.Constants.Companion.whoseTurn
import com.wajahatkarim3.easyflipview.EasyFlipView

object AnimationUtil {

    private val listOfOngoingAnimations = mutableListOf<ObjectAnimator>()

    fun flipView(v: EasyFlipView, isBadCard: Boolean, fIsBadCard: () -> Unit) {
        ObjectAnimator.ofFloat(v, View.TRANSLATION_X, 60f).apply {
            doOnStart {
                isAnimationRunning = true
            }
            doOnEnd {
                v.flipTheView()
                isAnimationRunning = false
                if (isBadCard) fIsBadCard.invoke()
            }
            duration = 100
            start()
        }
    }

    fun startPulsingCardsAnimation() {
        Log.d(TAG_GAMEACTIVITY, possibleMovesIndexes.toString())

        val teamToPulse = if (whoseTurn == Constants.WhoseTurn.BOTTOM) teamTopViews else teamBottomViews

        possibleMovesIndexes.forEach { view ->
            val pulseAnimation = ObjectAnimator.ofPropertyValuesHolder(
                teamToPulse[view],
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1.05f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.05f)
            ).apply {
                duration = 310
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.REVERSE

                doOnCancel {
                    teamToPulse[view].apply {
                        scaleX = 1f
                        scaleY = 1f
                    }
                }

                start()
            }

            listOfOngoingAnimations.add(pulseAnimation)
        }
    }

    fun stopAllPulsingCardAnimations() {
        listOfOngoingAnimations.forEach { it.cancel() }
        listOfOngoingAnimations.clear()
    }

    fun addGoalie(
        flipView: EasyFlipView,
        goalieView: View,
        flipViewOriginalX: Float,
        flipViewOriginalY: Float
    ): AnimatorSet {

        val aniX = ObjectAnimator.ofFloat(
            flipView,
            View.TRANSLATION_X,
            goalieView.x - flipViewOriginalX + ((goalieView.width / 2) - (goalieView.width / 2))
        )
        val aniY = ObjectAnimator.ofFloat(
            flipView,
            View.TRANSLATION_Y,
            goalieView.y - flipViewOriginalY - (goalieView.height / 4)
        )
        val aniRot = ObjectAnimator.ofFloat(flipView, View.ROTATION, 90f)

        return AnimatorSet().apply {
            isAnimationRunning = true
            startDelay = 300
            playTogether(aniX, aniY, aniRot)
            interpolator = LinearOutSlowInInterpolator()
            duration = 700
        }
    }

    fun addPlayer(flipView: EasyFlipView, targetView: AppCompatImageView): AnimatorSet {
        val aniX = ObjectAnimator.ofFloat(flipView, View.TRANSLATION_X, targetView.x - flipView.x + 60f)
        val aniY = ObjectAnimator.ofFloat(flipView, View.TRANSLATION_Y, targetView.y - flipView.y)

        return AnimatorSet().apply {
            isAnimationRunning = true
            playTogether(aniX, aniY)
            interpolator = LinearOutSlowInInterpolator()
            duration = 500
        }
    }

    fun attack(flipView: EasyFlipView, targetView: AppCompatImageView, isAttacking: Boolean): AnimatorSet {
        if (isAttacking) {
            val flipAniX = ObjectAnimator.ofFloat(flipView, View.TRANSLATION_X, targetView.x - flipView.x - 30f)
            val flipAniY = ObjectAnimator.ofFloat(flipView, View.TRANSLATION_Y, targetView.y - flipView.y + 30f)

            return AnimatorSet().apply {
                isAnimationRunning = true
                playTogether(flipAniX, flipAniY)
                interpolator = LinearOutSlowInInterpolator()
                duration = 500
            }
        } else {
            val flipOutAni = ObjectAnimator.ofFloat(flipView, View.TRANSLATION_X, 2000f)
            val victimOutAni = ObjectAnimator.ofFloat(targetView, View.TRANSLATION_X, 2000f)

            return AnimatorSet().apply {
                isAnimationRunning = true
                playTogether(flipOutAni, victimOutAni)
                interpolator = AnticipateInterpolator(1.5f)
                duration = 500
            }
        }
    }

    fun goalieSaved() {
        // FIXME glöm ej stopAllPulsing
    }

    fun scoredAtGoalie(
        flipView: EasyFlipView,
        targetView: EasyFlipView,
        fNotifyToggleTurn: () -> Unit,
        fRemoveCardFromDeck: () -> Unit,
        fRestoreFlipViews: () -> Unit,
        fAddNewGoalie: () -> Unit,
        fUpdateScores: () -> Unit
    ): AnimatorSet {
        stopAllPulsingCardAnimations()

        // Attacker
        val flipAniX = ObjectAnimator.ofFloat(flipView, View.TRANSLATION_X, targetView.x - flipView.x - 150f)
        val flipAniY =
            if (whoseTurn == Constants.WhoseTurn.BOTTOM) ObjectAnimator.ofFloat(flipView, View.TRANSLATION_Y, targetView.y - flipView.y)
            else ObjectAnimator.ofFloat(flipView, View.TRANSLATION_Y, targetView.y - flipView.y - (targetView.height / 2))

        return AnimatorSet().apply {
            isAnimationRunning = true
            playTogether(flipAniX, flipAniY)
            interpolator = LinearOutSlowInInterpolator()
            duration = 1000

            doOnEnd {
                // Victim goalie
                targetView.flipTheView()
                // Attacker
                ObjectAnimator.ofFloat(flipView, View.ROTATION, 720f).apply {
                    startDelay = 1000
                    duration = 500
                    // FIXME testa med x animation, men med ObjectAnimator.REVERSE som repeat

                    doOnEnd {
                        // Both
                        val attackerOutAni = ObjectAnimator.ofFloat(flipView, View.TRANSLATION_X, 2000f)
                        val victimOutAni = ObjectAnimator.ofFloat(targetView, View.TRANSLATION_X, 2000f)

                        AnimatorSet().apply {
                            startDelay = 500
                            playTogether(attackerOutAni, victimOutAni)
                            interpolator = AnticipateInterpolator(1.5f)
                            duration = 500
                            start()

                            doOnEnd {
                                isAnimationRunning = false
                                targetView.visibility = View.GONE

                                isOngoingGame = false
                                restoringPlayers = true

                                if (whoseTurn == Constants.WhoseTurn.BOTTOM) teamBottomScore++
                                else teamTopScore++

                                fUpdateScores.invoke()
                                fNotifyToggleTurn.invoke()
                                fRestoreFlipViews.invoke()
                                fAddNewGoalie.invoke()
                                fRemoveCardFromDeck.invoke()
                            }
                        }
                    }

                    start()
                }
            }
        }
    }

    fun badCardOut(
        flipView: EasyFlipView,
        fGetFirstCardInDeck: () -> Card,
        fToggleTurn: () -> Unit,
        fRestoreFlipView: () -> Unit,
        fRemoveCardFromDeck: () -> Unit,
        fIsThisTeamReady: () -> Boolean,
        fTriggerBadCard: () -> Unit
    ): ObjectAnimator? {
        return ObjectAnimator.ofFloat(flipView, View.TRANSLATION_X, 2000f).apply {
            interpolator = AnticipateInterpolator(1.25f)
            startDelay = 750
            duration = 750
            doOnStart { isAnimationRunning = true }
            doOnEnd {
                fToggleTurn.invoke()
                fRestoreFlipView.invoke()
                isAnimationRunning = false
                fRemoveCardFromDeck.invoke()

                if (!fIsThisTeamReady.invoke()) {
                    isOngoingGame = false
                    restoringPlayers = true
                }

                if (isOngoingGame && !GameLogic.isTherePossibleMove(
                        whoseTurn,
                        fGetFirstCardInDeck.invoke()
                    )
                )
                    fTriggerBadCard.invoke()
                else if (isOngoingGame && GameLogic.isTherePossibleMove(
                        whoseTurn,
                        fGetFirstCardInDeck.invoke()
                    )
                )
                    startPulsingCardsAnimation()
            }
        }
    }

    fun togglePuck(puck: AppCompatImageView, team: String): ObjectAnimator? {
        return ObjectAnimator.ofFloat(puck, View.TRANSLATION_Y, if (team.toLowerCase() == "bottom") 100f else -100f)
            .apply {
                duration = 300
                interpolator = OvershootInterpolator(2.5f)
            }
    }
}
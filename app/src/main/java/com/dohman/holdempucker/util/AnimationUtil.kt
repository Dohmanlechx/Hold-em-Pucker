package com.dohman.holdempucker.util

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import android.view.animation.TranslateAnimation
import androidx.appcompat.widget.AppCompatImageView
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
import com.dohman.holdempucker.util.Constants.Companion.teamTopScore
import com.dohman.holdempucker.util.Constants.Companion.whoseTurn
import com.wajahatkarim3.easyflipview.EasyFlipView

object AnimationUtil {

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

    fun scoredAtGoalie(
        flipView: EasyFlipView,
        targetView: EasyFlipView,
        fNotifyToggleTurn: () -> Unit,
        fRemoveCardFromDeck: () -> Unit,
        fShowPickedCard: () -> Unit,
        fRestoreFlipViews: () -> Unit,
        fAddNewGoalie: () -> Unit,
        fUpdateScores: () -> Unit
    ): AnimatorSet {
        // Attacker
        val flipAniX = ObjectAnimator.ofFloat(flipView, View.TRANSLATION_X, targetView.x - flipView.x - 120f)
        val flipAniY = ObjectAnimator.ofFloat(flipView, View.TRANSLATION_Y, targetView.y - flipView.y)

        return AnimatorSet().apply {
            isAnimationRunning = true
            playTogether(flipAniX, flipAniY)
            interpolator = LinearOutSlowInInterpolator()
            duration = 500

            doOnEnd {
                // Victim goalie
                targetView.flipTheView()
                // Attacker
                TranslateAnimation(-5f, 5f, 0f, 0f).apply {
                    startDelay = 1500
                    duration = 20
                    repeatMode = Animation.REVERSE
                    repeatCount = 10

                    setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {}
                        override fun onAnimationRepeat(animation: Animation?) {}
                        override fun onAnimationEnd(animation: Animation?) {
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
                                    targetView.visibility = View.GONE

                                    isOngoingGame = false
                                    restoringPlayers = true

                                    if (whoseTurn == Constants.WhoseTurn.BOTTOM) teamBottomScore++
                                    else teamTopScore++

                                    fNotifyToggleTurn.invoke()
                                    fRemoveCardFromDeck.invoke()
                                    fShowPickedCard.invoke()
                                    fRestoreFlipViews.invoke()
                                    fAddNewGoalie.invoke()
                                    fUpdateScores.invoke()
                                }
                            }
                        }
                    })

                    flipView.startAnimation(this)
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

    fun fadeIn(targetView: AppCompatImageView): ObjectAnimator? {
        return ObjectAnimator.ofFloat(targetView, View.ALPHA, 0f, 1f).apply {
            duration = 200
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
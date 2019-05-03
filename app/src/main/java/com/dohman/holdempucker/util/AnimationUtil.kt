package com.dohman.holdempucker.util

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.util.Log
import android.util.Property
import android.view.View
import android.view.animation.AnticipateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.dohman.holdempucker.cards.Card
import com.dohman.holdempucker.util.Constants.Companion.TAG_GAMEACTIVITY
import com.dohman.holdempucker.util.Constants.Companion.isAnimationRunning
import com.dohman.holdempucker.util.Constants.Companion.isOngoingGame
import com.dohman.holdempucker.util.Constants.Companion.justShotAtGoalie
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

    /*
    * Template/private functions
    * */

    private fun objAnimator(
        view: View,
        direction: Property<View, Float>,
        distance: Float
    ): ObjectAnimator = ObjectAnimator.ofFloat(view, direction, distance)

    private fun fadeAnimator(
        view: View,
        from: Float,
        to: Float
    ): ObjectAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, from, to)

    private fun scaleAnimator(
        view: View,
        scaleToX: Float,
        scaleToY: Float
    ): ObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(
        view,
        PropertyValuesHolder.ofFloat(View.SCALE_X, scaleToX),
        PropertyValuesHolder.ofFloat(View.SCALE_Y, scaleToY)
    )

    private fun onGoalieActionEnd(view: View, isGoal: Boolean = false) {
        view.visibility = View.GONE
        isOngoingGame = false
        restoringPlayers = true

        if (isGoal) {
            if (whoseTurn == Constants.WhoseTurn.BOTTOM) teamBottomScore++ else teamTopScore++
        }

        isAnimationRunning = false
    }

    /*
    * Animation functions
    * */

    fun startLampAnimation(v: View) {
        fadeAnimator(v, 1.0f, 0.3f).apply {
            duration = 100
            repeatCount = ObjectAnimator.INFINITE

            start()
        }
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
        cardsLeftText.scaleX = 1.3f
        cardsLeftText.scaleY = 1.3f

        objAnimator(flipView, View.TRANSLATION_X, 60f).apply {
            doOnStart {
                isAnimationRunning = true
                scaleAnimator(cardsLeftText, 1f, 1f).apply {
                    duration = 350
                    start()
                }
            }
            doOnEnd {
                flipView.flipTheView()
                if (isBadCard) fIsBadCard.invoke()
                if (!isOngoingGame && !doNotShowMessage && !justShotAtGoalie) fNotifyMessage.invoke("Please\nchoose a\nposition\nto add\nyour card.")
                if (justShotAtGoalie) justShotAtGoalie = false
                fSetOnClickListeners.invoke()
                isAnimationRunning = false
            }

            duration = 100
            start()
        }
    }

    fun startPulsingCardsAnimation(fNotifyMessage: (message: String) -> Unit) {

        Log.d(TAG_GAMEACTIVITY, possibleMovesIndexes.toString())

        val teamToPulse = if (whoseTurn == Constants.WhoseTurn.BOTTOM) teamTopViews else teamBottomViews

        val plural = if (possibleMovesIndexes.size == 1) "move" else "moves"
        fNotifyMessage.invoke("${possibleMovesIndexes.size} possible\n$plural.\nGo Attack!")

        possibleMovesIndexes.forEach { view ->
            listOfOngoingAnimations.add(scaleAnimator(
                teamToPulse[view],
                1.05f,
                1.05f
            ).apply {
                doOnCancel {
                    teamToPulse[view].apply {
                        scaleX = 1f
                        scaleY = 1f
                    }
                }

                duration = 310
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.REVERSE
                start()
            })
        }
    }

    fun stopAllPulsingCardAnimations() = listOfOngoingAnimations.let {
        it.forEach { anim -> anim.cancel() }
        it.clear()
    }

    fun addGoalieAnimation(
        flipView: EasyFlipView,
        goalieView: View,
        flipViewOriginalX: Float,
        flipViewOriginalY: Float
    ): AnimatorSet {

        val aniX = objAnimator(
            flipView,
            View.TRANSLATION_X,
            goalieView.x - flipViewOriginalX + ((goalieView.width / 2) - (goalieView.width / 2))
        )
        val aniY = objAnimator(
            flipView,
            View.TRANSLATION_Y,
            goalieView.y - flipViewOriginalY - (goalieView.height / 4)
        )
        val aniRot = objAnimator(flipView, View.ROTATION, 90f)

        return AnimatorSet().apply {
            isAnimationRunning = true
            interpolator = LinearOutSlowInInterpolator()
            startDelay = 150
            duration = 500
            playTogether(aniX, aniY, aniRot)
        }
    }

    fun addPlayerAnimation(
        flipView: EasyFlipView,
        targetView: AppCompatImageView
    ): AnimatorSet {
        isAnimationRunning = true

        val aniX = objAnimator(flipView, View.TRANSLATION_X, targetView.x - flipView.x + 60f)
        //val aniX = ObjectAnimator.ofFloat(flipView, View.TRANSLATION_X, flipView.x, targetView.x - 60f)
        val aniY = objAnimator(flipView, View.TRANSLATION_Y, targetView.y - flipView.y)
        //val aniY = ObjectAnimator.ofFloat(flipView, View.TRANSLATION_Y, flipView.y, targetView.y)

        return AnimatorSet().apply {
            interpolator = LinearOutSlowInInterpolator()
            duration = 400
            playTogether(aniX, aniY)
        }
    }

    fun attackAnimation(flipView: EasyFlipView, targetView: AppCompatImageView, isAttacking: Boolean): AnimatorSet {
        if (isAttacking) {
            val flipAniX = objAnimator(flipView, View.TRANSLATION_X, targetView.x - flipView.x - 30f)
            val flipAniY = objAnimator(flipView, View.TRANSLATION_Y, targetView.y - flipView.y + 30f)

            return AnimatorSet().apply {
                isAnimationRunning = true
                interpolator = LinearOutSlowInInterpolator()
                duration = 500
                playTogether(flipAniX, flipAniY)
            }
        } else {
            val flipOutAni = objAnimator(flipView, View.TRANSLATION_X, 2000f)
            val victimOutAni = objAnimator(targetView, View.TRANSLATION_X, 2000f)

            return AnimatorSet().apply {
                isAnimationRunning = true
                interpolator = AnticipateInterpolator(1.5f)
                duration = 500
                playTogether(flipOutAni, victimOutAni)
            }
        }
    }

    fun goalieSavedAnimation(
        flipView: EasyFlipView,
        targetView: EasyFlipView,
        tempGoalieCard: Card?,
        victimTeam: Array<Card?>,
        fNotifyToggleTurn: () -> Unit,
        fRestoreFlipViews: () -> Unit,
        fAddNewGoalie: () -> Unit,
        fNotifyMessage: (message: String) -> Unit
    ): AnimatorSet {
        // Attacker
        val flipAniX = objAnimator(flipView, View.TRANSLATION_X, targetView.x - flipView.x - 150f)
        val flipAniY =
            if (whoseTurn == Constants.WhoseTurn.BOTTOM) objAnimator(
                flipView,
                View.TRANSLATION_Y,
                targetView.y - flipView.y
            )
            else objAnimator(
                flipView,
                View.TRANSLATION_Y,
                targetView.y - flipView.y - (targetView.height / 2)
            )

        return AnimatorSet().apply {
            isAnimationRunning = true
            interpolator = LinearOutSlowInInterpolator()
            startDelay = 1500
            duration = 1000
            playTogether(flipAniX, flipAniY)

            doOnEnd {
                // Victim goalie
                targetView.flipTheView()
                // Attacker
                objAnimator(flipView, View.SCALE_X, 0.5f).apply {
                    startDelay = 1000
                    duration = 500

                    doOnStart {
                        val rankInterpreted = when (tempGoalieCard?.rank) {
                            11 -> "Jack"
                            12 -> "Queen"
                            13 -> "King"
                            14 -> "Ace"
                            else -> tempGoalieCard?.rank.toString()
                        }
                        fNotifyMessage.invoke("... of\nrank $rankInterpreted\nand the\ngoalie\nSAVED!")
                    }
                    doOnEnd {
                        // Both
                        val attackerOutAni = objAnimator(flipView, View.TRANSLATION_X, 2000f)
                        val victimOutAni = objAnimator(targetView, View.TRANSLATION_X, 2000f)

                        AnimatorSet().apply {
                            doOnEnd {
                                onGoalieActionEnd(targetView, isGoal = false)
                                victimTeam[5] = null

                                fNotifyToggleTurn.invoke()
                                fRestoreFlipViews.invoke()
                                fAddNewGoalie.invoke()
                            }

                            startDelay = 500
                            interpolator = AnticipateInterpolator(1.5f)
                            duration = 500
                            playTogether(attackerOutAni, victimOutAni)

                            start()
                        }
                    }

                    start()
                }
            }
        }





//        val flipAniX = objAnimator(flipView, View.TRANSLATION_X, targetView.x - flipView.x - 150f)
//        val flipAniY =
//            if (whoseTurn == Constants.WhoseTurn.BOTTOM) objAnimator(
//                flipView,
//                View.TRANSLATION_Y,
//                targetView.y - flipView.y
//            )
//            else objAnimator(
//                flipView,
//                View.TRANSLATION_Y,
//                targetView.y - flipView.y - (targetView.height / 2)
//            )
//
//        return AnimatorSet().apply {
//            isAnimationRunning = true
//            interpolator = LinearOutSlowInInterpolator()
//            startDelay = 1500
//            duration = 1000
//            playTogether(flipAniX, flipAniY)
//
//            doOnEnd {
//                // Victim goalie
//                targetView.flipTheView()
//                // Attacker
//                val jumpAni = objAnimator(flipView, View.Y, flipView.y - 100f).apply {
//                    startDelay = 1000
//                    duration = 300
//                    interpolator = LinearInterpolator()
//                }
//                val bounceAni = objAnimator(flipView, View.Y, flipView.y).apply {
//                    duration = 300
//                    interpolator = BounceInterpolator()
//                }
//
//                AnimatorSet().apply {
//                    playSequentially(jumpAni, bounceAni)
//
//                    doOnStart {
//                        val rankInterpreted = when (tempGoalieCard?.rank) {
//                            11 -> "Jack"
//                            12 -> "Queen"
//                            13 -> "King"
//                            14 -> "Ace"
//                            else -> tempGoalieCard?.rank.toString()
//                        }
//                        fNotifyMessage.invoke("... of\nrank $rankInterpreted\nand the\ngoalie\nSAVED!")
//                    }
//                    doOnEnd {
//                        // Both
//                        val attackerOutAni = objAnimator(flipView, View.TRANSLATION_X, 2000f)
//                        val victimOutAni = objAnimator(targetView, View.TRANSLATION_X, 2000f)
//
//                        AnimatorSet().apply {
//                            doOnEnd {
//                                onGoalieActionEnd(targetView, isGoal = false)
//                                victimTeam[5] = null
//
//                                fNotifyToggleTurn.invoke()
//                                fRestoreFlipViews.invoke()
//                                fAddNewGoalie.invoke()
//                            }
//
//                            startDelay = 500
//                            interpolator = AnticipateInterpolator(1.5f)
//                            duration = 500
//                            playTogether(attackerOutAni, victimOutAni)
//
//                            start()
//                        }
//                    }
//
//                    start()
//                }
//            }
//
//            start()
//        }
    }

    fun scoredAtGoalieAnimation(
        flipView: EasyFlipView,
        targetView: EasyFlipView,
        tempGoalieCard: Card?,
        fNotifyToggleTurn: () -> Unit,
        fRestoreFlipViews: () -> Unit,
        fAddNewGoalie: () -> Unit,
        fUpdateScores: () -> Unit,
        fNotifyMessage: (message: String) -> Unit
    ): AnimatorSet {
        // Attacker
        val flipAniX = objAnimator(flipView, View.TRANSLATION_X, targetView.x - flipView.x - 150f)
        val flipAniY =
            if (whoseTurn == Constants.WhoseTurn.BOTTOM) objAnimator(
                flipView,
                View.TRANSLATION_Y,
                targetView.y - flipView.y
            )
            else objAnimator(
                flipView,
                View.TRANSLATION_Y,
                targetView.y - flipView.y - (targetView.height / 2)
            )

        return AnimatorSet().apply {
            isAnimationRunning = true
            interpolator = LinearOutSlowInInterpolator()
            startDelay = 1500
            duration = 1000
            playTogether(flipAniX, flipAniY)

            doOnEnd {
                // Victim goalie
                targetView.flipTheView()
                // Attacker
                objAnimator(flipView, View.ROTATION, 720f).apply {
                    startDelay = 1000
                    duration = 500

                    doOnStart {
                        val rankInterpreted = when (tempGoalieCard?.rank) {
                            11 -> "Jack"
                            12 -> "Queen"
                            13 -> "King"
                            14 -> "Ace"
                            else -> tempGoalieCard?.rank.toString()
                        }
                        fNotifyMessage.invoke("... of\nrank $rankInterpreted\nand it's\nGOAL!")
                    }
                    doOnEnd {
                        // Both
                        val attackerOutAni = objAnimator(flipView, View.TRANSLATION_X, 2000f)
                        val victimOutAni = objAnimator(targetView, View.TRANSLATION_X, 2000f)

                        AnimatorSet().apply {
                            doOnEnd {
                                onGoalieActionEnd(targetView, isGoal = true)

                                fUpdateScores.invoke()
                                fNotifyToggleTurn.invoke()
                                fRestoreFlipViews.invoke()
                                fAddNewGoalie.invoke()
                            }

                            startDelay = 500
                            interpolator = AnticipateInterpolator(1.5f)
                            duration = 500
                            playTogether(attackerOutAni, victimOutAni)

                            start()
                        }
                    }

                    start()
                }
            }
        }
    }

    fun badCardOutAnimation(
        flipView: EasyFlipView,
        fGetFirstCardInDeck: () -> Card,
        fToggleTurn: () -> Unit,
        fRestoreFlipView: () -> Unit,
        fRemoveCardFromDeck: () -> Unit,
        fIsThisTeamReady: () -> Boolean,
        fTriggerBadCard: () -> Unit,
        fNotifyMessage: (message: String) -> Unit
    ): ObjectAnimator? {
        return objAnimator(flipView, View.TRANSLATION_X, 2000f).apply {
            doOnStart { isAnimationRunning = true }
            doOnEnd {
                fToggleTurn.invoke()
                fRestoreFlipView.invoke()
                fRemoveCardFromDeck.invoke()

                if (!fIsThisTeamReady.invoke()) {
                    isOngoingGame = false
                    restoringPlayers = true
                }

                if (isOngoingGame && !GameLogic.isTherePossibleMove(
                        whoseTurn,
                        fGetFirstCardInDeck.invoke()
                    )
                ) fTriggerBadCard.invoke()
                else if (isOngoingGame && GameLogic.isTherePossibleMove(
                        whoseTurn,
                        fGetFirstCardInDeck.invoke()
                    )
                ) startPulsingCardsAnimation { message -> fNotifyMessage.invoke(message) }

                isAnimationRunning = false
            }

            startDelay = 1250
            duration = 750
            interpolator = AnticipateInterpolator(1f)
        }
    }

    fun togglePuckAnimation(puck: AppCompatImageView, team: String): ObjectAnimator? {
        return objAnimator(puck, View.TRANSLATION_Y, if (team.toLowerCase() == "bottom") 100f else -100f)
            .apply {
                duration = 300
                interpolator = OvershootInterpolator(2.5f)
            }
    }
}
package com.dohman.holdempucker.util

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.dohman.holdempucker.GameActivity.Companion.isAnimationRunning
import com.wajahatkarim3.easyflipview.EasyFlipView

object AnimationUtil {

    fun flipView(v: EasyFlipView) {
        ObjectAnimator.ofFloat(v, View.TRANSLATION_X, 60f).apply {
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                    isAnimationRunning = true
                }

                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationEnd(animation: Animator?) {
                    v.flipTheView()
                    isAnimationRunning = false
                }
            })
            duration = 100
            start()
        }
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

        val set = AnimatorSet().apply {
            playTogether(aniX, aniY, aniRot)
            interpolator = LinearOutSlowInInterpolator()
            duration = 700
        }

        isAnimationRunning = true

        return set
    }

    fun addPlayer(flipView: EasyFlipView, targetView: AppCompatImageView): AnimatorSet {
        val aniX = ObjectAnimator.ofFloat(flipView, View.TRANSLATION_X, targetView.x - flipView.x + 60f)
        val aniY = ObjectAnimator.ofFloat(flipView, View.TRANSLATION_Y, targetView.y - flipView.y)

        val set = AnimatorSet().apply {
            playTogether(aniX, aniY)
            interpolator = LinearOutSlowInInterpolator()
            duration = 500
        }

        isAnimationRunning = true

        return set
    }

    fun attack(flipView: EasyFlipView, targetView: AppCompatImageView, isAttacker: Boolean): AnimatorSet {
        if (isAttacker) {
            val flipAniX = ObjectAnimator.ofFloat(flipView, View.TRANSLATION_X, targetView.x - flipView.x - 30f)
            val flipAniY = ObjectAnimator.ofFloat(flipView, View.TRANSLATION_Y, targetView.y - flipView.y + 30f)

            val set = AnimatorSet().apply {
                playTogether(flipAniX, flipAniY)
                interpolator = LinearOutSlowInInterpolator()
                duration = 500
            }

            isAnimationRunning = true

            return set

        } else {
            val flipOutAni = ObjectAnimator.ofFloat(flipView, View.TRANSLATION_X, 2000f)
            val victimOutAni = ObjectAnimator.ofFloat(targetView, View.TRANSLATION_X, 2000f)

            val set = AnimatorSet().apply {
                playTogether(flipOutAni, victimOutAni)
                interpolator = AnticipateInterpolator(1.5f)
                duration = 500
            }

            isAnimationRunning = true

            return set
        }
    }

    fun fadeIn(targetView: AppCompatImageView): ObjectAnimator? {
        return ObjectAnimator.ofFloat(targetView, View.ALPHA, 0f, 1f).apply {
            duration = 200
        }
    }

    fun togglePuck(puck: AppCompatImageView, team: String): ObjectAnimator? {
        return ObjectAnimator.ofFloat(puck, View.TRANSLATION_Y, if (team.toLowerCase() == "bottom") 100f else -100f).apply {
            duration = 300
            interpolator = OvershootInterpolator(2.5f)
        }
    }
}
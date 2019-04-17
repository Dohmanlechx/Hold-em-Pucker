package com.dohman.holdempucker.util

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
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

    fun addGoalie(flipView: EasyFlipView,
                  goalieView: View,
                  flipViewOriginalX: Float,
                  flipViewOriginalY: Float): AnimatorSet {

        val set = AnimatorSet()
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

        set.playTogether(aniX, aniY, aniRot)
        set.interpolator = LinearOutSlowInInterpolator()
        set.duration = 700
        isAnimationRunning = true

        return set
    }

}
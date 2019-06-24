package com.dohman.holdempucker.util

import android.content.Context
import android.os.Build
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import com.dohman.holdempucker.util.Constants.WhoseTurn.Companion.isBotMoving

object Util {

    fun rankToWord(rank: Int?): String = when (rank) {
        11 -> "Jack"
        12 -> "Queen"
        13 -> "King"
        14 -> "Ace"
        else -> rank.toString()
    }

    fun pulseCardsText(number: Int): String {
        val plural = if (number == 1) "move" else "moves"
        val numberToWord = when (number) {
            1 -> "One"
            2 -> "Two"
            3 -> "Three"
            else -> "$number"
        }

        return "$numberToWord possible $plural. Go Attack!"
    }

    fun getDelay(): Long {
        return if (isBotMoving()) {
            listOf<Long>(1000, 900, 800, 700, 600, 500).random()
        } else {
            0
        }
    }

    fun vibrate(context: Context, isPositive: Boolean) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect =
                    if (isPositive) VibrationEffect.createWaveform(longArrayOf(100, 100, 100), -1)
                    else VibrationEffect.createOneShot(20, -1)
                vibrator.vibrate(effect)
            } else {
                val duration = if (isPositive) 100L else 20L
                vibrator.vibrate(duration)
            }
        }
    }

    fun getOnlineInputTimer(fUpdateTheTimerText: (Long) -> Unit, fTimerReachedTheEnd: () -> Unit): CountDownTimer {
        return object : CountDownTimer(21000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                fUpdateTheTimerText.invoke(millisUntilFinished)
            }

            override fun onFinish() {
                fTimerReachedTheEnd.invoke()
            }
        }
    }
}
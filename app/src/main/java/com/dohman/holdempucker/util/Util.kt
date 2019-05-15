package com.dohman.holdempucker.util

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
            listOf<Long>(1000, 800, 600, 400).random()
        } else {
            0
        }
    }
}
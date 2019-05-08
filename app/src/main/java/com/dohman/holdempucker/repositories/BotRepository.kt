package com.dohman.holdempucker.repositories

import com.dohman.holdempucker.util.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BotRepository @Inject constructor(
    // Placeholder for other Repositories.
) {

    fun triggerMove(mode: Enum<Constants.GameMode>, possibleMoves: List<Int>): Int {
        return when (mode) {
            Constants.GameMode.RANDOM -> possibleMoves.random()
            else -> { -1 }
        }
    }
}
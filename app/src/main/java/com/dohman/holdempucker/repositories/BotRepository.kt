package com.dohman.holdempucker.repositories

import com.dohman.holdempucker.cards.Card
import com.dohman.holdempucker.util.Constants
import com.dohman.holdempucker.util.Constants.Companion.isRestoringPlayers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BotRepository @Inject constructor(
    // Placeholder for other Repositories.
) {

    fun getMoveIndex(mode: Enum<Constants.GameMode>, possibleMoves: List<Int>, playingCard: Card): Int {
        return when (mode) {
            Constants.GameMode.RANDOM -> if (possibleMoves.isNotEmpty()) possibleMoves.random() else -1
            Constants.GameMode.DEVELOPER -> getSmartMove(possibleMoves, playingCard)
            else -> {
                -1
            }
        }
    }

    private fun getSmartMove(possibleMoves: List<Int>, playingCard: Card): Int {
        return if (isRestoringPlayers) {
            // Add player
            if (playingCard.rank!! >= 10) {
                when {
                    possibleMoves.contains(1) -> 1
                    possibleMoves.contains(3) -> 3
                    possibleMoves.contains(4) -> 4
                    else -> possibleMoves.random()
                }
            } else {
                // Weaker than rank 10
                when {
                    possibleMoves.contains(0) -> 0
                    possibleMoves.contains(2) -> 2
                    else -> possibleMoves.random()
                }
            }
        } else {
            // Attack player
            possibleMoves.random()
        }
    }
}
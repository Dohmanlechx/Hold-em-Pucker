package com.dohman.holdempucker.repositories

import com.dohman.holdempucker.cards.Card
import com.dohman.holdempucker.util.Constants
import com.dohman.holdempucker.util.Constants.Companion.isRestoringPlayers
import com.dohman.holdempucker.util.Constants.Companion.teamBottom
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
            else -> -1
        }
    }

    private fun getSmartMove(possibleMoves: List<Int>, playingCard: Card): Int {
        if (possibleMoves.isEmpty()) return -1

        if (isRestoringPlayers) {
            // Add player
            return if (playingCard.beats(10)) {
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
            if (possibleMoves.contains(1)) {
                // First, check if center is alive. Then, check if it is equal to 10 or stronger. If yes, attack it.
                if (teamBottom[1]?.beats(10)!!) {
                    return if (playingCard.beats(teamBottom[1]?.rank!!)) 1
                    else attackTheStrongest(possibleMoves)
                }
            } else if (possibleMoves.contains(5)) {
                // Second, check if you can attack the goalie and if the playing card is equal to 10 or stronger. If yes, attack goalie.
                return if (playingCard.beats(10)) 5
                else attackTheStrongest(possibleMoves)
            } else {
                // Else, attack the strongest available card.
                return attackTheStrongest(possibleMoves)
            }
        }

        return possibleMoves.random()
    }

    private fun attackTheStrongest(possibleMoves: List<Int>): Int {
        // By this time, center is less than rank 10, but attack it anyway if it is alive.
        if (possibleMoves.contains(1)) return 1

        var strongestRank: Int = teamBottom[possibleMoves.first()]?.rank!!
        var index: Int = possibleMoves.first()

        possibleMoves.forEach {
            if (teamBottom[it]?.rank!! > strongestRank) {
                strongestRank = teamBottom[it]?.rank!!
                index = it
            }
        }

        return index
    }

    private fun Card.beats(value: Int): Boolean {
        return rank!! >= value
    }
}
package com.dohman.holdempucker.repositories

import com.dohman.holdempucker.models.Card
import com.dohman.holdempucker.util.Constants
import com.dohman.holdempucker.util.Constants.Companion.PLAYER_CENTER
import com.dohman.holdempucker.util.Constants.Companion.PLAYER_DEFENDER_LEFT
import com.dohman.holdempucker.util.Constants.Companion.PLAYER_DEFENDER_RIGHT
import com.dohman.holdempucker.util.Constants.Companion.PLAYER_FORWARD_LEFT
import com.dohman.holdempucker.util.Constants.Companion.PLAYER_FORWARD_RIGHT
import com.dohman.holdempucker.util.Constants.Companion.PLAYER_GOALIE
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
                    possibleMoves.contains(PLAYER_CENTER) -> PLAYER_CENTER
                    possibleMoves.contains(PLAYER_DEFENDER_LEFT) -> PLAYER_DEFENDER_LEFT
                    possibleMoves.contains(PLAYER_DEFENDER_RIGHT) -> PLAYER_DEFENDER_RIGHT
                    else -> possibleMoves.random()
                }
            } else {
                // Weaker than rank 10
                when {
                    possibleMoves.contains(PLAYER_FORWARD_LEFT) -> PLAYER_FORWARD_LEFT
                    possibleMoves.contains(PLAYER_FORWARD_RIGHT) -> PLAYER_FORWARD_RIGHT
                    else -> possibleMoves.random()
                }
            }
        } else {
            // Attack player
            if (possibleMoves.contains(PLAYER_CENTER)) {
                // First, check if center is alive. Then, check if it is equal to 10 or stronger. If yes, attack it.
                if (teamBottom[PLAYER_CENTER]?.beats(8)!!) {
                    return if (playingCard.beats(teamBottom[PLAYER_CENTER]?.rank!!)) PLAYER_CENTER
                    else attackTheStrongest(possibleMoves)
                }
            } else if (possibleMoves.contains(PLAYER_GOALIE)) {
                // Second, check if you can attack the goalie and if the playing card is equal to 10 or stronger. If yes, attack goalie.
                return if (playingCard.beats(10)) PLAYER_GOALIE
                else attackTheStrongest(possibleMoves)
            } else if (possibleMoves.contains(PLAYER_DEFENDER_LEFT)
                && teamBottom[PLAYER_DEFENDER_LEFT]?.beats(8)!!
            ) {
                // Check if any defender is free to attack, then check if its rank is equal to 8 or stronger.
                return PLAYER_DEFENDER_LEFT
            } else if (possibleMoves.contains(PLAYER_DEFENDER_RIGHT)
                && teamBottom[PLAYER_DEFENDER_RIGHT]?.beats(8)!!
            ) {
                return PLAYER_DEFENDER_RIGHT
            } else {
                // Else, attack the strongest available card.
                return attackTheStrongest(possibleMoves)
            }
        }

        // When none of the cases above suits, this runs
        return attackTheStrongest(possibleMoves)
    }

    private fun attackTheStrongest(possibleMoves: List<Int>): Int {
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
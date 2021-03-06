package com.dohman.holdempucker.util

import com.dohman.holdempucker.models.Card
import com.dohman.holdempucker.util.Constants.Companion.PLAYER_DEFENDER_LEFT
import com.dohman.holdempucker.util.Constants.Companion.PLAYER_DEFENDER_RIGHT
import com.dohman.holdempucker.util.Constants.Companion.PLAYER_GOALIE
import com.dohman.holdempucker.util.Constants.Companion.cases
import com.dohman.holdempucker.util.Constants.Companion.possibleMovesIndexes
import com.dohman.holdempucker.util.Constants.Companion.teamGreen
import com.dohman.holdempucker.util.Constants.Companion.teamPurple
import com.dohman.holdempucker.util.Constants.WhoseTurn.Companion.isTeamGreenTurn

object GameLogic {

    fun isGoalieThereOrAdd(goalieCard: Card): Boolean {
        val team = if (isTeamGreenTurn()) teamGreen else teamPurple

        team.let { if (it[PLAYER_GOALIE] != null) return true else it[PLAYER_GOALIE] = goalieCard }

        return false // But goalie is added now
    }

    fun isAttacked(currentCard: Card, victimTeam: Array<Card?>, spotIndex: Int): Boolean {
        victimTeam.let {
            if (currentCard.rank ?: 0 >= it[spotIndex]?.rank ?: 0) {
                it[spotIndex] = null
                return true
            }
        }

        return false
    }

    fun areEnoughForwardsDead(victimTeam: Array<Card?>, defenderPos: Int): Boolean {
        when (defenderPos) {
            PLAYER_DEFENDER_LEFT -> {
                for (i in 0..1) {
                    if (victimTeam[i] != null) return false
                }
            }
            PLAYER_DEFENDER_RIGHT -> {
                for (i in 1..2) {
                    if (victimTeam[i] != null) return false
                }
            }
        }

        return true
    }

    fun isAtLeastOneDefenderDead(victimTeam: Array<Card?>): Boolean {
        for (i in 3..4) {
            if (victimTeam[i] == null) return true
        }

        return false
    }

    fun isTherePossibleMove(currentCard: Card): Boolean {
        val victimTeam =
            if (isTeamGreenTurn()) teamPurple else teamGreen

        val currentCase = arrayListOf<Int>()
        victimTeam.forEachIndexed { index, card ->
            // Looking after survivors
            if (card != null) currentCase.add(index)
        }

        var result = false
        possibleMovesIndexes.clear()

        cases.forEachIndexed { index, caseElement ->
            if (caseElement == currentCase) {
                when (index) {
                    0 -> { // x xx xxx
                        for (i in 0..2) if (currentCard.rank!! >= victimTeam[i]?.rank!!) {
                            possibleMovesIndexes.add(i)
                            result = true
                        }
                    }

                    1 -> { // x xx xx-
                        for (i in 1..2) if (currentCard.rank!! >= victimTeam[i]?.rank!!) {
                            possibleMovesIndexes.add(i)
                            result = true
                        }
                    }

                    2 -> { // x xx x-x
                        for (i in 0..2 step 2) if (currentCard.rank!! >= victimTeam[i]?.rank!!) {
                            possibleMovesIndexes.add(i)
                            result = true
                        }
                    }

                    3 -> { // x xx -xx
                        for (i in 0..1) if (currentCard.rank!! >= victimTeam[i]?.rank!!) {
                            possibleMovesIndexes.add(i)
                            result = true
                        }
                    }

                    4 -> { // x xx --x
                        for (i in 0..4 step 4) if (currentCard.rank!! >= victimTeam[i]?.rank!!) {
                            possibleMovesIndexes.add(i)
                            result = true
                        }
                    }

                    5 -> { // x xx -x-
                        if (currentCard.rank!! >= victimTeam[1]?.rank!!) {
                            possibleMovesIndexes.add(1)
                            result = true
                        }
                    }

                    6 -> { // x xx x--
                        for (i in 2..3) if (currentCard.rank!! >= victimTeam[i]?.rank!!) {
                            possibleMovesIndexes.add(i)
                            result = true
                        }
                    }

                    7 -> { // x xx ---
                        for (i in 3..4) if (currentCard.rank!! >= victimTeam[i]?.rank!!) {
                            possibleMovesIndexes.add(i)
                            result = true
                        }
                    }
                    8 -> { // x -x ---
                        possibleMovesIndexes.add(5)

                        if (currentCard.rank!! >= victimTeam[3]?.rank!!) possibleMovesIndexes.add(3)

                        result = true
                    }
                    9 -> { // x x- ---
                        possibleMovesIndexes.add(5)

                        if (currentCard.rank!! >= victimTeam[4]?.rank!!) possibleMovesIndexes.add(4)

                        result = true
                    }
                    10 -> { // x -x --x
                        possibleMovesIndexes.add(5)

                        if (currentCard.rank!! >= victimTeam[0]?.rank!!) possibleMovesIndexes.add(0)

                        result = true
                    }
                    11 -> { // x x- x--
                        possibleMovesIndexes.add(5)

                        if (currentCard.rank!! >= victimTeam[2]?.rank!!) possibleMovesIndexes.add(2)

                        result = true
                    }
                    else -> {
                        // Only goalie is left
                        possibleMovesIndexes.add(5)
                        return true
                    }
                }
            }
        }

        return result
    }
}
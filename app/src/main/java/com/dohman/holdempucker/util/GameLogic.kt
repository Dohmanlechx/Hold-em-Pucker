package com.dohman.holdempucker.util

import com.dohman.holdempucker.cards.Card
import com.dohman.holdempucker.util.Constants.Companion.cases
import com.dohman.holdempucker.util.Constants.Companion.possibleMovesIndexes
import com.dohman.holdempucker.util.Constants.Companion.teamBottom
import com.dohman.holdempucker.util.Constants.Companion.teamTop
import com.dohman.holdempucker.util.Constants.Companion.whoseTurn

object GameLogic {

    fun isGoalieThereOrAdd(goalieCard: Card): Boolean {
        val team =
            if (whoseTurn == Constants.WhoseTurn.BOTTOM) teamBottom else teamTop

        team.let { if (it[5] != null) return true else it[5] = goalieCard }

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
            3 -> {
                for (i in 0..1) {
                    if (victimTeam[i] != null) return false
                }
            }
            4 -> {
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

    fun isTherePossibleMove(whoseTurn: Enum<Constants.WhoseTurn>, currentCard: Card): Boolean {
        val victimTeam =
            if (whoseTurn == Constants.WhoseTurn.BOTTOM) teamTop else teamBottom

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
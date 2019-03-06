package com.dohman.holdempucker.util

import com.dohman.holdempucker.GameActivity
import com.dohman.holdempucker.cards.Card
import com.dohman.holdempucker.util.GameLogic.Cases.Companion.cases

object GameLogic {

    fun isGoalieThere(goalieCard: Card): Boolean {
        val team =
            if (GameActivity.whoseTurn == GameActivity.WhoseTurn.BOTTOM) GameActivity.teamBottom else GameActivity.teamTop
        //team.let { if (!it.all { element -> element == null }) return true else it[5] = goalieCard }
        team.let { if (it[5] != null) return true else it[5] = goalieCard }

        return false // But goalie is added now
    }

    fun attack(currentCard: Card, victimTeam: Array<Card?>, spotIndex: Int): Boolean {
        victimTeam.let {
            if (currentCard.rank ?: 0 >= it[spotIndex]?.rank ?: 0) {
                it[spotIndex] = null
                return true
            }
        }

        return false
    }

    fun areEnoughForwardsOut(victimTeam: Array<Card?>, defenderPos: Int): Boolean {
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

    fun isAtLeastOneDefenderOut(victimTeam: Array<Card?>): Boolean {
        for (i in 3..4) {
            if (victimTeam[i] == null) return true
        }

        return false
    }

    fun isTherePossibleMove(whoseTurn: Enum<GameActivity.WhoseTurn>, currentCard: Card): Boolean {
        val victimTeam =
            if (whoseTurn == GameActivity.WhoseTurn.BOTTOM) GameActivity.teamTop else GameActivity.teamBottom

        val currentCase = arrayListOf<Int>()
        victimTeam.forEachIndexed { index, card ->
            // Looking after survivors
            if (card != null) currentCase.add(index)
        }

        cases.forEachIndexed { index, case ->
            if (case == currentCase) {
                when (index) {
                    0 -> { // x xx xxx
                        for (i in 0..2) if (currentCard.rank!! >= victimTeam[i]?.rank!!) return true
                    }

                    1 -> { // x xx xx-
                        for (i in 1..2) if (currentCard.rank!! >= victimTeam[i]?.rank!!) return true
                    }

                    2 -> { // x xx x-x
                        for (i in 0..2 step 2) if (currentCard.rank!! >= victimTeam[i]?.rank!!) return true
                    }

                    3 -> { // x xx -xx
                        for (i in 0..1) if (currentCard.rank!! >= victimTeam[i]?.rank!!) return true
                    }

                    4 -> { // x xx --x
                        for (i in 0..4 step 4) if (currentCard.rank!! >= victimTeam[i]?.rank!!) return true
                    }

                    5 -> { // x xx -x-
                        if (currentCard.rank!! >= victimTeam[1]?.rank!!) return true
                    }

                    6 -> { // x xx x--
                        for (i in 2..3) if (currentCard.rank!! >= victimTeam[i]?.rank!!) return true
                    }

                    7 -> { // x xx ---
                        for (i in 3..4) if (currentCard.rank!! >= victimTeam[i]?.rank!!) return true
                    }
                    else -> { // An alone defender
                        return true
                    }
                }
            }
        }

        return false
    }

    class Cases {
        companion object {
            val cases = mutableListOf<List<Int>>().apply {
                add(0, listOf(0, 1, 2, 3, 4, 5)) // ( 0, 1, 2)
                add(1, listOf(1, 2, 3, 4, 5)) // (1, 2, 3)
                add(2, listOf(0, 2, 3, 4, 5)) // (0, 2)
                add(3, listOf(0, 1, 3, 4, 5)) // (0, 1)
                add(4, listOf(0, 3, 4, 5)) // (0, 4)
                add(5, listOf(1, 3, 4, 5)) // (1)
                add(6, listOf(2, 3, 4, 5)) // (2, 3)
                add(7, listOf(3, 4, 5)) // (3, 4)
                // Below cases are not being checked, but needed so the attacker
                // can continue playing his turn.
                add(8, listOf(3, 5))
                add(9, listOf(4, 5))
                add(10, listOf(0, 3, 5))
                add(11, listOf(2, 4, 5))
                add(12, listOf(5))
            }
        }
    }
}
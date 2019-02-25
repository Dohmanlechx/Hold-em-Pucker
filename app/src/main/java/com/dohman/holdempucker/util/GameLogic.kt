package com.dohman.holdempucker.util

import com.dohman.holdempucker.GameActivity
import com.dohman.holdempucker.cards.Card
import com.dohman.holdempucker.util.GameLogic.Cases.Companion.cases

object GameLogic {

    fun isGoalieThere(goalieCard: Card): Boolean {
        val team =
            if (GameActivity.whoseTurn == GameActivity.WhoseTurn.BOTTOM) GameActivity.teamBottom else GameActivity.teamTop
        team.let { if (!it.all { element -> element == null }) return true else it[5] = goalieCard }

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

    fun isTherePossibleMove(whoseTurn: Enum<GameActivity.WhoseTurn>): Boolean {
        val victimTeam =
            if (whoseTurn == GameActivity.WhoseTurn.BOTTOM) GameActivity.teamTop else GameActivity.teamBottom

        val thisCase = arrayListOf<Int>()
        //val survivorsInTeam = arrayOfNulls<Card>(6) // FIXME Not needed?
        victimTeam.forEachIndexed { index, card -> // Looking after survivors
            if (card != null) thisCase.add(index)
            //survivorsInTeam[index] = card // FIXME Not needed?
        }

        thisCase.removeAt(thisCase.last()) // Removing goalie, not needed for cases

//        thisCase.let { thisCase -> // FIXME
//            cases.forEach {
//                if (it == thisCase) }
//        }

        return true
    }

    class Cases {
        companion object {
            val cases = mutableListOf<List<Int>>().apply {
                add(0, listOf(0, 1, 2, 3, 4)) // ( 0, 1, 2)
                add(1, listOf(1, 2, 3, 4)) // (1, 2, 3)
                add(2, listOf(0, 2, 3, 4)) // (0, 2)
                add(3, listOf(0, 1, 3, 4)) // (0, 1)
                add(4, listOf(0, 3, 4)) // (0, 4)
                add(5, listOf(1, 3, 4)) // (1)
                add(6, listOf(2, 3, 4)) // (2, 4)
                add(7, listOf(3, 4)) // (3, 4)
                add(8, listOf(4)) // (4) // FIXME This case and 9 probably not needed, since it is free to shoot at the goalie
                add(9, listOf(3)) // (3)
            }
        }
    }
}
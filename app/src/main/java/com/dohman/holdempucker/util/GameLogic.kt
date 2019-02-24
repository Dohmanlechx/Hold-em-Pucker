package com.dohman.holdempucker.util

import com.dohman.holdempucker.GameActivity
import com.dohman.holdempucker.cards.Card

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
}
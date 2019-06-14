package com.dohman.holdempucker.util

import com.dohman.holdempucker.models.Card

class Constants {
    companion object {

        // Tags
        const val TAG_MAINACTIVITY = "DBG: MainActivity.kt"
        const val TAG_GAMEACTIVITY = "DBG: GameFragment.kt"
        const val TAG_GAMEVIEWMODEL = "DBG: GameViewModel.kt"

        // Game Mode
        var currentGameMode = GameMode.NONE

        // Online
        var lobbyId = ""
        var isOpponentFound = false
        var isMyOnlineTeamGreen = false
        fun isOnlineMode() = currentGameMode == GameMode.ONLINE
        fun isNotOnlineMode() = currentGameMode != GameMode.ONLINE

        // Booleans
        var isVsBotMode = false
        var isOngoingGame = false // Set to true when all cards are laid out
        var isShootingAtGoalie = false // To prevent duplicate message
        var isRestoringPlayers = true // Set to true when a team need to lay out new cards to fulfill
        var areTeamsReadyToStartPeriod = false // Set to true as soon as both teams are full in the very beginning

        // Objects
        val teamPurple = arrayOfNulls<Card>(6)
        val teamGreen = arrayOfNulls<Card>(6)

        /*  Index 0 = Left forward | 1 = Center | 2 = Right forward
                        3 = Left defender | 4 = Right defender
                                    5 = Goalie                          */

        // Player Indexes
        const val PLAYER_FORWARD_LEFT = 0
        const val PLAYER_CENTER = 1
        const val PLAYER_FORWARD_RIGHT = 2
        const val PLAYER_DEFENDER_LEFT = 3
        const val PLAYER_DEFENDER_RIGHT = 4
        const val PLAYER_GOALIE = 5

        // Integers
        var period = 1
        var teamTopScore = 0
        var teamBottomScore = 0

        // Lists
        var possibleMovesIndexes = mutableListOf<Int>() // For the pulse animations and AI moves

        // Whose turn
        var whoseTurn = WhoseTurn.GREEN
        var whoseTeamStartedLastPeriod = whoseTurn

        // Cases
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

        fun resetBooleansToInitState() {
            isOngoingGame = false
            isShootingAtGoalie = false
            isRestoringPlayers = true
            areTeamsReadyToStartPeriod = false
        }
    }

    // Enums
    enum class WhoseTurn {
        GREEN, PURPLE;

        companion object {
            fun toggleTurn() {
                whoseTurn = if (whoseTurn == GREEN) PURPLE else GREEN
            }

            fun isBotMoving() = isTeamPurpleTurn() && isVsBotMode
            fun isOpponentMoving() = (isOnlineMode() && isTeamPurpleTurn() && isMyOnlineTeamGreen) || (isOnlineMode() && isTeamGreenTurn() && !isMyOnlineTeamGreen)
            fun isTeamGreenTurn() = whoseTurn == GREEN
            fun isTeamPurpleTurn() = whoseTurn == PURPLE
        }
    }

    enum class GameMode {
        NONE, RANDOM, DEVELOPER, FRIEND, ONLINE
    }
}
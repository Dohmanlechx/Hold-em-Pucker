package com.dohman.holdempucker.ui.fragments

import android.util.Log
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dohman.holdempucker.R
import com.dohman.holdempucker.cards.Card
import com.dohman.holdempucker.dagger.RepositoryComponent
import com.dohman.holdempucker.repositories.BotRepository
import com.dohman.holdempucker.repositories.CardRepository
import com.dohman.holdempucker.repositories.ResourceRepository
import com.dohman.holdempucker.util.Constants
import com.dohman.holdempucker.util.Constants.Companion.TAG_GAMEVIEWMODEL
import com.dohman.holdempucker.util.Constants.Companion.areTeamsReadyToStartPeriod
import com.dohman.holdempucker.util.Constants.Companion.currentGameMode
import com.dohman.holdempucker.util.Constants.Companion.isOngoingGame
import com.dohman.holdempucker.util.Constants.Companion.period
import com.dohman.holdempucker.util.Constants.Companion.isRestoringPlayers
import com.dohman.holdempucker.util.Constants.Companion.teamBottom
import com.dohman.holdempucker.util.Constants.Companion.teamBottomScore
import com.dohman.holdempucker.util.Constants.Companion.teamTop
import com.dohman.holdempucker.util.Constants.Companion.teamTopScore
import com.dohman.holdempucker.util.Constants.Companion.isVsBot
import com.dohman.holdempucker.util.Constants.Companion.whoseTeamStartedLastPeriod
import com.dohman.holdempucker.util.Constants.Companion.whoseTurn
import com.dohman.holdempucker.util.Constants.WhoseTurn.Companion.isTeamBottomTurn
import com.dohman.holdempucker.util.GameLogic
import com.dohman.holdempucker.util.Util
import javax.inject.Inject

class GameViewModel : ViewModel() {
    @Inject
    lateinit var appRepo: ResourceRepository
    @Inject
    lateinit var cardRepo: CardRepository
    @Inject
    lateinit var botRepo: BotRepository

    var cardDeck = mutableListOf<Card>()
    var firstCardInDeck: Card

    val messageNotifier = MutableLiveData<Pair<String, Boolean>>()
    val halfTimeNotifier = MutableLiveData<Int>()
    val whoseTurnNotifier = MutableLiveData<String>()
    val pickedCardNotifier = MutableLiveData<Int>()
    val cardsCountNotifier = MutableLiveData<Int>()
    val badCardNotifier = MutableLiveData<Boolean>()

    init {
        RepositoryComponent.inject(this)

        cardDeck = cardRepo.createCards() as MutableList<Card>
        firstCardInDeck = cardDeck.first()
    }

    /*
    * General functions
    * */

    fun getScreenWidth() = appRepo.getScreenWidth()

    fun setGameMode() {
        when (currentGameMode) {
            Constants.GameMode.RANDOM -> {
                Log.d(TAG_GAMEVIEWMODEL, "Game Mode: $currentGameMode")
                isVsBot = true
            }
            Constants.GameMode.DEVELOPER -> {
                Log.d(TAG_GAMEVIEWMODEL, "Game Mode: $currentGameMode")
                isVsBot = true
            }
            Constants.GameMode.FRIEND -> {
                Log.d(TAG_GAMEVIEWMODEL, "Game Mode: $currentGameMode")
                isVsBot = false
            }
            else -> {
                Log.d(TAG_GAMEVIEWMODEL, "Game Mode: $currentGameMode")
            }
        }
    }

    /*
    * Notify functions
    * */

    fun notifyPickedCard() {
        pickedCardNotifier.value = resIdOfCard(firstCardInDeck)
    }

    fun notifyToggleTurn() {
        Constants.WhoseTurn.toggleTurn()
        whoseTurnNotifier.value = whoseTurn.name
    }

    fun notifyMessage(message: String, isNeutralMessage: Boolean = false) {
        messageNotifier.value = Pair(message, isNeutralMessage)
    }

    /*
    * Card deck functions
    * */

    fun removeCardFromDeck(doNotNotify: Boolean = false): Boolean {
        // True = Game can continue
        cardDeck.remove(firstCardInDeck)
        return if (cardDeck.isEmpty()) {
            triggerHalfTime()
            false
        } else {
            firstCardInDeck = cardDeck.first()
            if (!doNotNotify) notifyPickedCard()
            cardsCountNotifier.value = cardDeck.size
            true
        }
    }

    private fun areThereEnoughCards(team: Array<Card?>): Boolean {
        val amountOfCardsToFill = team.filter { it == null }.size
        if ((amountOfCardsToFill + 4) > cardDeck.size) { // 4 is the minimum amount to score an goal
            triggerHalfTime()
            return false
        }

        return true
    }

    /*
    * Image/view functions
    * */

    fun resIdOfCard(card: Card?): Int {
        return card.let {
            appRepo.context.resources.getIdentifier(
                it?.src, "drawable", appRepo.context.packageName
            )
        }
    }

    fun notifyMessageAttackingGoalie() {
        firstCardInDeck.let {
            notifyMessage(
                "${it.suit.toString().toLowerCase().capitalize()} ${Util.rankToWord(it.rank)} attacks the goalie\n..."
            )
        }
    }

    /*
    * Game management functions
    * */

    fun gameManager(
        doNotToggleTurn: Boolean = false,
        fPrepareViewsToPulse: (() -> Unit)? = null
    ) {
        if ((!doNotToggleTurn && !isRestoringPlayers) || !areTeamsReadyToStartPeriod) notifyToggleTurn()

        if (!areTeamsReadyToStartPeriod) {
            areTeamsReady()
        } else {
            if (isThisTeamReady() && !isOngoingGame) {
                isOngoingGame = true
            }
        }

        if (!isGoalieThereOrAdd(firstCardInDeck)) { // If returned false, goalie is added
            gameManager()
            return
        }

        if (isOngoingGame && !GameLogic.isTherePossibleMove(firstCardInDeck)) triggerBadCard()
        else if (isOngoingGame && GameLogic.isTherePossibleMove(firstCardInDeck)
        ) fPrepareViewsToPulse?.invoke()
    }

    fun isNextPeriodReady(): Boolean {
        if (period > 3 && (teamBottomScore != teamTopScore)) {
            val message = when {
                teamBottomScore > teamTopScore -> "Team Bottom won with $teamBottomScore-$teamTopScore!\nNew game? Press anywhere!"
                else -> "Team Top won with $teamTopScore-$teamBottomScore!\nNew game? Press anywhere!"
            }
            notifyMessage(message, isNeutralMessage = true)

            return false
        } else {
            if (period > 3) notifyMessage(
                "Overtime! Play until all cards are out.\nPeriod: $period",
                isNeutralMessage = true
            )

            whoseTurn =
                if (whoseTeamStartedLastPeriod == Constants.WhoseTurn.BOTTOM) Constants.WhoseTurn.TOP else Constants.WhoseTurn.BOTTOM

            whoseTeamStartedLastPeriod = whoseTurn

            return true
        }
    }

    fun triggerBadCard() {
        badCardNotifier.value = true
    }

    fun addGoalToScore() {
        if (isTeamBottomTurn()) teamBottomScore++ else teamTopScore++
    }

    fun triggerHalfTime() {
        cardDeck = cardRepo.createCards() as MutableList<Card>
        firstCardInDeck = cardDeck.first()

        for (index in 0..5) {
            teamBottom[index] = null
            teamTop[index] = null
        }

        halfTimeNotifier.value = 1
        if (period <= 3) notifyMessage("Not enough cards. Period $period started.", isNeutralMessage = true)
        isOngoingGame = false
        areTeamsReadyToStartPeriod = false
    }

    /*
    * Teams functions
    * */

    private fun isGoalieThereOrAdd(goalieCard: Card): Boolean {
        if (GameLogic.isGoalieThereOrAdd(goalieCard)) return true

//        val doNotNotify = teamTop[5] == null || teamBottom[5] == null
//        removeCardFromDeck(doNotNotify = true)

        return false // But goalie is added now
    }

    private fun setPlayerInTeam(team: Array<Card?>, spotIndex: Int, fPrepareViewsToPulse: () -> Unit) {
        team[spotIndex] = firstCardInDeck
        if (removeCardFromDeck()) gameManager(false, fPrepareViewsToPulse)
    }

    private fun areTeamsReady(): Boolean {
        teamBottom.forEach { if (it == null) return false }
        teamTop.forEach { if (it == null) return false }

        isOngoingGame = true
        areTeamsReadyToStartPeriod = true
        isRestoringPlayers = false
        return true
    }

    fun isThisTeamReady(): Boolean {
        val teamToCheck =
            if (isTeamBottomTurn()) teamBottom else teamTop

        teamToCheck.forEach { if (it == null) return false }

        isRestoringPlayers = false
        return true
    }

    fun areEnoughForwardsOut(victimTeam: Array<Card?>, defenderPos: Int): Boolean {
        return GameLogic.areEnoughForwardsDead(victimTeam, defenderPos)
    }

    fun isAtLeastOneDefenderOut(victimTeam: Array<Card?>): Boolean {
        return GameLogic.isAtLeastOneDefenderDead(victimTeam)
    }

    /*
    * Player actions
    * */

    fun canAddPlayerView(view: AppCompatImageView, team: Array<Card?>, spotIndex: Int): Boolean {
        if (cardDeck.size <= 8 && !areThereEnoughCards(team)) return false
        if ((view.tag == Integer.valueOf(android.R.color.transparent) && isOngoingGame) || team[spotIndex] != null) return false
        return true
    }

    fun canAttack(
        victimTeam: Array<Card?>,
        spotIndex: Int,
        victimView: AppCompatImageView
    ): Boolean {
        if (victimView.tag == Integer.valueOf(android.R.color.transparent)) return false
        if (GameLogic.isAttacked(firstCardInDeck, victimTeam, spotIndex)) return true

        return false
    }

    /*
    * Bot actions
    * */

    fun botChooseEmptySpot(possibleMoves: List<Int>, fTriggerBotMove: (Int) -> Unit) {
        fTriggerBotMove.invoke(botRepo.getMoveIndex(currentGameMode, possibleMoves, firstCardInDeck))
    }

    fun botChooseIndexToAttack(indexes: List<Int>): Int =
        botRepo.getMoveIndex(currentGameMode, indexes, firstCardInDeck)

    /*
    * On animation ends
    * */

    fun onGoalieAddedAnimationEnd(view: AppCompatImageView) {
        view.setImageResource(R.drawable.red_back)
        view.tag = Integer.valueOf(R.drawable.red_back)
    }

    fun onPlayerAddedAnimationEnd(
        view: AppCompatImageView,
        team: Array<Card?>,
        spotIndex: Int,
        fPrepareViewsToPulse: () -> Unit
    ) {
        view.setImageResource(resIdOfCard(firstCardInDeck))
        view.tag = null
        setPlayerInTeam(team, spotIndex, fPrepareViewsToPulse)
    }

    fun onAttackedAnimationEnd(view: AppCompatImageView, fPrepareViewsToPulse: () -> Unit) {
        view.setImageResource(android.R.color.transparent)
        view.tag = Integer.valueOf(android.R.color.transparent)
        removeCardFromDeck()
        gameManager(true, fPrepareViewsToPulse)
    }
}

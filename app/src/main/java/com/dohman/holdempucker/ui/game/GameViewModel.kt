package com.dohman.holdempucker.ui.game

import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.dohman.holdempucker.R
import com.dohman.holdempucker.dagger.RepositoryComponent
import com.dohman.holdempucker.models.Card
import com.dohman.holdempucker.repositories.BotRepository
import com.dohman.holdempucker.repositories.CardRepository
import com.dohman.holdempucker.repositories.OnlinePlayRepository
import com.dohman.holdempucker.repositories.ResourceRepository
import com.dohman.holdempucker.util.Animations
import com.dohman.holdempucker.util.Constants
import com.dohman.holdempucker.util.Constants.Companion.PLAYER_CENTER
import com.dohman.holdempucker.util.Constants.Companion.PLAYER_DEFENDER_LEFT
import com.dohman.holdempucker.util.Constants.Companion.PLAYER_DEFENDER_RIGHT
import com.dohman.holdempucker.util.Constants.Companion.PLAYER_FORWARD_LEFT
import com.dohman.holdempucker.util.Constants.Companion.PLAYER_FORWARD_RIGHT
import com.dohman.holdempucker.util.Constants.Companion.areTeamsReadyToStartPeriod
import com.dohman.holdempucker.util.Constants.Companion.currentGameMode
import com.dohman.holdempucker.util.Constants.Companion.isNotOnlineMode
import com.dohman.holdempucker.util.Constants.Companion.isOngoingGame
import com.dohman.holdempucker.util.Constants.Companion.isOnlineMode
import com.dohman.holdempucker.util.Constants.Companion.isOpponentFound
import com.dohman.holdempucker.util.Constants.Companion.isRestoringPlayers
import com.dohman.holdempucker.util.Constants.Companion.isVsBotMode
import com.dohman.holdempucker.util.Constants.Companion.period
import com.dohman.holdempucker.util.Constants.Companion.teamGreen
import com.dohman.holdempucker.util.Constants.Companion.teamBottomScore
import com.dohman.holdempucker.util.Constants.Companion.teamPurple
import com.dohman.holdempucker.util.Constants.Companion.teamTopScore
import com.dohman.holdempucker.util.Constants.Companion.whoseTeamStartedLastPeriod
import com.dohman.holdempucker.util.Constants.Companion.whoseTurn
import com.dohman.holdempucker.util.Constants.WhoseTurn.Companion.isTeamGreenTurn
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
    @Inject
    lateinit var onlineRepo: OnlinePlayRepository

    var cardDeck = mutableListOf<Card>()
    var firstCardInDeck: Card

    val messageNotifier = MutableLiveData<Pair<String, Boolean>>()
    val halfTimeNotifier = MutableLiveData<Int>()
    val whoseTurnNotifier = MutableLiveData<String>()
    val pickedCardNotifier = MutableLiveData<Int>()
    val cardsCountNotifier = MutableLiveData<Int>()
    val badCardNotifier = MutableLiveData<Boolean>()
    // Online
    val onlineOpponentInputNotifier = MutableLiveData<Int>()
    val onlineOpponentFoundNotifier = MutableLiveData<Boolean>()
    val onlineOpponentHasDisconnected = MutableLiveData<Boolean>()

    private val periodObserver = Observer<Int> { newPeriod ->
        newPeriod?.let { if (newPeriod != period) triggerHalfTime(triggeredFromObserver = true) }
    }
    private val opponentFoundObserver = Observer<Boolean> { found ->
        isOpponentFound = found
        if (found && period == 1) onlineOpponentFoundNotifier.value = found
    }
    private val inputObserver = Observer<Int> { input ->
        onlineOpponentInputNotifier.value = input.takeIf { it in 0..5 }
    }
    private val onlineCardDeckObserver = Observer<List<Card>> { newCardDeck ->
        if (newCardDeck.isNotEmpty()) {
            cardDeck = newCardDeck.toMutableList()
            firstCardInDeck = cardDeck.first()
        } else {
            // FIXME OPPONENT HAS DISCONNECTED!
        }
    }
    private val opponentHasDisconnected = Observer<Boolean> { onlineOpponentHasDisconnected.value = it }

    init {
        RepositoryComponent.inject(this)

        cardDeck = cardRepo.createCards() as MutableList<Card>
        firstCardInDeck = cardDeck.first()
    }

    /*
    * General functions
    * */

    fun getScreenWidth() = appRepo.getScreenWidth()

    fun setGameMode(argsLobbyId: String? = null, lobbyName: String? = null, lobbyPassword: String? = null) {
        isVsBotMode = when (currentGameMode) {
            Constants.GameMode.RANDOM -> true
            Constants.GameMode.DEVELOPER -> true
            else -> false
        }

        if (currentGameMode == Constants.GameMode.ONLINE) setupOnlineGame(argsLobbyId, lobbyName, lobbyPassword)
    }

    override fun onCleared() {
        super.onCleared()

        onlineRepo.period.removeObserver(periodObserver)
        onlineRepo.opponentFound.removeObserver(opponentFoundObserver)
        onlineRepo.opponentInput.removeObserver(inputObserver)
        onlineRepo.onlineCardDeck.removeObserver(onlineCardDeckObserver)
        onlineRepo.opponentHasDisconnected.removeObserver(opponentHasDisconnected)
        onlineRepo.resetValues()
    }

    /*
    * Online functions
    * */

    private fun setupOnlineGame(argsLobbyId: String? = null, lobbyName: String? = null, lobbyPassword: String? = null) {
        if (argsLobbyId != null)
            onlineRepo.joinThisLobby(argsLobbyId)
        else if (lobbyName != null)
            onlineRepo.createLobby(cardDeck, lobbyName, lobbyPassword)

        // Lobby is created
        if (!onlineRepo.isMyTeamBottom()) {
            onlineRepo.setListenerForCardDeck()
            onlineRepo.onlineCardDeck.observeForever(onlineCardDeckObserver)
        }

        onlineRepo.setListenerForInput()
        onlineRepo.setListenerForPeriod()
        onlineRepo.period.observeForever(periodObserver)
        onlineRepo.opponentFound.observeForever(opponentFoundObserver)
        onlineRepo.opponentInput.observeForever(inputObserver)
        onlineRepo.opponentHasDisconnected.observeForever(opponentHasDisconnected)
    }

    fun isMyOnlineTeamBottom() = onlineRepo.isMyTeamBottom()

    fun clearAllValueEventListeners() = onlineRepo.removeAllValueEventListeners()

    fun removeLobbyFromDatabase() = onlineRepo.removeLobbyFromDatabase()

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

    fun notifyOnlineInput(input: Int) = onlineRepo.updateInput(input)

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

    private fun areThereEnoughCardsToScore(opponentTeam: Array<Card?>): Boolean {
        var amountOfCardsToAttack = 0

        if (opponentTeam[PLAYER_DEFENDER_LEFT] == null || opponentTeam[PLAYER_DEFENDER_RIGHT] == null) return true
        amountOfCardsToAttack++

        if (opponentTeam[PLAYER_CENTER] != null)
            amountOfCardsToAttack++

        if (opponentTeam[PLAYER_FORWARD_LEFT] != null || opponentTeam[PLAYER_FORWARD_RIGHT] != null)
            amountOfCardsToAttack++

        if (cardDeck.size < amountOfCardsToAttack) {
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

    fun checkGameSituation(
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
            checkGameSituation()
            return
        }

        if (isOngoingGame && !GameLogic.isTherePossibleMove(firstCardInDeck)) triggerBadCard()
        else if (isOngoingGame && GameLogic.isTherePossibleMove(firstCardInDeck)
        ) fPrepareViewsToPulse?.invoke()
    }

    fun isNextPeriodReady(): Boolean {
        return if (period > 3 && (teamBottomScore != teamTopScore)) false else {
            if (period > 3) notifyMessage(
                "Overtime! Play until all cards are out.\nPeriod: $period",
                isNeutralMessage = true
            )

            whoseTurn =
                if (whoseTeamStartedLastPeriod == Constants.WhoseTurn.GREEN) Constants.WhoseTurn.PURPLE else Constants.WhoseTurn.GREEN

            whoseTeamStartedLastPeriod = whoseTurn

            true
        }
    }

    private fun dealNewCardDeck() {
        cardDeck = cardRepo.createCards() as MutableList<Card>
        firstCardInDeck = cardDeck.first()
    }

    private fun triggerHalfTime(triggeredFromObserver: Boolean = false) {
        if (triggeredFromObserver && (period == onlineRepo.period.value)) return

        if (isNotOnlineMode()) {
            dealNewCardDeck()
        } else {
            if (isMyOnlineTeamBottom()) {
                dealNewCardDeck()
                onlineRepo.storeCardDeckInLobby(cardDeck)
            } else {
                if (!onlineRepo.hasCardDeckBeenRetrievedCorrectly(cardDeck))
                    onlineRepo.retrieveCardDeckFromLobby().let { newCardDeck ->
                        if (!newCardDeck.isNullOrEmpty()) {
                            cardDeck = newCardDeck.toMutableList()
                            firstCardInDeck = cardDeck.first()
                        }
                    }
            }
        }

        for (index in 0..5) {
            teamGreen[index] = null
            teamPurple[index] = null
        }

        period++
        halfTimeNotifier.value = 1
        if (isOnlineMode() && !triggeredFromObserver) onlineRepo.updatePeriod(period)
        if (period <= 3) notifyMessage("Not enough cards. Period $period started.", isNeutralMessage = true)
        isOngoingGame = false
        areTeamsReadyToStartPeriod = false
    }

    fun triggerBadCard() {
        badCardNotifier.value = true
    }

    fun addGoalToScore() {
        if (isTeamGreenTurn()) teamBottomScore++ else teamTopScore++
    }

    /*
    * Teams functions
    * */

    private fun isGoalieThereOrAdd(goalieCard: Card): Boolean {
        if (GameLogic.isGoalieThereOrAdd(goalieCard)) return true
        return false // But goalie is added now
    }

    private fun setPlayerInTeam(team: Array<Card?>, spotIndex: Int, fPrepareViewsToPulse: () -> Unit) {
        team[spotIndex] = firstCardInDeck
        if (removeCardFromDeck()) checkGameSituation(false, fPrepareViewsToPulse)
    }

    private fun areTeamsReady(): Boolean {
        teamGreen.forEach { if (it == null) return false }
        teamPurple.forEach { if (it == null) return false }

        isOngoingGame = true
        areTeamsReadyToStartPeriod = true
        isRestoringPlayers = false
        return true
    }

    fun isThisTeamReady(): Boolean {
        val teamToCheck =
            if (isTeamGreenTurn()) teamGreen else teamPurple

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

    fun getEmptySpots(views: List<View>): List<Int> {
        val list = mutableListOf<Int>()

        views.minus(views.last()).forEachIndexed { index, view ->
            if (view.tag == Integer.valueOf(android.R.color.transparent)) list.add(index)
        }

        return list
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
        if (cardDeck.size <= 3 && !areThereEnoughCardsToScore(if (isTeamGreenTurn()) teamPurple else teamGreen)) {
            Animations.stopAllPulsingCards()
            return false
        }
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
        if (removeCardFromDeck())
            checkGameSituation(true, fPrepareViewsToPulse)
    }
}

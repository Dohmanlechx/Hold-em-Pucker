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
import com.dohman.holdempucker.util.Constants
import com.dohman.holdempucker.util.Constants.Companion.areTeamsReadyToStartPeriod
import com.dohman.holdempucker.util.Constants.Companion.currentGameMode
import com.dohman.holdempucker.util.Constants.Companion.isNotOnlineMode
import com.dohman.holdempucker.util.Constants.Companion.isOngoingGame
import com.dohman.holdempucker.util.Constants.Companion.isOnlineMode
import com.dohman.holdempucker.util.Constants.Companion.isOpponentFound
import com.dohman.holdempucker.util.Constants.Companion.isRestoringPlayers
import com.dohman.holdempucker.util.Constants.Companion.isVsBotMode
import com.dohman.holdempucker.util.Constants.Companion.period
import com.dohman.holdempucker.util.Constants.Companion.teamBottom
import com.dohman.holdempucker.util.Constants.Companion.teamBottomScore
import com.dohman.holdempucker.util.Constants.Companion.teamTop
import com.dohman.holdempucker.util.Constants.Companion.teamTopScore
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
    // FIXME This doesnt get triggered when opponent starts period
    private val periodObserver = Observer<Int> { newPeriod ->
        newPeriod?.let { if (newPeriod != period) triggerHalfTime(triggeredFromObserver = true) }
    }
    private val opponentFoundObserver = Observer<Boolean> { found ->
        isOpponentFound = found
        if (found) onlineOpponentFoundNotifier.value = found
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
        isVsBotMode = when (currentGameMode) {
            Constants.GameMode.RANDOM -> true
            Constants.GameMode.DEVELOPER -> true
            else -> false
        }

        if (currentGameMode == Constants.GameMode.ONLINE) setupOnlineGame()
    }

    override fun onCleared() {
        super.onCleared()

        onlineRepo.period.removeObserver(periodObserver)
        onlineRepo.opponentFound.removeObserver(opponentFoundObserver)
        onlineRepo.opponentInput.removeObserver(inputObserver)
        onlineRepo.onlineCardDeck.removeObserver(onlineCardDeckObserver)
        onlineRepo.resetValues()
    }

    /*
    * Online functions
    * */

    private fun setupOnlineGame() {
        onlineRepo.searchForLobbyOrCreateOne(cardDeck = cardDeck) {
            if (!onlineRepo.isMyTeamBottom()) {
                onlineRepo.setListenerForCardDeck()
                onlineRepo.onlineCardDeck.observeForever(onlineCardDeckObserver)
            }

            // Started here since by then, lobbyId is set
            onlineRepo.setListenerForInput()
            onlineRepo.setListenerForPeriod()
            onlineRepo.period.observeForever(periodObserver)
        }

        onlineRepo.opponentFound.observeForever(opponentFoundObserver)
        onlineRepo.opponentInput.observeForever(inputObserver)
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
                if (whoseTeamStartedLastPeriod == Constants.WhoseTurn.BOTTOM) Constants.WhoseTurn.TOP else Constants.WhoseTurn.BOTTOM

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

        if (isNotOnlineMode) {
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
            teamBottom[index] = null
            teamTop[index] = null
        }

        period += 1
        halfTimeNotifier.value = 1
        if (isOnlineMode && !triggeredFromObserver) onlineRepo.updatePeriod(period)
        if (period <= 3) notifyMessage("Not enough cards. Period $period started.", isNeutralMessage = true)
        isOngoingGame = false
        areTeamsReadyToStartPeriod = false
    }

    fun triggerBadCard() {
        badCardNotifier.value = true
    }

    fun addGoalToScore() {
        if (isTeamBottomTurn()) teamBottomScore++ else teamTopScore++
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
        checkGameSituation(true, fPrepareViewsToPulse)
    }
}

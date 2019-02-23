package com.dohman.holdempucker

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dohman.holdempucker.cards.Card
import com.dohman.holdempucker.cards.CardDeck

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val cardDeck = CardDeck().cardDeck
    private var pickedCard: Card = cardDeck.first()
    lateinit var currentCard: Card

    val pickedCardNotifier = MutableLiveData<Int>()
    val cardsCountNotifier = MutableLiveData<Int>()

    val nfyCard = MutableLiveData<Map<Array<Card?>, Int>>() // FIXME: Not needed?

    val nfyTopGoalie = MutableLiveData<Boolean>()
    val nfyBtmGoalie = MutableLiveData<Boolean>()

    init {
        showPickedCard()
    }

    // ----- Notify functions ----- //
    private fun notifyPickedCard() {
        pickedCardNotifier.value = resIdOfCard(pickedCard)
    }

    private fun notifyGoalie() {
        when (GameActivity.whoseTurn) {
            GameActivity.WhoseTurn.BOTTOM -> nfyBtmGoalie.value = true
            GameActivity.WhoseTurn.TOP -> nfyTopGoalie.value = true
        }
    }

    private fun notifyCard(value: Map<Array<Card?>, Int>) { // FIXME: Not needed?
        nfyCard.value = value
    }


    // ----- Private functions ----- //
    private fun showPickedCard(doNotToggleTurn: Boolean = false) {
        checkIfTeamsAreReady()
        if (!doNotToggleTurn) toggleTurn()

        if (!isGoalieThere(pickedCard)) { // If returned false, goalie is added
            showPickedCard()
            return
        }

        pickedCard.let {
            notifyPickedCard()
            currentCard = it
            removeCardFromDeck()
        }
    }

    private fun removeCardFromDeck() {
        if (cardDeck.isEmpty()) {
            //halfTime() // FIXME
        }
        cardDeck.remove(pickedCard)
        pickedCard = cardDeck.first()
        cardsCountNotifier.value = cardDeck.size
    }

    private fun isGoalieThere(goalieCard: Card/*, team: Array<Card?>*/): Boolean {
        val team =
            if (GameActivity.whoseTurn == GameActivity.WhoseTurn.BOTTOM) GameActivity.teamBottom else GameActivity.teamTop
        team.let { if (!it.all { element -> element == null }) return true else it[5] = goalieCard }
        notifyGoalie()
        removeCardFromDeck()

        return false // But goalie is added now
    }

    private fun checkIfTeamsAreReady(): Boolean {
        GameActivity.teamBottom.forEach { if (it == null) return false }
        GameActivity.teamTop.forEach { if (it == null) return false }

        GameActivity.isOngoingGame = true
        return true
    }

    private fun toggleTurn() {
        GameActivity.WhoseTurn.toggleTurn()
    }


    // ----- Public functions ----- //
    fun attack(victimTeam: Array<Card?>, spotIndex: Int): Boolean {
        victimTeam.let {
            if (currentCard.rank ?: 0 >= it[spotIndex]?.rank ?: 0) {
                it[spotIndex] = null
                removeCardFromDeck()
                showPickedCard(doNotToggleTurn = true)
                return true
            }
        }

        return false
    }

    fun areAllForwardsOut(victimTeam: Array<Card?>): Boolean { // FIXME Not all attackers must be out, The defender cant to be out if the center and the forward at its side is alive
        for (i in 0..2) {
            if (victimTeam[i] != null) return false
        }

        return true
    }

    fun isAtLeastOneDefenderOut(victimTeam: Array<Card?>): Boolean {
        for (i in 4..5) {
            if (victimTeam[i] == null) return true
        }

        return false
    }

    fun setPlayerInTeam(team: Array<Card?>, spotIndex: Int) {
        team[spotIndex] = currentCard
        val map = mutableMapOf<Array<Card?>, Int>() // FIXME: Not needed?
        map[team] = spotIndex // FIXME: Not needed?
        notifyCard(map) // FIXME: Not needed?
        showPickedCard()
    }

    fun resIdOfCard(card: Card): Int {
        return card.let {
            getApplication<Application>().resources.getIdentifier(
                it.src, "drawable", getApplication<Application>().packageName
            )
        }
    }

    companion object {
        const val TAG = "DBG: GameViewModel.kt"
    }
}
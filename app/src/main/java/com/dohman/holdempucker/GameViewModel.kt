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

    val nfyTopGoalie = MutableLiveData<Boolean>() // FIXME notify for team top

    val nfyBtmGoalie = MutableLiveData<Boolean>()
    val nfyBtmLeftDefender = MutableLiveData<Card>()
    val nfyBtmRightDefender = MutableLiveData<Card>()
    val nfyBtmLeftForward = MutableLiveData<Card>()
    val nfyBtmCenter = MutableLiveData<Card>()
    val nfyBtmRightForward = MutableLiveData<Card>()

    init {
        showPickedCard()

    }

    // ----- Notify functions ----- //
    private fun notifyPickedCard() {
        pickedCardNotifier.value = resIdOfCard(pickedCard)
    }

    private fun notifyGoalie(isBottom: Boolean) {
        when (isBottom) {
            true -> nfyBtmGoalie.value = true
            false -> nfyTopGoalie.value = true
        }
    }

    private fun notifyCard(value: Map<Array<Card?>, Int>) { // FIXME: Not needed?
        nfyCard.value = value
    }


    // ----- Private functions ----- //
    private fun showPickedCard() {
        if (checkIfTeamsAreReady()) Log.d("MATCHEN ÄR REDO FAN", "REDO")

        if (!isGoalieThere(pickedCard, GameActivity.teamBottom)) { // If returned false, goalie is added
            takeNewCardFromDeck()
            return
        }

        pickedCard.let {
            notifyPickedCard()
            currentCard = it
            removeCardFromDeck()
        }

        if (cardDeck.isEmpty()) {
            //halfTime() // FIXME
        }
    }

    private fun removeCardFromDeck() {
        cardDeck.remove(pickedCard)
        pickedCard = cardDeck.first()
        cardsCountNotifier.value = cardDeck.size
    }

    private fun takeNewCardFromDeck() {
        //pickedCard = null // FIXME: Needed?
        showPickedCard()
    }

    private fun isGoalieThere(goalieCard: Card, team: Array<Card?>): Boolean {
        team.let { if (!it.all { element -> element == null }) return true else it[5] = goalieCard }
        notifyGoalie(isBottom = true)
        removeCardFromDeck()

        return false // But goalie is added now
    }

    private fun checkIfTeamsAreReady(): Boolean {
        GameActivity.teamBottom.forEach { if (it == null) return false }
        //GameActivity.teamTop.forEach { if (it == null) return false }

        GameActivity.isOngoingGame = true
        return true
    }


    // ----- Public functions ----- //
    fun setPlayerInTeam(team: Array<Card?>, spotIndex: Int) {
        team[spotIndex] = currentCard
        val map = mutableMapOf<Array<Card?>, Int>() // FIXME: Not needed?
        map[team] = spotIndex // FIXME: Not needed?
        notifyCard(map) // FIXME: Not needed?
        takeNewCardFromDeck()
    }

    fun resIdOfCard(card: Card): Int {
        return card.let {
            getApplication<Application>().resources.getIdentifier(
                it.src, "drawable", getApplication<Application>().packageName
            )
        }
    }
}
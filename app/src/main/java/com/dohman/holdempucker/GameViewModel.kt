package com.dohman.holdempucker

import android.app.Application
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

    val nfyBtmGoalie = MutableLiveData<Boolean>()

    init {
        showPickedCard()
    }

    // Private functions
    private fun setResIdForNewPickedCard() {
        pickedCardNotifier.value = resIdOfCard(pickedCard)
    }

    private fun setGoalie(isBottom: Boolean) {
        when (isBottom) {
            true -> nfyBtmGoalie.value = true
        }

    }

    private fun removeCardFromDeck() {
        cardDeck.remove(pickedCard)
        pickedCard = cardDeck.first()
        cardsCountNotifier.value = cardDeck.size
    }

    private fun takeNewCardFromDeck() {
        //pickedCard = null
        showPickedCard()
    }

    private fun isGoalieThere(goalieCard: Card, team: Array<Card?>): Boolean {
        team.let { if (!it.all { element -> element == null}) return true else it[5] = goalieCard }
        setGoalie(isBottom = true)
        removeCardFromDeck()

        return false // But goalie is added now
    }

    // Public functions

    fun showPickedCard() {
        if (!isGoalieThere(pickedCard, GameActivity.teamBottom)) { // If returned false, goalie is added
            takeNewCardFromDeck()
            return
        }

        pickedCard.let {
            setResIdForNewPickedCard()
            currentCard = it
            removeCardFromDeck()
        }

        if (cardDeck.isEmpty()) {
            //halfTime() // FIXME
        }
    }

    fun resIdOfCard(card: Card): Int {
        return card.let {
            getApplication<Application>().resources.getIdentifier(
                it.src, "drawable", getApplication<Application>().packageName
            )
        }
    }
}
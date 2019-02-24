package com.dohman.holdempucker

import android.app.Application
import android.util.Log
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
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

    // ----- Private functions ----- //
    fun showPickedCard(doNotToggleTurn: Boolean = false) { // FIXME set private
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

    fun removeCardFromDeck() { // FIXME set private
        if (cardDeck.isEmpty()) {
            //halfTime() // FIXME
        } else {
            cardDeck.remove(pickedCard)
            pickedCard = cardDeck.first()
            cardsCountNotifier.value = cardDeck.size
        }
    }

    private fun isGoalieThere(goalieCard: Card/*, team: Array<Card?>*/): Boolean {
        val team =
            if (GameActivity.whoseTurn == GameActivity.WhoseTurn.BOTTOM) GameActivity.teamBottom else GameActivity.teamTop
        team.let { if (!it.all { element -> element == null }) return true else it[5] = goalieCard }
        notifyGoalie()
        removeCardFromDeck()

        return false // But goalie is added now
    }

    private fun setPlayerInTeam(team: Array<Card?>, spotIndex: Int) {
        team[spotIndex] = currentCard
        showPickedCard()
    }

    private fun checkIfTeamsAreReady(): Boolean {
        GameActivity.teamBottom.forEach { if (it == null) return false }
        GameActivity.teamTop.forEach { if (it == null) return false }

        GameActivity.isOngoingGame = true
        return true
    }

    private fun resIdOfCard(card: Card): Int {
        return card.let {
            getApplication<Application>().resources.getIdentifier(
                it.src, "drawable", getApplication<Application>().packageName
            )
        }
    }

    private fun toggleTurn() {
        GameActivity.WhoseTurn.toggleTurn()
    }

    // ----- Public functions ----- //
    fun updateScores(topTeam: AppCompatTextView, bottomTeam: AppCompatTextView) {
        topTeam.text = GameActivity.teamTopScore.toString()
        bottomTeam.text = GameActivity.teamBottomScore.toString()
    }

    fun attack(victimTeam: Array<Card?>, spotIndex: Int, view: AppCompatImageView): Boolean {
        if (view.tag == Integer.valueOf(R.drawable.skull)) return false

        victimTeam.let {
            if (currentCard.rank ?: 0 >= it[spotIndex]?.rank ?: 0) {
                it[spotIndex] = null
                view.setImageResource(R.drawable.skull)
                view.tag = Integer.valueOf(R.drawable.skull)
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

    fun addPlayer(view: AppCompatImageView, team: Array<Card?>, spotIndex: Int) {
        if (view.drawable != null) return
        view.setImageResource(resIdOfCard(currentCard))
        setPlayerInTeam(team, spotIndex)
    }

    companion object {
        const val TAG = "DBG: GameViewModel.kt"
    }
}
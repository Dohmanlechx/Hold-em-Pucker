package com.dohman.holdempucker

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dohman.holdempucker.cards.Card
import com.dohman.holdempucker.cards.CardDeck
import com.dohman.holdempucker.util.GameLogic

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

        //if (GameActivity.isOngoingGame) checkPossibleMoves() // FIXME
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

    private fun isGoalieThere(goalieCard: Card): Boolean {
        if (GameLogic.isGoalieThere(goalieCard)) return true

        notifyGoalie()
        removeCardFromDeck()
        Toast.makeText( // FIXME Remove later
            getApplication<Application>().applicationContext,
            "Goalie ${GameActivity.whoseTurn} added!", Toast.LENGTH_SHORT
        ).show()

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

        if (GameLogic.attack(currentCard, victimTeam, spotIndex)) {
            view.setImageResource(R.drawable.skull)
            view.tag = Integer.valueOf(R.drawable.skull)
            removeCardFromDeck()
            showPickedCard(doNotToggleTurn = true)
            return true
        }

        return false
    }

    fun areEnoughForwardsOut(victimTeam: Array<Card?>, defenderPos: Int): Boolean {
        return GameLogic.areEnoughForwardsOut(victimTeam, defenderPos)
    }

    fun isAtLeastOneDefenderOut(victimTeam: Array<Card?>): Boolean {
        return GameLogic.isAtLeastOneDefenderOut(victimTeam)
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
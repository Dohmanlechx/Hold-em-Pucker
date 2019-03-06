package com.dohman.holdempucker

import android.app.Application
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dohman.holdempucker.cards.Card
import com.dohman.holdempucker.cards.CardDeck
import com.dohman.holdempucker.util.GameLogic

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val cardDeck = CardDeck().cardDeck
    private var firstCardInDeck: Card = cardDeck.first()
//    private var pickedCard: Card = firstCardInDeck

    val whoseTurnNotifier = MutableLiveData<String>()

    val pickedCardNotifier = MutableLiveData<Int>()
    val cardsCountNotifier = MutableLiveData<Int>()

    val nfyTopGoalie = MutableLiveData<Boolean>()
    val nfyBtmGoalie = MutableLiveData<Boolean>()

    init {
        showPickedCard()
    }

    // ----- Notify functions ----- //
    private fun notifyPickedCard() {
        pickedCardNotifier.value = resIdOfCard(firstCardInDeck)
    }

    private fun notifyGoalie() {
        when (GameActivity.whoseTurn) {
            GameActivity.WhoseTurn.BOTTOM -> nfyBtmGoalie.value = true
            GameActivity.WhoseTurn.TOP -> nfyTopGoalie.value = true
        }
    }

    // ----- Private functions ----- //
    fun showPickedCard(doNotToggleTurn: Boolean = false) { // FIXME set private
        if (!doNotToggleTurn && !GameActivity.restoringPlayers) toggleTurn()

        if (!GameActivity.areTeamsReadyToStartPeriod) {
            areTeamsReady()
        } else {
            if (isThisTeamReady() && !GameActivity.isOngoingGame) {
                GameActivity.isOngoingGame = true
            }
        }

        if (!isGoalieThereOrAdd(firstCardInDeck)) { // If returned false, goalie is added
            showPickedCard()
            return
        }

        while (GameActivity.isOngoingGame && !GameLogic.isTherePossibleMove(GameActivity.whoseTurn, firstCardInDeck)) {
            Toast.makeText(
                getApplication<Application>().applicationContext,
                "No possible move. Card (Rank ${firstCardInDeck.rank}) discarded, Switching turn...",
                Toast.LENGTH_LONG
            ).show()// FIXME
            toggleTurn()
            removeCardFromDeck()

            if (!isThisTeamReady()) {
                GameActivity.isOngoingGame = false
                GameActivity.restoringPlayers = true
            }
        }

    }


    fun removeCardFromDeck() { // FIXME set private
        if (cardDeck.isEmpty()) {
            //halfTime() // FIXME
        } else {
            cardDeck.remove(firstCardInDeck)
            firstCardInDeck = cardDeck.first()
            notifyPickedCard()
            cardsCountNotifier.value = cardDeck.size
        }
    }

    private fun isGoalieThereOrAdd(goalieCard: Card): Boolean {
        if (GameLogic.isGoalieThere(goalieCard)) return true

        notifyGoalie()
        removeCardFromDeck()

        return false // But goalie is added now
    }

    private fun setPlayerInTeam(team: Array<Card?>, spotIndex: Int) {
        team[spotIndex] = firstCardInDeck
        removeCardFromDeck()
        showPickedCard()
    }

    private fun areTeamsReady(): Boolean {
        GameActivity.teamBottom.forEach { if (it == null) return false }
        GameActivity.teamTop.forEach { if (it == null) return false }

        GameActivity.isOngoingGame = true
        GameActivity.areTeamsReadyToStartPeriod = true
        return true
    }

    private fun isThisTeamReady(): Boolean {
        val teamToCheck =
            if (GameActivity.whoseTurn == GameActivity.WhoseTurn.BOTTOM) GameActivity.teamBottom else GameActivity.teamTop

        teamToCheck.forEach { if (it == null) return false }

        GameActivity.restoringPlayers = false
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
        whoseTurnNotifier.value = GameActivity.whoseTurn.name
    }

    // ----- Public functions ----- //
    fun updateScores(topTeam: AppCompatTextView, bottomTeam: AppCompatTextView) {
        topTeam.text = GameActivity.teamTopScore.toString()
        bottomTeam.text = GameActivity.teamBottomScore.toString()
    }

    fun attack(victimTeam: Array<Card?>, spotIndex: Int, view: AppCompatImageView): Boolean {
        if (view.tag == Integer.valueOf(R.drawable.skull)) return false

        if (GameLogic.attack(firstCardInDeck, victimTeam, spotIndex) && spotIndex == 5) {
            // Goalie is attacked and it is Goal!
            Toast.makeText(
                getApplication<Application>().applicationContext,
                "Goal! Added new goalie.",
                Toast.LENGTH_LONG
            ).show()
            GameActivity.isOngoingGame = false
            GameActivity.restoringPlayers = true
            removeCardFromDeck()
            showPickedCard()
            return true
        } else if (GameLogic.attack(firstCardInDeck, victimTeam, spotIndex)) {
            view.setImageResource(R.drawable.skull)
            view.tag = Integer.valueOf(R.drawable.skull)
            removeCardFromDeck()
            showPickedCard(doNotToggleTurn = true)
            return true
        }

        return false
    }

    fun goalieAttacked(victimTeam: Array<Card?>) {
        Toast.makeText(
            getApplication<Application>().applicationContext,
            "Not goal. Goalie too strong. Added new goalie.",
            Toast.LENGTH_LONG
        ).show()
        victimTeam[5] = null
        GameActivity.isOngoingGame = false
        GameActivity.restoringPlayers = true
        removeCardFromDeck()
        showPickedCard()
    }

    fun areEnoughForwardsOut(victimTeam: Array<Card?>, defenderPos: Int): Boolean {
        return GameLogic.areEnoughForwardsOut(victimTeam, defenderPos)
    }

    fun isAtLeastOneDefenderOut(victimTeam: Array<Card?>): Boolean {
        return GameLogic.isAtLeastOneDefenderOut(victimTeam)
    }

    fun addPlayer(view: AppCompatImageView, team: Array<Card?>, spotIndex: Int) {
        if (view.tag == Integer.valueOf(R.drawable.skull) && GameActivity.isOngoingGame) return
        view.setImageResource(resIdOfCard(firstCardInDeck))
        view.tag = null
        setPlayerInTeam(team, spotIndex)
    }

    companion object {
        const val TAG = "DBG: GameViewModel.kt"
    }
}
package com.dohman.holdempucker

import android.app.Application
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dohman.holdempucker.cards.Card
import com.dohman.holdempucker.cards.CardDeck
import com.dohman.holdempucker.util.GameLogic

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private var cardDeck = CardDeck().cardDeck
    var firstCardInDeck: Card = cardDeck.first()

    val halfTimeNotifier = MutableLiveData<Int>()
    val whoseTurnNotifier = MutableLiveData<String>()
    val pickedCardNotifier = MutableLiveData<Int>()
    val cardsCountNotifier = MutableLiveData<Int>()
    val badCardNotifier = MutableLiveData<Boolean>()
    val nfyTopGoalie = MutableLiveData<Boolean>()
    val nfyBtmGoalie = MutableLiveData<Boolean>()

//    private var isBadCard = false

    /*
    * Notify functions
    * */

    private fun notifyPickedCard() {
        pickedCardNotifier.value = resIdOfCard(firstCardInDeck)
    }

    private fun notifyGoalie() {
        when (GameActivity.whoseTurn) {
            GameActivity.WhoseTurn.BOTTOM -> nfyBtmGoalie.value = true
            GameActivity.WhoseTurn.TOP -> nfyTopGoalie.value = true
        }
    }

    fun notifyToggleTurn() {
        GameActivity.WhoseTurn.toggleTurn()
        whoseTurnNotifier.value = GameActivity.whoseTurn.name
    }

    /*
    * Card deck functions
    * */

    fun removeCardFromDeck() {
        cardDeck.remove(firstCardInDeck)
        if (cardDeck.isEmpty()) {
            halfTime()
        } else {
            firstCardInDeck = cardDeck.first()
            notifyPickedCard()
            cardsCountNotifier.value = cardDeck.size
        }
    }

    private fun areThereEnoughCards(team: Array<Card?>): Boolean {
        val amountOfNulls = team.filter { it == null }.size
        if ((amountOfNulls + 4) > cardDeck.size) { // 4 is the minimum amount to score an goal
            halfTime()
            Toast.makeText(
                getApplication<Application>().applicationContext,
                "Not enough cards. New period started.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        return true
    }

    /*
    * Game management functions
    * */

    fun showPickedCard(doNotToggleTurn: Boolean = false) { // FIXME set private
        if ((!doNotToggleTurn && !GameActivity.restoringPlayers) || !GameActivity.areTeamsReadyToStartPeriod) notifyToggleTurn()

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

        if (GameActivity.isOngoingGame && !GameLogic.isTherePossibleMove(GameActivity.whoseTurn, firstCardInDeck)) triggerBadCard()
    }

    fun triggerBadCard() {
        Toast.makeText(
            getApplication<Application>().applicationContext,
            "No possible move. Card (Rank ${firstCardInDeck.rank}) discarded, Switching turn...",
            Toast.LENGTH_LONG
        ).show()// FIXME

        badCardNotifier.value = true

    }

    fun resIdOfCard(card: Card): Int {
        return card.let {
            getApplication<Application>().resources.getIdentifier(
                it.src, "drawable", getApplication<Application>().packageName
            )
        }
    }

    private fun halfTime() {
        cardDeck = CardDeck().cardDeck
        firstCardInDeck = cardDeck.first()
        for (index in 0..5) {
            GameActivity.teamBottom[index] = null
            GameActivity.teamTop[index] = null
        }
        halfTimeNotifier.value = 1
        GameActivity.isOngoingGame = false
        GameActivity.areTeamsReadyToStartPeriod = false
    }

    fun updateScores(topTeam: AppCompatTextView, bottomTeam: AppCompatTextView) {
        topTeam.text = GameActivity.teamTopScore.toString()
        bottomTeam.text = GameActivity.teamBottomScore.toString()
    }

    /*
    * Teams functions
    * */

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

    fun isThisTeamReady(): Boolean {
        val teamToCheck =
            if (GameActivity.whoseTurn == GameActivity.WhoseTurn.BOTTOM) GameActivity.teamBottom else GameActivity.teamTop

        teamToCheck.forEach { if (it == null) return false }

        GameActivity.restoringPlayers = false
        return true
    }

    fun areEnoughForwardsOut(victimTeam: Array<Card?>, defenderPos: Int): Boolean {
        return GameLogic.areEnoughForwardsOut(victimTeam, defenderPos)
    }

    fun isAtLeastOneDefenderOut(victimTeam: Array<Card?>): Boolean {
        return GameLogic.isAtLeastOneDefenderOut(victimTeam)
    }

    /*
    * Player actions
    * */

    fun canAddPlayerView(view: AppCompatImageView, team: Array<Card?>, spotIndex: Int): Boolean {
        if (cardDeck.size <= 8 && !areThereEnoughCards(team)) return false
        if ((view.tag == Integer.valueOf(android.R.color.transparent) && GameActivity.isOngoingGame) || team[spotIndex] != null) return false
        return true
    }

    fun canAttack(victimTeam: Array<Card?>, spotIndex: Int, view: AppCompatImageView): Boolean {
        if (view.tag == Integer.valueOf(android.R.color.transparent)) return false

        val goalieRank = victimTeam[5]?.rank
        if (GameLogic.attack(firstCardInDeck, victimTeam, spotIndex) && spotIndex == 5) {
            // Goalie is attacked and it is Goal!
            Toast.makeText(
                getApplication<Application>().applicationContext,
                "Goal against goalie (Rank: $goalieRank)! Added new goalie.",
                Toast.LENGTH_LONG
            ).show()
            notifyToggleTurn()
            GameActivity.isOngoingGame = false
            GameActivity.restoringPlayers = true
            removeCardFromDeck()
            showPickedCard()
            return true
        } else if (GameLogic.attack(firstCardInDeck, victimTeam, spotIndex)) {
            return true
        }

        return false
    }

    fun goalieSaved(victimTeam: Array<Card?>) {
        Toast.makeText(
            getApplication<Application>().applicationContext,
            "Not goal. Goalie (Rank: ${victimTeam[5]?.rank}) too strong. Added new goalie.",
            Toast.LENGTH_LONG
        ).show()
        Handler().postDelayed({
            notifyToggleTurn()
            victimTeam[5] = null
            GameActivity.isOngoingGame = false
            GameActivity.restoringPlayers = true
            removeCardFromDeck()
            showPickedCard()
        }, 2000)
    }

    /*
    * On animation ends
    * */

    fun onGoalieAddedAnimationEnd(view: AppCompatImageView) {
        view.setImageResource(R.drawable.red_back)
        view.tag = Integer.valueOf(R.drawable.red_back)
    }

    fun onPlayerAddedAnimationEnd(view: AppCompatImageView, team: Array<Card?>, spotIndex: Int) {
        view.setImageResource(resIdOfCard(firstCardInDeck))
        view.tag = null
        setPlayerInTeam(team, spotIndex)
    }

    fun onAttackedAnimationEnd(view: AppCompatImageView) {
        view.setImageResource(android.R.color.transparent)
        view.tag = Integer.valueOf(android.R.color.transparent)
        removeCardFromDeck()
            showPickedCard(doNotToggleTurn = true)

    }

    companion object {
        const val TAG = "DBG: GameViewModel.kt"
    }
}
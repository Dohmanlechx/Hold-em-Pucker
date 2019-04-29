package com.dohman.holdempucker.activities.viewmodels

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dohman.holdempucker.R
import com.dohman.holdempucker.cards.Card
import com.dohman.holdempucker.cards.CardDeck
import com.dohman.holdempucker.util.AnimationUtil
import com.dohman.holdempucker.util.Constants
import com.dohman.holdempucker.util.Constants.Companion.areTeamsReadyToStartPeriod
import com.dohman.holdempucker.util.Constants.Companion.isOngoingGame
import com.dohman.holdempucker.util.Constants.Companion.restoringPlayers
import com.dohman.holdempucker.util.Constants.Companion.teamBottom
import com.dohman.holdempucker.util.Constants.Companion.teamBottomScore
import com.dohman.holdempucker.util.Constants.Companion.teamTop
import com.dohman.holdempucker.util.Constants.Companion.teamTopScore
import com.dohman.holdempucker.util.Constants.Companion.whoseTurn
import com.dohman.holdempucker.util.GameLogic
import com.wajahatkarim3.easyflipview.EasyFlipView

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private var cardDeck = CardDeck().cardDeck
    var firstCardInDeck: Card = cardDeck.first()

    val messageNotifier = MutableLiveData<Pair<String, Boolean>>()
    val halfTimeNotifier = MutableLiveData<Int>()
    val whoseTurnNotifier = MutableLiveData<String>()
    val pickedCardNotifier = MutableLiveData<Int>()
    val cardsCountNotifier = MutableLiveData<Int>()
    val badCardNotifier = MutableLiveData<Boolean>()

    /*
    * Notify functions
    * */

    private fun notifyPickedCard() {
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
            notifyMessage("Not enough cards. New period started.", isNeutralMessage = true)
            return false
        }

        return true
    }

    /*
    * Image/view functions
    * */

    fun setImagesOnFlipView(
        flipView: EasyFlipView,
        front: AppCompatImageView,
        back: AppCompatImageView,
        resId: Int?,
        bitmap: Bitmap?,
        isVertical: Boolean
    ) {
        val cover = if (isVertical) R.drawable.red_back_vertical else R.drawable.red_back

        if (flipView.isBackSide) {
            back.setImageResource(cover)
            if (isVertical) resId?.let { front.setImageResource(it) } else bitmap?.let { front.setImageBitmap(it) }
        } else {
            front.setImageResource(cover)
            if (isVertical) resId?.let { back.setImageResource(it) } else bitmap?.let { back.setImageBitmap(it) }
        }

        flipView.visibility = View.VISIBLE
    }

    fun resIdOfCard(card: Card?): Int {
        return card.let {
            getApplication<Application>().resources.getIdentifier(
                it?.src, "drawable", getApplication<Application>().packageName
            )
        }
    }

    fun getRotatedBitmap(card: Card?): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(90f)

        val scaledBitmap = Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(getApplication<Application>().resources, resIdOfCard(card)),
            691,
            1056,
            true
        )

        return Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
    }

    /*
    * Game management functions
    * */

    fun showPickedCard(doNotToggleTurn: Boolean = false) {
        if ((!doNotToggleTurn && !restoringPlayers) || !areTeamsReadyToStartPeriod) notifyToggleTurn()

        if (!areTeamsReadyToStartPeriod) {
            areTeamsReady()
        } else {
            if (isThisTeamReady() && !isOngoingGame) {
                isOngoingGame = true
            }
        }

        if (!isGoalieThereOrAdd(firstCardInDeck)) { // If returned false, goalie is added
            showPickedCard()
            return
        }

        if (isOngoingGame && !GameLogic.isTherePossibleMove(whoseTurn, firstCardInDeck)) triggerBadCard()
        else if (isOngoingGame && GameLogic.isTherePossibleMove(whoseTurn, firstCardInDeck)) AnimationUtil.startPulsingCardsAnimation()
    }

    fun triggerBadCard() {
        badCardNotifier.value = true
    }

    private fun halfTime() {
        cardDeck = CardDeck().cardDeck
        firstCardInDeck = cardDeck.first()
        for (index in 0..5) {
            teamBottom[index] = null
            teamTop[index] = null
        }
        halfTimeNotifier.value = 1
        isOngoingGame = false
        areTeamsReadyToStartPeriod = false
    }

    fun updateScores(topTeam: AppCompatTextView, bottomTeam: AppCompatTextView) {
        topTeam.text = teamTopScore.toString()
        bottomTeam.text = teamBottomScore.toString()
    }

    /*
    * Teams functions
    * */

    private fun isGoalieThereOrAdd(goalieCard: Card): Boolean {
        if (GameLogic.isGoalieThereOrAdd(goalieCard)) return true

        removeCardFromDeck()
        return false // But goalie is added now
    }

    private fun setPlayerInTeam(team: Array<Card?>, spotIndex: Int) {
        team[spotIndex] = firstCardInDeck
        removeCardFromDeck()
        showPickedCard()
    }

    private fun areTeamsReady(): Boolean {
        teamBottom.forEach { if (it == null) return false }
        teamTop.forEach { if (it == null) return false }

        isOngoingGame = true
        areTeamsReadyToStartPeriod = true
        return true
    }

    fun isThisTeamReady(): Boolean {
        val teamToCheck =
            if (whoseTurn == Constants.WhoseTurn.BOTTOM) teamBottom else teamTop

        teamToCheck.forEach { if (it == null) return false }

        restoringPlayers = false
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

        if (GameLogic.isAttacked(firstCardInDeck, victimTeam, spotIndex) && spotIndex == 5) {
            // Goal at goalie
            return true
        } else if (GameLogic.isAttacked(firstCardInDeck, victimTeam, spotIndex)) {
            return true
        }

        return false
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
}
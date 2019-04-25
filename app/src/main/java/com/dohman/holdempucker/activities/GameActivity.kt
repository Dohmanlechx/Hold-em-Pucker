package com.dohman.holdempucker.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.animation.doOnEnd
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.dohman.holdempucker.activities.viewmodels.GameViewModel
import com.dohman.holdempucker.R
import com.dohman.holdempucker.cards.Card
import com.dohman.holdempucker.util.AnimationUtil
import com.dohman.holdempucker.util.Constants
import com.dohman.holdempucker.util.Constants.Companion.teamBottomViews
import com.dohman.holdempucker.util.Constants.Companion.isAnimationRunning
import com.dohman.holdempucker.util.Constants.Companion.isOngoingGame
import com.dohman.holdempucker.util.Constants.Companion.period
import com.dohman.holdempucker.util.Constants.Companion.teamBottom
import com.dohman.holdempucker.util.Constants.Companion.teamBottomScore
import com.dohman.holdempucker.util.Constants.Companion.teamTop
import com.dohman.holdempucker.util.Constants.Companion.teamTopScore
import com.dohman.holdempucker.util.Constants.Companion.teamTopViews
import com.dohman.holdempucker.util.Constants.Companion.whoseTeamStartedLastPeriod
import com.dohman.holdempucker.util.Constants.Companion.whoseTurn
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var vm: GameViewModel

    private var flipViewOriginalX: Float = 0f
    private var flipViewOriginalY: Float = 0f
    private var flipViewBtmOriginalX: Float = 0f
    private var flipViewBtmOriginalY: Float = 0f
    private var flipViewTopOriginalX: Float = 0f
    private var flipViewTopOriginalY: Float = 0f

    private var tempGoalieCard: Card? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_game)
        vm = ViewModelProviders.of(this).get(GameViewModel::class.java)

        vm.halfTimeNotifier.observe(this, Observer {
            clearAllCards(it)
            addGoalie(true)
        })
        vm.whoseTurnNotifier.observe(this, Observer { AnimationUtil.togglePuck(puck, it)?.start() })
        vm.pickedCardNotifier.observe(this, Observer { flipNewCard(it) })
        vm.cardsCountNotifier.observe(this, Observer { cards_left.text = it.toString() })
        vm.badCardNotifier.observe(
            this,
            Observer {
                flipNewCard(vm.resIdOfCard(vm.firstCardInDeck), isBadCard = true)
            })
        // vm.nfyBtmGoalie.observe(this, Observer { if (it) addGoalie(bottom = true)/*card_bm_goalie.setImageResource(R.drawable.red_back)*/ })
        //   vm.nfyTopGoalie.observe(this, Observer { if (it) addGoalie(bottom = false)/*card_top_goalie.setImageResource(R.drawable.red_back)*/ })

        vm.updateScores(top_team_score, bm_team_score)

        flip_view.post {
            flip_view.bringToFront()
            flipViewOriginalX = flip_view.x
            flipViewOriginalY = flip_view.y
        }

        flip_btm_goalie.post {
            flipViewBtmOriginalX = flip_btm_goalie.x
            flipViewBtmOriginalY = flip_btm_goalie.y
        }

        flip_top_goalie.post {
            flipViewTopOriginalX = flip_top_goalie.x
            flipViewTopOriginalY = flip_top_goalie.y
        }

        btn_debug.text = "Start!"
        btn_debug.setOnClickListener {
            addGoalie(true)
        }

        setOnClickListeners()
        storeAllViews()
    }

    private fun restoreFlipViewPosition() {
        flip_view.rotation = 0f
        flip_view.x = flipViewOriginalX
        flip_view.y = flipViewOriginalY
        flip_btm_goalie.x = flipViewBtmOriginalX
        flip_btm_goalie.y = flipViewBtmOriginalY
        flip_top_goalie.x = flipViewTopOriginalX
        flip_top_goalie.y = flipViewTopOriginalY
    }

    private fun storeAllViews() {
        teamBottomViews.apply {
            add(card_bm_forward_left)
            add(card_bm_center)
            add(card_bm_forward_right)
            add(card_bm_defender_left)
            add(card_bm_defender_right)
            add(card_bm_goalie)
        }

        teamTopViews.apply {
            add(card_top_forward_left)
            add(card_top_center)
            add(card_top_forward_right)
            add(card_top_defender_left)
            add(card_top_defender_right)
            add(card_top_goalie)
        }
    }

    /*
    * Animation initializer
    * */

    private fun flipNewCard(resId: Int, isBadCard: Boolean = false) {
        if (flip_view.isBackSide) {
            card_deck.setImageResource(resId)
            card_picked.setImageResource(R.drawable.red_back_vertical)
        } else {
            card_picked.setImageResource(resId)
            card_deck.setImageResource(R.drawable.red_back_vertical)
        }

        AnimationUtil.flipView(flip_view, cards_left, isBadCard) {
            // If it is bad card, this runs
            AnimationUtil.badCardOut(
                flip_view,
                { vm.firstCardInDeck },
                { vm.notifyToggleTurn() },
                { restoreFlipViewPosition() },
                { vm.removeCardFromDeck() },
                { vm.isThisTeamReady() },
                { vm.triggerBadCard() })?.start()
        }
    }

    private fun addGoalie(bottom: Boolean, doNotFlip: Boolean = false, doRemoveCardFromDeck: Boolean = false) {
        // ONLY adding view. No real goalie card is assigning to that team by this function.

        val view = if (bottom) card_bm_goalie else card_top_goalie

        card_deck.setImageResource(R.drawable.red_back_vertical)
        card_picked.setImageResource(R.drawable.red_back_vertical)

        AnimationUtil.addGoalie(
            flipView = flip_view,
            goalieView = view,
            flipViewOriginalX = flipViewOriginalX - 60f,
            flipViewOriginalY = flipViewOriginalY
        ).apply {
            doOnEnd {
                restoreFlipViewPosition()
                isAnimationRunning = false
                vm.onGoalieAddedAnimationEnd(view)
                if (card_top_goalie.tag != Integer.valueOf(R.drawable.red_back)) addGoalie(bottom = false) else {
                    if (!doNotFlip) flipNewCard(vm.resIdOfCard(vm.firstCardInDeck))
                    if (doRemoveCardFromDeck) vm.removeCardFromDeck()
                    vm.showPickedCard()
                }
            }
            start()
        }
    }

    private fun animateAddPlayer(targetView: AppCompatImageView, team: Array<Card?>, spotIndex: Int) {
        AnimationUtil.addPlayer(
            flipView = flip_view,
            targetView = targetView
        ).apply {
            doOnEnd {
                restoreFlipViewPosition()
                vm.onPlayerAddedAnimationEnd(targetView, team, spotIndex)
                isAnimationRunning = false
            }
            start()
        }

    }

    private fun animateAttack(targetView: AppCompatImageView) {
        val victimX = targetView.x
        val victimY = targetView.y

        AnimationUtil.attack(flipView = flip_view, targetView = targetView, isAttacking = true).apply {
            doOnEnd {
                targetView.bringToFront()
                flip_view.bringToFront()

                AnimationUtil.attack(flipView = flip_view, targetView = targetView, isAttacking = false).apply {
                    doOnEnd {
                        targetView.x = victimX
                        targetView.y = victimY
                        restoreFlipViewPosition()
                        vm.onAttackedAnimationEnd(targetView)
                        isAnimationRunning = false
                    }
                    start()
                }
            }
            start()
        }
    }

    /*
    * Game management
    * */

    private fun addPlayer(view: AppCompatImageView, team: Array<Card?>, spotIndex: Int) {
        if (vm.canAddPlayerView(view, team, spotIndex))
            animateAddPlayer(view, team, spotIndex)
    }

    private fun attackPlayer(victimTeam: Array<Card?>, spotIndex: Int, victimView: AppCompatImageView) {
        AnimationUtil.stopAllPulsingCardAnimations()
        if (spotIndex == 5) {
            // Attacking goalie
            if (vm.canAttack(victimTeam, spotIndex, victimView)) {
                if (whoseTurn == Constants.WhoseTurn.BOTTOM) {
                    if (flip_top_goalie.isBackSide) {
                        flip_top_goalie_back.setImageResource(R.drawable.red_back)
                        flip_top_goalie_front.setImageBitmap(vm.getRotatedBitmap(tempGoalieCard))
                    } else {
                        flip_top_goalie_front.setImageResource(R.drawable.red_back)
                        flip_top_goalie_back.setImageBitmap(vm.getRotatedBitmap(tempGoalieCard))
                    }

                    flip_top_goalie.visibility = View.VISIBLE

                    victimView.setImageResource(android.R.color.transparent)
                    victimView.tag = Integer.valueOf(android.R.color.transparent)

                    AnimationUtil.scoredAtGoalie(
                        flip_view,
                        flip_top_goalie,
                        { vm.notifyToggleTurn() },
    /*                    { vm.removeCardFromDeck() },*/
                        { restoreFlipViewPosition() },
                        { addGoalie(bottom = false, doNotFlip = true, doRemoveCardFromDeck = true) },
                        { vm.updateScores(top_team_score, bm_team_score) }
                    ).start()

                } else {
                    if (flip_btm_goalie.isBackSide) {
                        flip_btm_goalie_back.setImageResource(R.drawable.red_back)
                        flip_btm_goalie_front.setImageBitmap(vm.getRotatedBitmap(tempGoalieCard))
                    } else {
                        flip_btm_goalie_front.setImageResource(R.drawable.red_back)
                        flip_btm_goalie_back.setImageBitmap(vm.getRotatedBitmap(tempGoalieCard))
                    }

                    flip_btm_goalie.visibility = View.VISIBLE

                    victimView.setImageResource(android.R.color.transparent)
                    victimView.tag = Integer.valueOf(android.R.color.transparent)

                    AnimationUtil.scoredAtGoalie(
                        flip_view,
                        flip_btm_goalie,
                        { vm.notifyToggleTurn() },
                    /*    { vm.removeCardFromDeck() },*/
                        { restoreFlipViewPosition() },
                        { addGoalie(bottom = true, doNotFlip = true, doRemoveCardFromDeck = true) },
                        { vm.updateScores(top_team_score, bm_team_score) }
                    ).start()
                }
            }
        } else {
            // Attacking another player
            if (vm.canAttack(victimTeam, spotIndex, victimView))
                animateAttack(victimView)
        }
    }

    private fun prepareGoalieSaved(victimView: AppCompatImageView) {
        AnimationUtil.stopAllPulsingCardAnimations()

        if (whoseTurn == Constants.WhoseTurn.BOTTOM) {
            if (flip_top_goalie.isBackSide) {
                flip_top_goalie_back.setImageResource(R.drawable.red_back)
                flip_top_goalie_front.setImageBitmap(vm.getRotatedBitmap(tempGoalieCard))
            } else {
                flip_top_goalie_front.setImageResource(R.drawable.red_back)
                flip_top_goalie_back.setImageBitmap(vm.getRotatedBitmap(tempGoalieCard))
            }

            flip_top_goalie.visibility = View.VISIBLE

            victimView.setImageResource(android.R.color.transparent)
            victimView.tag = Integer.valueOf(android.R.color.transparent)

            AnimationUtil.goalieSaved(
                flip_view,
                flip_top_goalie,
                teamTop,
                { vm.notifyToggleTurn() },
                { vm.removeCardFromDeck() },
                { restoreFlipViewPosition() },
                { addGoalie(bottom = false, doNotFlip = true) }
            ).start()

        } else {
            if (flip_btm_goalie.isBackSide) {
                flip_btm_goalie_back.setImageResource(R.drawable.red_back)
                flip_btm_goalie_front.setImageBitmap(vm.getRotatedBitmap(tempGoalieCard))
            } else {
                flip_btm_goalie_front.setImageResource(R.drawable.red_back)
                flip_btm_goalie_back.setImageBitmap(vm.getRotatedBitmap(tempGoalieCard))
            }

            flip_btm_goalie.visibility = View.VISIBLE

            victimView.setImageResource(android.R.color.transparent)
            victimView.tag = Integer.valueOf(android.R.color.transparent)

            AnimationUtil.goalieSaved(
                flip_view,
                flip_btm_goalie,
                teamBottom,
                { vm.notifyToggleTurn() },
                { vm.removeCardFromDeck() },
                { restoreFlipViewPosition() },
                { addGoalie(bottom = true, doNotFlip = true) }
            ).start()
        }
    }

    private fun gameOver() {
        card_top_forward_left.setOnClickListener(null)
        card_top_center.setOnClickListener(null)
        card_top_forward_right.setOnClickListener(null)
        card_top_defender_left.setOnClickListener(null)
        card_top_defender_right.setOnClickListener(null)
        card_top_goalie.setOnClickListener(null)

        card_bm_forward_left.setOnClickListener(null)
        card_bm_center.setOnClickListener(null)
        card_bm_forward_right.setOnClickListener(null)
        card_bm_defender_left.setOnClickListener(null)
        card_bm_defender_right.setOnClickListener(null)
        card_bm_goalie.setOnClickListener(null)

        btn_debug.setOnClickListener(null)
    }

    private fun clearAllCards(nextPeriod: Int) {
        period += nextPeriod

        if (period > 3 && (teamBottomScore != teamTopScore)) {
            if (teamBottomScore > teamTopScore) btn_debug.text = "Bottom won!" else btn_debug.text = "Top won!"
            gameOver()
        } else {
            btn_debug.text = "Period: $period"

            whoseTurn =
                if (whoseTeamStartedLastPeriod == Constants.WhoseTurn.BOTTOM) Constants.WhoseTurn.TOP else Constants.WhoseTurn.BOTTOM

            card_top_forward_left.setImageResource(android.R.color.transparent)
            card_top_center.setImageResource(android.R.color.transparent)
            card_top_forward_right.setImageResource(android.R.color.transparent)
            card_top_defender_left.setImageResource(android.R.color.transparent)
            card_top_defender_right.setImageResource(android.R.color.transparent)
            card_top_goalie.setImageResource(android.R.color.transparent)
            card_top_goalie.tag = null

            card_bm_forward_left.setImageResource(android.R.color.transparent)
            card_bm_center.setImageResource(android.R.color.transparent)
            card_bm_forward_right.setImageResource(android.R.color.transparent)
            card_bm_defender_left.setImageResource(android.R.color.transparent)
            card_bm_defender_right.setImageResource(android.R.color.transparent)
            card_bm_goalie.setImageResource(android.R.color.transparent)
            card_bm_goalie.tag = null
        }
    }

    /*
    * OnClicklisteners
    * */

    private fun setOnClickListeners() {
        card_top_forward_left.setOnClickListener(this)
        card_top_center.setOnClickListener(this)
        card_top_forward_right.setOnClickListener(this)
        card_top_defender_left.setOnClickListener(this)
        card_top_defender_right.setOnClickListener(this)
        card_top_goalie.setOnClickListener(this)

        card_bm_forward_left.setOnClickListener(this)
        card_bm_center.setOnClickListener(this)
        card_bm_forward_right.setOnClickListener(this)
        card_bm_defender_left.setOnClickListener(this)
        card_bm_defender_right.setOnClickListener(this)
        card_bm_goalie.setOnClickListener(this)

    }

    override fun onClick(v: View) {
        val spotIndex: Int
        if (isAnimationRunning) return
        if (isOngoingGame) {
//            if (whoseTurn == WhoseTurn.BOTTOM) {
//                spotIndex = when (v.id) {
//                    R.id.card_top_forward_left -> 0
//                    R.id.card_top_center -> 1
//                    R.id.card_top_forward_right -> 2
//                    R.id.card_top_defender_left -> 3
//                    R.id.card_top_defender_right -> 4
//                    R.id.card_top_goalie -> 5
//                    else -> return
//                }
//            } else {
//                spotIndex = when (v.id) {
//                    R.id.card_bm_forward_left -> 0
//                    R.id.card_bm_center -> 1
//                    R.id.card_bm_forward_right -> 2
//                    R.id.card_bm_defender_left -> 3
//                    R.id.card_bm_defender_right -> 4
//                    R.id.card_bm_goalie -> 5
//                    else -> return
//                }
//            }
//            attackPlayer(findViewById(v.id), if (whoseTurn == WhoseTurn.BOTTOM) teamTop else teamBottom, spotIndex)
            if (whoseTurn == Constants.WhoseTurn.BOTTOM) {
                when (v.id) {
                    R.id.card_top_forward_left -> {
                        attackPlayer(teamTop, 0, card_top_forward_left)
                    }
                    R.id.card_top_center -> {
                        attackPlayer(teamTop, 1, card_top_center)
                    }
                    R.id.card_top_forward_right -> {
                        attackPlayer(teamTop, 2, card_top_forward_right)
                    }
                    R.id.card_top_defender_left -> {
                        if (vm.areEnoughForwardsOut(teamTop, 3))
                            attackPlayer(teamTop, 3, card_top_defender_left)
                    }
                    R.id.card_top_defender_right -> {
                        if (vm.areEnoughForwardsOut(teamTop, 4))
                            attackPlayer(teamTop, 4, card_top_defender_right)
                    }
                    R.id.card_top_goalie -> {
                        if (vm.isAtLeastOneDefenderOut(teamTop)) {
                            tempGoalieCard = teamTop[5]
                            if (vm.canAttack(teamTop, 5, card_top_goalie)) attackPlayer(teamTop, 5, card_top_goalie)
                            else prepareGoalieSaved(card_top_goalie)
                        }
                    }
                }
            } else {
                when (v.id) {
                    R.id.card_bm_forward_left -> {
                        attackPlayer(teamBottom, 0, card_bm_forward_left)
                    }
                    R.id.card_bm_center -> {
                        attackPlayer(teamBottom, 1, card_bm_center)
                    }
                    R.id.card_bm_forward_right -> {
                        attackPlayer(teamBottom, 2, card_bm_forward_right)
                    }
                    R.id.card_bm_defender_left -> {
                        if (vm.areEnoughForwardsOut(teamBottom, 3))
                            attackPlayer(teamBottom, 3, card_bm_defender_left)
                    }
                    R.id.card_bm_defender_right -> {
                        if (vm.areEnoughForwardsOut(teamBottom, 4))
                            attackPlayer(teamBottom, 4, card_bm_defender_right)
                    }
                    R.id.card_bm_goalie -> {
                        if (vm.isAtLeastOneDefenderOut(teamBottom)) {
                            tempGoalieCard = teamBottom[5]
                            if (vm.canAttack(teamBottom, 5, card_bm_goalie)) attackPlayer(teamBottom, 5, card_bm_goalie)
                            else prepareGoalieSaved(card_bm_goalie)
                        }
                    }
                }
            }
        } else {
            if (whoseTurn == Constants.WhoseTurn.BOTTOM) {
                spotIndex = when (v.id) {
                    R.id.card_bm_forward_left -> 0
                    R.id.card_bm_center -> 1
                    R.id.card_bm_forward_right -> 2
                    R.id.card_bm_defender_left -> 3
                    R.id.card_bm_defender_right -> 4
                    else -> return
                }
            } else {
                spotIndex = when (v.id) {
                    R.id.card_top_forward_left -> 0
                    R.id.card_top_center -> 1
                    R.id.card_top_forward_right -> 2
                    R.id.card_top_defender_left -> 3
                    R.id.card_top_defender_right -> 4
                    else -> return
                }
            }
            addPlayer(
                findViewById(v.id),
                if (whoseTurn == Constants.WhoseTurn.BOTTOM) teamBottom else teamTop,
                spotIndex
            )
        }
    }
}

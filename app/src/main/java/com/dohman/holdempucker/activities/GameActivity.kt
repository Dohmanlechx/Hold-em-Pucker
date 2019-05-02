package com.dohman.holdempucker.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.animation.doOnEnd
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearSnapHelper
import com.dohman.holdempucker.activities.viewmodels.GameViewModel
import com.dohman.holdempucker.R
import com.dohman.holdempucker.cards.Card
import com.dohman.holdempucker.ui.MessageTextItem
import com.dohman.holdempucker.ui.overrides.SpeedyLinearLayoutManager
import com.dohman.holdempucker.util.AnimationUtil
import com.dohman.holdempucker.util.Constants
import com.dohman.holdempucker.util.Constants.Companion.teamBottomViews
import com.dohman.holdempucker.util.Constants.Companion.isAnimationRunning
import com.dohman.holdempucker.util.Constants.Companion.isOngoingGame
import com.dohman.holdempucker.util.Constants.Companion.justShotAtGoalie
import com.dohman.holdempucker.util.Constants.Companion.period
import com.dohman.holdempucker.util.Constants.Companion.teamBottom
import com.dohman.holdempucker.util.Constants.Companion.teamBottomScore
import com.dohman.holdempucker.util.Constants.Companion.teamTop
import com.dohman.holdempucker.util.Constants.Companion.teamTopScore
import com.dohman.holdempucker.util.Constants.Companion.teamTopViews
import com.dohman.holdempucker.util.Constants.Companion.whoseTeamStartedLastPeriod
import com.dohman.holdempucker.util.Constants.Companion.whoseTurn
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var vm: GameViewModel

    private val itemAdapter = ItemAdapter<AbstractItem<*, *>>()
    private val fastAdapter = FastAdapter.with<AbstractItem<*, *>, ItemAdapter<AbstractItem<*, *>>>(itemAdapter)

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

        vm.messageNotifier.observe(this, Observer { updateMessageBox(it.first, it.second) })
        vm.halfTimeNotifier.observe(this, Observer {
            if (isNextPeriodReady(it)) addGoalieView(true, withStartDelay = true)
        })
        vm.whoseTurnNotifier.observe(this, Observer { AnimationUtil.togglePuckAnimation(puck, it)?.start() })
        vm.pickedCardNotifier.observe(this, Observer { flipNewCard(it) })
        vm.cardsCountNotifier.observe(this, Observer { cards_left.text = it.toString() })
        vm.badCardNotifier.observe(
            this,
            Observer {
                flipNewCard(vm.resIdOfCard(vm.firstCardInDeck), isBadCard = true)
                vm.notifyMessage("Aw, too\nweak card!\nIt goes\nout!")
            })

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

        computer_lamp.post {
            AnimationUtil.startLampAnimation(computer_lamp)
        }

        setupMessageRecycler()
        setOnClickListeners()
        storeAllViews()

        whole_view.setOnClickListener {
            teamBottomScore = 0
            teamTopScore = 0
            bm_team_score.text = teamBottomScore.toString()
            top_team_score.text = teamTopScore.toString()
            period = 1

            teamBottomViews.forEach { view -> view.setImageResource(android.R.color.transparent) }
            teamTopViews.forEach { view -> view.setImageResource(android.R.color.transparent) }

            card_top_goalie.tag = null
            card_bm_goalie.tag = null

            addGoalieView(true)
            it.visibility = View.GONE
        }
    }

    /*
    * Views management
    * */

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

    private fun setupMessageRecycler() {
        itemAdapter.clear()

        v_recycler.itemAnimator = null
        v_recycler.layoutManager = SpeedyLinearLayoutManager(
            applicationContext,
            SpeedyLinearLayoutManager.VERTICAL,
            false
        )
        v_recycler.adapter = fastAdapter
        v_recycler.isNestedScrollingEnabled = true

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(v_recycler)

        updateMessageBox("Press\nanywhere\nto start\nthe game!\nPeriod: $period", isNeutralMessage = true)
    }

    private fun updateMessageBox(message: String, isNeutralMessage: Boolean = false) {
        //itemAdapter.clear()

        if (!isNeutralMessage) itemAdapter.add(MessageTextItem(message, whoseTurn == Constants.WhoseTurn.TOP))
        else itemAdapter.add(MessageTextItem(message, isNeutralMessage = true))

        v_recycler.adapter?.itemCount?.minus(1)?.let { v_recycler.smoothScrollToPosition(it) }
    }

    /*
    * Animation initializer
    * */

    private fun flipNewCard(resId: Int, isBadCard: Boolean = false) {
        vm.setImagesOnFlipView(flip_view, card_deck, card_picked, resId, null, isVertical = true)

        AnimationUtil.flipPlayingCard(flip_view, cards_left, isBadCard, vm.cardDeck.size > 50, {
            // If it is bad card, this runs
            AnimationUtil.badCardOutAnimation(
                flip_view,
                { vm.firstCardInDeck },
                { vm.notifyToggleTurn() },
                { restoreFlipViewPosition() },
                { vm.removeCardFromDeck() },
                { vm.isThisTeamReady() },
                { vm.triggerBadCard() },
                { message -> vm.notifyMessage(message) })?.start()
        },
            { setOnClickListeners() },
            { message -> vm.notifyMessage(message) })
    }

    private fun addGoalieView(
        bottom: Boolean,
        doNotFlip: Boolean = false,
        doRemoveCardFromDeck: Boolean = false,
        withStartDelay: Boolean = false
    ) {
        // ONLY adding view. No real goalie card is assigning to that team by this function.
        removeAllOnClickListeners()

        val view = if (bottom) card_bm_goalie else card_top_goalie

        card_deck.setImageResource(R.drawable.red_back_vertical)
        card_picked.setImageResource(R.drawable.red_back_vertical)

        AnimationUtil.addGoalieAnimation(
            flipView = flip_view,
            goalieView = view,
            flipViewOriginalX = flipViewOriginalX - 60f,
            flipViewOriginalY = flipViewOriginalY
        ).apply {
            if (withStartDelay) startDelay = 1500

            doOnEnd {
                restoreFlipViewPosition()
                vm.onGoalieAddedAnimationEnd(view)
                if (card_top_goalie.tag != Integer.valueOf(R.drawable.red_back)) addGoalieView(bottom = false) else {
                    if (!doNotFlip) flipNewCard(vm.resIdOfCard(vm.firstCardInDeck))
                    if (doRemoveCardFromDeck) vm.removeCardFromDeck()
                    vm.showPickedCard()
                    cards_left.visibility = View.VISIBLE
                }
                isAnimationRunning = false
            }
            start()
        }
    }

    private fun animateAddPlayer(targetView: AppCompatImageView, team: Array<Card?>, spotIndex: Int) {
        removeAllOnClickListeners()
        AnimationUtil.addPlayerAnimation(
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
        removeAllOnClickListeners()
        AnimationUtil.stopAllPulsingCardAnimations()

        val victimX = targetView.x
        val victimY = targetView.y

        AnimationUtil.attackAnimation(flipView = flip_view, targetView = targetView, isAttacking = true).apply {
            doOnEnd {
                targetView.bringToFront()
                flip_view.bringToFront()

                AnimationUtil.attackAnimation(flipView = flip_view, targetView = targetView, isAttacking = false)
                    .apply {
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

    private fun attackPlayer(victimTeam: Array<Card?>, spotIndex: Int, victimView: AppCompatImageView) {
        if (spotIndex == 5) {
            // Attacking goalie
            justShotAtGoalie = true

            if (vm.canAttack(victimTeam, spotIndex, victimView)) {
                removeAllOnClickListeners()
                AnimationUtil.stopAllPulsingCardAnimations()

                notifyMessageAttackingGoalie()

                if (whoseTurn == Constants.WhoseTurn.BOTTOM) {
                    vm.setImagesOnFlipView(
                        flip_top_goalie,
                        flip_top_goalie_front,
                        flip_top_goalie_back,
                        null,
                        vm.getRotatedBitmap(tempGoalieCard),
                        isVertical = false
                    )

                    victimView.setImageResource(android.R.color.transparent)
                    victimView.tag = Integer.valueOf(android.R.color.transparent)

                    AnimationUtil.scoredAtGoalieAnimation(
                        flip_view,
                        flip_top_goalie,
                        tempGoalieCard,
                        { vm.notifyToggleTurn() },
                        { restoreFlipViewPosition() },
                        { addGoalieView(bottom = false, doNotFlip = true, doRemoveCardFromDeck = true) },
                        { vm.updateScores(top_team_score, bm_team_score) },
                        { message -> vm.notifyMessage(message) }
                    ).start()

                } else {
                    vm.setImagesOnFlipView(
                        flip_btm_goalie,
                        flip_btm_goalie_front,
                        flip_btm_goalie_back,
                        null,
                        vm.getRotatedBitmap(tempGoalieCard),
                        isVertical = false
                    )

                    victimView.setImageResource(android.R.color.transparent)
                    victimView.tag = Integer.valueOf(android.R.color.transparent)

                    AnimationUtil.scoredAtGoalieAnimation(
                        flip_view,
                        flip_btm_goalie,
                        tempGoalieCard,
                        { vm.notifyToggleTurn() },
                        { restoreFlipViewPosition() },
                        { addGoalieView(bottom = true, doNotFlip = true, doRemoveCardFromDeck = true) },
                        { vm.updateScores(top_team_score, bm_team_score) },
                        { message -> vm.notifyMessage(message) }
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
        removeAllOnClickListeners()
        AnimationUtil.stopAllPulsingCardAnimations()

        justShotAtGoalie = true

        notifyMessageAttackingGoalie()

        if (whoseTurn == Constants.WhoseTurn.BOTTOM) {
            vm.setImagesOnFlipView(
                flip_top_goalie,
                flip_top_goalie_front,
                flip_top_goalie_back,
                null,
                vm.getRotatedBitmap(tempGoalieCard),
                isVertical = false
            )

            victimView.setImageResource(android.R.color.transparent)
            victimView.tag = Integer.valueOf(android.R.color.transparent)

            AnimationUtil.goalieSavedAnimation(
                flip_view,
                flip_top_goalie,
                tempGoalieCard,
                teamTop,
                { vm.notifyToggleTurn() },
                { restoreFlipViewPosition() },
                { addGoalieView(bottom = false, doNotFlip = true) },
                { message -> vm.notifyMessage(message) }
            ).start()

        } else {
            vm.setImagesOnFlipView(
                flip_btm_goalie,
                flip_btm_goalie_front,
                flip_btm_goalie_back,
                null,
                vm.getRotatedBitmap(tempGoalieCard),
                isVertical = false
            )

            victimView.setImageResource(android.R.color.transparent)
            victimView.tag = Integer.valueOf(android.R.color.transparent)

            AnimationUtil.goalieSavedAnimation(
                flip_view,
                flip_btm_goalie,
                tempGoalieCard,
                teamBottom,
                { vm.notifyToggleTurn() },
                { restoreFlipViewPosition() },
                { addGoalieView(bottom = true, doNotFlip = true) },
                { message -> vm.notifyMessage(message) }
            ).start()
        }
    }

    private fun notifyMessageAttackingGoalie() {
        vm.firstCardInDeck.let {
            val rankInterpreted = when (it.rank) {
                11 -> "Jack"
                12 -> "Queen"
                13 -> "King"
                14 -> "Ace"
                else -> it.rank.toString()
            }

            vm.notifyMessage(
                "${it.suit.toString().toLowerCase().capitalize()} $rankInterpreted\nattacks the\ngoalie..."
            )
        }
    }

    /*
    * Game management
    * */

    private fun isNextPeriodReady(nextPeriod: Int): Boolean {
        cards_left.visibility = View.GONE

        period += nextPeriod

        if (period > 3 && (teamBottomScore != teamTopScore)) {
            when {
                teamBottomScore > teamTopScore -> vm.notifyMessage(
                    "Team Bottom\nwon with\n$teamBottomScore-$teamTopScore!\nNew game?\nPress anywhere!",
                    isNeutralMessage = true
                )
                teamBottomScore < teamTopScore -> vm.notifyMessage(
                    "Team Top\nwon with\n$teamTopScore-$teamBottomScore!\nNew game?\nPress anywhere!",
                    isNeutralMessage = true
                )
            }

            removeAllOnClickListeners()
            whole_view.visibility = View.VISIBLE
            return false
        } else {
            if (period > 3) vm.notifyMessage(
                "Overtime!\nPlay until\nall cards\nare out.\nPeriod: $period",
                isNeutralMessage = true
            )

            whoseTurn =
                if (whoseTeamStartedLastPeriod == Constants.WhoseTurn.BOTTOM) Constants.WhoseTurn.TOP else Constants.WhoseTurn.BOTTOM

            teamBottomViews.forEach { it.setImageResource(android.R.color.transparent) }
            teamTopViews.forEach { it.setImageResource(android.R.color.transparent) }

            card_top_goalie.tag = null
            card_bm_goalie.tag = null

            return true
        }
    }

    /*
    * OnClickListeners
    * */

    private fun setOnClickListeners() {
        teamBottomViews.forEach { it.setOnClickListener(this) }
        teamTopViews.forEach { it.setOnClickListener(this) }
    }

    private fun removeAllOnClickListeners() {
        teamBottomViews.forEach { it.setOnClickListener(null) }
        teamTopViews.forEach { it.setOnClickListener(null) }
    }

    override fun onClick(v: View) {
        val spotIndex: Int
        if (isOngoingGame) {
            if (isAnimationRunning || v.tag == Integer.valueOf(android.R.color.transparent)) return
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

            val view = findViewById<AppCompatImageView>(v.id)
            val team = if (whoseTurn == Constants.WhoseTurn.BOTTOM) teamBottom else teamTop

            if (vm.canAddPlayerView(view, team, spotIndex))
                animateAddPlayer(view, team, spotIndex)
        }
    }
}

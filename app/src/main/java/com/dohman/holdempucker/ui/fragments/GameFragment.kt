package com.dohman.holdempucker.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearSnapHelper
import com.dohman.holdempucker.R
import com.dohman.holdempucker.cards.Card
import com.dohman.holdempucker.ui.MessageTextItem
import com.dohman.holdempucker.ui.overrides.SpeedyLinearLayoutManager
import com.dohman.holdempucker.util.Constants
import com.dohman.holdempucker.util.Constants.Companion.isOngoingGame
import com.dohman.holdempucker.util.Constants.Companion.isJustShotAtGoalie
import com.dohman.holdempucker.util.Constants.Companion.period
import com.dohman.holdempucker.util.Constants.Companion.possibleMovesIndexes
import com.dohman.holdempucker.util.Constants.Companion.isRestoringPlayers
import com.dohman.holdempucker.util.Constants.Companion.teamBottom
import com.dohman.holdempucker.util.Constants.Companion.teamBottomScore
import com.dohman.holdempucker.util.Constants.Companion.teamTop
import com.dohman.holdempucker.util.Constants.Companion.teamTopScore
import com.dohman.holdempucker.util.Constants.Companion.whoseTeamStartedLastPeriod
import com.dohman.holdempucker.util.Constants.Companion.whoseTurn
import com.dohman.holdempucker.util.GameLogic
import com.dohman.holdempucker.util.Animations
import com.dohman.holdempucker.util.Constants.Companion.isVsBot
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.android.synthetic.main.computer_layout.*
import kotlinx.android.synthetic.main.game_fragment.*

class GameFragment : Fragment(), View.OnClickListener {
    private lateinit var vm: GameViewModel

    private val itemAdapter = ItemAdapter<AbstractItem<*, *>>()
    private val fastAdapter = FastAdapter.with<AbstractItem<*, *>, ItemAdapter<AbstractItem<*, *>>>(itemAdapter)

    private var fvMainX: Float = 0f
    private var fvMainY: Float = 0f
    private var fvGoalieBtmX: Float = 0f
    private var fvGoalieBtmY: Float = 0f
    private var fvGoalieTopX: Float = 0f
    private var fvGoalieTopY: Float = 0f

    private var teamBottomViews = mutableListOf<AppCompatImageView>()
    private var teamTopViews = mutableListOf<AppCompatImageView>()

    private var tempGoalieCard: Card? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        vm = ViewModelProviders.of(this).get(GameViewModel::class.java)

        // Observables
        vm.messageNotifier.observe(this, Observer { updateMessageBox(it.first, it.second) })
        vm.halfTimeNotifier.observe(this, Observer {
            if (isNextPeriodReady(it)) addGoalieView(true, withStartDelay = true)
        })
        vm.whoseTurnNotifier.observe(this, Observer { Animations.animatePuck(puck, it) })
        vm.pickedCardNotifier.observe(this, Observer { flipNewCard(it) })
        vm.cardsCountNotifier.observe(this, Observer { cards_left.text = it.toString() })
        vm.badCardNotifier.observe(
            this,
            Observer {
                flipNewCard(vm.resIdOfCard(vm.firstCardInDeck), isBadCard = true)
                vm.notifyMessage("Aw, too weak card! It goes out!")
            })
        // End of Observables

        return inflater.inflate(R.layout.game_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.updateScores(top_team_score, bm_team_score)

        var width = 0
        var height = 0

        flip_view.post {
            flip_view.bringToFront()
            fvMainX = flip_view.x
            fvMainY = flip_view.y

            // Getting width and height for Goalie FlipViews
            flip_view.apply {
                width = this.width
                height = this.height
            }
        }

        flip_btm_goalie.post {
            card_bm_goalie.apply {
                layoutParams.width = height
                layoutParams.height = width
            }

            background_bm_goalie.apply {
                layoutParams.width = height
                layoutParams.height = width
            }

            flip_btm_goalie.apply {
                layoutParams.width = height
                layoutParams.height = width
                bringToFront()
            }
        }

        flip_top_goalie.post {
            card_top_goalie.apply {
                layoutParams.width = height
                layoutParams.height = width
            }

            background_top_goalie.apply {
                layoutParams.width = height
                layoutParams.height = width
            }

            flip_top_goalie.apply {
                layoutParams.width = height
                layoutParams.height = width
                bringToFront()
            }

        }

        computer_lamp.post {
            Animations.animateLamp(computer_lamp)
        }

        setupMessageRecycler()

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

    override fun onResume() {
        super.onResume()
        storeAllViews()
        setOnClickListeners()
    }

    override fun onPause() {
        super.onPause()
        clearListsOfViews()
    }

    /*
    * Views management
    * */

    private fun restoreFlipViewPosition() {
        flip_view.rotation = 0f
        flip_view.x = fvMainX
        flip_view.y = fvMainY
        flip_btm_goalie.x = fvGoalieBtmX
        flip_btm_goalie.y = fvGoalieBtmY
        flip_top_goalie.x = fvGoalieTopX
        flip_top_goalie.y = fvGoalieTopY
    }

    private fun storeAllViews() {
        if (teamBottomViews.isEmpty()) {
            teamBottomViews.apply {
                add(card_bm_forward_left)
                add(card_bm_center)
                add(card_bm_forward_right)
                add(card_bm_defender_left)
                add(card_bm_defender_right)
                add(card_bm_goalie)
            }
        }

        if (teamTopViews.isEmpty()) {
            teamTopViews.apply {
                add(card_top_forward_left)
                add(card_top_center)
                add(card_top_forward_right)
                add(card_top_defender_left)
                add(card_top_defender_right)
                add(card_top_goalie)
            }
        }
    }

    // FIXME! This fun is only temporary. Leading into crashes. Cuz Constant storage of views... remove later!
    private fun clearListsOfViews() {
        teamBottomViews.clear()
        teamTopViews.clear()
    }

    private fun setupMessageRecycler() {
        itemAdapter.clear()

        v_recycler.itemAnimator = null
        v_recycler.layoutManager = SpeedyLinearLayoutManager(
            requireContext(),
            SpeedyLinearLayoutManager.VERTICAL,
            false
        )
        v_recycler.adapter = fastAdapter
        v_recycler.isNestedScrollingEnabled = true

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(v_recycler)

        updateMessageBox("Press anywhere to start the game! Period: $period", isNeutralMessage = true)
    }

    private fun updateMessageBox(message: String, isNeutralMessage: Boolean = false) {
        if (!isNeutralMessage) itemAdapter.add(
            MessageTextItem(
                message,
//                mikePenzPositions,
//                { pos -> mikePenzPositions.add(pos) },
                whoseTurn == Constants.WhoseTurn.TOP
            )
        )
        else itemAdapter.add(
            MessageTextItem(
                message,
//                mikePenzPositions,
//                { pos -> mikePenzPositions.add(pos) },
                isNeutralMessage = true
            )
        )

        v_recycler.adapter?.itemCount?.minus(1)?.let { v_recycler.smoothScrollToPosition(it) }
    }

    /*
    * Animation initializer
    * */

    private fun flipNewCard(resId: Int, isBadCard: Boolean = false) {
        vm.setImagesOnFlipView(flip_view, card_deck, card_picked, resId, null, isVertical = true)

        Animations.animateFlipPlayingCard(
            flip_view,
            cards_left,
            vm.cardDeck.size > 50,
            { onFlipPlayingCardEnd(isBadCard) },
            { message -> vm.notifyMessage(message) })

        // FIXME if (isVsBot)
    }

    private fun onFlipPlayingCardEnd(isBadCard: Boolean) {
        flip_view.flipTheView()

        if (!isBadCard) {
            setOnClickListeners()
        } else {
            // If it is bad card, this runs
            Animations.animateBadCard(
                flip_view,
                vm.getScreenWidth(),
                { removeAllOnClickListeners() },
                {
                    // OnStop
                    vm.notifyToggleTurn()
                    restoreFlipViewPosition()
                    vm.removeCardFromDeck()

                    if (!vm.isThisTeamReady()) {
                        isOngoingGame = false
                        isRestoringPlayers = true
                    }

                    if (isOngoingGame && !GameLogic.isTherePossibleMove(whoseTurn, vm.firstCardInDeck)) {
                        vm.triggerBadCard()
                    } else if (isOngoingGame && GameLogic.isTherePossibleMove(whoseTurn, vm.firstCardInDeck)) {
                        prepareViewsToPulse()
                    }
                })
        }

        if (isJustShotAtGoalie) isJustShotAtGoalie = false
    }

    private fun prepareViewsToPulse() {
        val teamToPulse = if (whoseTurn == Constants.WhoseTurn.BOTTOM) teamTopViews else teamBottomViews

        val viewsToPulse = mutableListOf<AppCompatImageView>()

        possibleMovesIndexes.forEach {
            viewsToPulse.add(teamToPulse[it])
        }

        Animations.animatePulsingCards(viewsToPulse as List<AppCompatImageView>) { message ->
            updateMessageBox(
                message
            )
        }
    }

    private fun addGoalieView(
        bottom: Boolean,
        doNotFlip: Boolean = false,
        doRemoveCardFromDeck: Boolean = false,
        withStartDelay: Boolean = false
    ) {
        if (fvGoalieBtmX == 0f) {
            fvGoalieBtmX = flip_btm_goalie.x
            fvGoalieBtmY = flip_btm_goalie.y
            fvGoalieTopX = flip_top_goalie.x
            fvGoalieTopY = flip_top_goalie.y
        }

        // ONLY adding view. No real goalie card is assigning to that team by this function.
        removeAllOnClickListeners()

        val view = if (bottom) card_bm_goalie else card_top_goalie

        card_deck.setImageResource(R.drawable.red_back_vertical)
        card_picked.setImageResource(R.drawable.red_back_vertical)

        val delay: Long = if (withStartDelay) 1500 else 150

        Animations.animateAddGoalie(
            flipView = flip_view,
            goalie = view,
            xForAttacker = card_bm_center.x,
            delay = delay
        )
        {
            // onStop
            restoreFlipViewPosition()
            vm.onGoalieAddedAnimationEnd(view)
            if (card_top_goalie.tag != Integer.valueOf(R.drawable.red_back)) addGoalieView(bottom = false) else {
                if (!doNotFlip) flipNewCard(vm.resIdOfCard(vm.firstCardInDeck))
                if (doRemoveCardFromDeck) vm.removeCardFromDeck()
                vm.showPickedCard()
                cards_left.visibility = View.VISIBLE
            }
        }
    }

    private fun animateAddPlayer(targetView: AppCompatImageView, team: Array<Card?>, spotIndex: Int) {
        removeAllOnClickListeners()
        Animations.animateAddPlayer(flip_view, targetView) {
            // OnStop
            restoreFlipViewPosition()
            vm.onPlayerAddedAnimationEnd(targetView, team, spotIndex) { prepareViewsToPulse() }
        }
    }

    private fun animateAttack(targetView: AppCompatImageView) {
        removeAllOnClickListeners()
        Animations.stopAllPulsingCards()

        val victimX = targetView.x
        val victimY = targetView.y

        Animations.animateAttackPlayer(flip_view, targetView, vm.getScreenWidth()) {
            // OnStop
            targetView.x = victimX
            targetView.y = victimY
            restoreFlipViewPosition()
            vm.onAttackedAnimationEnd(targetView) { prepareViewsToPulse() }
        }
    }

    private fun prepareAttackPlayer(victimTeam: Array<Card?>, spotIndex: Int, victimView: AppCompatImageView) {
        if (spotIndex == 5) {
            // Attacking goalie
            isJustShotAtGoalie = true

            if (vm.canAttack(victimTeam, spotIndex, victimView)) {
                removeAllOnClickListeners()
                Animations.stopAllPulsingCards()

                vm.notifyMessageAttackingGoalie()

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

                    Animations.animateScoredAtGoalie(
                        fading_view,
                        flip_view,
                        flip_top_goalie,
                        vm.getScreenWidth(),
                        card_top_center.x,
                        tempGoalieCard,
                        { message -> updateMessageBox(message) },
                        {
                            // OnStop
                            onGoalieActionEnd(flip_top_goalie, true, teamTop)
                            vm.updateScores(top_team_score, bm_team_score)
//                            vm.notifyToggleTurn()
//                            restoreFlipViewPosition()
                            addGoalieView(bottom = false, doNotFlip = true, doRemoveCardFromDeck = true)
                        }
                    )

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

                    Animations.animateScoredAtGoalie(
                        fading_view,
                        flip_view,
                        flip_btm_goalie,
                        vm.getScreenWidth(),
                        card_bm_center.x,
                        tempGoalieCard,
                        { message -> updateMessageBox(message) },
                        {
                            // OnStop
                            onGoalieActionEnd(flip_btm_goalie, true, teamBottom)
                            vm.updateScores(top_team_score, bm_team_score)
//                            vm.notifyToggleTurn()
//                            restoreFlipViewPosition()
                            addGoalieView(bottom = true, doNotFlip = true, doRemoveCardFromDeck = true)
                        }
                    )
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
        Animations.stopAllPulsingCards()

        isJustShotAtGoalie = true

        vm.notifyMessageAttackingGoalie()

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

            Animations.animateGoalieSaved(
                fading_view,
                flip_view,
                flip_top_goalie,
                vm.getScreenWidth(),
                card_top_center.x,
                tempGoalieCard,
                { message -> updateMessageBox(message) },
                {
                    // OnStop
                    onGoalieActionEnd(flip_top_goalie, false, teamTop)
                    addGoalieView(bottom = false, doNotFlip = true, doRemoveCardFromDeck = true)
                }
            )

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

            Animations.animateGoalieSaved(
                fading_view,
                flip_view,
                flip_btm_goalie,
                vm.getScreenWidth(),
                card_bm_center.x,
                tempGoalieCard,
                { message -> updateMessageBox(message) },
                {
                    // OnStop
                    onGoalieActionEnd(flip_btm_goalie, false, teamBottom)
                    addGoalieView(bottom = true, doNotFlip = true, doRemoveCardFromDeck = true)
                }
            )
        }
    }

    private fun onGoalieActionEnd(view: View, isGoal: Boolean = false, team: Array<Card?>) {
        fading_view.visibility = View.GONE
        view.visibility = View.GONE
        isOngoingGame = false
        isRestoringPlayers = true

        if (isGoal) {
            if (whoseTurn == Constants.WhoseTurn.BOTTOM) teamBottomScore++ else teamTopScore++
        }

        team[5] = null
        vm.notifyToggleTurn()
        restoreFlipViewPosition()
    }

    /*
    * Game management
    * */

    private fun isNextPeriodReady(nextPeriod: Int): Boolean {
        cards_left.visibility = View.GONE
        restoreFlipViewPosition()
        period += nextPeriod

        if (period > 3 && (teamBottomScore != teamTopScore)) {
            when {
                teamBottomScore > teamTopScore -> vm.notifyMessage(
                    "Team Bottom won with $teamBottomScore-$teamTopScore!\nNew game? Press anywhere!",
                    isNeutralMessage = true
                )
                teamBottomScore < teamTopScore -> vm.notifyMessage(
                    "Team Top won with $teamTopScore-$teamBottomScore!\nNew game? Press anywhere!",
                    isNeutralMessage = true
                )
            }

            removeAllOnClickListeners()
            whole_view.visibility = View.VISIBLE
            return false
        } else {
            if (period > 3) vm.notifyMessage(
                "Overtime! Play until all cards are out.\nPeriod: $period",
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
            if (v.tag == Integer.valueOf(android.R.color.transparent)) return
            if (whoseTurn == Constants.WhoseTurn.BOTTOM) {
                when (v.id) {
                    R.id.card_top_forward_left -> {
                        prepareAttackPlayer(teamTop, 0, card_top_forward_left)
                    }
                    R.id.card_top_center -> {
                        prepareAttackPlayer(teamTop, 1, card_top_center)
                    }
                    R.id.card_top_forward_right -> {
                        prepareAttackPlayer(teamTop, 2, card_top_forward_right)
                    }
                    R.id.card_top_defender_left -> {
                        if (vm.areEnoughForwardsOut(teamTop, 3))
                            prepareAttackPlayer(teamTop, 3, card_top_defender_left)
                    }
                    R.id.card_top_defender_right -> {
                        if (vm.areEnoughForwardsOut(teamTop, 4))
                            prepareAttackPlayer(teamTop, 4, card_top_defender_right)
                    }
                    R.id.card_top_goalie -> {
                        if (vm.isAtLeastOneDefenderOut(teamTop)) {
                            tempGoalieCard = teamTop[5]
                            if (vm.canAttack(teamTop, 5, card_top_goalie)) prepareAttackPlayer(
                                teamTop,
                                5,
                                card_top_goalie
                            )
                            else prepareGoalieSaved(card_top_goalie)
                        }
                    }
                }
            } else {
                when (v.id) {
                    R.id.card_bm_forward_left -> {
                        prepareAttackPlayer(teamBottom, 0, card_bm_forward_left)
                    }
                    R.id.card_bm_center -> {
                        prepareAttackPlayer(teamBottom, 1, card_bm_center)
                    }
                    R.id.card_bm_forward_right -> {
                        prepareAttackPlayer(teamBottom, 2, card_bm_forward_right)
                    }
                    R.id.card_bm_defender_left -> {
                        if (vm.areEnoughForwardsOut(teamBottom, 3))
                            prepareAttackPlayer(teamBottom, 3, card_bm_defender_left)
                    }
                    R.id.card_bm_defender_right -> {
                        if (vm.areEnoughForwardsOut(teamBottom, 4))
                            prepareAttackPlayer(teamBottom, 4, card_bm_defender_right)
                    }
                    R.id.card_bm_goalie -> {
                        if (vm.isAtLeastOneDefenderOut(teamBottom)) {
                            tempGoalieCard = teamBottom[5]
                            if (vm.canAttack(teamBottom, 5, card_bm_goalie)) prepareAttackPlayer(
                                teamBottom,
                                5,
                                card_bm_goalie
                            )
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

            val view = view?.findViewById<AppCompatImageView>(v.id)
            val team = if (whoseTurn == Constants.WhoseTurn.BOTTOM) teamBottom else teamTop

            view?.let {
                if (vm.canAddPlayerView(view, team, spotIndex))
                    animateAddPlayer(view, team, spotIndex)
            }
        }
    }
}

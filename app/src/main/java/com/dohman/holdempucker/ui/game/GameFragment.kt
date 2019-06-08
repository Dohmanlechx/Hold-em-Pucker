package com.dohman.holdempucker.ui.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.dohman.holdempucker.R
import com.dohman.holdempucker.models.Card
import com.dohman.holdempucker.ui.items.MessageTextItem
import com.dohman.holdempucker.util.*
import com.dohman.holdempucker.util.Constants.Companion.isOngoingGame
import com.dohman.holdempucker.util.Constants.Companion.isShootingAtGoalie
import com.dohman.holdempucker.util.Constants.Companion.period
import com.dohman.holdempucker.util.Constants.Companion.possibleMovesIndexes
import com.dohman.holdempucker.util.Constants.Companion.isRestoringPlayers
import com.dohman.holdempucker.util.Constants.Companion.teamBottom
import com.dohman.holdempucker.util.Constants.Companion.teamBottomScore
import com.dohman.holdempucker.util.Constants.Companion.teamTop
import com.dohman.holdempucker.util.Constants.Companion.teamTopScore
import com.dohman.holdempucker.util.Constants.Companion.PLAYER_GOALIE
import com.dohman.holdempucker.util.Constants.Companion.currentGameMode
import com.dohman.holdempucker.util.Constants.Companion.isGameLive
import com.dohman.holdempucker.util.Constants.Companion.isOnlineMode
import com.dohman.holdempucker.util.Constants.Companion.lobbyId
import com.dohman.holdempucker.util.Constants.WhoseTurn.Companion.isBotMoving
import com.dohman.holdempucker.util.Constants.WhoseTurn.Companion.isTeamBottomTurn
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.android.synthetic.main.computer_layout.*
import kotlinx.android.synthetic.main.game_fragment.*

class GameFragment : Fragment(), View.OnClickListener {
    private lateinit var vm: GameViewModel

    private val itemAdapter = ItemAdapter<AbstractItem<*, *>>()
    private val fastAdapter = FastAdapter.with<AbstractItem<*, *>, ItemAdapter<AbstractItem<*, *>>>(itemAdapter)

    // x and y values of all three FlipViews
    private var fvMainX: Float = 0f
    private var fvMainY: Float = 0f
    private var fvGoalieBtmX: Float = 0f
    private var fvGoalieBtmY: Float = 0f
    private var fvGoalieTopX: Float = 0f
    private var fvGoalieTopY: Float = 0f

    private val teamBottomViews = mutableListOf<AppCompatImageView>()
    private val teamTopViews = mutableListOf<AppCompatImageView>()

    private var tempGoalieCard: Card? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        vm = ViewModelProviders.of(this).get(GameViewModel::class.java)

        // Observables
        vm.messageNotifier.observe(viewLifecycleOwner, Observer { updateMessageBox(it.first, it.second) })
        vm.halfTimeNotifier.observe(viewLifecycleOwner, Observer {
            if (isNextPeriodReady(it)) addGoalieView(true, withStartDelay = true)
        })
        vm.whoseTurnNotifier.observe(viewLifecycleOwner, Observer { Animations.animatePuck(puck, it) })
        vm.pickedCardNotifier.observe(viewLifecycleOwner, Observer { flipNewCard(it) })
        vm.cardsCountNotifier.observe(viewLifecycleOwner, Observer { cards_left.text = it.toString() })
        vm.badCardNotifier.observe(
            viewLifecycleOwner,
            Observer {
                flipNewCard(vm.resIdOfCard(vm.firstCardInDeck), isBadCard = true)
                vm.notifyMessage("Aw, too weak card! It goes out!")
            })

        // Online
        vm.onlineOpponentInputNotifier.observe(viewLifecycleOwner, Observer { input ->
            input?.let {
                when (vm.isMyOnlineTeamBottom()) {
                    true -> {
                        if (isRestoringPlayers) {
                            animateAddPlayer(teamTopViews[input], teamTop, input)
                        } else {
                            if (input == PLAYER_GOALIE) {
                                tempGoalieCard = teamBottom[PLAYER_GOALIE]
                                if (vm.canAttack(teamBottom, PLAYER_GOALIE, card_bm_goalie))
                                    prepareAttackPlayer(teamBottom, input, teamBottomViews[input])
                                else
                                    prepareGoalieSaved(card_bm_goalie)
                            } else {
                                prepareAttackPlayer(teamBottom, input, teamBottomViews[input])
                            }
                        }
                    }
                    false -> {
                        if (isRestoringPlayers) {
                            animateAddPlayer(teamBottomViews[input], teamBottom, input)
                        } else {
                            if (input == PLAYER_GOALIE) {
                                tempGoalieCard = teamTop[PLAYER_GOALIE]
                                if (vm.canAttack(teamTop, PLAYER_GOALIE, card_top_goalie))
                                    prepareAttackPlayer(teamTop, input, teamTopViews[input])
                                else
                                    prepareGoalieSaved(card_top_goalie)
                            } else {
                                prepareAttackPlayer(teamTop, input, teamTopViews[input])
                            }
                        }
                    }
                }
            }
        })
        vm.onlineOpponentFoundNotifier.observe(
            viewLifecycleOwner,
            Observer { found ->
                if (found) {
                    v_progressbar.visibility = View.GONE
                    initGame()

                    online_team.text =
                        if (vm.isMyOnlineTeamBottom()) "Your team is BOTTOM/GREEN\nLobbyid: $lobbyId" else "Your team is TOP/PURPLE\n" +
                                "Lobbyid: $lobbyId"
                }
            })
        // End of Observables

        return inflater.inflate(R.layout.game_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        flip_view.post {
            flip_view.bringToFront()
            fvMainX = flip_view.x
            fvMainY = flip_view.y
        }

        flip_btm_goalie.post {
            ViewUtil.setScaleOnRotatedView(flip_view, card_bm_goalie)
            ViewUtil.setScaleOnRotatedView(flip_view, background_bm_goalie)
            ViewUtil.setScaleOnRotatedView(flip_view, flip_btm_goalie)
        }

        flip_top_goalie.post {
            ViewUtil.setScaleOnRotatedView(flip_view, card_top_goalie)
            ViewUtil.setScaleOnRotatedView(flip_view, background_top_goalie)
            ViewUtil.setScaleOnRotatedView(flip_view, flip_top_goalie)
        }

        computer_lamp.post {
            Animations.animateLamp(computer_lamp)
        }

        setupMessageRecycler()

        if (currentGameMode == Constants.GameMode.ONLINE) {

        } else {
            updateMessageBox("Press anywhere to start the game! Period: $period", isNeutralMessage = true)
            whole_view.setOnClickListener { initGame() }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.setGameMode()
        storeAllViews()
        setOnClickListeners()

        if (!isGameLive) v_progressbar.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isOnlineMode) vm.removeLobbyFromDatabase()
        vm.clearAllValueEventListeners()
        Animations.stopAllAnimations()
        Animations.stopAllPulsingCards()
    }

    private fun initGame() {
        period = 1
        teamBottomScore = 0
        teamTopScore = 0
        updateScores()

        Constants.resetBooleansToInitState()

        resetAllCards(teamBottomViews)
        resetAllCards(teamTopViews)
        card_top_goalie.tag = null
        card_bm_goalie.tag = null

        addGoalieView(bottom = true)
        whole_view.visibility = View.GONE
    }

    /*
    * Views management
    * */

    private fun updateScores() {
        val scorerTextView = when {
            teamTopScore > Integer.parseInt(top_team_score.text.toString()) -> top_team_score
            teamBottomScore > Integer.parseInt(bm_team_score.text.toString()) -> bm_team_score
            else -> null
        }

        if (scorerTextView == null) {
            top_team_score.text = teamTopScore.toString()
            bm_team_score.text = teamBottomScore.toString()
        } else {
            Animations.animateScore(scorerTextView) {
                Util.vibrate(requireContext(), true)
                top_team_score.text = teamTopScore.toString()
                bm_team_score.text = teamBottomScore.toString()
            }
        }
    }

    private fun restoreFlipViewsPosition() {
        flip_view.rotation = 0f
        flip_view.x = fvMainX
        flip_view.y = fvMainY
        flip_btm_goalie.x = fvGoalieBtmX
        flip_btm_goalie.y = fvGoalieBtmY
        flip_top_goalie.x = fvGoalieTopX
        flip_top_goalie.y = fvGoalieTopY
    }

    private fun storeAllViews() {
        teamBottomViews.clear()
        teamTopViews.clear()

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

    private fun resetAllCards(cardImageViews: List<AppCompatImageView>) {
        cardImageViews.forEach {
            it.setImageResource(android.R.color.transparent)
            it.tag = Integer.valueOf(android.R.color.transparent)
        }
    }

    private fun setupMessageRecycler() = v_recycler.apply {
        itemAnimator = null
        layoutManager = LinearLayoutManager(requireContext())
        adapter = fastAdapter
    }

    private fun updateMessageBox(message: String, isNeutralMessage: Boolean = false) {
        itemAdapter.clear()
        itemAdapter.add(MessageTextItem(message, isNeutralMessage))
    }

    /*
    * Animation initializer
    * */

    private fun flipNewCard(resId: Int, isBadCard: Boolean = false) {
        if (vm.cardDeck.size > 50) return

        ViewUtil.setImagesOnFlipView(
            flip_view,
            card_deck,
            card_picked,
            resId,
            null,
            isVertical = true
        )

        Animations.animateFlipPlayingCard(
            flip_view,
            cards_left,
            vm.cardDeck.size > 50,
            { onFlipPlayingCardEnd(isBadCard) },
            { message -> vm.notifyMessage(message) },
            { if (vm.cardDeck.size <= 1) card_background.visibility = View.GONE }
        )
    }

    private fun prepareViewsToPulse() {
        val teamToPulse = if (isTeamBottomTurn()) teamTopViews else teamBottomViews
        val viewsToPulse = mutableListOf<AppCompatImageView>()

        possibleMovesIndexes.forEach {
            viewsToPulse.add(teamToPulse[it])
        }

        Animations.animatePulsingCards(viewsToPulse as List<AppCompatImageView>) { message ->
            updateMessageBox(message)
        }
    }

    private fun addGoalieView(
        bottom: Boolean,
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

        val delay: Long = if (withStartDelay) 2500 else 250

        Animations.animateAddGoalie(
            flipView = flip_view,
            goalie = view,
            xForAttacker = card_bm_center.x,
            delay = delay
        )
        {
            // onStop
            restoreFlipViewsPosition()
            vm.onGoalieAddedAnimationEnd(view)

            vm.checkGameSituation()
            vm.removeCardFromDeck(doNotNotify = true)
            if (card_top_goalie.tag != Integer.valueOf(R.drawable.red_back)) {
                addGoalieView(bottom = false)
            } else {
                vm.notifyPickedCard()
                cards_left.visibility = View.VISIBLE
            }
        }
    }

    private fun animateAddPlayer(
        targetView: AppCompatImageView,
        team: Array<Card?>,
        spotIndex: Int
    ) {
        Animations.animateAddPlayer(flip_view, targetView) {
            // OnStop
            restoreFlipViewsPosition()
            vm.onPlayerAddedAnimationEnd(targetView, team, spotIndex) { prepareViewsToPulse() }
        }
    }

    private fun animateAttack(targetView: AppCompatImageView) {
        removeAllOnClickListeners()
        Animations.stopAllPulsingCards()

        val victimOriginalX = targetView.x
        val victimOriginalY = targetView.y

        Animations.animateAttackPlayer(flip_view, targetView, vm.getScreenWidth()) {
            // OnStop
            targetView.x = victimOriginalX
            targetView.y = victimOriginalY
            restoreFlipViewsPosition()
            vm.onAttackedAnimationEnd(targetView) { prepareViewsToPulse() }
        }
    }

    private fun prepareAttackPlayer(victimTeam: Array<Card?>, spotIndex: Int, victimView: AppCompatImageView) {
        if (spotIndex == 5) {
            // Attacking goalie
            isShootingAtGoalie = true

            if (vm.canAttack(victimTeam, spotIndex, victimView)) {
                removeAllOnClickListeners()
                Animations.stopAllPulsingCards()

                vm.notifyMessageAttackingGoalie()

                val isTargetGoalieBottom = !isTeamBottomTurn()
                val targetView = if (isTargetGoalieBottom) flip_btm_goalie else flip_top_goalie
                val targetViewFront = if (isTargetGoalieBottom) flip_btm_goalie_front else flip_top_goalie_front
                val targetViewBack = if (isTargetGoalieBottom) flip_btm_goalie_back else flip_top_goalie_back
                val targetTeam = if (isTargetGoalieBottom) teamBottom else teamTop

                ViewUtil.setImagesOnFlipView(
                    targetView,
                    targetViewFront,
                    targetViewBack,
                    null,
                    ViewUtil.getRotatedBitmap(requireContext(), vm.resIdOfCard(tempGoalieCard)),
                    isVertical = false
                )

                victimView.setImageResource(android.R.color.transparent)
                victimView.tag = Integer.valueOf(android.R.color.transparent)

                if (vm.cardDeck.size > 1)
                    vm.removeCardFromDeck(doNotNotify = true)

                Animations.animateScoredAtGoalie(
                    fading_view,
                    flip_view,
                    targetView,
                    vm.getScreenWidth(),
                    card_top_center.x,
                    tempGoalieCard,
                    { message -> updateMessageBox(message) },
                    {
                        // OnStop
                        onGoalieActionEnd(targetView, true, targetTeam)
                        updateScores()
                        if (vm.cardDeck.size <= 1)
                            vm.removeCardFromDeck(doNotNotify = true)
                        else
                            addGoalieView(bottom = isTargetGoalieBottom)
                    }
                )
            }
        } else {
            // Attacking another player
            if (vm.canAttack(victimTeam, spotIndex, victimView))
                animateAttack(victimView)
            else Util.vibrate(requireContext(), false)
        }
    }

    private fun prepareGoalieSaved(victimView: AppCompatImageView) {
        removeAllOnClickListeners()
        Animations.stopAllPulsingCards()

        isShootingAtGoalie = true

        vm.notifyMessageAttackingGoalie()

        val isTargetGoalieBottom = !isTeamBottomTurn()
        val targetView = if (isTargetGoalieBottom) flip_btm_goalie else flip_top_goalie
        val targetViewFront = if (isTargetGoalieBottom) flip_btm_goalie_front else flip_top_goalie_front
        val targetViewBack = if (isTargetGoalieBottom) flip_btm_goalie_back else flip_top_goalie_back
        val targetTeam = if (isTargetGoalieBottom) teamBottom else teamTop

        ViewUtil.setImagesOnFlipView(
            targetView,
            targetViewFront,
            targetViewBack,
            null,
            ViewUtil.getRotatedBitmap(requireContext(), vm.resIdOfCard(tempGoalieCard)),
            isVertical = false
        )

        victimView.setImageResource(android.R.color.transparent)
        victimView.tag = Integer.valueOf(android.R.color.transparent)

        if (vm.cardDeck.size > 1)
            vm.removeCardFromDeck(doNotNotify = true)

        Animations.animateGoalieSaved(
            fading_view,
            flip_view,
            targetView,
            vm.getScreenWidth(),
            card_top_center.x,
            tempGoalieCard,
            { message -> updateMessageBox(message) },
            {
                // OnStop
                onGoalieActionEnd(targetView, false, targetTeam)

                if (vm.cardDeck.size <= 1)
                    vm.removeCardFromDeck(doNotNotify = true)
                else
                    addGoalieView(bottom = isTargetGoalieBottom)
            }
        )
    }

    /*
    * On Animation Ends
    * */

    private fun onFlipPlayingCardEnd(isBadCard: Boolean) {
        if (flip_view == null) return

        flip_view.flipTheView()
        // Bot's turn
        if (isBotMoving() && !isBadCard) {
            // Adding player
            if (isRestoringPlayers) {
                vm.botChooseEmptySpot(vm.getEmptySpots(teamTopViews)) {
                    // Trigger the bot's move
                    if (it != -1) animateAddPlayer(teamTopViews[it], teamTop, it)
                }
            } else {
                // Attacking player
                when (val chosenIndex = vm.botChooseIndexToAttack(possibleMovesIndexes)) {
                    -1 -> { /* Do nothing */
                    }
                    PLAYER_GOALIE -> {
                        tempGoalieCard = teamBottom[PLAYER_GOALIE]
                        if (vm.canAttack(teamBottom, PLAYER_GOALIE, card_bm_goalie)) prepareAttackPlayer(
                            teamBottom,
                            PLAYER_GOALIE,
                            card_bm_goalie
                        )
                        else prepareGoalieSaved(card_bm_goalie)
                    }
                    else -> prepareAttackPlayer(teamBottom, chosenIndex, teamBottomViews[chosenIndex])
                }
            }
        } else {
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
                        restoreFlipViewsPosition()

                        if (!vm.isThisTeamReady()) {
                            updateMessageBox("Please choose a position.")
                            isOngoingGame = false
                            isRestoringPlayers = true
                        }

                        vm.removeCardFromDeck()

                        if (isOngoingGame && !GameLogic.isTherePossibleMove(vm.firstCardInDeck)) {
                            vm.triggerBadCard()
                        } else if (isOngoingGame && GameLogic.isTherePossibleMove(vm.firstCardInDeck)) {
                            prepareViewsToPulse()
                        }
                    })
            }
        }
    }

    private fun onGoalieActionEnd(view: View, isGoal: Boolean = false, team: Array<Card?>) {
        if (isShootingAtGoalie) isShootingAtGoalie = false
        fading_view.visibility = View.GONE
        view.visibility = View.GONE
        isOngoingGame = false
        isRestoringPlayers = true

        if (isGoal) vm.addGoalToScore()

        team[5] = null
        vm.notifyToggleTurn()
        restoreFlipViewsPosition()
    }

    /*
    * Game management
    * */

    private fun isNextPeriodReady(nextPeriod: Int): Boolean {
        removeAllOnClickListeners()

        isRestoringPlayers = true

        cards_left.text = getString(R.string.full_card_deck_amount)
        restoreFlipViewsPosition()

        return if (vm.isNextPeriodReady()) {
            card_background.visibility = View.VISIBLE
            resetAllCards(teamBottomViews)
            resetAllCards(teamTopViews)
            card_top_goalie.tag = null
            card_bm_goalie.tag = null
            true
        } else {
            whole_view.visibility = View.VISIBLE
            txt_winner.text = when {
                teamBottomScore > teamTopScore -> "Team Bottom\nwon with $teamBottomScore-$teamTopScore!"
                else -> "Team Top\nwon with $teamTopScore-$teamBottomScore!"
            }
            Animations.animateWinner(fading_view, lottie_trophy, txt_winner)
            Util.vibrate(requireContext(), true)

            fading_view.setOnClickListener {
                view?.let { Navigation.findNavController(it).popBackStack() }
            }
            false
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
        if (isBotMoving()) return

        val spotIndex: Int
        if (isOngoingGame) {
            if (v.tag == Integer.valueOf(android.R.color.transparent)) return
            if (isTeamBottomTurn()) {
                spotIndex = when (v.id) {
                    R.id.card_top_forward_left -> 0
                    R.id.card_top_center -> 1
                    R.id.card_top_forward_right -> 2
                    R.id.card_top_defender_left ->
                        if (vm.areEnoughForwardsOut(teamTop, 3)) 3 else return
                    R.id.card_top_defender_right ->
                        if (vm.areEnoughForwardsOut(teamTop, 4)) 4 else return
                    R.id.card_top_goalie -> if (vm.isAtLeastOneDefenderOut(teamTop)) 5 else return
                    else -> return
                }
            } else {
                spotIndex = when (v.id) {
                    R.id.card_bm_forward_left -> 0
                    R.id.card_bm_center -> 1
                    R.id.card_bm_forward_right -> 2
                    R.id.card_bm_defender_left ->
                        if (vm.areEnoughForwardsOut(teamBottom, 3)) 3 else return
                    R.id.card_bm_defender_right ->
                        if (vm.areEnoughForwardsOut(teamBottom, 4)) 4 else return
                    R.id.card_bm_goalie -> if (vm.isAtLeastOneDefenderOut(teamBottom)) 5 else return
                    else -> return
                }
            }

            val imageView = view?.findViewById<AppCompatImageView>(v.id)
            val targetTeam = if (isTeamBottomTurn()) teamTop else teamBottom

            imageView?.let {
                if (spotIndex == PLAYER_GOALIE) {
                    tempGoalieCard = targetTeam[PLAYER_GOALIE]
                    if (vm.canAttack(targetTeam, PLAYER_GOALIE, it))
                        prepareAttackPlayer(targetTeam, PLAYER_GOALIE, it)
                    else prepareGoalieSaved(it)
                } else {
                    prepareAttackPlayer(targetTeam, spotIndex, it)
                }
            }
        } else {
            if (isTeamBottomTurn()) {
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

            val imageView = view?.findViewById<AppCompatImageView>(v.id)
            val team = if (isTeamBottomTurn()) teamBottom else teamTop

            imageView?.let {
                if (vm.canAddPlayerView(imageView, team, spotIndex) && v.tag != null) {
                    removeAllOnClickListeners()
                    animateAddPlayer(imageView, team, spotIndex)
                } else {
                    Util.vibrate(requireContext(), false)
                }
            }
        }

        if (currentGameMode == Constants.GameMode.ONLINE) vm.notifyOnlineInput(spotIndex)
    }
}
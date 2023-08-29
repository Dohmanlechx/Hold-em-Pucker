package com.dohman.holdempucker.ui.game

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dohman.holdempucker.R
import com.dohman.holdempucker.databinding.ComputerLayoutBinding
import com.dohman.holdempucker.databinding.GameFragmentBinding
import com.dohman.holdempucker.models.Card
import com.dohman.holdempucker.ui.items.MessageTextItem
import com.dohman.holdempucker.util.*
import com.dohman.holdempucker.util.Constants.Companion.isOngoingGame
import com.dohman.holdempucker.util.Constants.Companion.isShootingAtGoalie
import com.dohman.holdempucker.util.Constants.Companion.period
import com.dohman.holdempucker.util.Constants.Companion.possibleMovesIndexes
import com.dohman.holdempucker.util.Constants.Companion.isRestoringPlayers
import com.dohman.holdempucker.util.Constants.Companion.teamGreen
import com.dohman.holdempucker.util.Constants.Companion.teamBottomScore
import com.dohman.holdempucker.util.Constants.Companion.teamPurple
import com.dohman.holdempucker.util.Constants.Companion.teamTopScore
import com.dohman.holdempucker.util.Constants.Companion.PLAYER_GOALIE
import com.dohman.holdempucker.util.Constants.Companion.currentGameMode
import com.dohman.holdempucker.util.Constants.Companion.isMyOnlineTeamGreen
import com.dohman.holdempucker.util.Constants.Companion.isNotOnlineMode
import com.dohman.holdempucker.util.Constants.Companion.isOnlineMode
import com.dohman.holdempucker.util.Constants.Companion.isWinnerDeclared
import com.dohman.holdempucker.util.Constants.Companion.lobbyId
import com.dohman.holdempucker.util.Constants.Companion.whoseTurn
import com.dohman.holdempucker.util.Constants.WhoseTurn.Companion.isBotMoving
import com.dohman.holdempucker.util.Constants.WhoseTurn.Companion.isOpponentMoving
import com.dohman.holdempucker.util.Constants.WhoseTurn.Companion.isTeamGreenTurn
import com.dohman.holdempucker.util.Constants.WhoseTurn.Companion.isTeamPurpleTurn
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.items.AbstractItem

class GameFragment : Fragment(), View.OnClickListener {
    private lateinit var vm: GameViewModel

    private val itemAdapter = ItemAdapter<AbstractItem<*, *>>()
    private val fastAdapter =
        FastAdapter.with<AbstractItem<*, *>, ItemAdapter<AbstractItem<*, *>>>(itemAdapter)

    // x and y values of all three FlipViews
    private var fvMainX: Float = 0f
    private var fvMainY: Float = 0f
    private var fvGoalieBtmX: Float = 0f
    private var fvGoalieBtmY: Float = 0f
    private var fvGoalieTopX: Float = 0f
    private var fvGoalieTopY: Float = 0f

    private val teamGreenViews = mutableListOf<AppCompatImageView>()
    private val teamPurpleViews = mutableListOf<AppCompatImageView>()

    private var tempGoalieCard: Card? = null

    private var onlineInputTimer: CountDownTimer? = null

    private var _binding: GameFragmentBinding? = null
    private var _computerBinding: ComputerLayoutBinding? = null
    private val binding get() = _binding!!
    private val computerBinding get() = _binding!!.idComputerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        period = 1
        isWinnerDeclared = false

        whoseTurn = Constants.WhoseTurn.GREEN
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        vm = ViewModelProviders.of(this).get(GameViewModel::class.java)
        _binding = GameFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        // Observables
        vm.messageNotifier.observe(viewLifecycleOwner, Observer { updateMessageBox(it.first, it.second) })
        vm.halfTimeNotifier.observe(viewLifecycleOwner, Observer {
            removeAllOnClickListeners()
            if (isNextPeriodReady(it)) addGoalieView(true, withStartDelay = true)
        })
        vm.whoseTurnNotifier.observe(viewLifecycleOwner, Observer { Animations.animatePuck(binding.puck, it) })
        vm.pickedCardNotifier.observe(viewLifecycleOwner, Observer { flipNewCard(it) })
        vm.cardsCountNotifier.observe(viewLifecycleOwner, Observer { binding.cardsLeft.text = it.toString() })
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
                            animateAddPlayer(teamPurpleViews[input], teamPurple, input)
                        } else {
                            if (input == PLAYER_GOALIE) {
                                tempGoalieCard = teamGreen[PLAYER_GOALIE]
                                if (vm.canAttack(teamGreen, PLAYER_GOALIE, binding.cardBmGoalie))
                                    prepareAttackPlayer(teamGreen, input, teamGreenViews[input])
                                else
                                    prepareGoalieSaved(binding.cardBmGoalie)
                            } else {
                                prepareAttackPlayer(teamGreen, input, teamGreenViews[input])
                            }
                        }
                    }
                    false -> {
                        if (isRestoringPlayers) {
                            animateAddPlayer(teamGreenViews[input], teamGreen, input)
                        } else {
                            if (input == PLAYER_GOALIE) {
                                tempGoalieCard = teamPurple[PLAYER_GOALIE]
                                if (vm.canAttack(teamPurple, PLAYER_GOALIE, binding.cardTopGoalie))
                                    prepareAttackPlayer(teamPurple, input, teamPurpleViews[input])
                                else
                                    prepareGoalieSaved(binding.cardTopGoalie)
                            } else {
                                prepareAttackPlayer(teamPurple, input, teamPurpleViews[input])
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
                    val message =
                        if (vm.isMyOnlineTeamBottom()) "Opponent joined, game is starting! Period: $period"
                        else "You joined, game is starting! Period: $period"

                    updateMessageBox(message, isNeutralMessage = true)

                    binding.vProgressbar.visibility = View.GONE
                    Handler().postDelayed({ initGame() }, 1000)

                    binding.txtOnlineTeam.apply {
                        val textMessage = if (vm.isMyOnlineTeamBottom()) "YOU ARE TEAM GREEN" else "YOU ARE TEAM PURPLE"
                        val textColor =
                            if (vm.isMyOnlineTeamBottom()) R.color.text_background_btm else R.color.text_background_top

                        text = textMessage
                        setTextColor(ContextCompat.getColor(requireContext(), textColor))
                    }
                }
            })
        vm.onlineOpponentHasDisconnected.observe(viewLifecycleOwner, Observer { disconnected ->
            if (disconnected && !isWinnerDeclared) {
                isWinnerDeclared = true
                vm.analyticsOnlineMatchDisconnected()
                onlineInputTimer?.cancel()
                binding.txtWinner.text = getString(R.string.opponent_disconnected)
                Animations.animateWinner(binding.fadingView, binding.lottieTrophy, binding.txtWinner)
                Util.vibrate(requireContext(), true)
                binding.fadingView.setOnClickListener { view?.let { Navigation.findNavController(it).popBackStack() } }
            }
        })
        // End of Observables

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.flipView.post {
            binding.flipView.bringToFront()
            fvMainX = binding.flipView.x
            fvMainY = binding.flipView.y
        }

        binding.flipBtmGoalie.post {
            ViewUtil.setScaleOnRotatedView(binding.flipView, binding.cardBmGoalie)
            ViewUtil.setScaleOnRotatedView(binding.flipView, binding.backgroundBmGoalie)
            ViewUtil.setScaleOnRotatedView(binding.flipView, binding.flipBtmGoalie)
        }

        binding.flipTopGoalie.post {
            ViewUtil.setScaleOnRotatedView(binding.flipView, binding.cardTopGoalie)
            ViewUtil.setScaleOnRotatedView(binding.flipView, binding.backgroundTopGoalie)
            ViewUtil.setScaleOnRotatedView(binding.flipView, binding.flipTopGoalie)
        }

        computerBinding.computerLamp.post {
            Animations.animateLamp(computerBinding.computerLamp)
        }

        setupMessageRecycler()

        if (isOnlineMode()) {
            binding.vProgressbar.visibility = View.VISIBLE
            updateMessageBox("Waiting\nfor\nopponent\n...", isNeutralMessage = true)
        } else {
            updateMessageBox(
                "Press anywhere to start the game! Period: $period",
                isNeutralMessage = true
            )
            binding.wholeView.setOnClickListener { initGame() }
        }

        vm.setGameMode(
            GameFragmentArgs.fromBundle(requireArguments()).argsLobbyId,
            GameFragmentArgs.fromBundle(requireArguments()).argsLobbyName,
            GameFragmentArgs.fromBundle(requireArguments()).argsLobbyPassword
        )
    }

    override fun onResume() {
        super.onResume()
        storeAllViews()
        setOnClickListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isOnlineMode()) {
            vm.removeLobbyFromDatabase()
            onlineInputTimer?.cancel()
        }
        lobbyId = ""
        vm.clearAllValueEventListeners()
        Animations.stopAllAnimations()
        Animations.stopAllPulsingCards()
        _binding = null
    }

    private fun initGame() {
        vm.analyticsMatchStarted(currentGameMode)

        teamBottomScore = 0
        teamTopScore = 0
        updateScores()

        Constants.resetBooleansToInitState()

        resetAllCards(teamGreenViews)
        resetAllCards(teamPurpleViews)
        binding.cardTopGoalie.tag = null
        binding.cardBmGoalie.tag = null

        addGoalieView(bottom = true)
        binding.wholeView.visibility = View.GONE
    }

    /*
    * Views management
    * */

    private fun updateScores() {
        if (binding.topTeamScore == null || binding.bmTeamScore == null) return

        val scorerTextView = when {
            teamTopScore > Integer.parseInt(binding.topTeamScore.text.toString()) -> binding.topTeamScore
            teamBottomScore > Integer.parseInt(binding.bmTeamScore.text.toString()) -> binding.bmTeamScore
            else -> null
        }

        if (scorerTextView == null) {
            binding.topTeamScore.text = teamTopScore.toString()
            binding.bmTeamScore.text = teamBottomScore.toString()
        } else {
            Animations.animateScore(scorerTextView) {
                Util.vibrate(requireContext(), true)
                binding.topTeamScore.text = teamTopScore.toString()
                binding.bmTeamScore.text = teamBottomScore.toString()
            }
        }
    }

    private fun updateTheTimerText(secsLeftInLong: Long) {
        binding.txtOnlineTimer.text = (secsLeftInLong / 1000).toString()
    }

    private fun restoreFlipViewsPosition() {
        binding.flipView.rotation = 0f
        binding.flipView.x = fvMainX
        binding.flipView.y = fvMainY
        binding.flipBtmGoalie.x = fvGoalieBtmX
        binding.flipBtmGoalie.y = fvGoalieBtmY
        binding.flipTopGoalie.x = fvGoalieTopX
        binding.flipTopGoalie.y = fvGoalieTopY
    }

    private fun storeAllViews() {
        teamGreenViews.clear()
        teamPurpleViews.clear()

        teamGreenViews.apply {
            add(binding.cardBmForwardLeft)
            add(binding.cardBmCenter)
            add(binding.cardBmForwardRight)
            add(binding.cardBmDefenderLeft)
            add(binding.cardBmDefenderRight)
            add(binding.cardBmGoalie)
        }

        teamPurpleViews.apply {
            add(binding.cardTopForwardLeft)
            add(binding.cardTopCenter)
            add(binding.cardTopForwardRight)
            add(binding.cardTopDefenderLeft)
            add(binding.cardTopDefenderRight)
            add(binding.cardTopGoalie)
        }
    }

    private fun resetAllCards(cardImageViews: List<AppCompatImageView>) {
        cardImageViews.forEach {
            it.setImageResource(android.R.color.transparent)
            it.tag = Integer.valueOf(android.R.color.transparent)
        }
    }

    private fun setupMessageRecycler() = computerBinding.vRecycler.apply {
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
            binding.flipView,
            binding.cardDeck,
            binding.cardPicked,
            resId,
            null,
            isVertical = true
        )

        Animations.animateFlipPlayingCard(
            binding.flipView,
            binding.cardsLeft,
            vm.cardDeck.size > 50,
            { onFlipPlayingCardEnd(isBadCard) },
            { vm.notifyMessage("Please choose a position.") },
            { if (vm.cardDeck.size <= 1) binding.cardBackground.visibility = View.GONE }
        )
    }

    private fun prepareViewsToPulse() {
        val teamToPulse = if (isTeamGreenTurn()) teamPurpleViews else teamGreenViews
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
            fvGoalieBtmX = binding.flipBtmGoalie.x
            fvGoalieBtmY = binding.flipBtmGoalie.y
            fvGoalieTopX = binding.flipTopGoalie.x
            fvGoalieTopY = binding.flipTopGoalie.y
        }

        // ONLY adding view. No real goalie card is assigning to that team by this function.
        removeAllOnClickListeners()

        val view = if (bottom) binding.cardBmGoalie else binding.cardTopGoalie

        binding.cardDeck.setImageResource(R.drawable.red_back_vertical)
        binding.cardPicked.setImageResource(R.drawable.red_back_vertical)

        val delay: Long = if (withStartDelay) 2500 else 250

        Animations.animateAddGoalie(
            flipView = binding.flipView,
            goalie = view,
            xForAttacker = binding.cardBmCenter.x,
            delay = delay
        )
        {
            // onStop
            restoreFlipViewsPosition()
            vm.onGoalieAddedAnimationEnd(view)

            vm.checkGameSituation()
            vm.removeCardFromDeck(doNotNotify = true)
            if (binding.cardTopGoalie.tag != Integer.valueOf(R.drawable.red_back)) {
                addGoalieView(bottom = false)
            } else {
                vm.notifyPickedCard()
                binding.cardsLeft.visibility = View.VISIBLE
            }
        }
    }

    private fun animateAddPlayer(
        targetView: AppCompatImageView,
        team: Array<Card?>,
        spotIndex: Int
    ) {
        Animations.animateAddPlayer(binding.flipView, targetView) {
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

        Animations.animateAttackPlayer(binding.flipView, targetView, vm.getScreenWidth()) {
            // OnStop
            targetView.x = victimOriginalX
            targetView.y = victimOriginalY
            restoreFlipViewsPosition()
            vm.onAttackedAnimationEnd(targetView) { prepareViewsToPulse() }
        }
    }

    private fun prepareAttackPlayer(
        victimTeam: Array<Card?>,
        spotIndex: Int,
        victimView: AppCompatImageView
    ) {
        if (spotIndex == 5) {
            // Attacking goalie
            isShootingAtGoalie = true

            if (isOnlineMode()) onlineInputTimer?.cancel()

            if (vm.canAttack(victimTeam, spotIndex, victimView)) {
                removeAllOnClickListeners()
                Animations.stopAllPulsingCards()

                vm.notifyMessageAttackingGoalie()

                val isTargetGoalieBottom = !isTeamGreenTurn()
                val targetView = if (isTargetGoalieBottom) binding.flipBtmGoalie else binding.flipTopGoalie
                val targetViewFront =
                    if (isTargetGoalieBottom) binding.flipBtmGoalieFront else binding.flipTopGoalieFront
                val targetViewBack =
                    if (isTargetGoalieBottom)binding.flipBtmGoalieBack else binding.flipTopGoalieBack 
                val targetTeam = if (isTargetGoalieBottom) teamGreen else teamPurple

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
                    binding.fadingView,
                    binding.flipView,
                    targetView,
                    vm.getScreenWidth(),
                    binding.cardTopCenter.x,
                    tempGoalieCard,
                    { message -> updateMessageBox(message) },
                    {
                        // OnStop
                        onGoalieActionEnd(targetView, true, targetTeam)
                        updateScores()
                        if (vm.cardDeck.size <= 1) {
                            if (isNotOnlineMode() || (isMyOnlineTeamGreen && isTeamGreenTurn()) || (!isMyOnlineTeamGreen && isTeamPurpleTurn()))
                                vm.removeCardFromDeck(doNotNotify = true)
                        } else {
                            addGoalieView(bottom = isTargetGoalieBottom)
                        }
                    }
                )
            }
        } else {
            // Attacking another player
            if (vm.canAttack(victimTeam, spotIndex, victimView))
                animateAttack(victimView)
            else
                Util.vibrate(requireContext(), false)
        }
    }

    private fun prepareGoalieSaved(victimView: AppCompatImageView) {
        removeAllOnClickListeners()
        Animations.stopAllPulsingCards()

        isShootingAtGoalie = true

        if (isOnlineMode()) onlineInputTimer?.cancel()

        vm.notifyMessageAttackingGoalie()

        val isTargetGoalieBottom = !isTeamGreenTurn()
        val targetView = if (isTargetGoalieBottom) binding.flipBtmGoalie else binding.flipTopGoalie
        val targetViewFront =
            if (isTargetGoalieBottom) binding.flipBtmGoalieFront else binding.flipTopGoalieFront
        val targetViewBack =
            if (isTargetGoalieBottom) binding.flipBtmGoalieBack  else binding.flipTopGoalieBack 
        val targetTeam = if (isTargetGoalieBottom) teamGreen else teamPurple

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
            binding.fadingView,
            binding.flipView,
            targetView,
            vm.getScreenWidth(),
            binding.cardTopCenter.x,
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
        if (binding.flipView == null) return

        binding.flipView.flipTheView()

        if (isOnlineMode()) {
            onlineInputTimer?.cancel()
            if (!isBadCard) {
                onlineInputTimer = Util.getOnlineInputTimer({ timeLeftInLong ->
                    updateTheTimerText(timeLeftInLong)
                }, {
                    if ((isOnlineMode() && vm.isMyOnlineTeamBottom() && isTeamGreenTurn())
                        || (isOnlineMode() && !vm.isMyOnlineTeamBottom() && isTeamPurpleTurn())
                    ) {
                        vm.removeLobbyFromDatabase()
                        Toast.makeText(
                            requireContext(),
                            "You didn't make input in time. You forfeited the game.",
                            Toast.LENGTH_LONG
                        ).show()
                        view?.findNavController()?.popBackStack()
                    }
                })
                onlineInputTimer?.start()
            }
        }

        // Bot's turn
        if (isBotMoving() && !isBadCard) {
            // Adding player
            if (isRestoringPlayers) {
                vm.botChooseEmptySpot(vm.getEmptySpots(teamPurpleViews)) {
                    // Trigger the bot's move
                    if (it != -1)
                        if (vm.canAddPlayerView(teamPurpleViews[it], teamPurple, it))
                            animateAddPlayer(teamPurpleViews[it], teamPurple, it)
                }
            } else {
                // Attacking player
                when (val chosenIndex = vm.botChooseIndexToAttack(possibleMovesIndexes)) {
                    -1 -> { /* Do nothing */
                    }

                    PLAYER_GOALIE -> {
                        tempGoalieCard = teamGreen[PLAYER_GOALIE]
                        if (vm.canAttack(
                                teamGreen,
                                PLAYER_GOALIE,
                                binding.cardBmGoalie
                            )
                        ) prepareAttackPlayer(
                            teamGreen,
                            PLAYER_GOALIE,
                            binding.cardBmGoalie
                        )
                        else prepareGoalieSaved(binding.cardBmGoalie)
                    }

                    else -> prepareAttackPlayer(teamGreen, chosenIndex, teamGreenViews[chosenIndex])
                }
            }
        } else {
            if (!isBadCard) {
                if ((isOnlineMode() && vm.isMyOnlineTeamBottom() && isTeamPurpleTurn())
                    || (isOnlineMode() && !vm.isMyOnlineTeamBottom() && isTeamGreenTurn())
                ) return

                val teamViews = if (isTeamGreenTurn()) teamGreenViews else teamPurpleViews
                val team = if (isTeamGreenTurn()) teamGreen else teamPurple
                val onlyOneEmptySpotOrNull = vm.getEmptySpots(teamViews).takeIf { it.size == 1 }

                if (isRestoringPlayers && onlyOneEmptySpotOrNull != null && vm.cardDeck.size >= 5) {
                    val index = onlyOneEmptySpotOrNull.first()

                    if (isOnlineMode()) vm.notifyOnlineInput(index)
                    if (vm.canAddPlayerView(teamViews[index], team, index))
                        animateAddPlayer(teamViews[index], team, index)
                } else {
                    setOnClickListeners()
                }
            } else {
                // If it is bad card, this runs
                Animations.animateBadCard(
                    binding.flipView,
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
        binding.fadingView.visibility = View.GONE
        view.visibility = View.GONE
        isOngoingGame = false
        isRestoringPlayers = true

        if (isGoal) vm.addGoalToScore()

        team[5] = null
        vm.notifyToggleTurn()
        if (vm.cardDeck.size > 1) restoreFlipViewsPosition() // restoreFlipViewsPosition() will be executed in isNextPeriodReady()
    }

    /*
    * Game management
    * */

    private fun isNextPeriodReady(nextPeriod: Int): Boolean {
        removeAllOnClickListeners()

        isRestoringPlayers = true

        binding.cardsLeft.text = getString(R.string.full_card_deck_amount)
        restoreFlipViewsPosition()

        return if (vm.isNextPeriodReady()) {
            binding.cardBackground.visibility = View.VISIBLE
            resetAllCards(teamGreenViews)
            resetAllCards(teamPurpleViews)
            binding.cardTopGoalie.tag = null
            binding.cardBmGoalie.tag = null
            true
        } else {
            if (isOnlineMode())
                vm.analyticsOnlineMatchFulfilled()
            else
                vm.analyticsMatchVsBotFulfilled(currentGameMode, teamBottomScore > teamTopScore)

            binding.txtWinner.text = when {
                teamBottomScore > teamTopScore -> "Team Green\nwon with $teamBottomScore-$teamTopScore!"
                else -> "Team Purple\nwon with $teamTopScore-$teamBottomScore!"
            }
            isWinnerDeclared = true
            Animations.animateWinner(binding.fadingView, binding.lottieTrophy, binding.txtWinner)
            Util.vibrate(requireContext(), true)

            binding.fadingView.setOnClickListener {
                view?.let {
                    Navigation.findNavController(it).popBackStack()
                }
            }
            false
        }
    }

    /*
    * OnClickListeners
    * */

    private fun setOnClickListeners() {
        teamGreenViews.forEach { it.setOnClickListener(this) }
        teamPurpleViews.forEach { it.setOnClickListener(this) }
    }

    private fun removeAllOnClickListeners() {
        teamGreenViews.forEach { it.setOnClickListener(null) }
        teamPurpleViews.forEach { it.setOnClickListener(null) }
    }

    override fun onClick(v: View) {
        if (isBotMoving() || isOpponentMoving()) return

        val spotIndex: Int
        if (isOngoingGame) {
            if (v.tag == Integer.valueOf(android.R.color.transparent)) return
            if (isTeamGreenTurn()) {
                spotIndex = when (v.id) {
                    R.id.card_top_forward_left -> 0
                    R.id.card_top_center -> 1
                    R.id.card_top_forward_right -> 2
                    R.id.card_top_defender_left ->
                        if (vm.areEnoughForwardsOut(teamPurple, 3)) 3 else return

                    R.id.card_top_defender_right ->
                        if (vm.areEnoughForwardsOut(teamPurple, 4)) 4 else return

                    R.id.card_top_goalie -> if (vm.isAtLeastOneDefenderOut(teamPurple)) 5 else return
                    else -> return
                }
            } else {
                spotIndex = when (v.id) {
                    R.id.card_bm_forward_left -> 0
                    R.id.card_bm_center -> 1
                    R.id.card_bm_forward_right -> 2
                    R.id.card_bm_defender_left ->
                        if (vm.areEnoughForwardsOut(teamGreen, 3)) 3 else return

                    R.id.card_bm_defender_right ->
                        if (vm.areEnoughForwardsOut(teamGreen, 4)) 4 else return

                    R.id.card_bm_goalie -> if (vm.isAtLeastOneDefenderOut(teamGreen)) 5 else return
                    else -> return
                }
            }

            val imageView = view?.findViewById<AppCompatImageView>(v.id)
            val targetTeam = if (isTeamGreenTurn()) teamPurple else teamGreen

            imageView?.let {
                if (isOnlineMode()) vm.notifyOnlineInput(spotIndex)

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
            if (isTeamGreenTurn()) {
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
            val team = if (isTeamGreenTurn()) teamGreen else teamPurple

            imageView?.let {
                if (vm.canAddPlayerView(imageView, team, spotIndex) && v.tag != null) {
                    if (isOnlineMode()) vm.notifyOnlineInput(spotIndex)
                    removeAllOnClickListeners()
                    animateAddPlayer(imageView, team, spotIndex)
                } else {
                    Util.vibrate(requireContext(), false)
                }
            }
        }
    }
}
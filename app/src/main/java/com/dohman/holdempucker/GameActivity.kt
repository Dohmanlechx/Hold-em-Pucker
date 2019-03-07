package com.dohman.holdempucker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.dohman.holdempucker.cards.Card
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var vm: GameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        vm = ViewModelProviders.of(this).get(GameViewModel::class.java)

        vm.halfTimeNotifier.observe(this, Observer { clearAllCards(it) })

        vm.whoseTurnNotifier.observe(this, Observer { txt_whoseturn.text = it })

        vm.pickedCardNotifier.observe(this, Observer { card_picked.setImageResource(it) })
        vm.cardsCountNotifier.observe(this, Observer { cards_left.text = it.toString() })

        vm.nfyBtmGoalie.observe(this, Observer { if (it) card_bm_goalie.setImageResource(R.drawable.red_back) })
        vm.nfyTopGoalie.observe(this, Observer { if (it) card_top_goalie.setImageResource(R.drawable.red_back) })

        vm.updateScores(top_team_score, bm_team_score)

        btn_debug.setOnClickListener(this)

        setOnClickListeners()
    }

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

        btn_debug.setOnClickListener(this)
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
        period + nextPeriod

        if (period > 3 && (teamBottomScore != teamTopScore)) {
            if (teamBottomScore > teamTopScore) btn_debug.text = "Bottom won!" else "Top won!"
            gameOver()
        } else {
            btn_debug.text = "Period: $period"

            whoseTurn = if (whoseTeamStartedLastPeriod == WhoseTurn.BOTTOM) WhoseTurn.TOP else WhoseTurn.BOTTOM

            card_top_forward_left.setImageResource(android.R.color.transparent)
            card_top_center.setImageResource(android.R.color.transparent)
            card_top_forward_right.setImageResource(android.R.color.transparent)
            card_top_defender_left.setImageResource(android.R.color.transparent)
            card_top_defender_right.setImageResource(android.R.color.transparent)
            card_top_goalie.setImageResource(android.R.color.transparent)

            card_bm_forward_left.setImageResource(android.R.color.transparent)
            card_bm_center.setImageResource(android.R.color.transparent)
            card_bm_forward_right.setImageResource(android.R.color.transparent)
            card_bm_defender_left.setImageResource(android.R.color.transparent)
            card_bm_defender_right.setImageResource(android.R.color.transparent)
            card_bm_goalie.setImageResource(android.R.color.transparent)
        }
    }

    override fun onClick(v: View) {
        if (isOngoingGame) {
            if (whoseTurn == WhoseTurn.BOTTOM) {
                when (v.id) {
                    R.id.card_top_forward_left -> {
                        vm.attack(teamTop, 0, card_top_forward_left)
                    }
                    R.id.card_top_center -> {
                        vm.attack(teamTop, 1, card_top_center)
                    }
                    R.id.card_top_forward_right -> {
                        vm.attack(teamTop, 2, card_top_forward_right)
                    }
                    R.id.card_top_defender_left -> {
                        if (vm.areEnoughForwardsOut(teamTop, 3))
                            vm.attack(teamTop, 3, card_top_defender_left)
                    }
                    R.id.card_top_defender_right -> {
                        if (vm.areEnoughForwardsOut(teamTop, 4))
                            vm.attack(teamTop, 4, card_top_defender_right)
                    }
                    R.id.card_top_goalie -> {
                        if (vm.isAtLeastOneDefenderOut(teamTop)) {
                            if (vm.attack(teamTop, 5, card_top_goalie)) {
                                teamBottomScore++
                                vm.updateScores(top_team_score, bm_team_score)
                            } else {
                                vm.goalieSaved(teamTop)
                            }
                        }
                    }
                    R.id.btn_debug -> {
                        vm.removeCardFromDeck()
                        vm.showPickedCard()
                    }
                }
            } else {
                when (v.id) {
                    R.id.card_bm_forward_left -> {
                        vm.attack(teamBottom, 0, card_bm_forward_left)
                    }
                    R.id.card_bm_center -> {
                        vm.attack(teamBottom, 1, card_bm_center)
                    }
                    R.id.card_bm_forward_right -> {
                        vm.attack(teamBottom, 2, card_bm_forward_right)
                    }
                    R.id.card_bm_defender_left -> {
                        if (vm.areEnoughForwardsOut(teamBottom, 3))
                            vm.attack(teamBottom, 3, card_bm_defender_left)
                    }
                    R.id.card_bm_defender_right -> {
                        if (vm.areEnoughForwardsOut(teamBottom, 4))
                            vm.attack(teamBottom, 4, card_bm_defender_right)
                    }
                    R.id.card_bm_goalie -> {
                        if (vm.isAtLeastOneDefenderOut(teamBottom)) {
                            if (vm.attack(teamBottom, 5, card_bm_goalie)) {
                                teamTopScore++
                                vm.updateScores(top_team_score, bm_team_score)
                            } else {
                                vm.goalieSaved(teamBottom)
                            }
                        }
                    }
                    R.id.btn_debug -> {
                        vm.removeCardFromDeck()
                        vm.showPickedCard()
                    }
                }
            }
        } else {
            if (whoseTurn == WhoseTurn.BOTTOM) {
                when (v.id) {
                    R.id.card_bm_forward_left -> {
                        vm.addPlayer(card_bm_forward_left, teamBottom, 0)
                    }
                    R.id.card_bm_center -> {
                        vm.addPlayer(card_bm_center, teamBottom, 1)
                    }
                    R.id.card_bm_forward_right -> {
                        vm.addPlayer(card_bm_forward_right, teamBottom, 2)
                    }
                    R.id.card_bm_defender_left -> {
                        vm.addPlayer(card_bm_defender_left, teamBottom, 3)
                    }
                    R.id.card_bm_defender_right -> {
                        vm.addPlayer(card_bm_defender_right, teamBottom, 4)
                    }
                }
            } else {
                when (v.id) {
                    R.id.card_top_forward_left -> {
                        vm.addPlayer(card_top_forward_left, teamTop, 0)
                    }
                    R.id.card_top_center -> {
                        vm.addPlayer(card_top_center, teamTop, 1)
                    }
                    R.id.card_top_forward_right -> {
                        vm.addPlayer(card_top_forward_right, teamTop, 2)
                    }
                    R.id.card_top_defender_left -> {
                        vm.addPlayer(card_top_defender_left, teamTop, 3)
                    }
                    R.id.card_top_defender_right -> {
                        vm.addPlayer(card_top_defender_right, teamTop, 4)
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "DBG: GameActivity.kt"

        var period = 1
        var isOngoingGame = false // Set to true when all cards are laid out
        var restoringPlayers = false // Set to true when a team need to lay out new cards to fulfill
        var areTeamsReadyToStartPeriod = false // Set to true as soon as both teams are full in the very beginning
        var whoseTurn = WhoseTurn.TOP
        var whoseTeamStartedLastPeriod = WhoseTurn.BOTTOM
        var teamTopScore: Int = 0
        var teamBottomScore: Int = 0
        val teamTop = arrayOfNulls<Card>(6)
        val teamBottom = arrayOfNulls<Card>(6)

        /*  Index 0 = Left forward | 1 = Center | 2 = Right forward
                        3 = Left defender | 4 = Right defender
                                    5 = Goalie                          */
    }

    enum class WhoseTurn {
        BOTTOM, TOP;

        companion object {
            fun toggleTurn() {
                whoseTurn = if (whoseTurn == BOTTOM) TOP else BOTTOM
            }
        }
    }
}

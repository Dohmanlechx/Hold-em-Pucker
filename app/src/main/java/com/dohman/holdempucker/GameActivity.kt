package com.dohman.holdempucker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

        vm.pickedCardNotifier.observe(this, Observer {
            card_picked.setImageResource(it)
            txt_whoseturn.text = whoseTurn.name
        })
        vm.cardsCountNotifier.observe(this, Observer { cards_left.text = it.toString() })

        vm.nfyCard.observe(this, Observer { updateCardImageResource(it) })
        vm.nfyBtmGoalie.observe(this, Observer { if (it) card_bm_goalie.setImageResource(R.drawable.red_back) })
        vm.nfyTopGoalie.observe(this, Observer { if (it) card_top_goalie.setImageResource(R.drawable.red_back) })

        setOnClickListeners()

    }

    private fun updateCardImageResource(value: Map<Array<Card?>, Int>) { // FIXME: Not needed?
        Log.d(TAG, value.toString())
    }

    private fun setOnClickListeners() {
        card_top_forward_left.setOnClickListener(this)
        card_top_center.setOnClickListener(this)
        card_top_forward_right.setOnClickListener(this)
        card_top_defender_left.setOnClickListener(this)
        card_top_defender_right.setOnClickListener(this)

        card_bm_forward_left.setOnClickListener(this)
        card_bm_center.setOnClickListener(this)
        card_bm_forward_right.setOnClickListener(this)
        card_bm_defender_left.setOnClickListener(this)
        card_bm_defender_right.setOnClickListener(this)
    }

    private fun updateCardImageView(view: AppCompatImageView) {
        view.setImageResource(vm.resIdOfCard(vm.currentCard))
    }

    override fun onClick(v: View) { // FIXME observe all cards?
        if (isOngoingGame) {
                if (whoseTurn == WhoseTurn.BOTTOM) {
                    when (v.id) {
                        R.id.card_top_forward_left -> {
                            if (vm.attack(teamTop, 0)) {
                                card_top_forward_left.setImageResource(R.drawable.skull)
                            }
                        }
                        R.id.card_top_center -> {

                        }
                        R.id.card_top_forward_right -> {

                        }
                        R.id.card_top_defender_left -> {

                        }
                        R.id.card_top_defender_right -> {

                        }
                        R.id.card_top_goalie -> {

                        }
                    }
                } else {
                    when (v.id) {
                        R.id.card_bm_forward_left -> {

                        }
                        R.id.card_bm_center -> {

                        }
                        R.id.card_bm_forward_right -> {

                        }
                        R.id.card_bm_defender_left -> {

                        }
                        R.id.card_bm_defender_right -> {

                        }
                        R.id.card_bm_goalie -> {

                        }
                }
            }
        } else {
            if (whoseTurn == WhoseTurn.BOTTOM) {
                when (v.id) {
                    R.id.card_bm_forward_left -> {
                        if (card_bm_forward_left.drawable != null) return
                        updateCardImageView(card_bm_forward_left)
                        vm.setPlayerInTeam(teamBottom, 0)
                    }
                    R.id.card_bm_center -> {
                        if (card_bm_center.drawable != null) return
                        updateCardImageView(card_bm_center)
                        vm.setPlayerInTeam(teamBottom, 1)
                    }
                    R.id.card_bm_forward_right -> {
                        if (card_bm_forward_right.drawable != null) return
                        updateCardImageView(card_bm_forward_right)
                        vm.setPlayerInTeam(teamBottom, 2)
                    }
                    R.id.card_bm_defender_left -> {
                        if (card_bm_defender_left.drawable != null) return
                        updateCardImageView(card_bm_defender_left)
                        vm.setPlayerInTeam(teamBottom, 3)
                    }
                    R.id.card_bm_defender_right -> {
                        if (card_bm_defender_right.drawable != null) return
                        updateCardImageView(card_bm_defender_right)
                        vm.setPlayerInTeam(teamBottom, 4)
                    }
                }
            } else {
                when (v.id) {
                    R.id.card_top_forward_left -> {
                        if (card_top_forward_left.drawable != null) return
                        updateCardImageView(card_top_forward_left)
                        vm.setPlayerInTeam(teamTop, 0)
                    }
                    R.id.card_top_center -> {
                        if (card_top_center.drawable != null) return
                        updateCardImageView(card_top_center)
                        vm.setPlayerInTeam(teamTop, 1)
                    }
                    R.id.card_top_forward_right -> {
                        if (card_top_forward_right.drawable != null) return
                        updateCardImageView(card_top_forward_right)
                        vm.setPlayerInTeam(teamTop, 2)
                    }
                    R.id.card_top_defender_left -> {
                        if (card_top_defender_left.drawable != null) return
                        updateCardImageView(card_top_defender_left)
                        vm.setPlayerInTeam(teamTop, 3)
                    }
                    R.id.card_top_defender_right -> {
                        if (card_top_defender_right.drawable != null) return
                        updateCardImageView(card_top_defender_right)
                        vm.setPlayerInTeam(teamTop, 4)
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "DBG: GameActivity.kt"

        var isOngoingGame = false // Set to true when all cards are laid out
        var whoseTurn = WhoseTurn.TOP
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

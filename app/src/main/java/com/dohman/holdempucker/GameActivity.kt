package com.dohman.holdempucker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.dohman.holdempucker.cards.Card
import com.dohman.holdempucker.cards.CardDeck
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : AppCompatActivity(), View.OnClickListener {
    private val cardDeck = CardDeck().cardDeck
    private var pickedCard: Card = cardDeck.first()
    lateinit var currentCard: Card

    private val teamTop = arrayOfNulls<Card>(6)
    private val teamBottom = arrayOfNulls<Card>(6)

    /*  Index 0 = Left forward | 1 = Center | 2 = Right forward
                3 = Left defender | 4 = Right defender
                            5 = Goalie                          */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        val vm = ViewModelProviders.of(this).get(GameViewModel::class.java)

        setOnClickListeners()

        showPickedCard()
        letPlayerChooseSpot()
    }

    private fun setOnClickListeners() {
        card_bm_forward_left.setOnClickListener(this)
        card_bm_center.setOnClickListener(this)
        card_bm_forward_right.setOnClickListener(this)
        card_bm_defender_left.setOnClickListener(this)
        card_bm_defender_right.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.card_bm_forward_left -> {
                if (card_bm_forward_left.drawable != null) return
                card_bm_forward_left.setImageResource(resIdOfCard(currentCard))
                teamBottom[0] = currentCard
                takeNewCardFromDeck()
            }
            R.id.card_bm_center -> {
                if (card_bm_center.drawable != null) return
                card_bm_center.setImageResource(resIdOfCard(currentCard))
                teamBottom[1] = currentCard
                takeNewCardFromDeck()
            }
            R.id.card_bm_forward_right -> {
                if (card_bm_forward_right.drawable != null) return
                card_bm_forward_right.setImageResource(resIdOfCard(currentCard))
                teamBottom[2] = currentCard
                takeNewCardFromDeck()
            }
            R.id.card_bm_defender_left -> {
                if (card_bm_defender_left.drawable != null) return
                card_bm_defender_left.setImageResource(resIdOfCard(currentCard))
                teamBottom[3] = currentCard
                takeNewCardFromDeck()
            }
            R.id.card_bm_defender_right -> {
                if (card_bm_defender_right.drawable != null) return
                card_bm_defender_right.setImageResource(resIdOfCard(currentCard))
                teamBottom[4] = currentCard
                takeNewCardFromDeck()
            }
        }
    }

    private fun showPickedCard() {
        if (!isGoalieThere(pickedCard)) { // If returned false, goalie is added
            takeNewCardFromDeck()
            return
        }

        pickedCard.let {
            card_picked.setImageResource(resIdOfCard(it))
            currentCard = it
            removeCardFromDeck()
        }

        if (cardDeck.isEmpty()) {
            //halfTime() // FIXME
        }
    }

    private fun removeCardFromDeck() {
        cardDeck.remove(pickedCard)
        pickedCard = cardDeck.first()
        cards_left.text = cardDeck.size.toString()
    }

    private fun takeNewCardFromDeck() {
        card_picked.setImageDrawable(null)
        showPickedCard()
    }

    private fun isGoalieThere(goalieCard: Card): Boolean {
        teamBottom.let { if (!it.all { element -> element == null }) return true else it[5] = goalieCard }
        card_bm_goalie.setImageResource(R.drawable.red_back)
        removeCardFromDeck()

        return false // But goalie is added now
    }

    private fun letPlayerChooseSpot() {

    }

    private fun resIdOfCard(card: Card): Int {
        return card.let { resources.getIdentifier(it.src, "drawable", packageName) }
    }

    companion object {
        const val TAG = "GameActivity.kt"
    }
}

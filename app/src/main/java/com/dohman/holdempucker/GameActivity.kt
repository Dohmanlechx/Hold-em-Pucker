package com.dohman.holdempucker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.dohman.holdempucker.cards.Card
import com.dohman.holdempucker.cards.CardDeck
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : AppCompatActivity(), View.OnClickListener {
    private val cardDeck = CardDeck().cardDeck
    private var pickedCard: Card = cardDeck.first()
    lateinit var currentCard: Card

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        setOnClickListeners()

        showRandomCard()
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
                takeNewCardFromDeck()
            }
            R.id.card_bm_center -> {
                if (card_bm_center.drawable != null) return
                card_bm_center.setImageResource(resIdOfCard(currentCard))
                takeNewCardFromDeck()
            }
            R.id.card_bm_forward_right -> {
                if (card_bm_forward_right.drawable != null) return
                card_bm_forward_right.setImageResource(resIdOfCard(currentCard))
                takeNewCardFromDeck()
            }
            R.id.card_bm_defender_left -> {
                if (card_bm_defender_left.drawable != null) return
                card_bm_defender_left.setImageResource(resIdOfCard(currentCard))
                takeNewCardFromDeck()
            }
            R.id.card_bm_defender_right -> {
                if (card_bm_defender_right.drawable != null) return
                card_bm_defender_right.setImageResource(resIdOfCard(currentCard))
                takeNewCardFromDeck()
            }
        }
    }

    private fun showRandomCard() {
        pickedCard.let {
            card_picked.setImageResource(resIdOfCard(it))
            currentCard = it
            cardDeck.remove(it)
            pickedCard = cardDeck.first()
            cards_left.text = cardDeck.size.toString()
        }

        if (cardDeck.isEmpty()) {
            //halfTime() // FIXME
        }
    }

    private fun takeNewCardFromDeck() {
        card_picked.setImageDrawable(null)
        showRandomCard()
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

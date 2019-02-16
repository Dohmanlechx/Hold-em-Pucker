package com.dohman.holdempucker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dohman.holdempucker.cards.CardDeck
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : AppCompatActivity() {
    private val cardDeck= CardDeck().cardDeck

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        showRandomCard()
    }

    private fun showRandomCard() {
        cardDeck.first().let {
            val resID = resources.getIdentifier(it.src, "drawable", packageName)
            card_picked.setImageResource(resID)
            cardDeck.remove(it)
            cards_left.text = cardDeck.size.toString()
        }
    }

    companion object {
        const val TAG = "GameActivity.kt"
    }
}

package com.dohman.holdempucker.cards

import android.util.Log

class CardDeck {
    lateinit var suits: Suits
    lateinit var card: Card

    init {
        initializeCards()
    }

    private fun initializeCards()/*: List<Card>*/ {
        enumValues<Suits>().forEach {
            
        }
    }

    companion object {
        const val TAG = "CardDeck.kt"
    }
}
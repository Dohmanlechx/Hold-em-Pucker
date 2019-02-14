package com.dohman.holdempucker.cards

class CardDeck {

    init {
        initializeCards()
    }

    private fun initializeCards(): List<Card> {
        val cardDeck = mutableListOf<Card>()
        enumValues<Suits>().forEach { suit ->
            suit.let {
                for (i in 2..14) {
                    val card = Card()
                    card.suit = it
                    card.rank = i
                    cardDeck.add(card)
                }
            }
        }

        return cardDeck
    }

    companion object {
        const val TAG = "CardDeck.kt"
    }
}
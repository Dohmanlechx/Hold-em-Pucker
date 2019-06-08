package com.dohman.holdempucker.repositories

import com.dohman.holdempucker.models.Card
import com.dohman.holdempucker.models.Suits
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepository @Inject constructor(
    // Placeholder for other Repositories.
) {

    fun createCards() = initializeCards().shuffled()

    private fun initializeCards(): MutableList<Card> {
        val cardDeck = mutableListOf<Card>()
        enumValues<Suits>().forEach { suit ->
            for (rank in 2..14) {
                val card = Card()
                card.suit = suit
                card.rank = rank
                card.src = getImageStr(suit, rank)
                cardDeck.add(card)
            }
        }

        return cardDeck
    }

    private fun getImageStr(suit: Suits, rank: Int): String {
        val suitFirstChar: Char = suit.toString().first().toLowerCase()
        val rankAsStr: String = if (rank <= 10) {
            rank.toString()
        } else {
            when (rank) {
                11 -> "j"
                12 -> "q"
                13 -> "k"
                else -> "a"
            }
        }

        return "$suitFirstChar$rankAsStr"
    }
}
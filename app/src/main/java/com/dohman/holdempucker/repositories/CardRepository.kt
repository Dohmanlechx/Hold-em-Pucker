package com.dohman.holdempucker.repositories

import com.dohman.holdempucker.cards.Card
import com.dohman.holdempucker.cards.Suits
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
            suit.let {
                for (i in 2..8) {
                    val card = Card()
                    card.suit = it
                    card.rank = i
                    card.src = getImageStr(it, i)
                    cardDeck.add(card)
                }
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
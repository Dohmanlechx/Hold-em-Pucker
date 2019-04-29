package com.dohman.holdempucker.cards

class CardDeck {
    val cardDeck = mutableListOf<Card>()

    init {
        initializeCards()
        cardDeck.shuffle()
        cardDeck.shuffle()
        cardDeck.shuffle()
    }

    private fun initializeCards(): List<Card> {
        enumValues<Suits>().forEach { suit ->
            suit.let {
                for (i in 2..14) {
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
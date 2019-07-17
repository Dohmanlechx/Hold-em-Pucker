package com.dohman.holdempucker.models

class Card {
    var suit: Suits? = null
    var rank: Int? = null
    var src: String? = null
    var idForOnline: Int? = null

    enum class Suits {
        SPADES,
        HEARTS,
        CLUBS,
        DIAMONDS
    }
}
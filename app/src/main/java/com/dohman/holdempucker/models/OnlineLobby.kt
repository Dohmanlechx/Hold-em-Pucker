package com.dohman.holdempucker.models

open class OnlineLobby(
    val id: String? = null,
    val name: String? = null,
    val period: Int? = null,
    val topPlayer: String? = null,
    val bottomPlayer: String? = null,
    val topInput: Int? = null,
    val bottomInput: Int? = null,
    val cardDeck: List<Card>? = null) {
}
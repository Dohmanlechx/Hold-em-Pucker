package com.dohman.holdempucker.models

class OnlineLobby(
    val id: String?,
    val topPlayer: String,
    val bottomPlayer: String,
    val topInput: Int,
    val bottomInput: Int,
    val cardDeck: List<Card>?) {
}
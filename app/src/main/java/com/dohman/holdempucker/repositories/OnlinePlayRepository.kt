package com.dohman.holdempucker.repositories

import androidx.lifecycle.MutableLiveData
import com.dohman.holdempucker.models.Card
import com.dohman.holdempucker.models.OnlineLobby
import com.dohman.holdempucker.util.Constants.Companion.isMyOnlineTeamBottom
import com.dohman.holdempucker.util.Constants.Companion.lobbyId
import com.google.firebase.database.*
import javax.inject.Inject

class OnlinePlayRepository @Inject constructor(
    private val db: DatabaseReference
) {
    enum class MyOnlineTeam {
        UNDEFINED, BOTTOM, TOP
    }

    var myOnlineTeam: Enum<MyOnlineTeam>

    // All paths in a lobby reference
    private val pathPeriod = "period"
    private val pathBottomInput = "bottomInput"
    private val pathBottomPlayer = "bottomPlayer"
    private val pathCardDeck = "cardDeck"
    private val pathId = "id"
    private val pathTopInput = "topInput"
    private val pathTopPlayer = "topPlayer"

    private var pathForInput: String = ""

    private val vlForPeriod = object : ValueEventListener {
        override fun onDataChange(periodSnapshot: DataSnapshot) {
            val newPeriod = (periodSnapshot.value as? Long)?.toInt()
            period.value = newPeriod
        }

        override fun onCancelled(p0: DatabaseError) {}
    }

    private val vlForInput = object : ValueEventListener {
        override fun onDataChange(inputChild: DataSnapshot) {
            val input = (inputChild.value as? Long)?.toInt()
            if (input in 0..5) opponentInput.value = input
        }

        override fun onCancelled(p0: DatabaseError) {}
    }

    private val vlForOpponentAwaiting = object : ValueEventListener {
        override fun onDataChange(topPlayerChild: DataSnapshot) {
            if (topPlayerChild.value != "") opponentFound.value = true
        }

        override fun onCancelled(p0: DatabaseError) {}
    }

    private val vlForOnlineCardDeck = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val cardList: MutableList<Card> = mutableListOf()

            val cardsAsChildren = snapshot.children
            // Firebase doesn't know how to cast to own classes, only primitive types, HashMap is one of them
            val hashMap = HashMap<String, Card>()
            cardsAsChildren.forEach { hashMap[it.key!!] = it.getValue(Card::class.java)!! }

            val arrayListOfCards: ArrayList<Card> = ArrayList(hashMap.values)
            arrayListOfCards.forEach { cardList.add(it) }

            onlineCardDeck.value = cardList.sortedBy { it.idForOnline }
        }

        override fun onCancelled(p0: DatabaseError) {}
    }

    val period: MutableLiveData<Int> = MutableLiveData()
    val opponentInput: MutableLiveData<Int> = MutableLiveData()
    val opponentFound: MutableLiveData<Boolean> = MutableLiveData()
    val onlineCardDeck: MutableLiveData<List<Card>> = MutableLiveData()

    init {
        myOnlineTeam = MyOnlineTeam.UNDEFINED
        period.value = 1
        opponentFound.value = false
    }

    fun isMyTeamBottom(): Boolean = myOnlineTeam == MyOnlineTeam.BOTTOM

    private fun thisLobby() = db.child(lobbyId)

    fun removeLobbyFromDatabase() = thisLobby().removeValue()

    fun updateInput(input: Int) {
        val ref: DatabaseReference =
            if (isMyTeamBottom()) thisLobby().child(pathBottomInput) else thisLobby().child(pathTopInput)

        ref.apply {
            // Firebase won't notify in the database if the value of value is same as last one
            setValue(-1)
            setValue(input)
        }
    }

    fun updatePeriod(period: Int) = thisLobby().child(pathPeriod).setValue(period)

    fun searchForLobbyOrCreateOne(cardDeck: List<Card>, fFirebaseTaskDone: () -> Unit) {
        var foundLobby = false

        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (lobby in dataSnapshot.children) {
                    if (lobby.child(pathTopPlayer).value == "") {
                        // There is bottom player in lobby waiting, go ahead and join
                        lobbyId = lobby.child(pathId).value as String
                        foundLobby = true
                        myOnlineTeam = MyOnlineTeam.TOP
                        isMyOnlineTeamBottom = false
                        joinThisLobby()
                        break
                    }
                }

                if (!foundLobby) {
                    myOnlineTeam = MyOnlineTeam.BOTTOM
                    isMyOnlineTeamBottom = true
                    createLobby(cardDeck)
                    waitForOpponent()
                }

                fFirebaseTaskDone.invoke()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun joinThisLobby() {
        opponentFound.value = true
        thisLobby().child(pathTopPlayer).setValue("taken")
    }

    private fun createLobby(cardDeck: List<Card>?) {
        lobbyId = db.push().key!! // Can't be null, since db is working here

        val sortedCardDeck = getSortedCardDeck(cardDeck)

        val lobby = OnlineLobby(lobbyId, 1, "", "taken", -1, -1, sortedCardDeck)
        thisLobby().setValue(lobby)
    }

    fun storeCardDeckInLobby(cardDeck: List<Card>?) {
        cardDeck?.forEachIndexed { index, card -> card.idForOnline = index }
        thisLobby().child(pathCardDeck).setValue(cardDeck)
    }

    private fun getSortedCardDeck(cardDeck: List<Card>?): List<Card>? {
        cardDeck?.forEachIndexed { index, card -> card.idForOnline = index }
        return cardDeck?.sortedBy { it.idForOnline }
    }

    fun setListenerForPeriod() = thisLobby().child(pathPeriod).addValueEventListener(vlForPeriod)

    fun setListenerForCardDeck() = thisLobby().child(pathCardDeck).addValueEventListener(vlForOnlineCardDeck)

    fun setListenerForInput() {
        pathForInput = if (isMyTeamBottom()) pathTopInput else pathBottomInput
        thisLobby().child(pathForInput).addValueEventListener(vlForInput)
    }

    fun hasCardDeckBeenRetrievedCorrectly(cardDeck: List<Card>): Boolean {
        val cardDeckFromLobby = onlineCardDeck.value
        return cardDeck == cardDeckFromLobby
    }

    fun retrieveCardDeckFromLobby() = onlineCardDeck.value

    private fun waitForOpponent() {
        thisLobby().child(pathTopPlayer).addValueEventListener(vlForOpponentAwaiting)
    }

    fun resetValues() {
        myOnlineTeam = MyOnlineTeam.UNDEFINED
        opponentInput.postValue(-1)
        opponentFound.postValue(false)
    }

    fun removeAllValueEventListeners() =
        thisLobby().apply {
            child(pathPeriod).removeEventListener(vlForPeriod)
            child(pathForInput).removeEventListener(vlForInput)
            child(pathTopPlayer).removeEventListener(vlForOpponentAwaiting)
            child(pathCardDeck).removeEventListener(vlForOnlineCardDeck)
        }
}
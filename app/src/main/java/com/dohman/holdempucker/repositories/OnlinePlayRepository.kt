package com.dohman.holdempucker.repositories

import androidx.lifecycle.MutableLiveData
import com.dohman.holdempucker.models.Card
import com.dohman.holdempucker.models.OnlineLobby
import com.dohman.holdempucker.util.Constants.Companion.lobbyId
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import javax.inject.Inject

class OnlinePlayRepository @Inject constructor(
    private val db: DatabaseReference
) {
    enum class MyOnlineTeam {
        UNDEFINED, BOTTOM, TOP
    }
    var myOnlineTeam: Enum<MyOnlineTeam>
    fun isMyTeamBottom(): Boolean = myOnlineTeam == MyOnlineTeam.BOTTOM

    private val listOfListeners = mutableListOf<ValueEventListener>()

    val opponentInput: MutableLiveData<Int> = MutableLiveData()
    val opponentFound: MutableLiveData<Boolean> = MutableLiveData()
    var cardDeckForJoiner: MutableLiveData<List<Card>> = MutableLiveData()

    init {
        myOnlineTeam = MyOnlineTeam.UNDEFINED
        opponentFound.value = false
    }

    private fun thisLobby() = db.child(lobbyId)

    private fun observeOpponentInput(myTeamIsBottom: Boolean) {
        val path = if (myTeamIsBottom) "topInput" else "bottomInput"

        listOfListeners.add(db.child(path).addValueEventListener(object : ValueEventListener {
            // FIXME Use lobbyid!
            override fun onDataChange(inputChild: DataSnapshot) {
                val input = (inputChild.value as Long).toInt()
                if (input != -1) opponentInput.value = input
            }

            override fun onCancelled(p0: DatabaseError) {}
        }))
    }

    fun updateInput(input: Int) {
        if (isMyTeamBottom()) thisLobby().child("bottomInput").setValue(input)
        else thisLobby().child("topInput").setValue(input)
    }

    fun searchForLobbyOrCreateOne(cardDeck: List<Card>, fFirebaseTaskDone: () -> Unit) {
        var foundLobby = false

        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (lobby in dataSnapshot.children) {
                    // FIXME Use lobbyid!
                    if (lobby.child("topPlayer").value == "") {
                        // There is bottom player in lobby waiting, go ahead and join
                        lobbyId = lobby.child("id").value as String
                        foundLobby = true
                        myOnlineTeam = MyOnlineTeam.TOP
                        joinThisLobby()
                        break
                    }
                }

                if (!foundLobby) {
                    myOnlineTeam = MyOnlineTeam.BOTTOM
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
        thisLobby().child("topPlayer").setValue("taken")
        retrieveCardDeckFromLobby()
    }

    private fun createLobby(cardDeck: List<Card>?) {
        lobbyId = db.push().key!! // Can't be null, since db is working here

        // Setting id on all the cards, so the opponent would retrieve the card deck in correct order
        cardDeck?.forEachIndexed { index, card -> card.idForOnline = index }

        val lobby = OnlineLobby(lobbyId, "", "taken", -1, -1, cardDeck)
        thisLobby().setValue(lobby)
    }

    private fun retrieveCardDeckFromLobby() {
        thisLobby().child("cardDeck").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cardList: MutableList<Card> = mutableListOf()

                val cardsAsChildren = snapshot.children
                // Firebase doesn't know how to cast to own classes, only primitive types, HashMap is one of them
                val hashMap = HashMap<String, Card>()
                cardsAsChildren.forEach { hashMap[it.key!!] = it.getValue(Card::class.java)!! }

                val arrayListOfCards: ArrayList<Card> = ArrayList(hashMap.values)
                arrayListOfCards.forEach { cardList.add(it) }

                cardDeckForJoiner.value = cardList.sortedBy { it.idForOnline }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun waitForOpponent() {
        listOfListeners.add(thisLobby().child("topPlayer").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(topPlayerChild: DataSnapshot) {
                if (topPlayerChild.value != "") opponentFound.value = true
            }

            override fun onCancelled(p0: DatabaseError) {}
        }))
    }

    fun clearAllListeners() = listOfListeners.clear()

}


















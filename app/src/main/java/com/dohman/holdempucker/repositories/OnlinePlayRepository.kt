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
    private val listOfListeners = mutableListOf<ValueEventListener>()

    val opponentInput: MutableLiveData<Int> = MutableLiveData()
    val opponentFound: MutableLiveData<Boolean> = MutableLiveData()

    init {
        opponentFound.value = false
    }

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

    fun updateInput(myTeamIsBottom: Boolean, input: Int) {
        if (myTeamIsBottom) db.child("bottomInput").setValue(input)
        else db.child("topInput").setValue(input)
    }

    fun searchForLobby(cardDeck: List<Card>?) {
        var foundLobby = false

        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (lobby in dataSnapshot.children) {
                    // FIXME Use lobbyid!
                    if (lobby.child("topPlayer").value == "") {
                        // There is bottom player in lobby waiting, go ahead and join
                        lobbyId = lobby.child("id").value as String
                        joinThisLobby()
                        foundLobby = true
                        break
                    }
                }

                if (!foundLobby) createLobby(cardDeck)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun joinThisLobby() {
        opponentFound.value = true
        db.child(lobbyId).child("topPlayer").setValue("taken")
    }

    private fun createLobby(cardDeck: List<Card>?) {
        lobbyId = db.push().key!! // Can't be null, since db is working here
        val lobby = OnlineLobby(lobbyId, "", "taken", -1, -1, cardDeck)

        db.child(lobbyId).setValue(lobby)

        waitForOpponent()
    }

    private fun waitForOpponent() {
        listOfListeners.add(db.child(lobbyId).child("topPlayer").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(topPlayerChild: DataSnapshot) {
                if (topPlayerChild.value != "") opponentFound.value = true
            }

            override fun onCancelled(p0: DatabaseError) {}
        }))
    }

    fun clearAllListeners() = listOfListeners.clear()
}


















package com.dohman.holdempucker.repositories

import androidx.lifecycle.MutableLiveData
import com.dohman.holdempucker.models.Card
import com.dohman.holdempucker.models.OnlineLobby
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import javax.inject.Inject

class OnlinePlayRepository @Inject constructor(
    private val db: DatabaseReference
) {
    var lobbyId: String = ""
    val opponentInput: MutableLiveData<Int> = MutableLiveData()

    private fun observeOpponentInput(myTeamIsBottom: Boolean) {
        val path = if (myTeamIsBottom) "topInput" else "bottomInput"

        db.child(path).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val input = (snapshot.value as Long).toInt()
                if (input != -1) opponentInput.value = input
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
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
                    if (lobby.child("topPlayer").value == "") {
                        // There is no top player in that lobby, go ahead and join
                        foundLobby = true
                        lobbyId = lobby.key!!
                        joinThisLobby()
                        break
                    }
                }

                if (!foundLobby) createLobby(cardDeck)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun joinThisLobby() {
        db.child(lobbyId).child("topPlayer").setValue("taken")
    }

    private fun createLobby(cardDeck: List<Card>?) {
        val id = db.push().key!! // Cant be null, since db is working here
        val lobby = OnlineLobby(id, "", "taken", -1, -1, cardDeck)

        db.child(id).setValue(lobby)
    }
}
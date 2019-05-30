package com.dohman.holdempucker.repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.dohman.holdempucker.models.Card
import com.dohman.holdempucker.models.OnlineLobby
import com.dohman.holdempucker.util.Constants.Companion.isMyTeamOnlineBottom
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import javax.inject.Inject

class OnlinePlayRepository @Inject constructor(
    private val db: DatabaseReference
) {
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

    fun joinLobby(cardDeck: List<Card>?) {
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (lobby in dataSnapshot.children) {
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("hejsan", "error")
            }
        })

        val lobbyId = db.push().key!! // Cant be null, since db is working here
        val lobby = OnlineLobby(lobbyId, "top", "bottom", -1, -1, cardDeck)

        db.child(lobbyId).setValue(lobby)

//        db.child("players").child("playerBottom").addListenerForSingleValueEvent(
//            object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    val value = snapshot.value
//                    if (value == "none") {
//                        seizeBottomSpot()
//                        observeOpponentInput(myTeamIsBottom = true)
//                    } else {
//                        seizeTopSpot()
//                        observeOpponentInput(myTeamIsBottom = false)
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {}
//            }
//        )
    }

    fun seizeBottomSpot() {
        db.child("players").child("playerBottom").setValue("seized")
        isMyTeamOnlineBottom = true
    }

    fun seizeTopSpot() {
        db.child("players").child("playerTop").setValue("seized")
        isMyTeamOnlineBottom = false
    }

    fun hasPlayerInLobby(): Boolean {
        var hasPlayer = false

        db.child("hasPlayer").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.value
                hasPlayer = value != "false"
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        return hasPlayer
    }
}
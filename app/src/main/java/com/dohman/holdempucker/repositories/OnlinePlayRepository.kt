package com.dohman.holdempucker.repositories

import com.dohman.holdempucker.util.Constants.Companion.isMyTeamOnlineBottom
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import javax.inject.Inject

class OnlinePlayRepository @Inject constructor(
    private val firebaseRef: DatabaseReference
) {

    fun updateInput(myTeamIsBottom: Boolean, input: Int) {
        if (myTeamIsBottom) firebaseRef.child("bottomInput").setValue(input)
        else firebaseRef.child("topInput").setValue(input)
    }

    fun joinLobby() {
        firebaseRef.child("players").child("playerBottom").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.value
                    if (value == "none") {
                        seizeBottomSpot()
                    } else {
                        seizeTopSpot()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            }
        )
    }

    fun seizeBottomSpot() {
        firebaseRef.child("players").child("playerBottom").setValue("seized")
        isMyTeamOnlineBottom = true
    }

    fun seizeTopSpot() {
        firebaseRef.child("players").child("playerTop").setValue("seized")
        isMyTeamOnlineBottom = false
    }

    fun hasPlayerInLobby(): Boolean {
        var hasPlayer = false

        firebaseRef.child("hasPlayer").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.value
                hasPlayer = value != "false"
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        return hasPlayer
    }
}
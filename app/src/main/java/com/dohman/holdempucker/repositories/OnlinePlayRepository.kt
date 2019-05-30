package com.dohman.holdempucker.repositories

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
        if (!hasPlayerInLobby()) {
            firebaseRef.child("hasPlayer").setValue("true")
        }
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
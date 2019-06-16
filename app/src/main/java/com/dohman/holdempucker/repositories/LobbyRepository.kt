package com.dohman.holdempucker.repositories

import androidx.lifecycle.MutableLiveData
import com.dohman.holdempucker.models.OnlineLobby
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import javax.inject.Inject

class LobbyRepository @Inject constructor(
    private val db: DatabaseReference
) {

    // All paths in a lobby reference
    private val pathPeriod = "period"
    private val pathBottomInput = "bottomInput"
    private val pathBottomPlayer = "bottomPlayer"
    private val pathCardDeck = "cardDeck"
    private val pathId = "id"
    private val pathTopInput = "topInput"
    private val pathTopPlayer = "topPlayer"

    val lobbies: MutableLiveData<List<OnlineLobby>> = MutableLiveData()

    private val vlForLobbies = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val lobbyList: MutableList<OnlineLobby> = mutableListOf()

            // Firebase doesn't know how to cast to own classes, only primitive types, HashMap is one of them
            val hashMap = HashMap<String, OnlineLobby>()

            for (lobby in snapshot.children) {
                if (lobby.exists()) {
                    hashMap[lobby.key!!] = lobby.getValue(OnlineLobby::class.java)!!
                }
            }

            val arrayListOfLobbies: ArrayList<OnlineLobby> = ArrayList(hashMap.values)
            arrayListOfLobbies.forEach { lobby -> lobbyList.add(lobby) }

            lobbies.value = lobbyList
        }

        override fun onCancelled(p0: DatabaseError) {}
    }

    init {
        db.addValueEventListener(vlForLobbies)
    }

    fun getAmountPlayersOfLobby(lobbyId: String?, fReturnedValue: (Int) -> Unit) {
        var result = 1

        lobbyId?.let {
            db.child(lobbyId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(lobby: DataSnapshot) {
                    if (lobby.child(pathTopPlayer).value == "taken")
                        result = 2

                    fReturnedValue.invoke(result)
                }

                override fun onCancelled(p0: DatabaseError) {}
            })
        }
    }

    fun removeAllValueListeners() {
        db.removeEventListener(vlForLobbies)
    }
}
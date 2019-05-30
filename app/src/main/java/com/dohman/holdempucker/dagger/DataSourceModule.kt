package com.dohman.holdempucker.dagger

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataSourceModule(/* Stuff like URLs here*/) {

    @Provides
    @Singleton
    fun getFirebaseRef(): DatabaseReference = FirebaseDatabase.getInstance().getReference("room")
}
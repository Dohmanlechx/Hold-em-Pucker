package com.dohman.holdempucker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dohman.holdempucker.cards.CardDeck

class MainActivity : AppCompatActivity() {
    val cardDeck = CardDeck()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    companion object {
        const val TAG = "MainActivity.kt"
    }
}

package com.dohman.holdempucker.repositories

import android.os.Bundle
import com.dohman.holdempucker.util.Constants
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject

class AnalyticsRepository @Inject constructor(
    private val analytics: FirebaseAnalytics
) {

    private val bundle = Bundle()

    fun matchStarted(mode: String) {
        bundle.clear()
        bundle.putString(GAME_MODE, mode.toLowerCase())
        analytics.logEvent(MATCH_STARTED, bundle)
    }

    fun matchVsBotFulfilled(mode: Constants.GameMode, won: Boolean) {
        val event = when (mode) {
            Constants.GameMode.RANDOM -> {
                if (won) WON_VS_EASY else LOST_VS_EASY
            }
            Constants.GameMode.DEVELOPER -> {
                if (won) WON_VS_HARD else LOST_VS_HARD
            }
            else -> null
        }

        bundle.clear()

        event?.let { analytics.logEvent(it, bundle) }
    }

    fun onlineMatchStarted() {
        bundle.clear()
        analytics.logEvent(ONLINE_MATCH_STARTED, bundle)
    }

    fun onlineMatchDisconnected() {
        bundle.clear()
        analytics.logEvent(ONLINE_MATCH_DISCONNECTED, bundle)
    }

    fun onlineMatchFulfilled() {
        bundle.clear()
        analytics.logEvent(ONLINE_MATCH_FULFILLED, bundle)
    }

    companion object {
        private const val GAME_MODE = "game_mode"
        private const val MATCH_STARTED = "match_started"
        private const val WON_VS_EASY = "won_vs_easy"
        private const val LOST_VS_EASY = "lost_vs_easy"
        private const val WON_VS_HARD = "won_vs_hard"
        private const val LOST_VS_HARD = "lost_vs_hard"
        private const val ONLINE_MATCH_STARTED = "online_match_started"
        private const val ONLINE_MATCH_DISCONNECTED = "online_match_disconnected"
        private const val ONLINE_MATCH_FULFILLED = "online_match_fulfilled"
        private const val MATCH_FORFEITED = "match_forfeited"
    }
}
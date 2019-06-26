package com.dohman.holdempucker.repositories

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject

class AnalyticsRepository @Inject constructor(
    private val analytics: FirebaseAnalytics
) {

    private val bundle = Bundle()

    fun matchStarted(mode: String) {
        bundle.putString(GAME_MODE, mode.toLowerCase())
        analytics.logEvent(MATCH_STARTED, bundle)
    }

    companion object {
        private const val GAME_MODE = "game_mode"
        private const val MATCH_STARTED = "match_started"
    }
}
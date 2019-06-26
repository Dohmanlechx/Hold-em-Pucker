package com.dohman.holdempucker.repositories

import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject

class AnalyticsRepository @Inject constructor(
    private val analytics: FirebaseAnalytics
) {


}
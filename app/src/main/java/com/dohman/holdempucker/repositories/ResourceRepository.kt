package com.dohman.holdempucker.repositories

import android.app.Application
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourceRepository @Inject constructor(
    private val application: Application
    // Placeholder for other Repositories.
) {
    val context
        get() = this.application

    val resources
        get() = this.application.resources
}
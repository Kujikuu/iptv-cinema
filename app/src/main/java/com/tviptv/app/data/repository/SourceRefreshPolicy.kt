package com.tviptv.app.data.repository

import com.tviptv.app.data.prefs.AppPreferences
import com.tviptv.app.data.prefs.RefreshInterval
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SourceRefreshPolicy @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    fun isRefreshDue(sourceUpdatedAt: Long, now: Long = System.currentTimeMillis()): Boolean {
        when (appPreferences.getRefreshInterval()) {
            RefreshInterval.MANUAL -> return false
            else -> {
                val intervalMs = appPreferences.getRefreshInterval().days * DAY_MS
                return sourceUpdatedAt <= 0L || now - sourceUpdatedAt >= intervalMs
            }
        }
    }

    fun shouldRefreshFromNetwork(channelCount: Int, sourceUpdatedAt: Long): Boolean {
        if (channelCount == 0) return true
        return isRefreshDue(sourceUpdatedAt)
    }

    companion object {
        private const val DAY_MS = 24 * 60 * 60 * 1000L
    }
}
